import java.io.*;

public class SaveManager {
    public static void savePlayer(Player p, String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("saves/" + filename))) {
            out.writeObject(p);
            System.out.println("Game saved.");
        } catch (IOException e) { System.out.println("Failed to save."); }
    }

    public static Player loadPlayer(String filename) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("saves/" + filename))) {
            return (Player) in.readObject();
        } catch (Exception e) { System.out.println("Failed to load."); return null; }
    }
}
