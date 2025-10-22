import java.util.Scanner;

public class GameEngine {
    private Player player;
    private Leaderboard leaderboard;
    private Scanner scanner = new Scanner(System.in);
    private int totalTime = 180; // total game time in seconds

    public GameEngine() {
        this.player = new Player();
        this.leaderboard = new Leaderboard();
    }

    public void startGame() {
        npcIntro();
        codeScreenIntro();
        player.addBugs(5); // start with some initial bugs

        while (totalTime > 0 && player.getBugs() > 0) {
            Utils.clearScreen();
            displayCodeScreen();
            displayActions();
            int choice = Utils.getChoice(scanner, 1, 6);
            confirmAndExecute(choice);

            totalTime -= player.getLastActionTime();

            if (Utils.randomChance(5)) handleCompilerError();
            player.bugsMultiply(); // animated in Player logs
        }

        endGame();
    }

    // --- NPC Intro ---
    private void npcIntro() {
        Utils.clearScreen();
        System.out.println("Project Manager: Welcome to programming! Let's start you off easy...");
        Utils.animateTyping("Project Manager: Your first project is pretty simple: debug the Orientation Game.", 40);
        Utils.animateTyping("Project Manager: Yes… the game you are about to play. It's self-aware, like this.", 40);
        Utils.animateTyping("Project Manager: Just dip your toes in, don’t worry, what could possibly go wrong?", 40);
        System.out.println("\nPress Enter to dive into the code…");
        scanner.nextLine();
    }

    // --- Code Screen Intro ---
    private void codeScreenIntro() {
        Utils.clearScreen();
        Utils.animateTyping("Loading Debugging Terminal...", 50);
        Utils.flashMessage(
            "┌────────────────────────────────────────┐\n" +
            "│           DEBUGGING TERMINAL           │\n" +
            "└────────────────────────────────────────┘", 2
        );
    }

    // --- Display ---
    private void displayCodeScreen() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║ TIME: " + totalTime + "s" + "   BUGS: " + player.getBugs());
        System.out.println("║ ENERGY: [" + Utils.dynamicBar(player.getEnergy(), 100, 20) + "] " + player.getEnergy() + "/100");
        System.out.println("║ CAFFEINE: " + player.getCaffeine() + "   GEAR: +" + player.getGearBonus());
        System.out.println("║ COPILOT USES: " + player.getCopilotUsed() + "/5");
        System.out.println("╚════════════════════════════════════════╝\n");

        System.out.println("Recent Logs:");
        player.displayLogs();
        System.out.println("----------------------------------------");
    }

    private void displayActions() {
        System.out.println("\nActions:");
        System.out.println("1. Squash Bug");
        System.out.println("2. Run Check");
        System.out.println("3. Refactor");
        System.out.println("4. Scan");
        System.out.println("5. Drink Coffee");
        System.out.println("6. Ask GitHub Copilot");
    }

    private void confirmAndExecute(int choice) {
        String desc = "";
        int actionTime = 0;
        switch (choice) {
            case 1: desc = "Attempt to remove bugs. Some may spawn back."; actionTime = 10; break;
            case 2: desc = "Run checks to find hidden bugs. Costs energy."; actionTime = 15; break;
            case 3: desc = "Refactor code to reduce future bug growth. Costs energy."; actionTime = 20; break;
            case 4: desc = "Scan code to predict bug growth. Costs little energy."; actionTime = 5; break;
            case 5: desc = "Drink coffee to restore energy. May increase next bug growth."; actionTime = 5; break;
            case 6: desc = "Ask GitHub Copilot for advice. Limited to 5 uses."; actionTime = 5; break;
        }

        Utils.animateTyping("[Action Preview] " + desc, 40);
        System.out.print("Confirm? (Y/N): ");
        String confirm = scanner.nextLine().trim().toUpperCase();
        if (!confirm.equals("Y")) {
            player.addLog("Action cancelled.");
            return;
        }

        player.setLastActionTime(actionTime);

        switch (choice) {
            case 1: player.squashBug(); break;
            case 2: player.runCheck(); break;
            case 3: player.refactor(); break;
            case 4: player.scan(); break;
            case 5: player.drinkCoffee(); break;
            case 6: player.askCopilot(); break;
        }
    }

    private void handleCompilerError() {
        Utils.animateTyping("\n[Compiler] ERROR: mysterious failure!", 50);
        System.out.print("Retry (Y/N)? ");
        String retry = scanner.nextLine().trim().toUpperCase();
        if (retry.equals("Y")) {
            totalTime -= 10;
            player.reduceEnergy(10);
            player.addLog("[Compiler] Retry executed. Time and energy spent.");
        } else {
            totalTime -= 5;
            player.addLog("[Compiler] Ignored. Hidden bugs may remain.");
        }
    }

    private void endGame() {
        System.out.println("\n=== DEBUGGING SESSION END ===");
        if (player.getBugs() <= 0) Utils.flashMessage("All bugs removed! You survived debugging.", 2);
        else Utils.flashMessage("Time's up! Bugs remain. Debugging failed.", 2);

        System.out.println("Bugs remaining: " + player.getBugs());
        System.out.println("Time left: " + totalTime + "s");
        System.out.println("Energy remaining: " + player.getEnergy());
        System.out.println("Copilot used: " + player.getCopilotUsed() + "/5");

        leaderboard.addScore(player, totalTime);
        leaderboard.displayTopScores();
    }
}
