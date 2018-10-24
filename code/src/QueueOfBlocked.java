import java.util.LinkedList;

public class QueueOfBlocked {
    private LinkedList<Integer> queue = new LinkedList<>();

    public Integer removeOfQueue() {
        return queue.remove();
    }

    public void addInQueue(int index) {
        queue.addLast(index);
    }

    public boolean empty() {
        return queue.isEmpty();
    }

    public LinkedList<Integer> getQueue() {
        return queue;
    }

    public void showQueue(TableOfProcess table) {
        System.out.println("Fila de Bloqueiados");
        for (int index : queue) {
            Bcp bcp = table.getBcp(index);
            String nameOfProcess = bcp.getNameOfProcess();
            System.out.println(nameOfProcess
                    + " || creditos "
                    + bcp.getCredits()
                    + " || wait " + bcp.getWaittingTime()
            + " || currentPc " + bcp.getPc()
            + " || reference " + index);
        }
        System.out.println();
    }

    public Integer peek() {
        return queue.peek();
    }
}
