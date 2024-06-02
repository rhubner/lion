package com.example.lion.controller;


import com.example.lion.controller.model.LionErrorResponse;
import com.example.lion.repository.StoredFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ErrorHandleTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StoredFileRepository storedFileRepository;

    @BeforeEach
    public void before() {
        storedFileRepository.deleteAll();
    }

    @Test
    public void notFound() {
        var response = restTemplate
            .withBasicAuth("user", "password")
                .getForEntity("http://localhost:" + port + "/file/perpetuum-mobile.txt", LionErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getDetail()).contains("Requested file doesn't exist.");
    }

    @Test
    public void duplicateContent() {

        var headers = new HttpHeaders();
        headers.add(FileController.VISIBILITY_HTTP_HEADER, "PRIVATE");

        var requestEntity = new HttpEntity<byte[]>(new byte[] {1,2,3,4,5}, headers);

        var response = restTemplate
                .withBasicAuth("user", "password")
                .exchange("http://localhost:" + port + "/file/file.txt",
                        HttpMethod.PUT,
                        requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        var errorResponse = restTemplate
                .withBasicAuth("user", "password")
                .exchange("http://localhost:" + port + "/file/file2.txt",
                        HttpMethod.PUT,
                        requestEntity, LionErrorResponse.class);

        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(errorResponse.getBody().getDetail()).contains("File with same content already exists");

    }

    @Test
    public void duplicateName() {

        var headers = new HttpHeaders();
        headers.add(FileController.VISIBILITY_HTTP_HEADER, "PRIVATE");

        var requestEntity = new HttpEntity<byte[]>(new byte[] {1,2,3,4,5}, headers);

        var response = restTemplate
                .withBasicAuth("user", "password")
                .exchange("http://localhost:" + port + "/file/file.txt",
                        HttpMethod.PUT,
                        requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        var requestEntity2 = new HttpEntity<byte[]>(new byte[] {1,2,3,4,5,6}, headers);

        var errorResponse = restTemplate
                .withBasicAuth("user", "password")
                .exchange("http://localhost:" + port + "/file/file.txt",
                        HttpMethod.PUT,
                        requestEntity2, LionErrorResponse.class);

        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(errorResponse.getBody().getDetail()).contains("File with same name already exists");

    }
}
