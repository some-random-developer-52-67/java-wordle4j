package ru.yandex.practicum;

import java.io.IOException;
import java.nio.file.Path;

public class EmptyDictionaryException extends IOException {

    public EmptyDictionaryException(Path path) {
        super("В словаре нет подходящих пятибуквенных слов: " + path);
    }
}
