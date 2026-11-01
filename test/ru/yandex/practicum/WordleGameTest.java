package ru.yandex.practicum;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WordleGameTest {

    private final PrintWriter log = new PrintWriter(new StringWriter());
    private final WordleDictionary dictionary = new WordleDictionary(
            List.of("герой", "гонец", "банан", "баран", "берег"),
            log
    );

    @Test
    void winsOnFirstAttemptAndRejectsFurtherMoves() throws GameException {
        WordleGame game = createGame("герой");

        assertEquals(6, game.getRemainingAttempts());
        assertFalse(game.isFinished());


        Move move = game.makeMove("ГЕРОЙ");


        assertEquals("+++++", move.feedback());
        assertTrue(game.isWon());
        assertTrue(game.isFinished());
        assertEquals(5, game.getRemainingAttempts());
        assertThrows(
                IllegalStateException.class,
                () -> game.makeMove("банан")
        );
        assertEquals(1, game.getHistory().size());
    }

    @Test
    void canWinOnLastAttempt() throws GameException {
        WordleGame game = createGame("герой");

        for (int i = 0; i < 5; i++) {
            game.makeMove("банан");
        }

        assertFalse(game.isFinished());


        game.makeMove("герой");


        assertEquals(0, game.getRemainingAttempts());
        assertTrue(game.isWon());
    }

    @Test
    void losesAfterSixValidGuesses() throws GameException {
        WordleGame game = createGame("герой");


        for (int i = 0; i < 6; i++) {
            game.makeMove("гонец");
        }


        assertTrue(game.isFinished());
        assertFalse(game.isWon());
        assertEquals("герой", game.getAnswer());
        assertThrows(
                IllegalStateException.class,
                () -> game.makeMove("герой")
        );
        assertEquals(0, game.getRemainingAttempts());
    }

    @Test
    void invalidInputDoesNotChangeStateAndShapeIsCheckedFirst() {
        WordleGame game = createGame("герой");
        List<String> invalidWords = List.of("", "кот", "abcde", " герой", "герой ", "12345");


        for (String word : invalidWords) {
            assertThrows(
                    InvalidWordException.class,
                    () -> game.makeMove(word)
            );
        }

        assertThrows(
                InvalidWordException.class,
                () -> game.makeMove(null)
        );
        assertThrows(
                WordNotFoundInDictionary.class,
                () -> game.makeMove("кошка")
        );


        assertEquals(6, game.getRemainingAttempts());
        assertTrue(game.getHistory().isEmpty());
    }

    @Test
    void normalizesAnswerAndInputAndReturnsImmutableHistory() throws GameException {
        WordleGame game = createGame("БЕРЁГ");
        List<Move> snapshot = game.getHistory();


        Move move = game.makeMove("БЕРЁГ");


        assertEquals("берег", move.word());
        assertEquals("+++++", move.feedback());
        assertTrue(snapshot.isEmpty());
        assertThrows(
                UnsupportedOperationException.class,
                () -> game.getHistory().clear()
        );
    }

    @Test
    void rejectsAnswerOutsideDictionary() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createGame("кошка")
        );
    }

    @Test
    void choosesRandomAnswerFromDictionary() {
        Random random = new Random(123);


        WordleGame game = new WordleGame(dictionary, random, log);


        assertTrue(dictionary.contains(game.getAnswer()));
    }

    private WordleGame createGame(String answer) {
        return new WordleGame(dictionary, answer, new Random(42), log);
    }
}
