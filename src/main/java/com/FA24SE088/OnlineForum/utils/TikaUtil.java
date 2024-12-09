package com.FA24SE088.OnlineForum.utils;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.tika.Tika;
import org.apache.tika.langdetect.optimaize.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TikaUtil {
    final Tika tika = new Tika();

    public String detectLanguage(String text) {
        LanguageDetector detector = new OptimaizeLangDetector().loadModels();
        LanguageResult result = detector.detect(text);
        return result.getLanguage();
    }

    public String detectMimeType(byte[] fileBytes) throws IOException {
        return tika.detect(fileBytes);
    }
}
