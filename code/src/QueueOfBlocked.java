import java.util.LinkedList;
import java.util.Queue;

public class QueueOfBlocked {
    private Queue<Process> queue = new LinkedList<Process>();

    public Process removeOfQueue() {
        return queue.remove();
    }

    public void addInQueue(Process process) {
        queue.add(process);
    }
}
