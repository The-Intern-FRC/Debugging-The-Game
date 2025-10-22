import java.util.Random;
import java.util.Scanner;

public class Utils {
    private static Random random = new Random();

    public static boolean randomChance(int percent) {
        return random.nextInt(100) < percent;
    }

    public static int randomInt(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    public static String randomBugType() {
        String[] types = {"Syntax", "Logic", "Runtime"};
        return types[random.nextInt(types.length)];
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void animateTyping(String text, int delayMs) {
        for (char c : text.toCharArray()) {
            System.out.print(c);
            try { Thread.sleep(delayMs); } catch(Exception e){}
        }
        System.out.println();
    }

    public static void flashMessage(String text, int times) {
        for (int i = 0; i < times; i++) {
            System.out.println(text);
            try { Thread.sleep(150); } catch(Exception e){}
            clearScreen();
        }
        System.out.println(text);
    }

    public static String dynamicBar(int current, int max, int length) {
        int filled = (int)((double)current / max * length);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < length; i++) {
            bar.append(i < filled ? "#" : "-");
        }
        return bar.toString();
    }

    public static int getChoice(Scanner scanner, int min, int max) {
        int choice = -1;
        while (true) {
            try {
                System.out.print("Choose an action (" + min + "-" + max + "): ");
                choice = Integer.parseInt(scanner.nextLine());
                if (choice >= min && choice <= max) break;
                else System.out.println("Invalid. Try again.");
            } catch(Exception e) { System.out.println("Invalid input."); }
        }
        return choice;
    }
}
