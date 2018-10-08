public class Bcp {
    private int pc = 1;
    private String state;
    private int priority;
    private int x = 0;
    private int y = 0;
    private int credits;
    private Process process;

    public Bcp(int pc, String state, int priority, int x, int y, int credits, Process process) {
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
        return this.process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public int getPc() {
        return this.pc;
    }

    public void setPc(int pc) {
        this.pc = pc;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getCredits() {
        return this.credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }
}
