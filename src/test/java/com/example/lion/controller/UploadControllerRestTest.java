package com.example.lion.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UploadControllerRestTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private RestClient restClient = RestClient.create();
    @Test
    public void testUploadDownloadDelete() {
        final var FILENAME = "filename.bin";

        var result = restClient.put()
                .uri(fileUrlPrefix() + FILENAME)
                .body(new byte[] {1,2,3,4,5,6,7,8,9,10})
                .header(UploadController.PERMISSION_HTTP_HEADER, "public")
                .header(UploadController.TAGS_HTTP_HEADER, "hello,radek,another-tag")
                .retrieve()
                .toEntity(String.class);
        assertThat(result.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());

        var downloadResult = restClient.get()
                .uri(fileUrlPrefix() + FILENAME)
                .retrieve()
                .toEntity(byte[].class);
        assertThat(downloadResult.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());
        var headers = downloadResult.getHeaders().get(UploadController.TAGS_HTTP_HEADER);
        assertThat(headers).isNotEmpty();
        assertThat(headers.get(0)).isEqualTo("hello,radek,another-tag");

        var deleteResult = restClient.delete()
                .uri(fileUrlPrefix() + FILENAME)
                .retrieve()
                .toBodilessEntity();

        assertThat(deleteResult.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());

    }

    public String fileUrlPrefix() {
        return "http://localhost:" + port + "/file/";
    }


}
