package com.FA24SE088.OnlineForum.utils;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
            Map.entry("conf", "Configuration")
    );

    public Map<String, Integer> countFileTypes(byte[] fileData) throws IOException, RarException {
        Map<String, Integer> fileTypeCountMap = new HashMap<>();

        if (isZipFile(fileData)) {
            processZipFile(fileData, fileTypeCountMap);
        } else if (isTarFile(fileData)) {
            processTarFile(fileData, fileTypeCountMap);
        } else if(isRarFile(fileData)){
            processRarFile(fileData, fileTypeCountMap);
        } else {
            throw new IllegalArgumentException("The provided data is neither a valid ZIP nor RAR nor TAR file.");
        }

        return fileTypeCountMap;
    }

    private void processZipFile(byte[] zipData, Map<String, Integer> fileTypeCountMap) throws IOException, RarException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipData))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String fileTypeOrLanguage = getLanguageFromFileName(entry.getName());
                    fileTypeCountMap.put(fileTypeOrLanguage, fileTypeCountMap.getOrDefault(fileTypeOrLanguage, 0) + 1);

                    if (entry.getName().toLowerCase().endsWith(".zip")) {
                        byte[] nestedZipData = zipInputStream.readAllBytes();
                        countFileTypes(nestedZipData);
                    } else if (entry.getName().toLowerCase().endsWith(".tar")) {
                        byte[] nestedTarData = zipInputStream.readAllBytes();
                        countFileTypes(nestedTarData);
                    } else if (entry.getName().toLowerCase().endsWith(".rar")) {
                        byte[] nestedRarData = zipInputStream.readAllBytes();
                        countFileTypes(nestedRarData);
                    }
                }
                zipInputStream.closeEntry();
            }
        }
    }

    private void processTarFile(byte[] tarData, Map<String, Integer> fileTypeCountMap) throws IOException, RarException {
        try (TarArchiveInputStream tarInputStream = new TarArchiveInputStream(new ByteArrayInputStream(tarData))) {
            TarArchiveEntry entry;
            while ((entry = tarInputStream.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String fileTypeOrLanguage = getLanguageFromFileName(entry.getName());
                    fileTypeCountMap.put(fileTypeOrLanguage, fileTypeCountMap.getOrDefault(fileTypeOrLanguage, 0) + 1);

                    if (entry.getName().toLowerCase().endsWith(".zip")) {
                        byte[] nestedZipData = readTarEntryContent(tarInputStream);
                        countFileTypes(nestedZipData);
                    } else if (entry.getName().toLowerCase().endsWith(".tar")) {
                        byte[] nestedTarData = readTarEntryContent(tarInputStream);
                        countFileTypes(nestedTarData);
                    } else if (entry.getName().toLowerCase().endsWith(".rar")) {
                        byte[] nestedRarData = readTarEntryContent(tarInputStream);
                        countFileTypes(nestedRarData);
                    }
                }
            }
        }
    }

    private void processRarFile(byte[] rarData, Map<String, Integer> fileTypeCountMap) throws IOException, RarException {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(rarData);
             Archive archive = new Archive(byteArrayInputStream)) {

            FileHeader fileHeader;
            while ((fileHeader = archive.nextFileHeader()) != null) {
                if (!fileHeader.isDirectory()) {
                    String fileTypeOrLanguage = getLanguageFromFileName(fileHeader.getFileNameString());
                    fileTypeCountMap.put(fileTypeOrLanguage, fileTypeCountMap.getOrDefault(fileTypeOrLanguage, 0) + 1);

                    if (fileHeader.getFileNameString().toLowerCase().endsWith(".zip")) {
                        byte[] nestedZipData = extractRarFile(archive, fileHeader);
                        countFileTypes(nestedZipData);
                    } else if (fileHeader.getFileNameString().toLowerCase().endsWith(".tar")) {
                        byte[] nestedTarData = extractRarFile(archive, fileHeader);
                        countFileTypes(nestedTarData);
                    } else if (fileHeader.getFileNameString().toLowerCase().endsWith(".rar")) {
                        byte[] nestedRarData = extractRarFile(archive, fileHeader);
                        countFileTypes(nestedRarData);
                    }
                }
            }
        }
    }
    private byte[] readTarEntryContent(TarArchiveInputStream tarInputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = tarInputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        return byteArrayOutputStream.toByteArray();
    }
    private byte[] extractRarFile(Archive archive, FileHeader fileHeader) throws RarException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        archive.extractFile(fileHeader, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
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
    private byte[] extractZipData(ZipInputStream zipInputStream) throws IOException {
        return zipInputStream.readAllBytes();
    }
    private boolean isZipFile(byte[] data) {
        return data.length >= 4
                && data[0] == 0x50  // 'P'
                && data[1] == 0x4B  // 'K'
                && data[2] == 0x03  // 0x03
                && data[3] == 0x04; // 0x04
    }
    private boolean isRarFile(byte[] data) {
        return data.length >= 7
                && data[0] == 0x52  // 'R'
                && data[1] == 0x61  // 'a'
                && data[2] == 0x72  // 'r'
                && data[3] == 0x21  // '!'
                && data[4] == 0x1A  // (Control-Z)
                && data[5] == 0x07  // ?
                && data[6] == 0x00; // Null byte
    }
    private boolean isTarFile(byte[] data) {
        return data.length >= 265
                && data[257] == 'u'  // 'u'
                && data[258] == 's'  // 's'
                && data[259] == 't'  // 't'
                && data[260] == 'a'  // 'a'
                && data[261] == 'r'; // 'r'
    }
}
