import java.util.Comparator;
import java.util.PriorityQueue;

public class ReadyQueue {
    private Comparator<Bcp> comparator = new PriorityComparator();
    private PriorityQueue<Bcp> queue = new PriorityQueue<Bcp>(10, comparator);

    public Bcp removeOfQueue() {
        return queue.remove();
    }

    public void addInQueue(Bcp bcp) {
        queue.add(bcp);
    }
}
