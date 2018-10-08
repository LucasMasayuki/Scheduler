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
}
