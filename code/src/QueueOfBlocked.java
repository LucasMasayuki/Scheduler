import java.util.LinkedList;
import java.util.Queue;

public class QueueOfBlocked {
    private Queue<Bcp> queue = new LinkedList<>();

    public Bcp removeOfQueue() {
        return queue.remove();
    }

    public void addInQueue(Bcp bcp) {
        queue.add(bcp);
    }

    public boolean empty() {
        return queue.isEmpty();
    }

    public Queue<Bcp> getQueue() {
        return queue;
    }

    public void showQueue() {
        System.out.println("Fila de Bloqueiados");
        for (Bcp element : queue) {
            String nameOfProcess = element.getNameOfProcess();
            System.out.println(nameOfProcess);
        }
    }

    public boolean checkIfFirstOfQueueOverWaittingTime() {
        Bcp bcp = queue.peek();
        if (bcp.overWaittingTime()) {
            return true;
        }
        return false;
    }
}
