import java.util.*;

public class Upgrade {
    public static void buyGear(Scanner sc, Player player){
        Gear[] options={
            new Gear("Debugger Gloves",0.1,0.0,0.0,0.0),
            new Gear("Refactor Hat",0.0,0.1,0.0,0.0),
            new Gear("CI Boots",0.0,0.0,0.1,0.0),
            new Gear("Bug Shield",0.0,0.0,0.0,0.05)
        };
        System.out.println("Available Gear:");
        for(int i=0;i<options.length;i++){
            System.out.println("["+i+"] "+options[i].getName()+" | Cost: 30 credits");
        }
        System.out.print("> ");
        String choice = sc.nextLine().trim();
        try{
            int idx=Integer.parseInt(choice);
            if(idx<0||idx>=options.length){System.out.println("Invalid."); return;}
            if(player.getCredits()<30){System.out.println("Not enough credits."); return;}
            player.addCredits(-30); player.addGear(options[idx]); System.out.println("Bought "+options[idx].getName());
        } catch(Exception e){System.out.println("Invalid input.");}
        Utils.waitForEnter(sc);
    }

    public static void equipGear(Scanner sc, Player player){
        System.out.println("All owned gear is automatically active. Nothing to do.");
        Utils.waitForEnter(sc);
    }

    public static void buyUpgrade(Scanner sc, Player player){
        System.out.println("Upgrade shop under construction.");
        Utils.waitForEnter(sc);
    }
}
