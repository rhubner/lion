package com.example.lion.service;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class TikaMimeTypeDetector {

    TikaConfig tika = new TikaConfig();

    public TikaMimeTypeDetector() throws TikaException, IOException {
    }

    public String detectFile(Path file) throws IOException {
        var metadata = new Metadata();
        var xxx = tika.getDetector().detect(TikaInputStream.get(file), metadata);
        return xxx.toString();
    }


}
