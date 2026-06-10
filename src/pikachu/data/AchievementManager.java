package pikachu.data;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages the persistence of game achievements (high scores).
 * Handles loading, saving, and retrieving top achievements from a local text file.
 */
public class AchievementManager {

    private static final String STORAGE_RESOURCE = "/pikachu/data/achievements.txt";
    private final Path filePath;

    public AchievementManager() {
        this.filePath = resolveStoragePath();
    }

    private Path resolveStoragePath() {
        try {
            URL resourceUrl = getClass().getResource(STORAGE_RESOURCE);
            if (resourceUrl != null && "file".equalsIgnoreCase(resourceUrl.getProtocol())) {
                return Paths.get(resourceUrl.toURI());
            }
        } catch (Exception ignored) {
        }
        return Paths.get(System.getProperty("user.dir"), "src", "pikachu", "data", "achievements.txt");
    }

    /**
     * Load achievements from storage file. Creates file if missing.
     */
    public List<Achievement> loadAchievements() {
        try {
            if (Files.notExists(filePath)) {
                Files.createDirectories(filePath.getParent());
                Files.createFile(filePath);
            }
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            List<Achievement> achievements = new ArrayList<>();
            for (String line : lines) {
                Achievement a = Achievement.parse(line);
                if (a != null) achievements.add(a);
            }
            return achievements;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Add a new achievement and persist the updated list.
     * If newAchievement is null or score <= 0, it will be ignored.
     * After adding, the list is sorted (descending) and saved.
     */
    public void addAchievement(Achievement newAchievement) {
        if (newAchievement == null) return;
        try {
            if (newAchievement.getScore() <= 0) return;
        } catch (Exception e) {
            return;
        }

        List<Achievement> achievements = loadAchievements();
        achievements.add(newAchievement);
        // Sort based on Achievement.compareTo (expected descending)
        Collections.sort(achievements);
        saveAchievements(achievements);
    }

    /**
     * Save the provided list of achievements to file (overwrites).
     */
    private void saveAchievements(List<Achievement> achievements) {
        try {
            Files.createDirectories(filePath.getParent());
            List<String> lines = new ArrayList<>();
            for (Achievement a : achievements) {
                lines.add(a.serialize());
            }
            Files.write(filePath, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ignored) {
        }
    }

    /**
     * Get top achievements filtered by difficulty. Returns up to 'max' entries.
     * Difficulty match is case-insensitive.
     */
    public List<Achievement> getTopAchievements(int max, String difficulty) {
        if (difficulty == null) difficulty = "";
        String diffLower = difficulty.trim().toLowerCase();
        List<Achievement> all = loadAchievements();
        List<Achievement> filtered = all.stream()
                .filter(a -> {
                    String d = a.getDifficulty();
                    return d != null && d.trim().toLowerCase().equals(diffLower);
                })
                .collect(Collectors.toList());
        Collections.sort(filtered);
        return filtered.size() <= max ? filtered : filtered.subList(0, max);
    }
}