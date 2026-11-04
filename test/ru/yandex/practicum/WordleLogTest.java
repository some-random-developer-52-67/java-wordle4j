package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WordleLogTest {

    @TempDir
    Path directory;

    @Test
    void createsParentDirectoriesAndWritesUtf8Log() throws IOException {
        Path dictionaryPath = directory.resolve("words.txt");
        Path logPath = directory.resolve("nested/game.log");


        try (PrintWriter log = WordleLog.open(dictionaryPath, logPath)) {
            log.print("Проверка записи");
            WordleLog.check(log);
        }


        assertEquals("Проверка записи", Files.readString(logPath));
    }

    @Test
    void detectsSuppressedWriteFailure() {
        PrintWriter log = new PrintWriter(new StringWriter());
        log.close();


        log.println("Проверка записи");


        assertThrows(
                LogWriteException.class,
                () -> WordleLog.check(log)
        );
    }
}
