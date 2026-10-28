package ru.yandex.practicum;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WordleDictionaryTest {

    private final PrintWriter log = new PrintWriter(new StringWriter());

    @Test
    void normalizesFiltersAndDeduplicatesWords() {
        List<String> words = List.of("ЁЖИК", "БЕРЁГ", "берег", "ГЕРОЙ", "кот", "wordl", "герой ");


        WordleDictionary dictionary = new WordleDictionary(words, log);


        assertEquals(List.of("берег", "герой"), dictionary.getWords());
        assertTrue(dictionary.contains("БЕРЁГ"));
        assertFalse(dictionary.contains("банан"));
    }

    @Test
    void validatesShapeWithoutAcceptingSpacesOrLatinLetters() {
        List<String> invalidWords = List.of("", "кот", "бананы", "банaн", "12345", " герой", "герой\n");


        assertTrue(WordleDictionary.isValidWord("БЕРЁГ"));

        for (String word : invalidWords) {
            assertFalse(WordleDictionary.isValidWord(word), word);
        }

        assertFalse(WordleDictionary.isValidWord(null));
    }

    @Test
    void protectsStoredWordsFromChanges() {
        List<String> source = new ArrayList<>(List.of("герой"));
        WordleDictionary dictionary = new WordleDictionary(source, log);


        source.clear();


        assertEquals(List.of("герой"), dictionary.getWords());
        assertThrows(
                UnsupportedOperationException.class,
                () -> dictionary.getWords().clear()
        );
    }

    @Test
    void rejectsEmptyDictionary() {
        List<String> words = List.of("кот");


        assertThrows(
                IllegalArgumentException.class,
                () -> new WordleDictionary(words, log)
        );
    }

    @Test
    void selectsOnlyDictionaryWords() {
        WordleDictionary dictionary = new WordleDictionary(
                List.of("герой", "банан"),
                log
        );
        Random random = new Random(42);


        List<String> selected = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            selected.add(dictionary.randomWord(random));
        }


        for (String word : selected) {
            assertTrue(dictionary.contains(word));
        }
    }
}
