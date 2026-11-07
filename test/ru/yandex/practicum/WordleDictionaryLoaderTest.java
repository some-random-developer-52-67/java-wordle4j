package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WordleDictionaryLoaderTest {

    @TempDir
    Path directory;

    private final StringWriter messages = new StringWriter();
    private final WordleDictionaryLoader loader = new WordleDictionaryLoader(new PrintWriter(messages));

    @Test
    void readsEntireUtf8FileAndFiltersAfterNormalization() throws IOException {
        Path file = directory.resolve("words.txt");

        Files.writeString(
                file,
                "абажур\nБЕРЁГ\nберег\n\nкот\nгерой\nwordl\n",
                StandardCharsets.UTF_8
        );


        WordleDictionary dictionary = loader.load(file);


        assertEquals(List.of("берег", "герой"), dictionary.getWords());
        assertTrue(messages.toString().contains("Прочитано строк: 7"));
    }

    @Test
    void reportsMissingFileSeparatelyFromEmptyDictionary() {
        Path file = directory.resolve("missing.txt");


        assertThrows(
                FileNotFoundException.class,
                () -> loader.load(file)
        );
    }

    @Test
    void rejectsEmptyFileAndFileWithoutSuitableWords() throws IOException {
        Path file = directory.resolve("empty.txt");

        Files.writeString(file, "");


        assertThrows(
                EmptyDictionaryException.class,
                () -> loader.load(file)
        );


        Files.writeString(file, "кот\nабажур\n");


        assertThrows(
                EmptyDictionaryException.class,
                () -> loader.load(file)
        );
    }
}
