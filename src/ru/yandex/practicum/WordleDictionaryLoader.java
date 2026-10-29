package ru.yandex.practicum;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
этот класс содержит в себе всю рутину по работе с файлами словарей и с кодировками
    ему нужны методы по загрузке списка слов из файла по имени файла
    на выходе должен быть класс WordleDictionary
 */
public class WordleDictionaryLoader {

    private final PrintWriter log;

    public WordleDictionaryLoader(PrintWriter log) {
        this.log = Objects.requireNonNull(log, "Лог не должен быть null");
    }

    public WordleDictionary load(Path path) throws IOException {
        log.println("Загрузка словаря: " + path);

        List<String> words = new ArrayList<>();
        int lines = 0;

        try (
                FileReader fileReader = new FileReader(path.toFile(), StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(fileReader)
        ) {
            String line;

            while ((line = reader.readLine()) != null) {
                lines++;
                String word = WordleDictionary.normalize(line);

                if (WordleDictionary.isValidWord(word)) {
                    words.add(word);
                }
            }
        }

        log.println("Прочитано строк: " + lines);

        if (words.isEmpty()) {
            throw new EmptyDictionaryException(path);
        }

        return new WordleDictionary(words, log);
    }
}
