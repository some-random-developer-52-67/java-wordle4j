package ru.yandex.practicum;

public class WordNotFoundInDictionary extends GameException {

    public WordNotFoundInDictionary(String word) {
        super("Слова «%s» нет в словаре".formatted(word));
    }
}
