public class Gear {
    private String name;
    private double squashBonus, refactorBonus, checkEfficiency, bugReduction;

    public Gear(String name,double squash,double refactor,double check,double reduce){
        this.name=name; squashBonus=squash; refactorBonus=refactor; checkEfficiency=check; bugReduction=reduce;
    }

    public String getName(){return name;}
    public double getSquashBonus(){return squashBonus;}
    public double getRefactorBonus(){return refactorBonus;}
    public double getCheckEfficiency(){return checkEfficiency;}
    public double getBugReduction(){return bugReduction;}
}
