import java.util.*;

public class Player {
    private int energy = 100;
    private int caffeine = 0;
    private int gearBonus = 0;
    private int copilotUsed = 0;
    private boolean copilotActive = false;
    private int lastActionTime = 0;

    private Map<String, Integer> bugs = new HashMap<>();
    private Queue<BugEvent> futureBugs = new LinkedList<>();
    private Queue<String> logs = new LinkedList<>();

    public Player() {
        bugs.put("Syntax", 3);
        bugs.put("Logic", 2);
        bugs.put("Runtime", 1);
    }

    // --- Logs ---
    public void addLog(String log) {
        if (logs.size() >= 5) logs.poll();
        logs.offer(log);
    }

    public void displayLogs() {
        if (logs.isEmpty()) System.out.println(" - No recent activity.");
        else for (String log : logs) System.out.println(" - " + log);
    }

    // --- Actions ---
    public void squashBug() {
        if (bugs.isEmpty()) {
            addLog("No bugs to squash.");
            energy -= 5;
            return;
        }

        List<String> bugTypes = new ArrayList<>(bugs.keySet());
        String target = bugTypes.get(new Random().nextInt(bugTypes.size()));

        // Chance to fail and spawn new bug type
        if (Utils.randomChance(20 - gearBonus * 2)) {
            String newBugType = Utils.randomBugType();
            bugs.put(newBugType, bugs.getOrDefault(newBugType, 0) + 1);
            addLog("Squash failed! Spawned new bug type: " + newBugType);
        } else {
            bugs.put(target, bugs.get(target) - 1);
            if (bugs.get(target) <= 0) bugs.remove(target);
            addLog("Squashed " + target + " bug! Remaining bugs: " + totalBugs());
        }

        // Delayed consequences
        if (Utils.randomChance(50)) {
            int delayed = Utils.randomInt(2, 5);
            futureBugs.offer(new BugEvent(target, delayed));
            addLog(delayed + " delayed " + target + " bug(s) may return later!");
        }

        energy -= 10;
    }

    public void runCheck() {
        addLog("Running code checks...");
        int found = Utils.randomInt(0, 3);
        for (int i = 0; i < found; i++) {
            String bugType = Utils.randomBugType();
            bugs.put(bugType, bugs.getOrDefault(bugType, 0) + 1);
            addLog("Check uncovered a new " + bugType + " bug!");
        }
        energy -= 15;
    }

    public void refactor() {
        addLog("Refactoring code...");
        energy -= 20;
        gearBonus += 1;
        addLog("Gear improved! Reduces critical bug chance by " + gearBonus * 5 + "%");
    }

    public void scan() {
        addLog("Scanning code...");
        if (Utils.randomChance(50)) addLog("Some bugs may slow growth next turn.");
        else addLog("Scan shows code is messy.");
        energy -= 5;
    }

    public void drinkCoffee() {
        caffeine++;
        energy += 20;
        if (energy > 100) energy = 100;
        addLog("Coffee consumed. Energy: " + energy + "/100");
        if (Utils.randomChance(25)) {
            String bugType = Utils.randomBugType();
            bugs.put(bugType, bugs.getOrDefault(bugType, 0) + 1);
            addLog("Caffeine jitter! A " + bugType + " bug appeared.");
        }
    }

    public void askCopilot() {
        if (copilotUsed >= 5) {
            addLog("Copilot limit reached! No more advice.");
            copilotActive = false;
            return;
        }
        copilotUsed++;
        copilotActive = true;
        addLog("GitHub Copilot advice received! Slightly restored energy and clarity.");
        energy += 15;
        if (energy > 100) energy = 100;
    }

    // --- Bug Multiplication & Delayed Events ---
    public void bugsMultiply() {
        for (String type : new HashSet<>(bugs.keySet())) {
            int count = bugs.get(type);
            int multiplier = 1 + (int)((100 - energy) / 25);
            if (Utils.randomChance(30 + count * 5)) {
                int grow = Utils.randomInt(1, Math.min(5, count)) * multiplier;
                bugs.put(type, count + grow);
                addLog(type + " bug(s) multiplied! Total now: " + bugs.get(type));
            }
        }
    }

    public void processFutureBugs() {
        if (!futureBugs.isEmpty()) {
            BugEvent event = futureBugs.poll();
            bugs.put(event.type, bugs.getOrDefault(event.type, 0) + event.count);
            addLog(event.count + " delayed " + event.type + " bug(s) returned!");
        }
    }

    // --- Utilities ---
    public int totalBugs() {
        return bugs.values().stream().mapToInt(i -> i).sum();
    }

    public int getEnergy() { return energy; }
    public void reduceEnergy(int n) { energy -= n; }
    public int getCaffeine() { return caffeine; }
    public int getGearBonus() { return gearBonus; }
    public int getCopilotUsed() { return copilotUsed; }
    public boolean isCopilotActive() { return copilotActive; }
    public void setLastActionTime(int t) { lastActionTime = t; }
    public int getLastActionTime() { return lastActionTime; }

    private static class BugEvent {
        String type;
        int count;
        BugEvent(String t, int c) { type = t; count = c; }
    }

    public Map<String, Integer> getBugs() {
        return bugs;
    }
}
