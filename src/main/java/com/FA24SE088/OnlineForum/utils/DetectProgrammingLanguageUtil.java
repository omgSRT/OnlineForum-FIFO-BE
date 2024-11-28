package com.FA24SE088.OnlineForum.utils;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.sf.sevenzipjbinding.*;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
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

            // Python-related
            Map.entry("py", "Python"),
            Map.entry("pyc", "Python"), // Compiled Python
            Map.entry("pyo", "Python"), // Optimized Python
            Map.entry("pyw", "Python"), // Windows Python scripts

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

            // C# and .NET
            Map.entry("cs", "C#"),
            Map.entry("xaml", "C#"), // XML-based UI definitions for .NET

            // Ruby-related
            Map.entry("rb", "Ruby"),
            Map.entry("erb", "Ruby"), // Embedded Ruby
            Map.entry("rake", "Ruby"), // Rake build scripts

            // Go language
            Map.entry("go", "Go"),

            // PHP-related
            Map.entry("php", "PHP"),
            Map.entry("phtml", "PHP"), // PHP HTML embedded files
            Map.entry("php4", "PHP"),
            Map.entry("php5", "PHP"),

            // Kotlin-related
            Map.entry("kt", "Kotlin"),
            Map.entry("kts", "Kotlin"), // Kotlin scripts

            // Dart and Flutter
            Map.entry("dart", "Dart"),

            // Swift-related
            Map.entry("swift", "Swift"),

            // Rust-related
            Map.entry("rs", "Rust"),

            // Scala-related
            Map.entry("scala", "Scala"),
            Map.entry("sc", "Scala"), // Scala scripts

            // Julia-related
            Map.entry("jl", "Julia"),

            // Perl-related
            Map.entry("pl", "Perl"),
            Map.entry("pm", "Perl"), // Perl module files

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
            Map.entry("scss", "CSS"), // SASS CSS preprocessor
            Map.entry("less", "CSS"), // LESS CSS preprocessor
            Map.entry("xml", "XML"),
            Map.entry("xsl", "XML"), // XML stylesheet

            // Special files
            Map.entry("readme", "Documentation"),
            Map.entry("gitignore", "Configuration"),
            Map.entry("gitattributes", "Configuration"),
            Map.entry("editorconfig", "Configuration"),
            Map.entry("dockerfile", "Configuration"),
            Map.entry("docker-compose.yml", "Configuration"),
            Map.entry("license", "Legal"),

            // Databases
            Map.entry("sql", "DATABASE"),
            Map.entry("sqlite", "DATABASE"),
            Map.entry("db", "DATABASE"),
            Map.entry("db3", "DATABASE"), // SQLite3 database
            Map.entry("json", "DATABASE"), // JSON databases
            Map.entry("yaml", "DATABASE"), // YAML configuration files

            // Miscellaneous
            Map.entry("bat", "Batch"),
            Map.entry("sh", "Shell Script"),
            Map.entry("zsh", "Shell Script"),
            Map.entry("bash", "Shell Script"),
            Map.entry("conf", "Configuration"),

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
    public static Map<String, Integer> countLanguagesInGz(byte[] gzFileBytes) throws IOException {
        Map<String, Integer> languageCountMap = new HashMap<>();
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(gzFileBytes);
             GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = gzipInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            String extractedFileName = "extractedFile"; // Or get the name from metadata if available
            String extension = getFileExtension(extractedFileName);
            String language = extensionToLanguageMap.getOrDefault(extension, "Unknown");
            languageCountMap.put(language, languageCountMap.getOrDefault(language, 0) + 1);
        }
        return languageCountMap;
    }
//    public static Map<String, Integer> countLanguagesIn7z(byte[] sevenZipFileBytes) throws Exception {
//        Map<String, Integer> languageCountMap = new HashMap<>();
//        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(sevenZipFileBytes)) {
//            IInArchive inArchive = SevenZip.openInArchive(null, byteArrayInputStream);
//            int numberOfItems = inArchive.getNumberOfItems();
//
//            for (int i = 0; i < numberOfItems; i++) {
//                IArchiveItem item = inArchive.getItem(i);
//                String fileName = item.getFileName();
//                String extension = getFileExtension(fileName);
//
//                // Get the language from the extension
//                String language = extensionToLanguageMap.getOrDefault(extension, "Unknown");
//
//                // Increment the language count
//                languageCountMap.put(language, languageCountMap.getOrDefault(language, 0) + 1);
//            }
//        }
//        return languageCountMap;
//    }

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
    private byte[] extractZipData(ZipInputStream zipInputStream) throws IOException {
        return zipInputStream.readAllBytes();
    }
}
