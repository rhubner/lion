package com.example.lion.controller;

import com.example.lion.controller.model.StoredFileDTO;
import com.example.lion.domain.StoredFile;
import com.example.lion.domain.Visibility;
import com.example.lion.service.ListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "List", description = "Search API")
@RestController
@RequestMapping("/list")
@Validated
public class ListController {

    @Value("${server.url}" )
    private String serverUrl;

    @Autowired
    private ListService listService;

    @Operation(summary = "List uploaded files", security = @SecurityRequirement(name = "http-auth"))
    @ApiResponse(
            responseCode = "200",
            description = "Return all file matching filter criteria."
    )
    @GetMapping()
    @ResponseBody
    public ResponseEntity<List<StoredFileDTO>> listPrivate(
            @Parameter(description = "Limit selection to PRIVATE or PUBLIC files", required = true, example = "PUBLIC",
                schema = @Schema(allowableValues = {"PUBLIC", "PRIVATE"})
            )
            @RequestParam(value = "visibility", required = false)  @Pattern(regexp = "PRIVATE|PUBLIC") String visibility,

            @Parameter(description = "Allow filter by tag. Max one tag", example = "holiday")
            @RequestParam(value = "tags", required = false) @Pattern(regexp = "[a-z0-1\\-]+") String tagsFilter,

            @Parameter(description = "Allow specifying sorting field", example = "uploadDate",
            schema = @Schema(allowableValues = {"name", "uploadDate", "tag", "contentType", "size"}))
            @RequestParam(value = "sortBy",required = false) @Pattern(regexp = "name|uploadDate|tag|contentType|size") String sortBy,

            @Parameter(description = "By default API return only first 10 item. To get other items, page need to be specified.", example = "10")
            @RequestParam(value = "page", required = false, defaultValue = "0") @Min(0) @Max(Integer.MAX_VALUE) int pageNumber
    ) {
        var vis = Visibility.PUBLIC;
        if(visibility != null) {
            vis = Visibility.valueOf(visibility);
        }

        var page = PageRequest.of(pageNumber, 10);

        if(sortBy != null && !"".equals(sortBy)) {
            page = page.withSort(Sort.Direction.ASC, sortBy);
        }

        var responseBody = listService.listFiles(vis, tagsFilter, page)
                .stream().map(file -> new StoredFileDTO(
                        file.getName(),
                        file.getTags(),
                        file.getContentType(),
                        file.getUploadDate(),
                        UriComponentsBuilder.fromHttpUrl(serverUrl)
                        .pathSegment("file")
                        .pathSegment(file.getName()).build().toUriString(),
                        file.getSize()
                ) ).collect(Collectors.toList());

        return ResponseEntity.ok(responseBody);
    }
}