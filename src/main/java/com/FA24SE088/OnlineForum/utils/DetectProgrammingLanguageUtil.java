package com.FA24SE088.OnlineForum.utils;

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
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DetectProgrammingLanguageUtil {
    private static final Map<String, String> extensionToLanguageMap = Map.<String, String>ofEntries(
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
            Map.entry("js", "JavaScript"),
            Map.entry("mjs", "JavaScript"), // ES Modules
            Map.entry("cjs", "JavaScript"), // CommonJS
            Map.entry("jsx", "React"),
            Map.entry("ts", "TypeScript"),
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

            // Special files
            Map.entry("readme", "Documentation"),
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
            Map.entry("license", "Legal"),
            Map.entry("bat", "Batch"),
            Map.entry("sh", "Shell Script"),
            Map.entry("zsh", "Shell Script"),
            Map.entry("bash", "Shell Script"),

            // Databases
            Map.entry("sql", "SQL"),
            Map.entry("sqlite", "SQLITE"),
            Map.entry("db", "DATABASE"),
            Map.entry("json", "JSON"), // JSON databases
            Map.entry("yaml", "YAML"), // YAML configuration files
            Map.entry("yml", "YML"), // YML configuration files

            // Unknown Extension
            Map.entry("unknown", "Unknown")
    );

    public Map<String, Integer> countLanguagesInZip(byte[] zipFileBytes) throws IOException {
        Map<String, Integer> languageCountMap = new HashMap<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipFileBytes))) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                String extension = getFileExtension(zipEntry.getName());
                String language = extensionToLanguageMap.getOrDefault(extension, "Unknown");
                // Count languages in the map
                languageCountMap.put(language, languageCountMap.getOrDefault(language, 0) + 1);
                zipInputStream.closeEntry();
            }
        }
        return languageCountMap;
    }
    public Map<String, Integer> countLanguagesInTar(byte[] tarFileBytes) throws IOException {
        Map<String, Integer> languageCountMap = new HashMap<>();
        try (TarArchiveInputStream tarInputStream = new TarArchiveInputStream(new ByteArrayInputStream(tarFileBytes))) {
            TarArchiveEntry tarEntry;
            while ((tarEntry = tarInputStream.getNextTarEntry()) != null) {
                String extension = getFileExtension(tarEntry.getName());
                String language = extensionToLanguageMap.getOrDefault(extension, "Unknown");
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
                String extension = getFileExtension(fileHeader.getFileNameString());
                String language = extensionToLanguageMap.getOrDefault(extension, "Unknown");
                languageCountMap.put(language, languageCountMap.getOrDefault(language, 0) + 1);
            }
        }
        return languageCountMap;
    }
    //bug af
    public Map<String, Integer> countLanguagesIn7z(byte[] sevenZBytes) throws IOException {
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
                String language = extensionToLanguageMap.getOrDefault(extension, "Unknown");

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
    public static String getLanguageFromFileName(String fileName) {
        fileName = fileName.toLowerCase();

        if (fileName.startsWith("readme")) return "Documentation";
        if (fileName.equals(".gitignore")) return "Configuration";
        if (fileName.equals(".gitattributes")) return "Configuration";
        if (fileName.equals(".editorconfig")) return "Configuration";
        if (fileName.equals("dockerfile")) return "Configuration";
        if (fileName.startsWith("license")) return "Legal";

        String extension = getFileExtension(fileName);
        return extensionToLanguageMap.getOrDefault(extension, "Unknown");
    }
}
