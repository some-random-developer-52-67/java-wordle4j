package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/*
в этом классе хранится словарь и состояние игры
    текущий шаг
    всё что пользователь вводил
    правильный ответ

в этом классе нужны методы, которые
    проанализируют совпадение слова с ответом
    предложат слово-подсказку с учётом всего, что вводил пользователь ранее

не забудьте про специальные типы исключений для игровых и неигровых ошибок
 */
public class WordleGame {

    public static final int MAX_ATTEMPTS = 6;

    private final String answer;
    private final WordleDictionary dictionary;
    private final Random random;
    private final PrintWriter log;
    private final List<Move> history = new ArrayList<>();
    private final Set<String> suggestedWords = new HashSet<>();

    private List<String> candidates;

    public WordleGame(WordleDictionary dictionary, Random random, PrintWriter log) {
        this(dictionary, dictionary.randomWord(random), random, log);
    }

    public WordleGame(
            WordleDictionary dictionary,
            String answer,
            Random random,
            PrintWriter log
    ) {
        this.dictionary = Objects.requireNonNull(dictionary, "Словарь не должен быть null");
        this.answer = WordleDictionary.normalize(answer);
        this.random = Objects.requireNonNull(random, "Генератор случайных чисел не должен быть null");
        this.log = Objects.requireNonNull(log, "Лог не должен быть null");

        if (!dictionary.contains(this.answer)) {
            throw new IllegalArgumentException("Ответ должен присутствовать в словаре");
        }

        candidates = dictionary.getWords();

        log.println("Новая игра, ответ: " + this.answer);
    }

    public Move makeMove(String input) throws InvalidWordException, WordNotFoundInDictionary {
        requireActiveGame();

        String word = validateGuess(input);
        Move move = new Move(word, WordleDictionary.feedback(answer, word));

        filterCandidates(move);
        history.add(move);
        logMove(move);

        return move;
    }

    public String suggestWord() {
        requireActiveGame();

        List<String> available = candidates.stream()
                .filter(word -> !suggestedWords.contains(word))
                .toList();

        if (available.isEmpty()) {
            throw new IllegalStateException("Не осталось новых слов для подсказки");
        }

        String suggestion = available.get(random.nextInt(available.size()));
        suggestedWords.add(suggestion);

        log.println("Подсказка: " + suggestion + ", доступных вариантов: " + available.size());

        return suggestion;
    }

    public List<String> getCandidates() {
        return List.copyOf(candidates);
    }

    public int getRemainingAttempts() {
        return MAX_ATTEMPTS - history.size();
    }

    public List<Move> getHistory() {
        return List.copyOf(history);
    }

    public String getAnswer() {
        return answer;
    }

    public boolean isWon() {
        return !history.isEmpty() && history.get(history.size() - 1).word().equals(answer);
    }

    public boolean isFinished() {
        return isWon() || getRemainingAttempts() == 0;
    }

    private String validateGuess(String input) throws InvalidWordException, WordNotFoundInDictionary {
        if (!WordleDictionary.isValidWord(input)) {
            throw new InvalidWordException();
        }

        String word = WordleDictionary.normalize(input);

        if (!dictionary.contains(word)) {
            throw new WordNotFoundInDictionary(word);
        }

        return word;
    }

    private void filterCandidates(Move move) {
        List<String> matching = candidates.stream()
                .filter(candidate -> WordleDictionary.feedback(candidate, move.word()).equals(move.feedback()))
                .toList();

        if (!matching.contains(answer)) {
            throw new IllegalStateException("При фильтрации потерян правильный ответ");
        }

        candidates = matching;
    }

    private void logMove(Move move) {
        StringBuilder message = new StringBuilder("Ход: ")
                .append(history.size())
                .append(", слово: ").append(move.word())
                .append(", результат: ").append(move.feedback())
                .append(", осталось попыток: ").append(getRemainingAttempts())
                .append(", кандидатов: ").append(candidates.size());

        log.println(message);
    }

    private void requireActiveGame() {
        if (isFinished()) {
            throw new IllegalStateException("Игра уже завершена");
        }
    }
}
