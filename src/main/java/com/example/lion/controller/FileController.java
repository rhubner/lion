package com.example.lion.controller;


import com.example.lion.controller.model.LionErrorResponse;
import com.example.lion.domain.Visibility;
import com.example.lion.service.DuplicateFileException;
import com.example.lion.service.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "File", description = "Storage API")
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

    @Operation(summary = "Upload a file", security = @SecurityRequirement(name = "http-auth"))
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Return JSON with download URL",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = "url",
                                                    schema = @Schema(
                                                            type = "String",
                                                            example = "http://localhost/file/file.txt"

                                                    )
                                            )
                                    }

                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Duplicate content or file name",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = LionErrorResponse.class)
                            )
                    }

            )

    })
    @PutMapping("/{filename}")
    public ObjectNode putFile(
            @Parameter(description = "Name of the file to be uploaded. Must be unique", required = true, example = "burj-khalifa.jpg")
            @PathVariable("filename") @Pattern(regexp = FILENAME_PATTERN) String fileName,

            @Parameter(description = "Comma separated list of up to 5 tags.", example = "downtown,holiday,UAE")
            @RequestHeader(value = TAGS_HTTP_HEADER, required = false)  @Pattern(regexp = "^$|[a-z0-9\\-]+(,[\\-a-z0-9]+){0,3}") String tag,

            @Parameter(description = "Define if file is PRIVATE or PUBLIC", required = true, example = "PUBLIC", schema = @Schema(allowableValues = {"PUBLIC", "PRIVATE"}))
            @RequestHeader(value = VISIBILITY_HTTP_HEADER)  @Pattern(regexp = "PRIVATE|PUBLIC") String visibilityHeader,

            @Parameter(description = "User provided content type. If not specified, system will try to detect content type.", example = "image/jpeg")
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

    @Operation(summary = "Download already uploaded file.", security = @SecurityRequirement(name = "http-auth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return file content"),
            @ApiResponse(responseCode = "404", description = "File not found, or user doesn't have permission to download file")
    }
    )
    @GetMapping("/{filename}")
    public void getFile(
            @Parameter(description = "Name of the file to download", required = true, example = "burj-khalifa.jpg")
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

    @Operation(summary = "Rename already uploaded file", security = @SecurityRequirement(name = "http-auth"))
    @PatchMapping("/{filename}")
    public HttpStatus renameFile(
            @Parameter(description = "Name of the file to be renamed", required = true, example = "burj-khalifa.jpg")
            @PathVariable("filename") @Pattern(regexp = FILENAME_PATTERN) String fileName,
                                 @RequestBody @Valid RenameData renameData) {
        storageService.rename(fileName, renameData.getNewFileName());
        return HttpStatus.OK;
    }


    @Operation(summary = "Delete already uploaded file.", security = @SecurityRequirement(name = "http-auth"))
    @DeleteMapping("/{filename}")
    public HttpStatus deleteFile(
            @Parameter(description = "Name of the file to be deleted", required = true, example = "burj-khalifa.jpg")
            @PathVariable("filename") @Pattern(regexp = FILENAME_PATTERN) String fileName
    ) throws IOException {
        storageService.delete(fileName);
        return HttpStatus.OK;
    }

}
