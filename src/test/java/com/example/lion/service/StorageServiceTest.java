package com.example.lion.service;

import com.example.lion.domain.StoredFile;
import com.example.lion.domain.Visibility;
import com.example.lion.repository.StoredFileRepository;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class StorageServiceTest {

    @Autowired
    private StorageService storageService;

    @MockBean
    private StoredFileRepository storedFileRepository;

    @WithMockUser(username = "user")
    @Test
    public void mimeTypeAutoDetect() throws IOException {

        try(var in = new ByteArrayInputStream(IOUtils.resourceToByteArray("/img.lossless"))) {
            storageService.storeFile("test-name", new String[]{}, Optional.empty(), Visibility.PRIVATE, in);
        }
        ArgumentCaptor<StoredFile> argument = ArgumentCaptor.forClass(StoredFile.class);

        verify(storedFileRepository).save(argument.capture());

        assertThat(argument.getValue().getContentType()).isEqualTo("image/png");

    }
}
