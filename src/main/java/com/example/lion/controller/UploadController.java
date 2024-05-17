package com.example.lion.controller;


import com.example.lion.domain.Visibility;
import com.example.lion.service.StorageService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/file")
public class UploadController {

    public static final String TAGS_HTTP_HEADER = "x-tags";
    public static final String VISIBILITY_HTTP_HEADER = "x-visibility";

    @Autowired
    private StorageService storageService;

    @PutMapping("/{filename}")
    public String putFile(
            @PathVariable("filename") String fileName,
            @RequestHeader(value = TAGS_HTTP_HEADER, required = false) String tag,
            @RequestHeader(value = VISIBILITY_HTTP_HEADER) String visibilityHeader,
            @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) Optional<String> contentType,
            InputStream inputStream
    ) throws IOException {
        var visibility = Visibility.valueOf(visibilityHeader);

        if(tag != null  && !"".equals(tag)) {
            var tags = tag.split(",");
            storageService.storeFile(fileName, tags, contentType, visibility, inputStream);
        }else {
            storageService.storeFile(fileName, new String[] {}, contentType, visibility, inputStream);
        }
        return "{url: 'http://localhost:8080/" + fileName + "}";
    }

    @GetMapping("/{filename}")
    public void getFile(
            @PathVariable("filename") String fileName,
            HttpServletResponse response

    ) throws IOException {
        var file = storageService.readFile(fileName);
        var fileMetaData = storageService.getFileMetaData(fileName);
        if(fileMetaData.isPresent()) {
            response.setStatus(HttpStatus.OK.value());
            response.setContentLengthLong(Files.size(file));
            var tags = fileMetaData.get().getTags();
            if(tags != null && tags.length > 0) {
                response.addHeader(TAGS_HTTP_HEADER, Arrays.stream(tags).collect(Collectors.joining(",")));
            }
            response.addHeader(HttpHeaders.CONTENT_TYPE, fileMetaData.get().getContentType());
            FileUtils.copyFile(file.toFile(), response.getOutputStream());
        } else {
            response.setStatus(HttpStatus.NOT_FOUND.value());
        }
    }

    @PatchMapping("/{filename}")
    public HttpStatus renameFile(@PathVariable("filename") String fileName,
                                 @RequestBody RenameData renameData) {
        storageService.rename(fileName, renameData.getNewFileName());
        return HttpStatus.OK;
    }


    @DeleteMapping("/{filename}")
    public HttpStatus deleteFile(
            @PathVariable("filename") String fileName
    ) throws IOException {
        storageService.delete(fileName);
        return HttpStatus.OK;
    }

}
