package ru.yandex.practicum;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WordleHintsTest {

    private final PrintWriter log = new PrintWriter(new StringWriter());
    private final List<String> words = List.of(
            "герой", "гонец", "город", "горох", "голос", "голод", "порог", "пирог",
            "банан", "баран", "кабан", "кошка", "касса", "соска"
    );
    private final WordleDictionary dictionary = new WordleDictionary(words, log);

    @Test
    void initiallyUsesEntireDictionaryAndDoesNotExposeMutableCandidates() {
        WordleGame game = createGame("герой");


        List<String> candidates = game.getCandidates();


        assertEquals(words, candidates);
        assertThrows(
                UnsupportedOperationException.class,
                candidates::clear
        );
    }

    @Test
    void preservesAnswerAndEveryPreviousConstraint() throws GameException {
        for (String answer : words) {
            WordleGame game = createGame(answer);

            for (String guess : List.of("банан", "гонец", "касса")) {
                if (game.isFinished()) {
                    break;
                }


                game.makeMove(guess);


                assertTrue(game.getCandidates().contains(answer), answer);

                for (String candidate : words) {
                    boolean consistent = game.getHistory().stream()
                            .allMatch(move -> WordleDictionary.feedback(candidate, move.word())
                                    .equals(move.feedback()));

                    assertEquals(consistent, game.getCandidates().contains(candidate), candidate);
                }
            }
        }
    }

    @Test
    void handlesExtraOccurrencesAndFixedPositions() throws GameException {
        WordleGame game = createGame("баран");


        Move move = game.makeMove("банан");
        String suggestion = game.suggestWord();


        assertEquals("++-++", move.feedback());
        assertEquals(List.of("баран"), game.getCandidates());
        assertEquals("баран", suggestion);
    }

    @Test
    void suggestionsNeverRepeatEvenWithoutSubmittingThem() {
        WordleGame game = createGame("герой");
        List<String> suggestions = new ArrayList<>();


        for (int i = 0; i < words.size(); i++) {
            suggestions.add(game.suggestWord());
        }


        assertEquals(words.size(), new HashSet<>(suggestions).size());
        assertEquals(new HashSet<>(words), new HashSet<>(suggestions));
        assertThrows(
                IllegalStateException.class,
                game::suggestWord
        );
        assertEquals(6, game.getRemainingAttempts());
    }

    @Test
    void automaticMovesTerminateAndDoNotRepeat() throws GameException {
        for (String answer : words) {
            WordleGame game = createGame(answer);
            Set<String> suggestions = new HashSet<>();


            while (!game.isFinished()) {
                String suggestion = game.suggestWord();

                assertTrue(game.getCandidates().contains(suggestion));
                assertTrue(suggestions.add(suggestion));

                game.makeMove(suggestion);

                assertTrue(game.getCandidates().contains(answer));
            }


            assertTrue(game.getHistory().size() <= 6);
            assertThrows(
                    IllegalStateException.class,
                    game::suggestWord
            );
        }
    }

    @Test
    void invalidMoveDoesNotFilterCandidates() {
        WordleGame game = createGame("герой");


        assertThrows(
                WordNotFoundInDictionary.class,
                () -> game.makeMove("арбуз")
        );


        assertEquals(words, game.getCandidates());
    }

    private WordleGame createGame(String answer) {
        return new WordleGame(dictionary, answer, new Random(42), log);
    }
}
