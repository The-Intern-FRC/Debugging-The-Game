import java.io.*;

public class SaveManager {
    private static final String SAVE_PATH = "saves/save.dat";

    public static void save(Player player) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_PATH));
            out.writeObject(player);
            out.close();
        } catch(Exception e) {
            System.out.println("Failed to save game: " + e.getMessage());
        }
    }

    public static Player load() {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(SAVE_PATH));
            Player player = (Player) in.readObject();
            in.close();
            return player;
        } catch(Exception e) {
            System.out.println("No save found. Starting new game.");
            return new Player();
        }
    }
}
