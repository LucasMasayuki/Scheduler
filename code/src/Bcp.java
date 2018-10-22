public class Bcp {
    private int pc;
    private String psw;
    private int priority;
    private int x = 0;
    private int y = 0;
    private int credits = 0;
    private Process process;
    private String nameOfProcess;
    private int times;
    private int waittingTime = 0;

    public Bcp(int pc, String psw, int priority, Process process, String nameOfProcess, int quantum) {
        this.psw = psw;
        this.pc = pc;
        this.priority = priority;
        this.credits = priority;
        this.process = process;
        this.nameOfProcess = nameOfProcess;
        this.times = quantum;
    }

    public String getNameOfProcess() {
        return nameOfProcess;
    }

    public Process getProcess() {
        return this.process;
    }

    public int getPc() {
        return this.pc;
    }

    public void setPc(int pc) {
        this.pc = pc;
    }

    public void setState(String psw) {
        this.psw = psw;
    }

    public int getPriority() {
        return this.priority;
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

    public void updateTimes() {
        this.times *= 2;
    }

    public int getTimes() {
        return this.times;
    }

    public int getWaittingTime() {
        return this.waittingTime;
    }

    public void decreaseWaittingTime() {
        this.waittingTime--;
    }

    public void setWaittingTime() {
        this.waittingTime = 2;
    }

    public boolean overWaittingTime() {
        return this.waittingTime == 0;
    }
}
