package com.example.lion.controller;


import com.example.lion.domain.Visibility;
import com.example.lion.service.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/file")
@Validated
public class FileController {

    public static final String TAGS_HTTP_HEADER = "x-tags";
    public static final String VISIBILITY_HTTP_HEADER = "x-visibility";
    public static final String FILENAME_PATTERN = "[A-Za-z0-9]+[\\.\\-A-Za-z0-9]*";

    @Autowired
    private StorageService storageService;

    @Value("${server.url}" )
    private String serverUrl;

    @PutMapping("/{filename}")
    public ObjectNode putFile(
            @PathVariable("filename") @Pattern(regexp = FILENAME_PATTERN) String fileName,
            @RequestHeader(value = TAGS_HTTP_HEADER, required = false)  @Pattern(regexp = "^$|[a-z0-9\\-]+(,[\\-a-z0-9]+){0,3}") String tag,
            @RequestHeader(value = VISIBILITY_HTTP_HEADER)  @Pattern(regexp = "PRIVATE|PUBLIC") String visibilityHeader,
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
        var uri = UriComponentsBuilder.fromHttpUrl(serverUrl)
                .pathSegment("file")
                .pathSegment(fileName).build();

        ObjectMapper mapper = new ObjectMapper();
        var root = mapper.createObjectNode();
        root.put("url", uri.toString());

        return root;
    }

    @GetMapping("/{filename}")
    public void getFile(
            @PathVariable("filename") @Pattern(regexp = FILENAME_PATTERN) String fileName,
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
    public HttpStatus renameFile(@PathVariable("filename") @Pattern(regexp = FILENAME_PATTERN) String fileName,
                                 @RequestBody @Valid RenameData renameData) {
        storageService.rename(fileName, renameData.getNewFileName());
        return HttpStatus.OK;
    }


    @DeleteMapping("/{filename}")
    public HttpStatus deleteFile(
            @PathVariable("filename") @Pattern(regexp = FILENAME_PATTERN) String fileName
    ) throws IOException {
        storageService.delete(fileName);
        return HttpStatus.OK;
    }

}
