public class Upgrade {
    private String name;
    private String description;
    private int bonus;

    public Upgrade(String n, String d, int b) {
        name = n; description = d; bonus = b;
    }

    public int getBonus() { return bonus; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
