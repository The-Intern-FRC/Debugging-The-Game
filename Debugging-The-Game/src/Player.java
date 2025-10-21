import java.util.*;

public class Player {
    private String name;
    private int level=1;
    private int xp=0;
    private int credits=50;
    private int energy=100;
    private int maxEnergy=100;
    private List<Gear> gearList = new ArrayList<>();
    private List<String> badges = new ArrayList<>();

    public Player(String name){this.name=name;}

    public String getName(){return name;}
    public int getLevel(){return level;}
    public int getXp(){return xp;}
    public int getCredits(){return credits;}
    public int getEnergy(){return energy;}
    public int getMaxEnergy(){return maxEnergy;}

    public void gainXp(int amount){xp+=amount; tryLevelUp();}
    public void addCredits(int c){credits+=c;}
    public void addEnergy(int e){energy=Math.min(maxEnergy,energy+e);}
    public boolean consumeEnergy(int e){if(energy>=e){energy-=e; return true;} return false;}

    public void tryLevelUp(){
        int threshold = level*50;
        if(xp>=threshold){
            xp-=threshold; level++; maxEnergy+=10; energy=maxEnergy;
            badges.add("Level "+level+" Badge");
            System.out.println("🎉 Level Up! You are now level "+level);
        }
    }

    public double getSquashBonus(){return gearList.stream().mapToDouble(Gear::getSquashBonus).sum();}
    public double getRefactorBonus(){return gearList.stream().mapToDouble(Gear::getRefactorBonus).sum();}
    public double getCheckEfficiency(){return gearList.stream().mapToDouble(Gear::getCheckEfficiency).sum();}
    public double getMultiplicativeReduction(){return gearList.stream().mapToDouble(Gear::getBugReduction).sum();}

    public void recoverEnergyOnTick(){addEnergy(2);}

    public String listGearNames(){
        if(gearList.isEmpty()) return "None";
        StringBuilder sb = new StringBuilder();
        for(Gear g:gearList) sb.append(g.getName()).append(" ");
        return sb.toString().trim();
    }

    public String listBadges(){
        if(badges.isEmpty()) return "None";
        return String.join(", ", badges);
    }

    public String getTitle(){
        if(level>=10) return "Code Overlord";
        if(level>=5) return "Debugging Guru";
        return "Junior Coder";
    }

    public void addGear(Gear g){gearList.add(g);}
}
