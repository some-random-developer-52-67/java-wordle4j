package ru.yandex.practicum;

public abstract class GameException extends Exception {

    protected GameException(String message) {
        super(message);
    }
}
