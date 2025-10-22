import java.io.Serializable;
import java.util.*;

/**
 * Player state and actions for Debugging-The-Game.
 * Serializable so SaveManager can persist it.
 */
public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int COPILOT_MAX = 5;

    // Core state
    private int energy = 100;
    private int caffeine = 0;
    private int gearBonus = 1;   // improves squash removal and lowers some risks
    private int copilotUsed = 0;
    private boolean copilotActive = false;

    private int lastActionTime = 0;

    // Bugs by type (LinkedHashMap to keep insertion order predictable in UI)
    private Map<String, Integer> bugs = new LinkedHashMap<>();

    // Delayed events: holds events that trigger on or after a specific turn
    private Queue<DelayedEvent> delayed = new LinkedList<>();

    // Logs queue for UI (last 6 messages). Transient to avoid serializing UI noise.
    private transient Deque<String> logs = new ArrayDeque<>();

    // Refactor state (reduces growth percent for next N turns)
    private int refactorTurnsRemaining = 0;
    private int refactorReductionPercent = 0; // percent, e.g. 50 means 50% reduction

    private transient Random rng = new Random();

    public Player() {
        // Starting mixed bugs (tunable)
        bugs.put("Syntax", 3);
        bugs.put("Logic", 3);
        bugs.put("Runtime", 2);
        initTransient();
        addLog("Player initialized. Welcome to the Orientation Game.");
    }

    // Initialize transient fields (useful after deserialization)
    private void initTransient() {
        if (logs == null) logs = new ArrayDeque<>();
        if (rng == null) rng = new Random();
    }

    // -------------------------
    // Logging
    // -------------------------
    public void addLog(String s) {
        initTransient();
        if (logs.size() >= 6) logs.removeFirst();
        logs.addLast(s);
    }

    public void displayLogs() {
        initTransient();
        if (logs.isEmpty()) {
            System.out.println(" - No recent activity.");
            return;
        }
        for (String l : logs) System.out.println(" - " + l);
    }

    // -------------------------
    // Actions (public, used by GameEngine)
    // -------------------------

    /**
     * Squash a bug. Chance of backfire depends on gear and energy.
     * currentTurn is used to schedule delayed consequences.
     */
    public void squashBug(int currentTurn) {
        initTransient();
        if (totalBugs() == 0) {
            addLog("No bugs to squash.");
            reduceEnergy(6);
            return;
        }

        String target = pickTargetType();

        // backfire chance increases when gear is low or energy is low
        int backfireChance = 30 - gearBonus * 3 + (energy < 30 ? 10 : 0);
        if (rng.nextInt(100) < backfireChance) {
            int spawn = Utils.randomInt(2, 5);
            String newType = Utils.randomBugType();
            bugs.put(newType, bugs.getOrDefault(newType, 0) + spawn);
            addLog("Backfire! Squash caused " + spawn + " " + newType + " bug(s) to appear!");
            // also schedule delayed event (worse return)
            scheduleDelayedEvent(currentTurn + Utils.randomInt(2, 4),
                    new BugEvent(newType, Utils.randomInt(1, 3)));
        } else {
            // success: removal amount depends on energy and gear
            int baseRemove = 1 + gearBonus;
            if (energy > 70) baseRemove += 1;
            int remove = Math.min(bugs.getOrDefault(target, 0), baseRemove + Utils.randomInt(0, 1));
            bugs.put(target, bugs.get(target) - remove);
            if (bugs.get(target) <= 0) bugs.remove(target);
            addLog("Squashed " + remove + " " + target + " bug(s). Remaining: " + totalBugs());
            // chance of a delayed smaller reappearance
            if (rng.nextInt(100) < 40) {
                int delayed = Utils.randomInt(1, 3);
                scheduleDelayedEvent(currentTurn + Utils.randomInt(2, 5),
                        new BugEvent(target, delayed));
                addLog("Warning: traces may reappear later (" + delayed + " " + target + ").");
            }
        }
        reduceEnergy(12);
    }

    public void runCheck() {
        addLog("Running checks...");
        int effect = Utils.randomInt(0, 3);
        if (effect > 0) {
            for (int i = 0; i < effect; i++) {
                String t = Utils.randomBugType();
                bugs.put(t, bugs.getOrDefault(t, 0) + 1);
                addLog("Check revealed a hidden " + t + " bug.");
            }
        } else {
            addLog("Checks found no new bugs.");
        }
        reduceEnergy(18);
    }

    public void refactor(int currentTurn) {
        addLog("Refactoring: reorganizing code to reduce future growth...");
        // set refactor for next 3 turns
        refactorTurnsRemaining = 3;
        refactorReductionPercent = 50 + gearBonus * 5; // base 50% reduction + gear
        addLog("Refactor active: reduces growth by " + refactorReductionPercent + "% for " + refactorTurnsRemaining + " turns.");
        reduceEnergy(22);
    }

    public void scan() {
        double base = totalBugs() * (0.20 + rng.nextDouble() * 0.30);
        // apply refactor reduction
        base *= (1.0 - (refactorReductionPercent / 100.0));
        // caffeine increases growth slightly
        base *= (1.0 + caffeine * 0.08);
        int estimate = Math.max(0, (int)Math.round(base));
        String level = estimate <= 2 ? "LOW" : (estimate <= 6 ? "MEDIUM" : "HIGH");
        addLog("Scan: Estimated next-turn growth: ~" + estimate + " (" + level + ")");
        reduceEnergy(6);
    }

    public void drinkCoffee() {
        caffeine = Math.min(5, caffeine + 1);
        energy = Math.min(100, energy + 25);
        addLog("Coffee: energy +25. Now " + energy + ". Caffeine: " + caffeine);
        // jitter chance
        if (rng.nextInt(100) < 20) {
            String t = Utils.randomBugType();
            bugs.put(t, bugs.getOrDefault(t, 0) + 1);
            addLog("Jitters: a small " + t + " bug appeared.");
        }
    }

    public void askCopilot() {
        if (copilotUsed >= COPILOT_MAX) {
            addLog("Copilot: limit reached.");
            copilotActive = false;
            return;
        }
        copilotUsed++;
        copilotActive = true;
        // Copilot may auto-fix or give hint
        if (rng.nextBoolean()) {
            int removed = 0;
            for (String type : new ArrayList<>(bugs.keySet())) {
                int take = Math.min(bugs.get(type), Utils.randomInt(1, 2));
                bugs.put(type, bugs.get(type) - take);
                removed += take;
                if (bugs.get(type) <= 0) bugs.remove(type);
            }
            addLog("Copilot auto-fix removed " + removed + " bugs.");
        } else {
            addLog("Copilot hint: consider refactoring or focusing 'Logic' bugs.");
            energy = Math.min(100, energy + 10);
        }
    }

    // -------------------------
    // Multiplication & delayed events
    // -------------------------
    public void bugsMultiply() {
        Map<String, Integer> snapshot = new HashMap<>(bugs);
        for (String type : snapshot.keySet()) {
            int count = snapshot.get(type);
            double baseGrowth = count * (0.20 + rng.nextDouble() * 0.30); // 20-50% growth
            if (energy < 40) baseGrowth *= 1.5;             // low energy amplifies growth
            baseGrowth *= (1.0 + caffeine * 0.08);         // caffeine effect
            baseGrowth *= (1.0 - refactorReductionPercent / 100.0); // refactor reduces

            int grow = Math.max(0, (int)Math.round(baseGrowth));
            if (grow > 0) {
                int crossType = (int)Math.round(grow * 0.3);
                int sameTypeGrow = grow - crossType;
                bugs.put(type, bugs.getOrDefault(type, 0) + sameTypeGrow);
                for (int i = 0; i < crossType; i++) {
                    String t2 = Utils.randomBugType();
                    bugs.put(t2, bugs.getOrDefault(t2, 0) + 1);
                }
                addLog(type + " multiplied: +" + sameTypeGrow + " (+" + crossType + " cross-type).");
            }
        }

        // decrement refactor duration and possibly remove effect
        if (refactorTurnsRemaining > 0) {
            refactorTurnsRemaining--;
            if (refactorTurnsRemaining == 0) {
                refactorReductionPercent = 0;
                addLog("Refactor effect has worn off.");
            }
        }
    }

    private void scheduleDelayedEvent(int triggerTurn, BugEvent be) {
        delayed.add(new DelayedEvent(triggerTurn, be));
    }

    public void processFutureBugs(int currentTurn) {
        Iterator<DelayedEvent> it = delayed.iterator();
        List<DelayedEvent> toRemove = new ArrayList<>();
        while (it.hasNext()) {
            DelayedEvent de = it.next();
            if (de.triggerTurn <= currentTurn) {
                BugEvent be = de.event;
                bugs.put(be.type, bugs.getOrDefault(be.type, 0) + be.count);
                addLog("Delayed event: " + be.count + " " + be.type + " bug(s) returned.");
                toRemove.add(de);
            }
        }
        // remove processed events
        delayed.removeAll(toRemove);
    }

    // passive drain each turn (fatigue)
    public void applyPassiveTurnEffect() {
        reduceEnergy(3);
        if (energy <= 0) {
            int penalty = Utils.randomInt(2, 5);
            for (int i = 0; i < penalty; i++) {
                String t = Utils.randomBugType();
                bugs.put(t, bugs.getOrDefault(t, 0) + 1);
            }
            addLog("Exhaustion: " + penalty + " bug(s) appeared.");
            energy = 5; // recover a tiny bit to avoid infinite loops
        }
    }

    // -------------------------
    // Helpers & getters
    // -------------------------
    private String pickTargetType() {
        // pick the bug type with the highest count (simple heuristic)
        return bugs.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .get().getKey();
    }

    public int totalBugs() {
        return bugs.values().stream().mapToInt(i -> i).sum();
    }

    public String bugSummary() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> e : bugs.entrySet()) {
            sb.append(e.getKey()).append(":").append(e.getValue()).append(" ");
        }
        return sb.toString().trim();
    }

    public void addBugs(int n) {
        // add warmup bugs to Logic by default
        bugs.put("Logic", bugs.getOrDefault("Logic", 0) + n);
        addLog(n + " bug(s) injected as warmup.");
    }

    // energy lower bound is 0
    public void reduceEnergy(int n) {
        energy = Math.max(0, energy - n);
    }

    public int getEnergy() { return energy; }
    public int getCaffeine() { return caffeine; }
    public int getGearBonus() { return gearBonus; }
    public int getCopilotUsed() { return copilotUsed; }
    public boolean isCopilotActive() { return copilotActive; }
    public void resetCopilotActive() { copilotActive = false; }
    public void setLastActionTime(int t) { lastActionTime = t; }
    public int getLastActionTime() { return lastActionTime; }

    // -------------------------
    // Internal small classes used for delayed events (Serializable)
    // -------------------------
    private static class BugEvent implements Serializable {
        private static final long serialVersionUID = 1L;
        String type;
        int count;
        BugEvent(String t, int c) { type = t; count = c; }
    }

    private static class DelayedEvent implements Serializable {
        private static final long serialVersionUID = 1L;
        int triggerTurn;
        BugEvent event;
        DelayedEvent(int t, BugEvent e) { triggerTurn = t; event = e; }
    }
}
