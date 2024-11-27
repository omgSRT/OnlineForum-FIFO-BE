package com.FA24SE088.OnlineForum.utils;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.tika.langdetect.optimaize.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.springframework.stereotype.Component;
import org.apache.tika.language.detect.LanguageDetector;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LanguageDetectionUtil {
    public String detectLanguage(String text) {
        LanguageDetector detector = new OptimaizeLangDetector().loadModels();
        LanguageResult result = detector.detect(text);
        return result.getLanguage();
    }
}
