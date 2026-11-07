package ru.yandex.practicum;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

final class WordleLog {

    private WordleLog() {
    }

    static PrintWriter open(Path dictionaryPath, Path logPath) throws IOException {
        if (isSameFile(dictionaryPath, logPath)) {
            throw new IllegalArgumentException("Файл лога должен отличаться от файла словаря");
        }

        Files.createDirectories(logPath.toAbsolutePath().getParent());

        return new PrintWriter(Files.newBufferedWriter(logPath, StandardCharsets.UTF_8));
    }

    static void check(PrintWriter log) throws LogWriteException {
        if (log.checkError()) {
            throw new LogWriteException();
        }
    }

    private static boolean isSameFile(Path dictionaryPath, Path logPath) throws IOException {
        if (dictionaryPath.toAbsolutePath().normalize().equals(logPath.toAbsolutePath().normalize())) {
            return true;
        }

        return Files.exists(dictionaryPath)
                && Files.exists(logPath)
                && Files.isSameFile(dictionaryPath, logPath);
    }
}
