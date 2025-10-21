import java.util.*;

public class Utils {
    public static void clearScreen(){System.out.print("\033[H\033[2J"); System.out.flush();}
    public static void sleep(int ms){try{Thread.sleep(ms);}catch(Exception e){}}
    public static void waitForEnter(Scanner sc){System.out.println("Press Enter..."); sc.nextLine();}
    public static String colorYellow(String s){return "\033[33m"+s+"\033[0m";}
    public static String colorCyan(String s){return "\033[36m"+s+"\033[0m";}

    public static String energyBar(int current,int max){
        int total=20; int filled=(int)((current/(double)max)*total);
        StringBuilder sb=new StringBuilder("[");
        for(int i=0;i<filled;i++) sb.append("#");
        for(int i=filled;i<total;i++) sb.append("-");
        sb.append("]");
        return sb.toString();
    }

    public static void progressBar(int seconds){
        for(int i=0;i<=20;i++){
            System.out.print("\r[");
            for(int j=0;j<i;j++) System.out.print("#");
            for(int j=i;j<20;j++) System.out.print("-");
            System.out.print("]");
            sleep(seconds*50);
        }
        System.out.println();
    }

    public static String bugHydraVisual(int count){
        int heads=Math.min(count,10);
        StringBuilder sb=new StringBuilder();
        sb.append("Bug Hydra: ");
        for(int i=0;i<heads;i++) sb.append("🐛");
        if(count>10) sb.append("+").append(count-10);
        return sb.toString();
    }
}
