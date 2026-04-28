package org.example;

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Укажите путь к файлу! Пример: java -jar program.jar /путь/к/файлу");
            System.exit(1);
        }

        String filePath = args[0];

        List<String> originalLines = new ArrayList<>();
        List<List<String>> linesFields = new ArrayList<>();
        Set<String> uniqueSet = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                List<String> fields = splitLine(line);
                if (fields == null) {
                    continue;
                }

                if (uniqueSet.add(line)) {
                    originalLines.add(line);
                    linesFields.add(fields);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
            System.exit(1);
        }

        int n = originalLines.size();
        if (n == 0) {
            System.out.println("0");
            return;
        }

        int maxCol = 0;
        for (List<String> fields : linesFields) {
            maxCol = Math.max(maxCol, fields.size());
        }

        UnionFind uf = new UnionFind(n);

        for (int col = 0; col < maxCol; col++) {
            Map<String, Integer> valueToRow = new HashMap<>();
            for (int i = 0; i < n; i++) {
                List<String> fields = linesFields.get(i);
                String value = col < fields.size() ? fields.get(col) : "";

                if (value != null && !value.isEmpty()) {
                    Integer prev = valueToRow.putIfAbsent(value, i);
                    if (prev != null) {
                        uf.union(i, prev);
                    }
                }
            }
        }

        List<List<Integer>> groups = buildGroups(uf, n);

        printResult(groups, originalLines);
    }


    private static List<String> splitLine(String line) {
        List<String> fields = new ArrayList<>();
        int i = 0;
        int n = line.length();

        while (i < n) {
            // Пустое поле (начинается с ;)
            if (line.charAt(i) == ';') {
                fields.add("");
                i++;
                continue;
            }

            // Поле в кавычках
            if (line.charAt(i) == '"') {
                int j = i + 1;
                // Ищем закрывающую кавычку
                while (j < n && line.charAt(j) != '"') {
                    j++;
                }

                // Нет закрывающей кавычки - битая строка
                if (j >= n) {
                    return null;
                }

                // Проверяем, что после кавычки идёт ; или конец строки
                if (j + 1 < n && line.charAt(j + 1) != ';') {
                    return null;
                }

                // Извлекаем значение без кавычек
                String value = line.substring(i + 1, j);
                fields.add(value);
                i = j + 1;

                // Пропускаем разделитель, если он есть
                if (i < n && line.charAt(i) == ';') {
                    i++;
                }
            }
            // Поле без кавычек
            else {
                int j = i;
                while (j < n && line.charAt(j) != ';') {
                    // Если внутри поля встретилась кавычка - битая строка
                    if (line.charAt(j) == '"') {
                        return null;
                    }
                    j++;
                }

                String value = line.substring(i, j);
                fields.add(value);
                i = j;

                // Пропускаем разделитель, если он есть
                if (i < n && line.charAt(i) == ';') {
                    i++;
                }
            }
        }

        return fields;
    }

    private static List<List<Integer>> buildGroups(UnionFind uf, int n) {
        Map<Integer, List<Integer>> groupsMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int root = uf.find(i);
            groupsMap.computeIfAbsent(root, k -> new ArrayList<>()).add(i);
        }

        List<List<Integer>> largeGroups = new ArrayList<>();
        for (List<Integer> group : groupsMap.values()) {
            if (group.size() > 1) {
                largeGroups.add(group);
            }
        }

        // Сортируем по убыванию размера группы
        largeGroups.sort((a, b) -> Integer.compare(b.size(), a.size()));
        return largeGroups;
    }

    private static void printResult(List<List<Integer>> groups, List<String> originalLines) {
        System.out.println(groups.size());

        int groupNum = 1;
        for (List<Integer> group : groups) {
            System.out.println("Группа " + groupNum);
            for (int idx : group) {
                System.out.println(originalLines.get(idx));
            }
            if (groupNum < groups.size()) {
                System.out.println();
            }
            groupNum++;
        }
    }
}
