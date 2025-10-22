public class Gear {
    private int bonus = 1;
    private boolean refactorActive = false;

    public int getBonus() { return bonus + (refactorActive ? 2 : 0); }
    public void addRefactorBonus() { refactorActive = true; }
}
