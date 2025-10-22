import java.util.Random;

public class Utils {
    private static Random random = new Random();

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static boolean randomChance(int percent) {
        return random.nextInt(100) < percent;
    }

    public static int randomInt(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    public static int getChoice(java.util.Scanner scanner, int min, int max) {
        int choice = 0;
        while (choice < min || choice > max) {
            System.out.print("Enter choice (" + min + "-" + max + "): ");
            try { choice = Integer.parseInt(scanner.nextLine()); }
            catch (Exception e) { choice = 0; }
        }
        return choice;
    }

    public static void flashMessage(String msg, int times) {
        for (int i = 0; i < times; i++) {
            clearScreen();
            System.out.println(msg);
            try { Thread.sleep(500); } catch (InterruptedException e) {}
            clearScreen();
            try { Thread.sleep(200); } catch (InterruptedException e) {}
        }
        System.out.println(msg);
    }

    public static void animateTyping(String msg, int delayMs) {
        for (char c : msg.toCharArray()) {
            System.out.print(c);
            try { Thread.sleep(delayMs); } catch (InterruptedException e) {}
        }
        System.out.println();
    }

    public static String dynamicBar(int value, int max, int width) {
        int filled = (int)((double)value / max * width);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < width; i++) bar.append(i < filled ? "#" : " ");
        return bar.toString();
    }
}
