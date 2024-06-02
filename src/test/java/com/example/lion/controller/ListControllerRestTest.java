package com.example.lion.controller;

import com.example.lion.controller.model.StoredFileDTO;
import com.example.lion.domain.Visibility;
import com.example.lion.repository.StoredFileRepository;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.StreamingHttpOutputMessage;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.io.IOException;
import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ListControllerRestTest {

    @LocalServerPort
    private int port;

    private RestClient restClient = RestClient.builder()
            .defaultHeader("Authorization", "Basic dXNlcjpwYXNzd29yZA==")
            .build();

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StoredFileRepository storedFileRepository;

    @BeforeEach
    public void before() throws IOException {
        storedFileRepository.deleteAll();

        uploadFile("single-file.bin", new byte[] {5,6,7,3,1,2,1,2,2,2}, Visibility.PRIVATE, "dubai");

        uploadFile("burj-khalifa.jpg", IOUtils.resourceToByteArray("/img.lossy"), Visibility.PRIVATE, "burj-khalifa,jpg");
        uploadFile("burj-khalifa.png", IOUtils.resourceToByteArray("/img.lossless"), Visibility.PRIVATE, "burj-khalifa,png");

        for(int i = 0 ; i < 17 ; i++) {
            String formatted = String.format("%02d",i);
            uploadFile("public-file" + formatted, new byte[] {1,2,3, (byte) i}, Visibility.PUBLIC, "picture,tag" + i);
        }

        for(int i = 0 ; i < 17 ; i++) {
            String formatted = String.format("%02d",i);

            uploadFile("private-file" + formatted, new byte[] {3,2,1, (byte) i}, Visibility.PRIVATE, "picture,tag" + i);
        }

    }

    @Test
    public void listPublicFiles() {
        var result = restTemplate
                .withBasicAuth("user", "password")
                .getForEntity(listUrlPrefix(),
                StoredFileDTO[].class
                );

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(10);
        assertThat(result.getBody()).anyMatch(x -> x.getName().equals("public-file01"));

    }

    @Test
    public void listWithMimeType() {
        var result = restTemplate
                .withBasicAuth("user", "password")
                .getForEntity(listUrlPrefix() + "?visibility=PRIVATE&tags=burj-khalifa&sortBy=uploadDate",
                        StoredFileDTO[].class
                );

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = result.getBody();
        assertThat(body).hasSize(2);
        assertThat(body)
                .extracting(StoredFileDTO::getContentType)
                .containsExactly("image/jpeg", "image/png");

        //TODO - File is not on this URL. How to distinguish between URL for testing and production.
        //     - Service can be behind proxy and port number may not be valid.
        assertThat(body[0].getUrl()).isEqualTo("http://localhost:8080/file/burj-khalifa.jpg");

    }

    @Test
    public void testPage() {

        var result = restClient.get()
                .uri(uriBuilder -> setupListUrl(uriBuilder)
                        .queryParam("visibility", "PRIVATE")
                        .queryParam("page", "1")
                        .queryParam("sortBy", "name")
                        .build()
                )
                .retrieve()
                .toEntity(String.class);
        assertThat(result.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());
        System.out.println(result.getBody());
    }



    private void uploadFile(String filename, byte[] data, Visibility visibility, String tag) {

        var body = new StreamingHttpOutputMessage.Body() { //Custom body to prevent setting default content type.
            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                outputStream.write(data);
            }
        };

        var request = restClient.put()
                .uri(fileUrlPrefix() + filename)
                .contentType(null)
                .body(body)
                .header(FileController.VISIBILITY_HTTP_HEADER, visibility.name());
        if(tag != null) {
            request = request.header(FileController.TAGS_HTTP_HEADER, tag);
        }
        request.retrieve().toEntity(String.class);
    }


    public String fileUrlPrefix() {
        return "http://localhost:" + port + "/file/";
    }

    private UriBuilder setupListUrl(UriBuilder uriBuilder) {
        return uriBuilder.scheme("http").host("localhost").port(port).path("/list");
    }
    public String listUrlPrefix() {
        return "http://localhost:" + port + "/list";
    }

    private static final class NoOpResponseErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
        }
    }

}
