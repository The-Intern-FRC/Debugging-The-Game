public class Bug {
    private String type;
    private int severity;

    public Bug(String type, int severity) {
        this.type = type;
        this.severity = severity;
    }

    public String getType() { return type; }
    public int getSeverity() { return severity; }
    public void increaseSeverity(int n) { severity += n; }
}
