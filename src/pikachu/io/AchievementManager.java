package pikachu.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AchievementManager {

    private static final Path DATA_FILE = Paths.get("data", "achievements.txt");

    public AchievementManager() {
        try {
            if (Files.notExists(DATA_FILE.getParent())) {
                Files.createDirectories(DATA_FILE.getParent());
            }
            if (Files.notExists(DATA_FILE)) {
                Files.createFile(DATA_FILE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<ScoreEntry> loadTopScores(int limit) {
        List<ScoreEntry> list = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(DATA_FILE, StandardCharsets.UTF_8);
            for (String line : lines) {
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    int score = Integer.parseInt(parts[0]);
                    String difficulty = parts[1];
                    list.add(new ScoreEntry(score, difficulty));
                }
            }
        } catch (IOException ignored) {
        }
        list.sort(Comparator.comparingInt(ScoreEntry::getScore).reversed());
        return list.size() > limit ? list.subList(0, limit) : list;
    }

    public void saveScore(int score, String difficulty) {
        String line = score + "|" + difficulty;
        try (BufferedWriter writer = Files.newBufferedWriter(DATA_FILE, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class ScoreEntry {
        private final int score;
        private final String difficulty;

        public ScoreEntry(int score, String difficulty) {
            this.score = score;
            this.difficulty = difficulty;
        }

        public int getScore() {
            return score;
        }

        public String getDifficulty() {
            return difficulty;
        }
    }
}