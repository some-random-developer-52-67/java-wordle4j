package ru.yandex.practicum;

import org.junit.jupiter.api.Test;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WordleOptionsTest {

    @Test
    void usesDefaultPathsWithoutArguments() {
        String[] args = {};


        WordleOptions options = WordleOptions.parse(args);


        assertEquals(Path.of("words_ru.txt"), options.dictionaryPath());
        assertEquals(Path.of("out/wordle.log"), options.logPath());
    }

    @Test
    void acceptsDictionaryPathAndKeepsDefaultLogPath() {
        String[] args = {"other-words.txt"};


        WordleOptions options = WordleOptions.parse(args);


        assertEquals(Path.of("other-words.txt"), options.dictionaryPath());
        assertEquals(Path.of("out/wordle.log"), options.logPath());
    }

    @Test
    void acceptsBothPaths() {
        String[] args = {"other-words.txt", "logs/game.log"};


        WordleOptions options = WordleOptions.parse(args);


        assertEquals(Path.of("other-words.txt"), options.dictionaryPath());
        assertEquals(Path.of("logs/game.log"), options.logPath());
    }

    @Test
    void rejectsUnexpectedArguments() {
        String[] args = {"words.txt", "game.log", "extra"};


        assertThrows(
                IllegalArgumentException.class,
                () -> WordleOptions.parse(args)
        );
    }

    @Test
    void rejectsInvalidPaths() {
        String[] args = {"invalid\0path"};


        assertThrows(
                InvalidPathException.class,
                () -> WordleOptions.parse(args)
        );
    }
}
