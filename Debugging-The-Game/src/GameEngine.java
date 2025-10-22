import java.util.Scanner;

public class GameEngine {
    private Player player;
    private Leaderboard leaderboard;
    private Scanner scanner = new Scanner(System.in);
    private int totalTime = 240; // seconds
    private int turn = 0;

    public GameEngine() {
        player = new Player();
        leaderboard = new Leaderboard();
    }

    public void startGame() {
        projectManagerIntro();
        codeScreenIntro();

        player.addLog("Project loaded: Orientation Game (self-debugging exercise).");

        while (totalTime > 0 && player.totalBugs() > 0) {
            turn++;
            Utils.clearScreen();
            player.processFutureBugs(turn);
            displayCodeScreen();
            displayActionsAndCosts();
            int choice = Utils.getChoice(scanner, 1, 6);
            confirmAndExecute(choice);

            // apply time and passive effects
            totalTime -= player.getLastActionTime();
            player.applyPassiveTurnEffect();
            player.bugsMultiply();

            // Midgame: automatic Copilot intervention once as safety net if dire
            if (!player.isCopilotActive()
                    && player.getEnergy() < 30
                    && player.totalBugs() >= 12
                    && player.getCopilotUsed() < Player.COPILOT_MAX) {
                player.addLog("Auto-Copilot: Critical state detected — Copilot auto-intervening.");
                player.askCopilot();
            }

            // occasional compiler error
            if (Utils.randomChance(8)) {
                handleCompilerError();
            }
        }

        endGame();
    }

    private void projectManagerIntro() {
        Utils.clearScreen();
        Utils.animateTyping("Project Manager: Welcome. Your first project is simple: debug the Orientation Game — yes, this game.", 28);
        Utils.animateTyping("Project Manager: I'm going to walk you through your options. Learn them. Use them wisely.", 28);
        Utils.animateTyping("", 10);

        Utils.animateTyping("Walkthrough (Action : effect — time | energy) [Risk -> Reward]:", 20);
        Utils.animateTyping("1) Squash Bug : Remove 1-3 bugs (depends on type & gear) — 12s | 12 energy [HIGH RISK -> HIGH REWARD]", 6);
        Utils.animateTyping("    Risk: chance of backfire and delayed returns. Use when you can afford the blowback.", 6);
        Utils.animateTyping("2) Run Check : Reveal hidden bugs (0-3) — 18s | 18 energy [INFO -> PLANNING]", 6);
        Utils.animateTyping("    Benefit: exposes hidden issues so you can plan; may increase visible bug count temporarily.", 6);
        Utils.animateTyping("3) Refactor : Reduce bug growth significantly for next 3 turns — 24s | 22 energy [DEFENSIVE -> LONG-TERM]", 6);
        Utils.animateTyping("4) Scan : Estimate next-turn growth (LOW/MED/HIGH) — 6s | 6 energy [LOW RISK -> PLANNING]", 6);
        Utils.animateTyping("5) Drink Coffee : +25 energy instantly, but increases growth chance next turn — 6s | 0 energy [SHORT TERM FIX -> RISK]", 6);
        Utils.animateTyping("6) Ask GitHub Copilot : Hint or auto-fix. Limited lifeline (6 uses). — 6s | 0 energy [LIFELINE]", 6);
        Utils.animateTyping("", 20);

        Utils.animateTyping("Press Enter to dive into the terminal. Early wins can cause late pain. Play smart.", 22);
        scanner.nextLine();
    }

    private void codeScreenIntro() {
        Utils.clearScreen();
        Utils.animateTyping("Loading Debugging Terminal...", 40);
        Utils.flashMessage(
                "┌────────────────────────────────────────────┐\n" +
                "│            DEBUGGING TERMINAL              │\n" +
                "│   // WARNING: This environment is hostile  │\n" +
                "└────────────────────────────────────────────┘", 2);
    }

    private void displayCodeScreen() {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.printf("║ TIME: %4ds    TURN: %3d    BUGS: %3d                   ║%n",
                totalTime, turn, player.totalBugs());
        System.out.printf("║ ENERGY: [%s] %3d/100    CAFFEINE: %d    GEAR: +%d    ║%n",
                Utils.dynamicBar(player.getEnergy(), 100, 22), player.getEnergy(), player.getCaffeine(), player.getGearBonus());
        System.out.printf("║ COPILOT: %d/%d (active:%s)                                 ║%n",
                player.getCopilotUsed(), Player.COPILOT_MAX, player.isCopilotActive() ? "Y" : "N");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");

        System.out.println("Bug breakdown (type:count): " + player.bugSummary());
        System.out.println("Recent Logs:");
        player.displayLogs();
        System.out.println("----------------------------------------------------------");
    }

