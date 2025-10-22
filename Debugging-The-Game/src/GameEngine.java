import java.util.Scanner;

public class GameEngine {
    private Player player;
    private Leaderboard leaderboard;
    private Scanner scanner = new Scanner(System.in);
    private int totalTime = 180;

    public GameEngine() {
        player = new Player();
        leaderboard = new Leaderboard();
    }

    public void startGame() {
        npcIntro();
        codeScreenIntro();

        while (totalTime > 0 && player.totalBugs() > 0) {
            Utils.clearScreen();
            player.processFutureBugs();
            displayCodeScreen();
            displayActions();
            int choice = Utils.getChoice(scanner, 1, 6);
            confirmAndExecute(choice);

            totalTime -= player.getLastActionTime();
            player.bugsMultiply();

            // Passive energy drain
            player.reduceEnergy(2);
            if (player.getEnergy() <= 0) {
                int penalty = Utils.randomInt(2, 5);
                player.addLog("Exhaustion! " + penalty + " bugs appear while you collapse.");
                for (int i = 0; i < penalty; i++) {
                    String type = Utils.randomBugType();
                    player.addLog("A " + type + " bug spawned from fatigue.");
                }
            }

            // Midgame Copilot magical save
            if (!player.isCopilotActive() && player.getEnergy() < 30 && player.totalBugs() > 10) {
                player.addLog("GitHub Copilot magically intervenes to prevent disaster!");
                player.askCopilot();
            }

            if (Utils.randomChance(10)) handleCompilerError();
        }

        endGame();
    }

    private void npcIntro() {
        Utils.clearScreen();
        System.out.println("Project Manager: Welcome to programming! Here's your first project...");
        Utils.animateTyping("Project Manager: It's a simple start: debug the Orientation Game.", 40);
        Utils.animateTyping("Project Manager: (Yes, that means this game itself.)", 40);
        System.out.println("\nPress Enter to dive into the terminal…");
        scanner.nextLine();
    }

    private void codeScreenIntro() {
        Utils.clearScreen();
        Utils.animateTyping("Loading Debugging Terminal...", 50);
        Utils.flashMessage(
            "┌──────────────────────────────┐\n" +
            "│     DEBUGGING TERMINAL       │\n" +
            "└──────────────────────────────┘", 2
        );
    }

    private void displayCodeScreen() {
        System.out.println("\n╔════════════════════════════════╗");
        System.out.println("║ TIME: " + totalTime + "s  BUGS: " + player.totalBugs());
        System.out.println("║ ENERGY: [" + Utils.dynamicBar(player.getEnergy(), 100, 20) + "] " + player.getEnergy() + "/100");
        System.out.println("║ CAFFEINE: " + player.getCaffeine() + "  GEAR: +" + player.getGearBonus());
        System.out.println("║ COPILOT USES: " + player.getCopilotUsed() + "/5");
        System.out.println("╚════════════════════════════════╝\n");

        System.out.println("Recent Logs:");
        player.displayLogs();
        System.out.println("--------------------------------");
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
            case 2: desc = "Run checks to find hidden bugs."; actionTime = 15; break;
            case 3: desc = "Refactor code to reduce future bug growth."; actionTime = 20; break;
            case 4: desc = "Scan code to predict bug growth."; actionTime = 5; break;
            case 5: desc = "Drink coffee to restore energy."; actionTime = 5; break;
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
        player.addLog("Compiler Error! Code won't run. Costs energy/time to retry.");
        player.reduceEnergy(5);
    }

    private void endGame() {
        Utils.clearScreen();
        if (player.totalBugs() <= 0) System.out.println("Victory! But debugging never ends.");
        else if (totalTime <= 0) System.out.println("Time's up! Code still broken.");
        System.out.println("\nFinal Stats:");
        displayCodeScreen();
        leaderboard.update(player.totalBugs(), totalTime);
    }
}
