import java.util.*;
import java.time.*;

public class GameEngine {
    private final Scanner scanner = new Scanner(System.in);
    private final Random rng = new Random();

    private Player player;
    private Leaderboard leaderboard;
    private SaveManager saveManager;

    private int timeLimitSeconds = 180; // 3 minutes sprint
    private Instant startTime;
    private List<Bug> bugs = new ArrayList<>();
    private double globalMultiplyChance = 0.25;
    private int maxBugs = 100;

    public GameEngine() {
        this.leaderboard = new Leaderboard("../data/leaderboard.txt");
        this.saveManager = new SaveManager("../data/saves/");
    }

    public void start() {
        Utils.clearScreen();
        System.out.println("=== DEBUGGING-THE-GAME ===");
        System.out.print("Enter player name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) name = "anon";

        player = saveManager.loadOrCreatePlayer(name);
        System.out.println("\nWelcome back, " + player.getName() + " — level " + player.getLevel() + " " + player.getTitle());

        initBugs(6);
        startTime = Instant.now();

        gameLoop();

        int score = player.getXp() + player.getCredits() + player.getLevel() * 10;
        leaderboard.recordScore(player.getName(), score);
        leaderboard.showTopScores();
        saveManager.savePlayer(player);
        System.out.println("\nSaved player. Try not to ruin your rank.");
    }

    private void initBugs(int n) {
        bugs.clear();
        for (int i = 0; i < n; i++) bugs.add(Bug.random(rng));
    }

    private long secondsElapsed() {
        return Duration.between(startTime, Instant.now()).getSeconds();
    }

    private long timeLeft() {
        long left = timeLimitSeconds - secondsElapsed();
        return Math.max(0, left);
    }

    private void gameLoop() {
        while (true) {
            Utils.clearScreen();
            showStatus();

            if (timeLeft() <= 0) {
                System.out.println("\n⏰ Deadline reached — code shipped. Not a good look.");
                break;
            }
            if (bugs.isEmpty()) {
                System.out.println("\n🏆 All bugs exterminated! You win this sprint.");
                player.gainXp(100);
                player.addCredits(50);
                break;
            }
            if (bugs.size() >= maxBugs) {
                System.out.println("\n💥 Bugocalypse — repo consumed. Project dead.");
                break;
            }

            System.out.println("\nActions: [1] Squash [2] Run Checks [3] Refactor [4] Scan [5] Caffeinate [6] Gear/Upgrades [7] Save & Quit");
            System.out.print("> ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": actionSquash(); break;
                case "2": actionRunChecks(); break;
                case "3": actionRefactor(); break;
                case "4": actionScan(); break;
                case "5": actionCaffeinate(); break;
                case "6": manageGearAndUpgrades(); break;
                case "7":
                    saveManager.savePlayer(player);
                    System.out.println("Saved. Exiting.");
                    return;
                default:
                    System.out.println("Invalid choice.");
                    Utils.waitForEnter(scanner);
            }

            backgroundBugMultiplication();
            player.recoverEnergyOnTick();
            Utils.sleep(400);
        }
    }

    private void showStatus() {
        System.out.println(Utils.colorYellow("Time left: " + timeLeft() + "s"));
        System.out.println("Player: " + Utils.colorCyan(player.getName()) + " | Level: " + player.getLevel() + " | XP: " + player.getXp() + " | Credits: " + player.getCredits());
        System.out.println("Energy: " + Utils.energyBar(player.getEnergy(), player.getMaxEnergy()) + " | Gear: " + player.listGearNames());
        System.out.println("Bugs (" + bugs.size() + "): " + Bug.summary(bugs));
        System.out.println("Badges: " + player.listBadges());
        System.out.println("Global multiply chance: " + (int)(globalMultiplyChance*100) + "%");
        System.out.println(Utils.bugHydraVisual(bugs.size()));
    }

    private void actionSquash() {
        int cost = 10;
        if (!player.consumeEnergy(cost)) {
            System.out.println("Not enough energy. Try caffeinate.");
            Utils.waitForEnter(scanner);
            return;
        }
        Bug target = chooseBug();
        double baseSuccess = 0.6 + player.getSquashBonus();
        System.out.println("Squashing bug...");
        Utils.progressBar(3);
        if (rng.nextDouble() < baseSuccess) {
            bugs.remove(target);
            int gainedXP = 10 + target.getDifficulty() * 2;
            player.gainXp(gainedXP);
            player.addCredits(5);
            System.out.println("You squashed a " + target.getType() + " bug! +" + gainedXP + " XP, +5 credits.");
            if (rng.nextDouble() < globalMultiplyChance*(1-player.getMultiplicativeReduction())) {
                int spawn = rng.nextInt(3)+1;
                for (int i=0;i<spawn;i++) bugs.add(Bug.random(rng));
                System.out.println("But it spawned " + spawn + " new bugs!");
            }
        } else {
            System.out.println("Squash failed. Bug escaped!");
        }
        advanceClock(3);
        Utils.waitForEnter(scanner);
    }

    private Bug chooseBug() {
        System.out.println("Target pick: [1] Random [2] Most dangerous");
        String pick = scanner.nextLine().trim();
        if ("2".equals(pick)) {
            return Collections.max(bugs, Comparator.comparingInt(Bug::getDifficulty));
        } else {
            return bugs.get(rng.nextInt(bugs.size()));
        }
    }

    private void actionRunChecks() {
        int creditCost = 8;
        if (player.getCredits()<creditCost) {
            System.out.println("Need "+creditCost+" credits to run checks.");
            Utils.waitForEnter(scanner); return;
        }
        player.addCredits(-creditCost);
        System.out.println("Running CI checks...");
        Utils.progressBar(2);
        globalMultiplyChance = Math.max(0.05, globalMultiplyChance - (0.08 + player.getCheckEfficiency()));
        player.gainXp(12);
        System.out.println("Checks done. Multiply chance reduced, +12 XP.");
        advanceClock(6);
        Utils.waitForEnter(scanner);
    }

    private void actionRefactor() {
        int energyCost=25;
        if (!player.consumeEnergy(energyCost)) { System.out.println("Not enough energy."); Utils.waitForEnter(scanner); return; }
        System.out.println("Refactoring...");
        Utils.progressBar(3);
        double success = 0.45+player.getRefactorBonus();
        if (rng.nextDouble()<success) {
            int fixed = 1+rng.nextInt(6)+player.getLevel()/2;
            fixed=Math.min(fixed,bugs.size());
            for(int i=0;i<fixed;i++) bugs.remove(rng.nextInt(bugs.size()));
            player.gainXp(fixed*8); player.addCredits(10);
            System.out.println("Refactor success! Fixed "+fixed+" bugs. +"+(fixed*8)+" XP, +10 credits.");
        } else {
            int chaos=1+rng.nextInt(8);
            for(int i=0;i<chaos;i++) bugs.add(Bug.random(rng));
            System.out.println("Refactor chaos! "+chaos+" new bugs spawned.");
        }
        advanceClock(10);
        Utils.waitForEnter(scanner);
    }

    private void actionScan() {
        int energyCost=15;
        if(!player.consumeEnergy(energyCost)){System.out.println("Not enough energy."); Utils.waitForEnter(scanner); return;}
        System.out.println("Scanning...");
        Utils.progressBar(2);
        Map<Bug.Type,Integer> counts=new EnumMap<>(Bug.Type.class);
        for(Bug.Type t:Bug.Type.values()) counts.put(t,0);
        for(Bug b:bugs) counts.put(b.getType(), counts.get(b.getType())+1);
        List<Bug.Type> pool=new ArrayList<>();
        for(Bug.Type t:counts.keySet()){int c=counts.get(t)+1; for(int i=0;i<c;i++) pool.add(t);}
        Bug.Type prediction=pool.get(rng.nextInt(pool.size()));
        System.out.println("Scan predicts next multiplier likely: "+prediction);
        player.gainXp(6);
        advanceClock(4);
        Utils.waitForEnter(scanner);
    }

    private void actionCaffeinate() {
        System.out.println("[1] Small Coffee (+20 energy, 10 credits, 6s) [2] Espresso (+45 energy, 25 credits, 12s)");
        System.out.print("> "); String c=scanner.nextLine().trim();
        if("1".equals(c)){
            if(player.getCredits()<10){System.out.println("Not enough credits."); Utils.waitForEnter(scanner); return;}
            player.addCredits(-10); player.addEnergy(20); System.out.println("Small coffee consumed."); advanceClock(6);
        } else if("2".equals(c)){
            if(player.getCredits()<25){System.out.println("Not enough credits."); Utils.waitForEnter(scanner); return;}
            player.addCredits(-25); player.addEnergy(45); if(rng.nextDouble()<0.1){bugs.add(Bug.random(rng)); System.out.println("Jitters spawned a tiny bug.");}
            System.out.println("Espresso boost!"); advanceClock(12);
        } else System.out.println("Aborted.");
        Utils.waitForEnter(scanner);
    }

    private void manageGearAndUpgrades() {
        boolean back=false;
        while(!back){
            Utils.clearScreen();
            System.out.println("=== Gear & Upgrades ===");
            System.out.println("[1] Buy Gear  [2] Equip Gear  [3] Buy Upgrades  [4] Back");
            System.out.println("Credits: "+player.getCredits()+" | XP: "+player.getXp());
            System.out.print("> "); String s=scanner.nextLine().trim();
            switch(s){
                case"1": Upgrade.buyGear(scanner,player); break;
                case"2": Upgrade.equipGear(scanner,player); break;
                case"3": Upgrade.buyUpgrade(scanner,player); break;
                case"4": back=true; break;
                default: System.out.println("Invalid."); Utils.waitForEnter(scanner);
            }
        }
    }

    private void backgroundBugMultiplication(){
        List<Bug> snapshot=new ArrayList<>(bugs);
        for(Bug b:snapshot){
            double chance=globalMultiplyChance*b.getTypeFactor()*(1-player.getMultiplicativeReduction());
            if(rng.nextDouble()<chance){
                int spawn=1+rng.nextInt(2);
                for(int i=0;i<spawn;i++) bugs.add(Bug.random(rng));
            }
        }
        if(rng.nextDouble()<0.03) globalMultiplyChance=Math.min(0.6,globalMultiplyChance+0.01);
    }

    private void advanceClock(int seconds){
        Utils.sleep(200);
        startTime=startTime.minusSeconds(seconds);
        player.gainXp(seconds/2);
        player.addCredits(seconds/2);
        player.tryLevelUp();
    }
}
