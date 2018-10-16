import java.util.List;

public class Dispatcher {
    // Get process from memory and recover pc, x and y of current process
    public List<String> recoverContext(Memory memory, Bcp bcp, Cpu cpu) {
        int reference = bcp.getProcess().referenceOfMemory;
        cpu.setPc(bcp.getPc());
        cpu.setX(bcp.getX());
        cpu.setY(bcp.getY());

        return memory.get(reference);
    }

    public void updateBcp(Bcp bcp, String psw, Cpu cpu) {
        if (!psw.isEmpty()) {
            bcp.setState(psw);
        }

        int credit = bcp.getCredits();
        if (credit > 0) {
            bcp.setCredits(credit - 1);
        }

        bcp.setX(cpu.getX());
        bcp.setPc(cpu.getPc());
        bcp.setY(cpu.getY());
    }
}
