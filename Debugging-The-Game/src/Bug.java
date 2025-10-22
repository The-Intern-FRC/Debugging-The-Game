public class Bug {
    private int severity;
    private boolean hidden;

    public Bug(int severity, boolean hidden) {
        this.severity = severity;
        this.hidden = hidden;
    }

    public int getSeverity() { return severity; }
    public boolean isHidden() { return hidden; }
}