    private void displayActionsAndCosts() {
        System.out.println("\nActions (time | energy):");
        System.out.println("1) Squash Bug         — 12s | 12 energy    [HIGH RISK / HIGH REWARD]");
        System.out.println("2) Run Check          — 18s | 18 energy    [REVEAL / PLAN]");
        System.out.println("3) Refactor           — 24s | 22 energy    [DEFENSIVE / LONG-TERM]");
        System.out.println("4) Scan               —  6s | 6 energy     [LOW RISK / INFO]");
        System.out.println("5) Drink Coffee       —  6s | 0 energy     [ENERGY BOOST / INCREASE GROWTH]");
        System.out.println("6) Ask GitHub Copilot —  6s | 0 energy     [CRITICAL HELP, " + Player.COPILOT_MAX + " USES]");
    }

    private void confirmAndExecute(int choice) {
        String desc;
        int actionTime;
        switch (choice) {
            case 1: desc = "Squash bugs (quick removal, delayed risk)"; actionTime = 12; break;
            case 2: desc = "Run code checks (reveal hidden issues)"; actionTime = 18; break;
            case 3: desc = "Refactor (reduce growth next 3 turns)"; actionTime = 24; break;
            case 4: desc = "Scan (estimate next-turn growth)"; actionTime = 6; break;
            case 5: desc = "Drink coffee (+25 energy, increases next-turn growth)"; actionTime = 6; break;
            case 6: desc = "Ask GitHub Copilot (hint or small auto-fix)"; actionTime = 6; break;
            default: desc = ""; actionTime = 0;
        }

        Utils.animateTyping("[Action Preview] " + desc, 28);
        System.out.print("Confirm action? (Y/N): ");
        String c = scanner.nextLine().trim().toUpperCase();
        if (!"Y".equals(c)) {
            player.addLog("Action cancelled by player.");
            player.setLastActionTime(0);
            return;
        }

        player.setLastActionTime(actionTime);
        switch (choice) {
            case 1: player.squashBug(turn); break;
            case 2: player.runCheck(); break;
            case 3: player.refactor(turn); break;
            case 4: player.scan(); break;
            case 5: player.drinkCoffee(); break;
            case 6: player.askCopilot(); break;
        }
    }

    private void handleCompilerError() {
        Utils.animateTyping("\n[Compiler] ERROR: mysterious failure (not your code).", 30);
        System.out.print("Retry (Y) or Ignore (N)? ");
        String s = scanner.nextLine().trim().toUpperCase();
        if ("Y".equals(s)) {
            player.addLog("[Compiler] Retry executed. Time and energy spent.");
            player.setLastActionTime(player.getLastActionTime() + 10);
            player.reduceEnergy(10);
            totalTime -= 10;
        } else {
            player.addLog("[Compiler] Ignored. Hidden issues may remain.");
            totalTime -= 5;
        }
    }

    private void endGame() {
        Utils.clearScreen();
        if (player.totalBugs() <= 0) {
            Utils.flashMessage("=== VICTORY: All bugs eliminated. You won. ===", 2);
            Utils.animateTyping("Project Manager: Wait... you actually did it. I... cannot compute.", 28);
            Utils.animateTyping("Project Manager: This was supposed to be an orientation exercise. That was not part of the plan.", 28);
            Utils.animateTyping("\nSYSTEM: Launching secondary terminal... (press Enter)", 28);
            scanner.nextLine();
            Utils.clearScreen();
            // Secondary ominous terminal
            Utils.flashMessage(
                "████████████████████████████████████████████████████████\n" +
                "██  SECONDARY DEBUG ENVIRONMENT INITIALIZED           ██\n" +
                "██  WARNING: Unknown dependencies detected             ██\n" +
                "██  SUGGESTION: Proceed with caution.                 ██\n" +
                "████████████████████████████████████████████████████████", 2);
            Utils.animateTyping("TO BE CONTINUED...", 120);
        } else if (totalTime <= 0) {
            Utils.flashMessage("=== TIME UP: Project shipped buggy. ===", 2);
            Utils.animateTyping("Project Manager: We shipped. It compiles. People will complain. It's fine.", 30);
        } else {
            Utils.flashMessage("=== SESSION ENDED ===", 1);
        }

        System.out.println("\nFinal status:");
        displayCodeScreen();
        leaderboard.update(player.totalBugs(), totalTime);
        leaderboard.display();
    }
}
