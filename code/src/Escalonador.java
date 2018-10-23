import java.util.*;

public class Escalonador {
    private static String[] psw = { "PRONTO", "EXECUTANDO", "BLOQUEIADO" };

    // Redistribute credits
    private static void _verifyAndRedistributeCredits(
            List<LinkedList<Integer>> readyQueue,
            QueueOfBlocked queueOfBlocked,
            TableOfProcess table
    ) {
        boolean allZeroCredits = true;
        int maxcredits = readyQueue.size();
        List<Integer> queue;
        Bcp bcp;

        for (int i = maxcredits - 1; i > 0; i--) {
            queue = readyQueue.get(i);
            if (!queue.isEmpty()) {
                for (int index: queue) {
                    bcp = table.getBcp(index);

                    if (bcp.getCredits() > 0) {
                        allZeroCredits = false;
                    }
                }
            }
        }

        if (!queueOfBlocked.empty()) {
            for (int index : queueOfBlocked.getQueue()) {
                bcp = table.getBcp(index);
                if (bcp.getCredits() > 0) {
                    allZeroCredits = false;
                }
            }
        }

        if (allZeroCredits) {
            _redistributeCredits(readyQueue, queueOfBlocked, table);
        }
    }

    private static void _redistributeCredits(
            List<LinkedList<Integer>> readyQueue,
            QueueOfBlocked queueOfBlocked,
            TableOfProcess table
    ) {
        int maxcredits = readyQueue.size();
        List<Integer> queue;
        Bcp bcp;

        for (int i = maxcredits - 1; i > 0; i--) {
            queue = readyQueue.get(i);
            if (!queue.isEmpty()) {

                for (int reference : queue) {
                    bcp = table.getBcp(reference);
                    bcp.setCredits(bcp.getPriority());
                }
            }
        }

        if (!queueOfBlocked.empty()) {
            for (int index : queueOfBlocked.getQueue()) {
                bcp = table.getBcp(index);
                bcp.setCredits(bcp.getPriority());
            }
        }
    }

    public void moveToBlockedQueue(
            List<LinkedList<Integer>> readyQueue,
            QueueOfBlocked queueOfBlocked,
            TableOfProcess tableOfProcess,
            int reference
    ) {
        int credit = tableOfProcess.getBcp(reference).getCredits();

        // Add in blocked queue
        queueOfBlocked.addInQueue(reference);

        readyQueue.get(credit).remove();
    }

    public void removeDoneProcess(
            List<LinkedList<Integer>> readyQueue,
            TableOfProcess tableOfProcess,
            int reference,
            Memory memory
    ) {
        Bcp bcp = tableOfProcess.getBcp(reference);
        int credit = bcp.getCredits();

        // Remove process from memory, ready queue, credit queues and table of process
        readyQueue.get(credit).remove();

        // Remove from memory
        memory.remove(bcp.getProcess());

        // Remove from table of process
        tableOfProcess.removeOfTable(reference);
    }

    public void moveQueues(
            List<LinkedList<Integer>> readyQueue,
            TableOfProcess tableOfProcess,
            int reference
    ) {
        Bcp bcp = tableOfProcess.getBcp(reference);
        int credit = bcp.getCredits();

        readyQueue.get(credit).remove();

        if (credit != 0) {
            // if have more than 0 credits put the decrease credit in first of queue
            readyQueue.get(credit - 1).addFirst(reference);
            return;
        }

        // Round-Robin if have 0 credits
        readyQueue.get(credit).addLast(reference);
    }

    public void returnBlockedInReadyQueue(
            List<LinkedList<Integer>> readyQueue,
            QueueOfBlocked queueOfBlocked,
            TableOfProcess tableOfProcess
    ) {
        // Remove from blocked queue
        int reference = queueOfBlocked.removeOfQueue();

        // Get bcp with reference
        Bcp bcp = tableOfProcess.getBcp(reference);
        bcp.setState(psw[0]);

        int credit = bcp.getCredits();

        // Add in the correct queue of credits
        readyQueue.get(credit).add(reference);
    }


    public static int getNext(
            List<LinkedList<Integer>> readyQueue,
            QueueOfBlocked queueOfBlocked,
            TableOfProcess tableOfProcess,
            int maxPriority
    ) {
        LinkedList<Integer> queue;
        int credit = 0;

        // Verify if all elements in queue have credit 0
        _verifyAndRedistributeCredits(readyQueue, queueOfBlocked, tableOfProcess);

        // Get the higher credit queue
        for (int i = maxPriority; i > 0; i--) {
            queue = readyQueue.get(i);

            if (!queue.isEmpty()) {
                credit = i;
                break;
            }
        }

        return readyQueue.get(credit).getFirst();
    }
}
