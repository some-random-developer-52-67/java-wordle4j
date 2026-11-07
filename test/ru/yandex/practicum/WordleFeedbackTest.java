package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WordleFeedbackTest {

    @ParameterizedTest
    @CsvSource({
            "герой, гонец, +^-^-",
            "герой, герой, +++++",
            "герой, банан, -----",
            "баран, банан, ++-++",
            "банан, баран, ++-++",
            "кошка, касса, +---+",
            "касса, соска, ^-+^+",
            "БЕРЁГ, берег, +++++"
    })
    void comparesWordsWithExactMatchesTakingPriority(String answer, String guess, String expected) {
        String result = WordleDictionary.feedback(answer, guess);


        assertEquals(expected, result);
    }

    @Test
    void rejectsMalformedWordsBeforeComparing() {
        assertThrows(
                IllegalArgumentException.class,
                () -> WordleDictionary.feedback("кот", "герой")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> WordleDictionary.feedback("герой", "abcde")
        );
    }
}
