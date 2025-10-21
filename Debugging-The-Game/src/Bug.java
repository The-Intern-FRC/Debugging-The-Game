import java.util.*;

public class Bug {
    public enum Type{LOGIC, SYNTAX, RUNTIME, PERFORMANCE, SECURITY}

    private Type type;
    private int difficulty;
    private static Random rng = new Random();

    public Bug(Type type, int difficulty){this.type=type; this.difficulty=difficulty;}
    public Type getType(){return type;}
    public int getDifficulty(){return difficulty;}
    public double getTypeFactor(){
        switch(type){
            case LOGIC: return 1.0;
            case SYNTAX: return 0.8;
            case RUNTIME: return 1.1;
            case PERFORMANCE: return 1.3;
            case SECURITY: return 1.5;
        } return 1.0;
    }

    public static Bug random(Random rng){
        Type t = Type.values()[rng.nextInt(Type.values().length)];
        int diff = rng.nextInt(5)+1;
        return new Bug(t,diff);
    }

    public static String summary(List<Bug> bugs){
        Map<Type,Integer> count = new EnumMap<>(Type.class);
        for(Type t:Type.values()) count.put(t,0);
        for(Bug b:bugs) count.put(b.getType(), count.get(b.getType())+1);
        StringBuilder sb=new StringBuilder();
        for(Type t:Type.values()) sb.append(t.toString().charAt(0)).append(":").append(count.get(t)).append(" ");
        return sb.toString().trim();
    }
}
