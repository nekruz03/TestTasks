package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        Set<String> uniqueSet = new HashSet<>();
        ArrayList<String> uniqueLines = new ArrayList<>();
        if (args.length == 0) {
            System.err.println("Укажите путь к файлу! Пример: java -jar program.jar /путь/к/файлу");
            System.exit(1);
        }
        String filePath = args[0];

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("\"")) continue;
                if (uniqueSet.add(line)) {
                    uniqueLines.add(line);
                    createPosition(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int n = uniqueLines.size();
        UnionFind unionFind = new UnionFind(n);

        for (int col = 0; col < maxCol; col++) {
            Map<String, Integer> valueToRow = new HashMap<>();
            for (int i = 0; i < n; i++) {
                String line = uniqueLines.get(i);
                int[] pos = position.get(i);
                String value = getColumnValue(line, pos, col);
                if (!value.isEmpty()) {
                    Integer prev = valueToRow.putIfAbsent(value, i);
                    if (prev != null) {
                        unionFind.union(i, prev);
                    }
                }
            }
        }
        List<List<Integer>> largeGroups = buildGroups(unionFind, n);

        printGroups(largeGroups, uniqueLines);
    }

    private static final ArrayList<int[]> position = new ArrayList<>();
    private static int maxCol = 0;

    private static void createPosition(String line) {
        List<Integer> pos = new ArrayList<>();
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == ';') {
                pos.add(i);
            }
        }
        int[] array = new int[pos.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = pos.get(i);
        }
        position.add(array);
        int cols = array.length + 1;
        if (cols > maxCol) maxCol = cols;
    }

    private static String getColumnValue(String line, int[] pos, int col) {
        int start, end;
        if (col == 0) {
            start = 0;
        } else {
            if (col - 1 < pos.length) {
                start = pos[col - 1] + 1;
            } else {
                return "";
            }
        }
        if (col < pos.length) {
            end = pos[col] - 1;
        } else {
            end = line.length() - 1;
        }
        if (start <= end) {
            return line.substring(start, end + 1);
        } else {
            return "";
        }
    }

    private static List<List<Integer>> buildGroups(UnionFind uf, int n) {
        Map<Integer, List<Integer>> groups = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int root = uf.find(i);
            List<Integer> list = groups.get(root);
            if (list == null) {
                list = new ArrayList<>();
                groups.put(root, list);
            }
            list.add(i);
        }

        List<List<Integer>> largeGroups = new ArrayList<>();
        for (List<Integer> list : groups.values()) {
            if (list.size() > 1) largeGroups.add(list);
        }

        largeGroups.sort((a, b) -> Integer.compare(b.size(), a.size()));
        return largeGroups;
    }

    private static void printGroups(List<List<Integer>> largeGroups, List<String> uniqueLines) {
        System.out.println(largeGroups.size());
        int groupNum = 1;
        for (List<Integer> group : largeGroups) {
            System.out.println("Группа " + groupNum);
            for (int idx : group) {
                System.out.println(uniqueLines.get(idx));
            }
            groupNum++;
        }
    }
}