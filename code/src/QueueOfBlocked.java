import java.util.LinkedList;

public class QueueOfBlocked {
    private LinkedList<Integer> queue = new LinkedList<>();

    public Integer removeOfQueue() {
        return queue.removeFirst();
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
            System.out.println(nameOfProcess);
        }
    }

}
