import java.util.*;

public class ReadyQueue {
    private List<Bcp> queue = new ArrayList<>();

    public void addInQueue(Bcp bcp) {
        queue.add(bcp);
    }

    public void removeOfQueue(Bcp bcp) {
        queue.remove(bcp);
    }

    public void showQueue() {
        System.out.println("Fila de prontos");
        for (Bcp element : queue) {
            System.out.println(element.getProcess().process.get(0));
        }
    }

    public void orderByPriority() {
        if (queue.size() > 0) {
            Comparator<Bcp> comparator = new PriorityComparator();
            Collections.sort(queue, comparator);
        }
    }

    public List<Bcp> getQueue() {
        return this.queue;
    }
}
