import java.util.Random;
import java.util.Scanner;

public class Utils {
    private static Random rng = new Random();

    public static boolean randomChance(int percent) {
        return rng.nextInt(100) < percent;
    }

    public static int randomInt(int min, int max) {
        if (max < min) return min;
        return rng.nextInt(max - min + 1) + min;
    }

    public static String randomBugType() {
        String[] types = {"Syntax", "Logic", "Runtime"};
        return types[rng.nextInt(types.length)];
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void animateTyping(String text, int delayMs) {
        for (char c : text.toCharArray()) {
            System.out.print(c);
            try { Thread.sleep(delayMs); } catch (InterruptedException e) {}
        }
        System.out.println();
    }

    public static void flashMessage(String text, int times) {
        for (int i = 0; i < times; i++) {
            clearScreen();
            System.out.println(text);
            try { Thread.sleep(350); } catch (InterruptedException e) {}
        }
        System.out.println(text);
    }

    public static String dynamicBar(int current, int max, int width) {
        int filled = (int)Math.round(((double)current / (double)max) * width);
        filled = Math.max(0, Math.min(width, filled));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < width; i++) sb.append(i < filled ? "#" : " ");
        return sb.toString();
    }

    public static int getChoice(Scanner scanner, int min, int max) {
        int choice = -1;
        while (true) {
            try {
                System.out.print("Choose (" + min + "-" + max + "): ");
                choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice >= min && choice <= max) return choice;
            } catch (Exception e) {}
            System.out.println("Invalid input. Try again.");
        }
    }
}
