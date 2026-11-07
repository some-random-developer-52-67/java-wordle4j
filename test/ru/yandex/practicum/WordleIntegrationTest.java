package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WordleIntegrationTest {

    private static final int AUTOMATIC_GAMES = 100;
    private static final int TEST_ALPHABET_SIZE = 3;
    private static final int GENERATED_WORDS = 243;

    @TempDir
    Path directory;

    @Test
    void loadsActualDictionaryAndRunsOneHundredAutomaticGames() throws IOException, GameException {
        PrintWriter log = new PrintWriter(Writer.nullWriter());


        WordleDictionary dictionary = new WordleDictionaryLoader(log).load(Path.of("words_ru.txt"));


        assertFalse(dictionary.getWords().isEmpty());
        assertEquals(dictionary.getWords().size(), new HashSet<>(dictionary.getWords()).size());

        for (String word : dictionary.getWords()) {
            assertTrue(WordleDictionary.isValidWord(word));
            assertEquals(WordleDictionary.normalize(word), word);
        }

        for (int seed = 0; seed < AUTOMATIC_GAMES; seed++) {
            WordleGame game = new WordleGame(dictionary, new Random(seed), log);


            playAutomatically(game);


            assertTrue(game.isFinished());
            assertTrue(game.getHistory().size() <= WordleGame.MAX_ATTEMPTS);
            assertEquals(
                    WordleGame.MAX_ATTEMPTS - game.getHistory().size(),
                    game.getRemainingAttempts()
            );

            if (game.isWon()) {
                assertEquals("+++++", game.getHistory().get(game.getHistory().size() - 1).feedback());
            } else {
                assertEquals(0, game.getRemainingAttempts());
            }
        }
    }

    @Test
    void consoleCanPlayActualDictionaryUsingOnlyEnters() throws IOException {
        Path logFile = directory.resolve("game.log");
        StringWriter screen = new StringWriter();


        Wordle.run(
                Path.of("words_ru.txt"),
                logFile,
                new StringReader("\n".repeat(6)),
                new PrintWriter(screen),
                new Random(42)
        );


        String text = screen.toString();
        long guesses = text.lines()
                .filter(line -> line.contains("Подсказка: "))
                .count();

        assertTrue(guesses >= 1 && guesses <= 6);
        assertTrue(text.contains("Загаданное слово:"));
        assertTrue(text.contains("Вы угадали слово") || text.contains("Попытки закончились"));
        assertFalse(text.contains("Не удалось"));
        assertFalse(text.contains("Ввод завершён"));
        assertTrue(Files.readString(logFile).contains("Игра завершена"));
    }

    @Test
    void neverTruncatesDictionaryWhenLogPointsToSameFile() throws IOException {
        Path dictionary = directory.resolve("words.txt");
        StringWriter screen = new StringWriter();

        Files.writeString(dictionary, "герой\n");


        Wordle.run(
                dictionary,
                directory.resolve("./words.txt"),
                new StringReader("\n"),
                new PrintWriter(screen),
                new Random(42)
        );


        assertEquals("герой\n", Files.readString(dictionary));
        assertTrue(screen.toString().contains("Файл лога должен отличаться"));
    }

    @Test
    void neverTruncatesDictionaryThroughHardLink() throws IOException {
        Path dictionary = directory.resolve("words.txt");
        Path logFile = directory.resolve("game.log");
        StringWriter screen = new StringWriter();

        Files.writeString(dictionary, "герой\n");
        Files.createLink(logFile, dictionary);


        Wordle.run(
                dictionary,
                logFile,
                new StringReader("\n"),
                new PrintWriter(screen),
                new Random(42)
        );


        assertEquals("герой\n", Files.readString(dictionary));
        assertTrue(screen.toString().contains("Файл лога должен отличаться"));
    }

    @Test
    void feedbackRespectsExactPositionsAndOccurrenceCountsExhaustively() {
        List<String> words = IntStream.range(0, GENERATED_WORDS)
                .mapToObj(this::generateWord)
                .toList();


        for (String answer : words) {
            for (String guess : words) {
                String result = WordleDictionary.feedback(answer, guess);


                assertExactPositions(answer, guess, result);
                assertOccurrenceCounts(answer, guess, result);
            }
        }
    }

    private void playAutomatically(WordleGame game) throws GameException {
        Set<String> suggestions = new HashSet<>();

        while (!game.isFinished()) {
            String suggestion = game.suggestWord();

            assertTrue(suggestions.add(suggestion));

            game.makeMove(suggestion);

            assertTrue(game.getCandidates().contains(game.getAnswer()));
            assertTrue(game.getRemainingAttempts() >= 0);
        }
    }

    private String generateWord(int number) {
        StringBuilder word = new StringBuilder();

        for (int i = 0; i < WordleDictionary.WORD_LENGTH; i++) {
            word.append((char) ('а' + number % TEST_ALPHABET_SIZE));
            number /= TEST_ALPHABET_SIZE;
        }

        return word.toString();
    }

    private void assertExactPositions(String answer, String guess, String result) {
        for (int i = 0; i < WordleDictionary.WORD_LENGTH; i++) {
            assertEquals(answer.charAt(i) == guess.charAt(i), result.charAt(i) == '+');
        }
    }

    private void assertOccurrenceCounts(String answer, String guess, String result) {
        for (char letter = 'а'; letter < 'а' + TEST_ALPHABET_SIZE; letter++) {
            int inAnswer = 0;
            int inGuess = 0;
            int marked = 0;

            for (int i = 0; i < WordleDictionary.WORD_LENGTH; i++) {
                if (answer.charAt(i) == letter) {
                    inAnswer++;
                }

                if (guess.charAt(i) == letter) {
                    inGuess++;

                    if (result.charAt(i) != '-') {
                        marked++;
                    }
                }
            }

            assertEquals(Math.min(inAnswer, inGuess), marked);
        }
    }
}
