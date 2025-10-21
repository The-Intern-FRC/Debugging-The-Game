import java.util.*;
import java.io.*;

public class Leaderboard {
    private String filePath;

    public Leaderboard(String path){filePath=path;}

    public void recordScore(String name,int score){
        try(FileWriter fw=new FileWriter(filePath,true); BufferedWriter bw=new BufferedWriter(fw)){
            bw.write(name+","+score+"\n");
        } catch(IOException e){System.out.println("Leaderboard error.");}
    }

    public void showTopScores(){
        Map<String,Integer> scores = new HashMap<>();
        try(Scanner sc=new Scanner(new File(filePath))){
            while(sc.hasNextLine()){
                String[] parts=sc.nextLine().split(",");
                if(parts.length<2) continue;
                String n=parts[0]; int s=Integer.parseInt(parts[1]);
                scores.put(n, Math.max(scores.getOrDefault(n,0),s));
            }
        } catch(Exception e){System.out.println("No leaderboard yet.");}
        List<Map.Entry<String,Integer>> list = new ArrayList<>(scores.entrySet());
        list.sort((a,b)->b.getValue()-a.getValue());
        System.out.println("\n=== Leaderboard ===");
        int rank=1;
        for(Map.Entry<String,Integer> e:list){
            System.out.println("#"+rank+" "+e.getKey()+" - "+e.getValue());
            if(rank++>=10) break;
        }
    }
}
