import java.util.*;

public class Leaderboard {
    private List<Integer> scores = new ArrayList<>();

    public void update(int remainingBugs, int timeLeft) {
        int score = Math.max(0, 2000 - remainingBugs * 15 + Math.max(0, timeLeft) * 6);
        scores.add(score);
        Collections.sort(scores, Collections.reverseOrder());
    }

    public void display() {
        System.out.println("\n=== LEADERBOARD (top 5) ===");
        for (int i = 0; i < Math.min(5, scores.size()); i++) {
            System.out.println((i+1) + ". " + scores.get(i));
        }
        System.out.println("============================\n");
    }
}
