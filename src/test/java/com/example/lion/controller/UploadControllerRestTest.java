package com.example.lion.controller;

import com.example.lion.repository.StoredFileRepository;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.StreamingHttpOutputMessage;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UploadControllerRestTest {

    @LocalServerPort
    private int port;

    private RestClient restClient = RestClient.builder()
            .defaultHeader("Authorization", "Basic dXNlcjpwYXNzd29yZA==")
            .build();

    @Autowired
    private StoredFileRepository storedFileRepository;

    @BeforeEach
    public void before() {
        storedFileRepository.deleteAll();
    }

    @Test
    public void testUploadDownloadDelete() {
        final var FILENAME = "filename.bin";

        var result = restClient.put()
                .uri(fileUrlPrefix() + FILENAME)
                .body(new byte[] {1,2,3,4,5,6,7,8,9,10})
                .header(FileController.VISIBILITY_HTTP_HEADER, "PUBLIC")
                .header(FileController.TAGS_HTTP_HEADER, "hello,radek,another-tag")
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .retrieve()
                .toEntity(String.class);
        assertThat(result.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());

        var downloadResult = restClient.get()
                .uri(fileUrlPrefix() + FILENAME)
                .retrieve()
                .toEntity(byte[].class);
        assertThat(downloadResult.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());
        var tagsHeader = downloadResult.getHeaders().get(FileController.TAGS_HTTP_HEADER);
        assertThat(tagsHeader).isNotEmpty();
        assertThat(tagsHeader.get(0)).isEqualTo("hello,radek,another-tag");


        var contentTypeHeader = downloadResult.getHeaders().get(HttpHeaders.CONTENT_TYPE);
        assertThat(contentTypeHeader).isNotEmpty();
        assertThat(contentTypeHeader.get(0)).isEqualTo("image/jpeg");



        var deleteResult = restClient.delete()
                .uri(fileUrlPrefix() + FILENAME)
                .retrieve()
                .toBodilessEntity();

        assertThat(deleteResult.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());

    }

    @Test
    public void testRename() {
        final var FILENAME = "filename.bin";
        final var NEW_FILE_NAME = "second-file.bin";


        var result = restClient.put()
                .uri(fileUrlPrefix() + FILENAME)
                .body(new byte[] {1,2,3,4,5,6,7,8,9,10})
                .header(FileController.VISIBILITY_HTTP_HEADER, "PUBLIC")
                .header(FileController.TAGS_HTTP_HEADER, "hello,radek,another-tag")
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .retrieve()
                .toEntity(String.class);
        assertThat(result.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());


        var renameResult = restClient.patch()
                .uri(fileUrlPrefix() + FILENAME)
                .body("{ \"newFileName\" : \""+ NEW_FILE_NAME + "\" }")
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity();

        assertThat(renameResult.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());


        var downloadResult = restClient.get()
                .uri(fileUrlPrefix() + NEW_FILE_NAME)
                .retrieve()
                .toEntity(byte[].class);
        assertThat(downloadResult.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());
        var tagsHeader = downloadResult.getHeaders().get(FileController.TAGS_HTTP_HEADER);
        assertThat(tagsHeader).isNotEmpty();
        assertThat(tagsHeader.get(0)).isEqualTo("hello,radek,another-tag");


        var contentTypeHeader = downloadResult.getHeaders().get(HttpHeaders.CONTENT_TYPE);
        assertThat(contentTypeHeader).isNotEmpty();
        assertThat(contentTypeHeader.get(0)).isEqualTo("image/jpeg");



        var deleteResult = restClient.delete()
                .uri(fileUrlPrefix() + NEW_FILE_NAME)
                .retrieve()
                .toBodilessEntity();

        assertThat(deleteResult.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());

    }

    @Test
    public void testDuplicateUpload() {
        final var FILENAME = "filename.bin";

        var result = restClient.put()
                .uri(fileUrlPrefix() + FILENAME)
                .body(new byte[] {1,2,3,4,5,6,7,8,9,10})
                .header(FileController.VISIBILITY_HTTP_HEADER, "PUBLIC")
                .header(FileController.TAGS_HTTP_HEADER, "hello,radek,another-tag")
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .retrieve()
                .toEntity(String.class);
        assertThat(result.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());


        var result2 = restClient.put()
                .uri(fileUrlPrefix() + "otherName.bin")
                .body(new byte[] {1,2,3,4,5,6,7,8,9,10})
                .header(FileController.VISIBILITY_HTTP_HEADER, "PUBLIC")
                .header(FileController.TAGS_HTTP_HEADER, "hello,radek,another-tag")
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .retrieve()
                .onStatus(new NoOpResponseErrorHandler())
                .toBodilessEntity();
        assertThat(result2.getStatusCode().value()).isEqualTo(HttpStatus.CONFLICT.value());

        var result3 = restClient.put()
                .uri(fileUrlPrefix() + FILENAME)
                .body(new byte[] {10,9,8,7,6,5,4,3,2,1})
                .header(FileController.VISIBILITY_HTTP_HEADER, "PUBLIC")
                .header(FileController.TAGS_HTTP_HEADER, "hello,radek,another-tag")
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .retrieve()
                .onStatus(new NoOpResponseErrorHandler())
                .toBodilessEntity();
        assertThat(result3.getStatusCode().value()).isEqualTo(HttpStatus.CONFLICT.value());

        var deleteResult = restClient.delete()
                .uri(fileUrlPrefix() + FILENAME)
                .retrieve()
                .toBodilessEntity();

        assertThat(deleteResult.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());

    }

    @Test
    public void testUploadJpeg() throws IOException {
        final var FILENAME = "img.lossy";

        var data = IOUtils.resourceToByteArray("/img.lossy");
        var body = new StreamingHttpOutputMessage.Body() { //Custom body to prevent setting default content type.
            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                outputStream.write(data);
            }
        };

        var result = restClient.put()
                .uri(fileUrlPrefix() + FILENAME)
                .body(body)
                .contentType(null)
                .header(FileController.VISIBILITY_HTTP_HEADER, "PUBLIC")
                .retrieve()
                .toEntity(String.class);
        assertThat(result.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());

        var downloadResult = restClient.get()
                .uri(fileUrlPrefix() + FILENAME)
                .retrieve()
                .toEntity(byte[].class);
        assertThat(downloadResult.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());
        var contentType = downloadResult.getHeaders().get(HttpHeaders.CONTENT_TYPE);
        assertThat(contentType).isNotEmpty();
        assertThat(contentType.get(0)).isEqualTo("image/jpeg");
    }

    public String fileUrlPrefix() {
        return "http://localhost:" + port + "/file/";
    }

    private static final class NoOpResponseErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
        }
    }

}
