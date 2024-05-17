package com.example.lion.controller;


import com.example.lion.service.StorageService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/file")
public class UploadController {

    public static final String TAGS_HTTP_HEADER = "x-tags";
    public static final String PERMISSION_HTTP_HEADER = "x-permission";

    @Autowired
    private StorageService storageService;

    @PutMapping("/{filename}")
    public String putFile(
        @PathVariable("filename") String fileName,
        @RequestHeader(value = TAGS_HTTP_HEADER, required = false) String tag,
        @RequestHeader(value = PERMISSION_HTTP_HEADER) String permission,
        InputStream inputStream
    ) throws IOException {
        if(tag != null  && !"".equals(tag)) {
            var tags = tag.split(",");
            storageService.storeFile(fileName, tags, inputStream);
        }else {
            storageService.storeFile(fileName, new String[] {}, inputStream);
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
            FileUtils.copyFile(file.toFile(), response.getOutputStream());
        } else {
            response.setStatus(HttpStatus.NOT_FOUND.value());
        }
    }

    @DeleteMapping("/{filename}")
    public HttpStatus deleteFile(
            @PathVariable("filename") String fileName
    ) throws IOException {
        storageService.delete(fileName);
        return HttpStatus.OK;
    }

}
