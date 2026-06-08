package pikachu.data;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public List<Achievement> loadAchievements() {
        try {
            if (Files.notExists(filePath)) {
                Files.createDirectories(filePath.getParent());
                Files.createFile(filePath);
            }
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            List<Achievement> achievements = new ArrayList<>();
            for (String line : lines) {
                Achievement achievement = Achievement.parse(line);
                if (achievement != null) {
                    achievements.add(achievement);
                }
            }
            return achievements;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public List<Achievement> getTopAchievements(int max) {
        List<Achievement> achievements = loadAchievements();
        Collections.sort(achievements);
        return achievements.size() <= max ? achievements : achievements.subList(0, max);
    }

    public void addAchievement(Achievement achievement) {
        if (achievement == null) {
            return;
        }
        List<Achievement> achievements = loadAchievements();
        achievements.add(achievement);
        saveAchievements(achievements);
    }

    private void saveAchievements(List<Achievement> achievements) {
        try {
            Files.createDirectories(filePath.getParent());
            List<String> lines = new ArrayList<>();
            for (Achievement achievement : achievements) {
                lines.add(achievement.serialize());
            }
            Files.write(filePath, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ignored) {
        }
    }
}