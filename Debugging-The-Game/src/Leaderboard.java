import java.util.ArrayList;
import java.util.Comparator;

public class Leaderboard {
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<Integer> scores = new ArrayList<>();

    public void addScore(Player player, int timeRemaining) {
        names.add("Player");
        scores.add(timeRemaining);
    }

    public void displayTopScores() {
        System.out.println("\n=== LEADERBOARD ===");
        for (int i = 0; i < scores.size(); i++) {
            System.out.println((i+1) + ". " + names.get(i) + " - Time Left: " + scores.get(i) + "s");
        }
    }
}
