public class Cpu {
    private int pc = 0;
    private int x = 0;
    private int y = 0;

    public int getPc() {
        return pc;
    }

    public void setPc(int pc) {
        this.pc = pc;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void process(Bcp bcp, String line) {
        // Discover if is assignment and which register is receiving the value
        boolean isXRegister = line.charAt(0) == 'X';
        boolean isYRegister = line.charAt(0) == 'Y';
        boolean passFromEqual = line.charAt(1) == '=';

        if (passFromEqual && isXRegister) {
            x = Integer.parseInt(line.substring(2, line.length()));
        }

        if (passFromEqual && isYRegister) {
            y = Integer.parseInt(line.substring(2, line.length()));
        }
    }

    // Update pc of cpu
    public void useCpu() {
        this.pc++;
    }

    public void freeCpu() {
        pc = 0;
        x = 0;
        y = 0;
    }
}
