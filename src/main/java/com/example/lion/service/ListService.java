package com.example.lion.service;

import com.example.lion.domain.StoredFile;
import com.example.lion.domain.Visibility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import static org.springframework.data.mongodb.core.query.Criteria.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.util.List;

@Component
public class ListService {


    @Autowired
    private MongoTemplate mongoTemplate;

    public List<StoredFile> listFiles(
            Visibility visibility,
            String tagFilter,
            String sortBy
    ) {
        final var user = SecurityContextHolder.getContext().getAuthentication().getName();

        var criteria = visibility == Visibility.PUBLIC ? where("visibility").is("PUBLIC") : where("user").is(user);

        if(tagFilter != null) {
            criteria = criteria.and("tags").is(tagFilter);
        }

        var query = new Query(criteria);
        if(sortBy != null) {
            query = query.with(Sort.by(sortBy));
        }

        var result = mongoTemplate.find(query, StoredFile.class);

        return result;
    }


}
