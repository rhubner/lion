package com.example.lion.controller;

import com.example.lion.controller.model.StoredFileDTO;
import com.example.lion.domain.StoredFile;
import com.example.lion.domain.Visibility;
import com.example.lion.service.ListService;
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

@RestController
@RequestMapping("/list")
@Validated
public class ListController {

    @Value("${server.url}" )
    private String serverUrl;

    @Autowired
    private ListService listService;

    @GetMapping()
    @ResponseBody
    public ResponseEntity<List<StoredFileDTO>> listPrivate(
            @RequestParam(value = "visibility", required = false)  @Pattern(regexp = "PRIVATE|PUBLIC") String visibility,
            @RequestParam(value = "tags", required = false) @Pattern(regexp = "[a-z0-1\\-]+") String tagsFilter,
            @RequestParam(value = "sortBy",required = false) @Pattern(regexp = "name|uploadDate|tag|contentType|size") String sortBy,
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