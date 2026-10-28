package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

/*
этот класс содержит в себе список слов List<String>
    его методы похожи на методы списка, но учитывают особенности игры
    также этот класс может содержать рутинные функции по сравнению слов, букв и т.д.
 */
public class WordleDictionary {

    public static final int WORD_LENGTH = 5;

    private static final Pattern WORD_PATTERN = Pattern.compile("[а-я]{" + WORD_LENGTH + "}");

    private final List<String> words;
    private final Set<String> wordSet;

    public WordleDictionary(Collection<String> source, PrintWriter log) {
        Objects.requireNonNull(source, "Список слов не должен быть null");
        Objects.requireNonNull(log, "Лог не должен быть null");

        Set<String> uniqueWords = normalizeWords(source);

        if (uniqueWords.isEmpty()) {
            throw new IllegalArgumentException("Словарь не содержит подходящих слов");
        }

        words = List.copyOf(uniqueWords);
        wordSet = Set.copyOf(uniqueWords);

        log.println("Слов в игровом словаре: " + words.size());
    }

    public static String normalize(String word) {
        return Objects.requireNonNull(word, "Слово не должно быть null")
                .toLowerCase(Locale.ROOT)
                .replace('ё', 'е');
    }

    public static boolean isValidWord(String word) {
        return word != null && WORD_PATTERN.matcher(normalize(word)).matches();
    }

    public boolean contains(String word) {
        return wordSet.contains(normalize(word));
    }

    public List<String> getWords() {
        return words;
    }

    public String randomWord(Random random) {
        Objects.requireNonNull(random, "Генератор случайных чисел не должен быть null");

        return words.get(random.nextInt(words.size()));
    }

    private static Set<String> normalizeWords(Collection<String> source) {
        Set<String> uniqueWords = new LinkedHashSet<>();

        for (String word : source) {
            String normalized = normalize(word);

            if (isValidWord(normalized)) {
                uniqueWords.add(normalized);
            }
        }

        return uniqueWords;
    }

}
