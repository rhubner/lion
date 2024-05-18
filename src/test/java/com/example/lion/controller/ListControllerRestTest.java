package com.example.lion.controller;

import com.example.lion.domain.Visibility;
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
    private StoredFileRepository storedFileRepository;

    @BeforeEach
    public void before() {
        storedFileRepository.deleteAll();

        uploadFile("single-file.bin", new byte[] {5,6,7,3,1,2,1,2,2,2}, Visibility.PRIVATE, "dubai");

        for(int i = 0 ; i < 10 ; i++) {
            uploadFile("public-file" + i, new byte[] {1,2,3, (byte) i}, Visibility.PUBLIC, "picture,tag" + i);
        }

        for(int i = 0 ; i < 10 ; i++) {
            uploadFile("private-file" + i, new byte[] {3,2,1, (byte) i}, Visibility.PRIVATE, "picture,tag" + i);
        }

    }

    @Test
    public void testList() {

        var result = restClient.get()
                .uri(uriBuilder -> setupListUrl(uriBuilder)
                                .queryParam("tags", "dubai")
                                .queryParam("visibility", "PRIVATE")
                                .build()
                        )
                .retrieve()
                .toEntity(String.class);
        assertThat(result.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());
        System.out.println(result.getBody());
    }



    private void uploadFile(String filename, byte[] data, Visibility visibility, String tag) {
        var request = restClient.put()
                .uri(fileUrlPrefix() + filename)
                .body(data)
                .header(UploadController.VISIBILITY_HTTP_HEADER, visibility.name());
        if(tag != null) {
            request = request.header(UploadController.TAGS_HTTP_HEADER, tag);
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
