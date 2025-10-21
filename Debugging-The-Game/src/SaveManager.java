import java.io.*;
import java.util.*;

public class SaveManager {
    private String folder;

    public SaveManager(String folder){this.folder=folder;}

    public Player loadOrCreatePlayer(String name){
        File f = new File(folder+name+".sav");
        if(f.exists()){
            try(ObjectInputStream ois=new ObjectInputStream(new FileInputStream(f))){
                return (Player) ois.readObject();
            } catch(Exception e){System.out.println("Load failed, creating new player.");}
        }
        return new Player(name);
    }

    public void savePlayer(Player p){
        try{
            new File(folder).mkdirs();
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(folder+p.getName()+".sav"));
            oos.writeObject(p);
            oos.close();
        } catch(Exception e){System.out.println("Save failed.");}
    }
}
