package com.example.lion.service;


import com.example.lion.domain.StoredFile;
import com.example.lion.domain.Visibility;
import com.example.lion.repository.StoredFileRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ListServiceTest {

    @Autowired
    private StoredFileRepository storedFileRepository;

    @Autowired
    private ListService listService;

    final Instant startTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    @BeforeAll
    public void prepareTestData() {

        var testData = new ArrayList<StoredFile>(20);

        for (int i = 0; i < 20; i++) {
            var iAsText = String.format("%02d",i);
            testData.add(new StoredFile(
                    "id" + iAsText,
                    new String[]{"tag-" + iAsText},
                    Visibility.PUBLIC,
                    "public-file-" + iAsText + ".txt",
                    1024 - i, "user", "public-file-hash" + iAsText,
                    startTime.minus(i, ChronoUnit.MINUTES), //Upload time in different order
                    "text/plain"
            ));
        }
        storedFileRepository.deleteAll();
        storedFileRepository.saveAll(testData);
    }


    @WithMockUser(username = "user")
    @Test
    public void filterByTag() {
        var page = PageRequest.of(0, 10);

        var result = listService.listFiles(Visibility.PUBLIC, "tag-00", page);

        assertThat(result)
                .isNotEmpty()
                .hasSize(1);

        assertThat(result.get(0).getTags()).containsExactly("tag-00");
        assertThat(result).extracting(StoredFile::getSize).containsExactly(1024l);
    }

    @WithMockUser(username = "user")
    @Test
    public void sortByUploadDate() {
        var page = PageRequest.of(0, 10)
                .withSort(Sort.Direction.ASC, "uploadDate");

        var result = listService.listFiles(Visibility.PUBLIC, null, page);

        assertThat(result)
                .isNotEmpty()
                .hasSize(10);
        assertThat(result)
                .extracting(StoredFile::getUploadDate)
                .isSorted()
                .endsWith(
                        startTime
                                .minus(10, ChronoUnit.MINUTES)
                );

        page = PageRequest.of(1, 10)
                .withSort(Sort.Direction.ASC, "uploadDate");

        result = listService.listFiles(Visibility.PUBLIC, null, page);

        assertThat(result)
                .isNotEmpty()
                .hasSize(10);

        assertThat(result)
                .extracting(StoredFile::getUploadDate)
                .isSorted()
                .endsWith(startTime);
    }

    @WithMockUser(username = "user")
    @Test
    public void sortByName() {
        var page = PageRequest.of(0, 10)
                .withSort(Sort.Direction.ASC, "name");

        var result = listService.listFiles(Visibility.PUBLIC, null, page);

        assertThat(result)
                .isNotEmpty()
                .hasSize(10);
        assertThat(result)
                .extracting(StoredFile::getName)
                .isSorted()
                .startsWith("public-file-00.txt")
                .endsWith("public-file-09.txt");

        page = PageRequest.of(1, 10)
                .withSort(Sort.Direction.ASC, "name");

        result = listService.listFiles(Visibility.PUBLIC, null, page);

        assertThat(result)
                .isNotEmpty()
                .hasSize(10);

        assertThat(result)
                .extracting(StoredFile::getName)
                .isSorted()
                .startsWith("public-file-10.txt")
                .endsWith("public-file-19.txt");
    }

    @WithMockUser(username = "user")
    @Test
    public void sortByUploadSize() {
        var page = PageRequest.of(0, 10)
                .withSort(Sort.Direction.ASC, "size");

        var result = listService.listFiles(Visibility.PUBLIC, null, page);

        assertThat(result)
                .isNotEmpty()
                .hasSize(10);
        assertThat(result)
                .extracting(StoredFile::getName)
                .startsWith("public-file-19.txt")
                .endsWith("public-file-10.txt");

    }


}