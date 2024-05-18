package com.example.lion.controller;

import com.example.lion.domain.StoredFile;
import com.example.lion.domain.Visibility;
import com.example.lion.service.ListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/list")
public class ListController {

    @Autowired
    private ListService listService;

    @GetMapping()
    @ResponseBody
    public List<StoredFile> listPrivate(
            @RequestParam(value = "visibility", required = false) String visibility,
            @RequestParam(value = "tags", required = false) String tagsFilter,
            @RequestParam(value = "sortBy",required = false) String sortBy,
            @RequestParam(value = "page", required = false, defaultValue = "0") int pageNumber
    ) {
        var vis = Visibility.PUBLIC;
        if(visibility != null) {
            vis = Visibility.valueOf(visibility);
        }

        var page = PageRequest.of(pageNumber, 10);

        if(sortBy != null && !"".equals(sortBy)) {
            page = page.withSort(Sort.Direction.ASC, sortBy);
        }

        return listService.listFiles(vis, tagsFilter, page);
    }
}