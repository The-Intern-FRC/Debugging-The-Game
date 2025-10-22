import java.util.LinkedList;
import java.util.Queue;

public class Player {
    private int bugs = 0;
    private int energy = 100;
    private int caffeine = 0;
    private int gearBonus = 0;
    private int copilotUsed = 0;
    private int lastActionTime = 0;

    private Queue<String> logs = new LinkedList<>();

    // --- Logs Management ---
    public void addLog(String log) {
        if (logs.size() >= 5) logs.poll(); // keep last 5 logs
        logs.offer(log);
    }

    public void displayLogs() {
        if (logs.isEmpty()) System.out.println(" - No recent activity.");
        else {
            for (String log : logs) System.out.println(" - " + log);
        }
    }

    // --- Actions ---
    public void squashBug() {
        if (bugs > 0) {
            bugs--;
            addLog("Squashed a bug! Bugs left: " + bugs);
        } else {
            addLog("No bugs to squash.");
        }
        energy -= 5;
    }

    public void runCheck() {
        addLog("Running checks... uncovering hidden issues.");
        if (Utils.randomChance(30)) {
            int found = Utils.randomInt(1, 3);
            bugs += found;
            addLog(found + " new bug(s) revealed!");
        } else {
            addLog("No hidden bugs found.");
        }
        energy -= 10;
    }

    public void refactor() {
        addLog("Refactoring code... may reduce future bug growth.");
        energy -= 15;
        gearBonus += 1;
        addLog("Gear improved! Bonus now: +" + gearBonus);
    }

    public void scan() {
        addLog("Scanning code for potential issues...");
        if (Utils.randomChance(50)) {
            addLog("Prediction: bug growth may slow next turn.");
        } else {
            addLog("Prediction: code still messy, no change.");
        }
        energy -= 5;
    }

    public void drinkCoffee() {
        caffeine += 1;
        energy += 20;
        if (energy > 100) energy = 100;
        addLog("Coffee consumed. Energy: " + energy + "/100");
        // caffeine may increase next bug growth slightly
        if (Utils.randomChance(25)) {
            bugs += 1;
            addLog("Uh oh… caffeine-induced bug appeared!");
        }
    }

    public void askCopilot() {
        if (copilotUsed >= 5) {
            addLog("Copilot limit reached! Can't ask for more help.");
            return;
        }
        copilotUsed++;
        addLog("GitHub Copilot advice received. Useful? Probably slightly.");
        // minor effect: small energy recovery
        energy += 5;
        if (energy > 100) energy = 100;
    }

    // --- Bug Multiplication ---
    public void bugsMultiply() {
        int newBugs = 0;
        if (Utils.randomChance(40)) {
            newBugs = Utils.randomInt(1, 3);
            bugs += newBugs;
            addLog(newBugs + " bug(s) multiplied unexpectedly!");
        }
    }

    // --- Getters / Setters ---
    public int getBugs() { return bugs; }
    public int getEnergy() { return energy; }
    public void reduceEnergy(int n) { energy -= n; }
    public int getCaffeine() { return caffeine; }
    public int getGearBonus() { return gearBonus; }
    public int getCopilotUsed() { return copilotUsed; }
    public void setLastActionTime(int t) { lastActionTime = t; }
    public int getLastActionTime() { return lastActionTime; }

    public void addBugs(int n) {
        bugs += n;
        addLog(n + " bug(s) appeared! Total bugs: " + bugs);
    }
}
