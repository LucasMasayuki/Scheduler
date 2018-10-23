import java.util.List;

public class Dispatcher {
    // Get process from memory and recover pc, x and y of current process
    public List<String> recoverContext(Memory memory, Bcp bcp, Processor processor) {

        // Recover context from bcp
        processor.setPc(bcp.getPc());
        processor.setX(bcp.getX());
        processor.setY(bcp.getY());

        return memory.get(bcp.getProcess().referenceOfMemory);
    }

    public void updateBcp(Bcp bcp, String psw, Processor processor, String type) {
        if (!psw.isEmpty()) {
            bcp.setState(psw);
        }

        int credit = bcp.getCredits();

        bcp.setX(processor.getX());
        bcp.setPc(processor.getPc());
        bcp.setY(processor.getY());

        switch (type) {
            case "E/S":
                // Doubles the quantum for the next time the process runs
                bcp.updateTimes();

                // Starts timer blocked in two
                bcp.setWaittingTime();

                if (credit > 0) {
                    bcp.setCredits(credit - 1);
                }

                break;

            case "OVERQUANTUM":
                // Doubles the quantum for the next time the process runs
                bcp.updateTimes();

                if (credit > 0) {
                    bcp.setCredits(credit - 1);
                }

                break;
        }
    }

    public void updateWaittingTimeBcp(Bcp bcp) {
        bcp.decreaseWaittingTime();
    }
}
