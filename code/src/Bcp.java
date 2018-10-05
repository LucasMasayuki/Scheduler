public class Bcp {
    private int pc;
    private String state;
    private int priority;
    private String x;
    private String y;
    private int credits;
    private Process process;

    public Bcp(int pc, String state, int priority, String x, String y, int credits, Process process) {
        this.state = state;
        this.pc = pc;
        this.priority = priority;
        this.x = x;
        this.y = y;
        this.credits = credits;
        this.process = process;
    }

    public Bcp(int pc, String state, int priority, int credits, Process process) {
        this.state = state;
        this.pc = pc;
        this.priority = priority;
        this.credits = credits;
        this.process = process;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public int getPc() {
        return pc;
    }

    public void setPc(int pc) {
        this.pc = pc;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }
}
