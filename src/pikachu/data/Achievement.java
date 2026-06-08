package pikachu.data;

public class Achievement implements Comparable<Achievement> {
    private final int score;
    private final String difficulty;

    public Achievement(int score, String difficulty) {
        this.score = score;
        this.difficulty = difficulty;
    }

    public int getScore() {
        return score;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public static Achievement parse(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] parts = line.split(",", 2);
        if (parts.length < 2) {
            return null;
        }
        try {
            int score = Integer.parseInt(parts[0].trim());
            String diff = parts[1].trim();
            return new Achievement(score, diff);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String serialize() {
        return score + "," + difficulty;
    }

    @Override
    public int compareTo(Achievement other) {
        return Integer.compare(other.score, this.score);
    }
}