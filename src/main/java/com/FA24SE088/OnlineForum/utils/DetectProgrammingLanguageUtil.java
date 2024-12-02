package com.FA24SE088.OnlineForum.utils;

import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DetectProgrammingLanguageUtil {
    private static final Map<String, String> EXTENSION_TO_LANGUAGE_MAP = Map.<String, String>ofEntries(
            // Java-related
            Map.entry("java", "Java"),
            Map.entry("jsp", "Java"), // Java Server Pages
            Map.entry("class", "Java"),
            Map.entry("jar", "Java"),
            Map.entry("war", "Java"),

            // Python-related
            Map.entry("py", "Python"),
            Map.entry("pyc", "Python"), // Compiled Python
            Map.entry("pyo", "Python"), // Optimized Python
            Map.entry("pyw", "Python"), // Windows Python scripts
            Map.entry("ipynb", "Python"),
            Map.entry("pyd", "Python"),

            // JavaScript and its frameworks
            Map.entry("js", "JS"),
            Map.entry("mjs", "JS"), // ES Modules
            Map.entry("cjs", "JS"), // CommonJS
            Map.entry("jsx", "React"),
            Map.entry("ts", "TS"),
            Map.entry("tsx", "React"),

            // C-based languages
            Map.entry("c", "C"),
            Map.entry("h", "C"), // Header files
            Map.entry("cpp", "C++"),
            Map.entry("cxx", "C++"),
            Map.entry("cc", "C++"),
            Map.entry("hh", "C++"), // Header files for C++
            Map.entry("hpp", "C++"),
            Map.entry("hxx", "C++"),
            Map.entry("inl", "C++"),

            // C# and .NET
            Map.entry("cs", "C#"),
            Map.entry("xaml", "C#"), // XML-based UI definitions for .NET
            Map.entry("dll", "C#"),
            Map.entry("exe", "C#"),
            Map.entry("resx", "C#"),

            // Ruby-related
            Map.entry("rb", "Ruby"),
            Map.entry("erb", "Ruby"), // Embedded Ruby
            Map.entry("rake", "Ruby"), // Rake build scripts
            Map.entry("gem", "Ruby"),
            Map.entry("rbw", "Ruby"),

            // Go language
            Map.entry("go", "Go"),

            // PHP-related
            Map.entry("php", "PHP"),
            Map.entry("phtml", "PHP"), // PHP HTML embedded files
            Map.entry("php4", "PHP"),
            Map.entry("php5", "PHP"),
            Map.entry("phar", "PHP"),

            // Kotlin-related
            Map.entry("kt", "Kotlin"),
            Map.entry("kts", "Kotlin"), // Kotlin scripts

            // Dart and Flutter
            Map.entry("dart", "Dart"),
            Map.entry("flutter", "Flutter"),

            // Swift-related
            Map.entry("swift", "Swift"),
            Map.entry("xcodeproj", "Swift"),

            // Rust-related
            Map.entry("rs", "Rust"),
            Map.entry("cargo", "Rust"),

            // Scala-related
            Map.entry("scala", "Scala"),
            Map.entry("sc", "Scala"), // Scala scripts
            Map.entry("sbt", "Scala"),

            // Julia-related
            Map.entry("jl", "Julia"),

            // Perl-related
            Map.entry("pl", "Perl"),
            Map.entry("pm", "Perl"), // Perl module files
            Map.entry("t", "Perl"),

            // Nim-related
            Map.entry("nim", "Nim"),
            Map.entry("nims", "Nim"), // NimScript files

            // Vala-related
            Map.entry("vala", "Vala"),
            Map.entry("vapi", "Vala"), // Vala API files

            // Elixir-related
            Map.entry("ex", "Elixir"),
            Map.entry("exs", "Elixir"),

            // Svelte
            Map.entry("svelte", "Svelte"),

            // Web development (HTML/CSS/XML)
            Map.entry("html", "HTML"),
            Map.entry("htm", "HTML"), // Alternate HTML extension
            Map.entry("css", "CSS"),
            Map.entry("scss", "CSS"), // SCSS CSS preprocessor
            Map.entry("sass", "CSS"), // SASS CSS preprocessor
            Map.entry("less", "CSS"), // LESS CSS preprocessor
            Map.entry("xml", "XML"),
            Map.entry("xsl", "XML"), // XML stylesheet
            Map.entry("xsd", "XML"),
            Map.entry("svg", "SVG"),

            //Objective C
            Map.entry("m", "Objective-C"),
            Map.entry("mm", "Objective-C"),

            //Assembly
            Map.entry("asm", "Assembly"),
            Map.entry("masm", "Assembly"),
            Map.entry("nasm", "Assembly"),
            Map.entry("tasm", "Assembly"),
            Map.entry("gas", "Assembly"),
            Map.entry("a", "Assembly"),
            Map.entry("s", "Assembly"),
            Map.entry("i", "Assembly"),
            Map.entry("inc", "Assembly"),
            Map.entry("lst", "Assembly"),
            Map.entry("o", "Assembly"),
            Map.entry("obj", "Assembly"),
            Map.entry("tpl", "Assembly"),
            Map.entry("def", "Assembly"),

            // Configuration Files
            Map.entry("gitignore", "Configuration"),
            Map.entry("gitattributes", "Configuration"),
            Map.entry("editorconfig", "Configuration"),
            Map.entry("env", "Configuration"),
            Map.entry("ini", "Configuration"),
            Map.entry("conf", "Configuration"),
            Map.entry("dockerfile", "Configuration"),
            Map.entry("docker-compose.yml", "Configuration"),
            Map.entry("apache", "Configuration"),
            Map.entry("nginx", "Configuration"),
            Map.entry("toml", "Configuration"),
            Map.entry("license", "Configuration"),
            Map.entry("yaml", "Configuration"), // YAML configuration files
            Map.entry("yml", "Configuration"), // YML configuration files

            // Databases
            Map.entry("sql", "DATABASE"),
            Map.entry("sqlite", "DATABASE"),
            Map.entry("db", "DATABASE"),
            Map.entry("json", "DATABASE"), // JSON databases

            // Documentation
            Map.entry("readme", "Documentation"),
            Map.entry("txt", "Documentation"),
            Map.entry("excel", "Documentation"),
            Map.entry("docx", "Documentation"),
            Map.entry("pptx", "Documentation"),

            // Unknown Extension
            Map.entry("unknown", "Unknown")
    );
    private static final Map<String, String> LANGUAGE_CATEGORY_MAP = Map.<String, String>ofEntries(
            Map.entry("Java", "Programming"),
            Map.entry("Python", "Programming"),
            Map.entry("JS", "Programming"),
            Map.entry("C", "Programming"),
            Map.entry("C++", "Programming"),
            Map.entry("C#", "Programming"),
            Map.entry("Ruby", "Programming"),
            Map.entry("Go", "Programming"),
            Map.entry("PHP", "Programming"),
            Map.entry("Kotlin", "Programming"),
            Map.entry("Dart", "Programming"),
            Map.entry("Flutter", "Programming"),
            Map.entry("Swift", "Programming"),
            Map.entry("Rust", "Programming"),
            Map.entry("Scala", "Programming"),
            Map.entry("Julia", "Programming"),
            Map.entry("Perl", "Programming"),
            Map.entry("Nim", "Programming"),
            Map.entry("Vala", "Programming"),
            Map.entry("Elixir", "Programming"),
            Map.entry("Svelte", "Programming"),
            Map.entry("React", "Programming"),
            Map.entry("TS", "Programming"),
            Map.entry("Assembly", "Programming"),
            Map.entry("HTML", "Programming"),
            Map.entry("CSS", "Programming"),
            Map.entry("XML", "Programming"),
            Map.entry("SVG", "Programming"),

            Map.entry("DATABASE", "DATABASE"),

            Map.entry("Configuration", "Configuration"),

            Map.entry("Documentation", "Documentation"),

            Map.entry("Unknown", "Unknown")
    );
    private static final Map<String, Integer> CATEGORY_PRIORITY = Map.of(
            "Programming", 3,
            "DATABASE", 1,
            "Configuration", 0,
            "Documentation", 0,
            "Unknown", 0
    );

    public String determineProgrammingLanguage(byte[] bytes, String contentType) throws IOException, RarException {
        if(contentType == null || contentType.isEmpty()){
            return "Unknown";
        }

        Map<String, Integer> map = switch (contentType) {
            case "application/x-zip-compressed" -> countLanguagesInZip(bytes);
            case "application/x-tar" -> countLanguagesInTar(bytes);
            case "application/x-compressed" -> countLanguagesInRar(bytes);
            default -> null;
        };

        if (map == null || map.isEmpty()) {
            return "Unknown";
        }

        map.remove("Unknown");

        if (map.isEmpty()) {
            return "Unknown";
        }

        return determineDominantLanguage(map);
    }

    private Map<String, Integer> countLanguagesInZip(byte[] zipFileBytes) throws IOException {
        Map<String, Integer> languageCountMap = new HashMap<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipFileBytes))) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                String language = getLanguageFromFileName(zipEntry.getName());
                languageCountMap.put(language, languageCountMap.getOrDefault(language, 0) + 1);
                zipInputStream.closeEntry();
            }
        }
        return languageCountMap;
    }
    private Map<String, Integer> countLanguagesInTar(byte[] tarFileBytes) throws IOException {
        Map<String, Integer> languageCountMap = new HashMap<>();
        try (TarArchiveInputStream tarInputStream = new TarArchiveInputStream(new ByteArrayInputStream(tarFileBytes))) {
            TarArchiveEntry tarEntry;
            while ((tarEntry = tarInputStream.getNextTarEntry()) != null) {
                String language = getLanguageFromFileName(tarEntry.getName());
                languageCountMap.put(language, languageCountMap.getOrDefault(language, 0) + 1);
            }
        }
        return languageCountMap;
    }
    public Map<String, Integer> countLanguagesInRar(byte[] rarFileBytes) throws IOException, RarException {
        Map<String, Integer> languageCountMap = new HashMap<>();
        try (Archive archive = new Archive(new ByteArrayInputStream(rarFileBytes))) {
            FileHeader fileHeader;
            while ((fileHeader = archive.nextFileHeader()) != null) {
                String language = getLanguageFromFileName(fileHeader.getFileNameString());
                languageCountMap.put(language, languageCountMap.getOrDefault(language, 0) + 1);
            }
        }
        return languageCountMap;
    }
    //bug af
    private Map<String, Integer> countLanguagesIn7z(byte[] sevenZBytes) throws IOException {
        Map<String, Integer> languageCountMap = new HashMap<>();

        File tempFile = File.createTempFile("temp-archive", ".7z");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(sevenZBytes);
        }

        try (SevenZFile sevenZFile = new SevenZFile(tempFile)) {
            SevenZArchiveEntry entry;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }

                String fileName = entry.getName();
                String extension = getFileExtension(fileName);
                String language = EXTENSION_TO_LANGUAGE_MAP.getOrDefault(extension, "Unknown");

                // Count languages in the map
                languageCountMap.put(language, languageCountMap.getOrDefault(language, 0) + 1);
            }
        } finally {
            Files.delete(tempFile.toPath());
        }

        return languageCountMap;
    }
    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
    private static String getLanguageFromFileName(String fileName) {
        fileName = fileName.toLowerCase();

        if (fileName.startsWith("readme")) return "Documentation";
        if (fileName.equals(".gitignore")) return "Configuration";
        if (fileName.equals(".gitattributes")) return "Configuration";
        if (fileName.equals(".editorconfig")) return "Configuration";
        if (fileName.equals("dockerfile")) return "Configuration";
        if (fileName.startsWith("license")) return "Legal";

        String extension = getFileExtension(fileName);
        return EXTENSION_TO_LANGUAGE_MAP.getOrDefault(extension, "Unknown");
    }
    private Map<String, Integer> countLanguages(Map<String, Integer> languageCountMap) {
        Map<String, Integer> languagePriorityMap = new HashMap<>();

        for (Map.Entry<String, Integer> entry : languageCountMap.entrySet()) {
            String language = entry.getKey();
            int count = entry.getValue();

            String category = LANGUAGE_CATEGORY_MAP.getOrDefault(language, "Unknown");
            int priority = CATEGORY_PRIORITY.getOrDefault(category, 1);
            int weightedCount = count * priority;

            languagePriorityMap.put(language, languagePriorityMap.getOrDefault(language, 0) + weightedCount);
        }

        return languagePriorityMap;
    }
    public String determineDominantLanguage(Map<String, Integer> languageCountMap) {
        Map<String, Integer> languagePriorityMap = countLanguages(languageCountMap);

        return languagePriorityMap.entrySet()
                .stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)  // Return the language with the highest count
                .orElse("Unknown");  // In case there are no languages found
    }
}
