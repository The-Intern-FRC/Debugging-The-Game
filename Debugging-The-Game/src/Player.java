import java.io.Serializable;
import java.util.*;

/**
 * Player with tuned parameters for Option A balance.
 */
public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int COPILOT_MAX = 6; // slightly more forgiving

    private int energy = 100;
    private int caffeine = 0;
    private int gearBonus = 1;   // base gear that helps a bit
    private int copilotUsed = 0;
    private boolean copilotActive = false;

    private int lastActionTime = 0;

    private Map<String, Integer> bugs = new LinkedHashMap<>();
    private Queue<DelayedEvent> delayed = new LinkedList<>();
    private transient Deque<String> logs = new ArrayDeque<>();

    private int refactorTurnsRemaining = 0;
    private int refactorReductionPercent = 0; // percent

    private transient Random rng = new Random();

    public Player() {
        bugs.put("Syntax", 3);
        bugs.put("Logic", 3);
        bugs.put("Runtime", 2);
        initTransient();
        addLog("Player initialized.");
    }

    private void initTransient() {
        if (logs == null) logs = new ArrayDeque<>();
        if (rng == null) rng = new Random();
    }

    // Logging
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

    // Actions
    public void squashBug(int currentTurn) {
        initTransient();
        if (totalBugs() == 0) {
            addLog("No bugs to squash.");
            reduceEnergy(6);
            return;
        }

        String target = pickTargetType();

        // backfire chance tuned (Option A)
        int backfireChance = 22 - gearBonus * 2 + (energy < 30 ? 10 : 0);
        if (rng.nextInt(100) < backfireChance) {
            int spawn = Utils.randomInt(2, 5);
            String newType = Utils.randomBugType();
            bugs.put(newType, bugs.getOrDefault(newType, 0) + spawn);
            addLog("Backfire! Squash caused " + spawn + " " + newType + " bug(s).");
            scheduleDelayedEvent(currentTurn + Utils.randomInt(2, 4),
                    new BugEvent(newType, Utils.randomInt(1, 3)));
        } else {
            int baseRemove = 1 + gearBonus;
            if (energy > 70) baseRemove += 1;
            int remove = Math.min(bugs.getOrDefault(target, 0), baseRemove + Utils.randomInt(0, 1));
            bugs.put(target, bugs.get(target) - remove);
            if (bugs.get(target) <= 0) bugs.remove(target);
            addLog("Squashed " + remove + " " + target + " bug(s). Remaining: " + totalBugs());
            if (rng.nextInt(100) < 35) {
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
        addLog("Refactoring: reorganizing code...");
        refactorTurnsRemaining = 3;
        refactorReductionPercent = 60 + gearBonus * 6; // stronger in Option A
        addLog("Refactor active: reduces growth by " + refactorReductionPercent + "% for " + refactorTurnsRemaining + " turns.");
        reduceEnergy(22);
    }

    public void scan() {
        double base = totalBugs() * (0.12 + rng.nextDouble() * 0.20); // tuned base growth
        base *= (1.0 - (refactorReductionPercent / 100.0));
        base *= (1.0 + caffeine * 0.08);
        int estimate = Math.max(0, (int)Math.round(base));
        String level = estimate <= 2 ? "LOW" : (estimate <= 6 ? "MEDIUM" : "HIGH");
        addLog("Scan: Estimated next-turn growth: ~" + estimate + " (" + level + ")");
        reduceEnergy(6);
    }

    public void drinkCoffee() {
        caffeine = Math.min(5, caffeine + 1);
        energy = Math.min(100, energy + 25);
        addLog("Coffee: energy +25 (now " + energy + "). Caffeine: " + caffeine);
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
        // Copilot effect: sometimes larger rescue early-midgame; weaker later
        if (rng.nextDouble() < 0.6) {
            int removed = 0;
            for (String type : new ArrayList<>(bugs.keySet())) {
                int take = Math.min(bugs.get(type), Utils.randomInt(1, 3));
                bugs.put(type, bugs.get(type) - take);
                removed += take;
                if (bugs.get(type) <= 0) bugs.remove(type);
            }
            addLog("Copilot auto-fix removed " + removed + " bugs.");
        } else {
            addLog("Copilot hint: consider refactor or targeting 'Logic' bugs.");
            energy = Math.min(100, energy + 10);
        }

        // mark copilot as active briefly
        copilotActive = true;
    }

    // Multiplication & delayed events
    public void bugsMultiply() {
        Map<String, Integer> snapshot = new HashMap<>(bugs);
        for (String type : snapshot.keySet()) {
            int count = snapshot.get(type);
            double baseGrowth = count * (0.12 + rng.nextDouble() * 0.20); // tuned smaller base
            if (energy < 40) baseGrowth *= 1.5;
            baseGrowth *= (1.0 + caffeine * 0.08);
            baseGrowth *= (1.0 - refactorReductionPercent / 100.0);

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
        delayed.removeAll(toRemove);
    }

    // Passive drain each turn
    public void applyPassiveTurnEffect() {
        reduceEnergy(2); // slightly less harsh than before
        if (energy <= 0) {
            int penalty = Utils.randomInt(2, 4);
            for (int i = 0; i < penalty; i++) {
                String t = Utils.randomBugType();
                bugs.put(t, bugs.getOrDefault(t, 0) + 1);
            }
            addLog("Exhaustion: " + penalty + " bug(s) appeared.");
            energy = 5;
        }
    }

    // helpers & getters
    private String pickTargetType() {
        return bugs.entrySet().stream().max(Comparator.comparingInt(Map.Entry::getValue))
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

    public void addLog(String s) { initTransient(); if (logs.size() >= 6) logs.removeFirst(); logs.addLast(s); }
    public void addBugs(int n) { bugs.put("Logic", bugs.getOrDefault("Logic", 0) + n); addLog(n + " bug(s) injected (warmup)."); }
    public void reduceEnergy(int n) { energy = Math.max(0, energy - n); }
    public int getEnergy() { return energy; }
    public int getCaffeine() { return caffeine; }
    public int getGearBonus() { return gearBonus; }
    public int getCopilotUsed() { return copilotUsed; }
    public boolean isCopilotActive() { return copilotActive; }
    public void resetCopilotActive() { copilotActive = false; }
    public void setLastActionTime(int t) { lastActionTime = t; }
    public int getLastActionTime() { return lastActionTime; }

    // Internal classes for delayed events
    private static class BugEvent implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        String type;
        int count;
        BugEvent(String t, int c) { type = t; count = c; }
    }

    private static class DelayedEvent implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        int triggerTurn;
        BugEvent event;
        DelayedEvent(int t, BugEvent e) { triggerTurn = t; event = e; }
    }
}
