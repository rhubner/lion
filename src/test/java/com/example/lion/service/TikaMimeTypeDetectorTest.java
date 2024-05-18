package com.example.lion.service;

import org.apache.tika.exception.TikaException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class TikaMimeTypeDetectorTest {

    @Test
    public void testJpegDetection() throws TikaException, IOException {
        TikaMimeTypeDetector t = new TikaMimeTypeDetector();
        var result = t.detectFile(Paths.get("src/test/resources/img.lossy"));

        assertThat(result).isEqualTo("image/jpeg");

    }

    @Test
    public void testPngDetection() throws TikaException, IOException {
        TikaMimeTypeDetector t = new TikaMimeTypeDetector();
        var result = t.detectFile(Paths.get("src/test/resources/img.lossless"));

        assertThat(result).isEqualTo("image/png");

    }



}
