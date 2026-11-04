package ru.yandex.practicum;

import java.io.IOException;

class LogWriteException extends IOException {

    LogWriteException() {
        super("Ошибка записи в лог-файл");
    }
}
