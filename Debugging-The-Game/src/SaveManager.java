import java.io.*;

public class SaveManager {
    private static final String PATH = "saves/player.sav";

    public static void save(Player p) {
        try {
            new File("saves").mkdirs();
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PATH));
            oos.writeObject(p);
            oos.close();
            System.out.println("Saved.");
        } catch (Exception e) {
            System.out.println("Save failed: " + e.getMessage());
        }
    }

    public static Player load() {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(PATH));
            Player p = (Player) ois.readObject();
            ois.close();
            return p;
        } catch (Exception e) {
            System.out.println("No save found; starting new.");
            return new Player();
        }
    }
}
