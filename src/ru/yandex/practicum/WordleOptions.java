package ru.yandex.practicum;

import java.nio.file.Path;

record WordleOptions(Path dictionaryPath, Path logPath) {

    private static final int MAX_ARGUMENTS = 2;
    private static final String DEFAULT_DICTIONARY_PATH = "words_ru.txt";
    private static final String DEFAULT_LOG_PATH = "out/wordle.log";

    static WordleOptions parse(String[] args) {
        if (args.length > MAX_ARGUMENTS) {
            throw new IllegalArgumentException("Запуск: Wordle [путь к словарю] [путь к логу]");
        }

        String dictionaryPath = args.length > 0
                ? args[0]
                : DEFAULT_DICTIONARY_PATH;
        String logPath = args.length > 1
                ? args[1]
                : DEFAULT_LOG_PATH;

        return new WordleOptions(Path.of(dictionaryPath), Path.of(logPath));
    }
}
