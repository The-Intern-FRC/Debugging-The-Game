public class Gear {
    private String name;
    private int bonus; // reduces chance of bug multiplication

    public Gear(String name, int bonus) {
        this.name = name;
        this.bonus = bonus;
    }

    public String getName() { return name; }
    public int getBonus() { return bonus; }
}

