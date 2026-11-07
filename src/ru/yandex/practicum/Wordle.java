package ru.yandex.practicum;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Random;

/*
в главном классе нам нужно:
    создать лог-файл (он должен передаваться во все классы)
    создать загрузчик словарей WordleDictionaryLoader
    загрузить словарь WordleDictionary с помощью класса WordleDictionaryLoader
    затем создать игру WordleGame и передать ей словарь
    вызвать игровой метод в котором в цикле опрашивать пользователя и передавать информацию в игру
    вывести состояние игры и конечный результат
 */
public class Wordle {

    public static void main(String[] args) {
        PrintWriter output = new PrintWriter(
                new OutputStreamWriter(System.out, StandardCharsets.UTF_8),
                true
        );

        try {
            WordleOptions options = WordleOptions.parse(args);

            run(
                    options.dictionaryPath(),
                    options.logPath(),
                    new InputStreamReader(System.in, StandardCharsets.UTF_8),
                    output,
                    new Random()
            );
        } catch (InvalidPathException exception) {
            output.println("Некорректный путь к файлу словаря или лога");
        } catch (IllegalArgumentException exception) {
            output.println(exception.getMessage());
        }
    }

    static void run(
            Path dictionaryPath,
            Path logPath,
            Reader input,
            PrintWriter output,
            Random random
    ) {
        PrintWriter log;

        try {
            log = WordleLog.open(dictionaryPath, logPath);
        } catch (IllegalArgumentException exception) {
            output.println(exception.getMessage());
            output.flush();
            return;
        } catch (IOException | RuntimeException exception) {
            output.println("Не удалось создать лог-файл, проверьте путь и права доступа");
            output.flush();
            return;
        }

        try (log) {
            runSession(dictionaryPath, input, output, random, log);
        }

        if (log.checkError()) {
            output.println("Не удалось записать лог-файл, проверьте свободное место и права доступа");
        }

        output.flush();
    }

    static void play(
            WordleGame game,
            Reader input,
            PrintWriter output,
            PrintWriter log
    ) throws IOException {
        WordleLog.check(log);

        BufferedReader reader = new BufferedReader(input);

        output.println("Угадайте существительное из пяти букв, у вас шесть попыток");
        output.println("+ — точное совпадение, ^ — буква на другом месте, - — нет подходящего вхождения");
        output.println("Пустой Enter — компьютер выбирает слово и делает им ход");

        while (!game.isFinished()) {
            printPrompt(game, output);

            String word = reader.readLine();

            if (word == null) {
                output.println("Ввод завершён, игра остановлена");
                log.println("Конец ввода, сделано ходов: " + game.getHistory().size());
                break;
            }

            playTurn(game, word, output, log);
            WordleLog.check(log);
        }

        printResult(game, output, log);
        WordleLog.check(log);
        output.flush();
    }

    private static void runSession(
            Path dictionaryPath,
            Reader input,
            PrintWriter output,
            Random random,
            PrintWriter log
    ) {
        try {
            WordleDictionary dictionary = new WordleDictionaryLoader(log).load(dictionaryPath);
            WordleGame game = new WordleGame(dictionary, random, log);

            play(game, input, output, log);
        } catch (FileNotFoundException exception) {
            reportFailure("Файл словаря не найден или недоступен", exception, output, log);
        } catch (EmptyDictionaryException exception) {
            reportFailure("В словаре нет подходящих пятибуквенных слов", exception, output, log);
        } catch (IOException exception) {
            reportFailure("Не удалось прочитать словарь или пользовательский ввод", exception, output, log);
        } catch (RuntimeException exception) {
            reportFailure("Не удалось продолжить игру, подробности записаны в лог", exception, output, log);
        }
    }

    private static void printPrompt(WordleGame game, PrintWriter output) {
        StringBuilder message = new StringBuilder("Осталось попыток: ")
                .append(game.getRemainingAttempts())
                .append("\n> ");

        output.print(message);
        output.flush();
    }

    private static void playTurn(WordleGame game, String word, PrintWriter output, PrintWriter log) {
        try {
            if (word.isEmpty()) {
                word = game.suggestWord();
                output.println("Подсказка: " + word);
            }

            Move move = game.makeMove(word);

            output.println(move.feedback());
        } catch (GameException exception) {
            output.println(exception.getMessage());
            log.println("Отклонён ввод: %s, причина: %s".formatted(word, exception.getMessage()));
        }
    }

    private static void printResult(WordleGame game, PrintWriter output, PrintWriter log) {
        if (game.isWon()) {
            output.println("Вы угадали слово");
        } else if (game.isFinished()) {
            output.println("Попытки закончились");
        }

        output.println("Загаданное слово: " + game.getAnswer());
        log.println("Игра завершена, победа: " + game.isWon() + ", осталось попыток: " + game.getRemainingAttempts());
    }

    private static void reportFailure(String message, Exception exception, PrintWriter output, PrintWriter log) {
        if (!log.checkError()) {
            output.println(message);
            exception.printStackTrace(log);
        }
    }
}
