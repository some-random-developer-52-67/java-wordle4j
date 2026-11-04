package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WordleTest {

    @TempDir
    Path directory;

    private final StringWriter screen = new StringWriter();
    private final StringWriter messages = new StringWriter();
    private final PrintWriter output = new PrintWriter(screen);
    private final PrintWriter log = new PrintWriter(messages);

    @Test
    void displaysFriendlyErrorsAndDoesNotSpendAttempts() throws IOException {
        WordleGame game = createGame();
        StringReader input = new StringReader("кот\nabcde\nкошка\nГЕРОЙ\n");


        Wordle.play(game, input, output, log);


        assertEquals(1, game.getHistory().size());
        assertTrue(screen.toString().contains("Введите слово из пяти русских букв"));
        assertTrue(screen.toString().contains("Слова «кошка» нет в словаре"));
        assertTrue(screen.toString().contains("+++++"));
        assertTrue(screen.toString().contains("Вы угадали слово"));
        assertTrue(screen.toString().contains("Загаданное слово: герой"));
        assertFalse(screen.toString().contains("Exception"));
        assertTrue(messages.toString().contains("Отклонён ввод: кот"));
    }

    @Test
    void emptyEnterSubmitsSuggestionAndSpendsAttempt() throws IOException {
        WordleDictionary dictionary = new WordleDictionary(List.of("герой"), log);
        WordleGame game = new WordleGame(dictionary, new Random(42), log);


        Wordle.play(game, new StringReader("\n"), output, log);


        assertTrue(game.isWon());
        assertEquals(5, game.getRemainingAttempts());
        assertTrue(screen.toString().contains("Подсказка: герой"));
        assertTrue(screen.toString().contains("+++++"));
    }

    @Test
    void eofStopsSessionAndRevealsAnswer() throws IOException {
        WordleGame game = createGame();


        Wordle.play(game, new StringReader(""), output, log);


        assertTrue(game.getHistory().isEmpty());
        assertTrue(screen.toString().contains("Ввод завершён"));
        assertTrue(screen.toString().contains("Загаданное слово: герой"));
        assertFalse(screen.toString().contains("Попытки закончились"));
    }

    @Test
    void stopsAfterSixGuessesWithoutReadingSeventh() throws IOException {
        WordleGame game = createGame();
        StringReader input = new StringReader("банан\n".repeat(6) + "герой\n");


        Wordle.play(game, input, output, log);


        assertEquals(6, game.getHistory().size());
        assertFalse(game.isWon());
        assertTrue(screen.toString().contains("Попытки закончились"));
        assertTrue(screen.toString().contains("Загаданное слово: герой"));
    }

    @Test
    void loadsDictionaryAndClosesLogAfterGame() throws IOException {
        Path dictionary = directory.resolve("words.txt");
        Path logFile = directory.resolve("logs/game.log");

        Files.writeString(dictionary, "ГЕРОЙ\nкот\n");


        Wordle.run(dictionary, logFile, new StringReader("\n"), output, new Random(42));


        String contents = Files.readString(logFile);

        assertTrue(screen.toString().contains("Вы угадали слово"));
        assertTrue(contents.contains("Прочитано строк: 2"));
        assertTrue(contents.contains("Игра завершена, победа: true"));
        assertFalse(screen.toString().contains("Новая игра, ответ:"));
    }

    @Test
    void distinguishesMissingDictionaryFromEmptyDictionary() throws IOException {
        Path dictionary = directory.resolve("words.txt");
        Path logFile = directory.resolve("game.log");


        Wordle.run(dictionary, logFile, new StringReader(""), output, new Random(42));


        assertTrue(screen.toString().contains("Файл словаря не найден"));
        assertTrue(Files.readString(logFile).contains("FileNotFoundException"));


        screen.getBuffer().setLength(0);
        Files.writeString(dictionary, "кот\n");


        Wordle.run(dictionary, logFile, new StringReader(""), output, new Random(42));


        assertTrue(screen.toString().contains("В словаре нет подходящих"));
        assertTrue(Files.readString(logFile).contains("EmptyDictionaryException"));
        assertFalse(screen.toString().contains("Exception"));
    }

    @Test
    void reportsLogCreationFailureBeforeStartingGame() {
        Path dictionary = directory.resolve("words.txt");


        Wordle.run(dictionary, directory, new StringReader(""), output, new Random(42));


        assertTrue(screen.toString().contains("Не удалось создать лог-файл"));
        assertFalse(screen.toString().contains("Угадайте"));
    }

    @Test
    void logsInputFailureAtApplicationBoundary() throws IOException {
        Path dictionary = directory.resolve("words.txt");
        Path logFile = directory.resolve("game.log");
        Reader input = createBrokenReader();

        Files.writeString(dictionary, "герой\n");


        Wordle.run(dictionary, logFile, input, output, new Random(42));


        assertTrue(screen.toString().contains("Не удалось прочитать"));
        assertFalse(screen.toString().contains("test input failure"));
        assertTrue(Files.readString(logFile).contains("test input failure"));
    }

    @Test
    void detectsPrintWriterErrorsInsteadOfIgnoringThem() {
        WordleGame game = createGame();
        StringReader input = new StringReader("герой\n");
        PrintWriter brokenLog = new PrintWriter(createBrokenWriter());

        brokenLog.println("test");


        assertThrows(
                LogWriteException.class,
                () -> Wordle.play(game, input, output, brokenLog)
        );
    }

    private WordleGame createGame() {
        WordleDictionary dictionary = new WordleDictionary(
                List.of("герой", "гонец", "банан"),
                log
        );

        return new WordleGame(dictionary, "герой", new Random(42), log);
    }

    private Reader createBrokenReader() {
        return new Reader() {

            @Override
            public int read(char[] buffer, int offset, int length) throws IOException {
                throw new IOException("test input failure");
            }

            @Override
            public void close() {
            }
        };
    }

    private Writer createBrokenWriter() {
        return new Writer() {

            @Override
            public void write(char[] buffer, int offset, int length) throws IOException {
                throw new IOException("test log failure");
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
    }
}
