import java.util.*;

public class Escalonador {
    private static Clock clock = new Clock();
    private static Dispatcher dispatcher = new Dispatcher();

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

        for (int i = maxcredits - 1; i >= 0; i--) {
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

        for (int i = maxcredits - 1; i >= 0; i--) {
            queue = readyQueue.get(i);
            if (!queue.isEmpty()) {

                for (int reference : queue) {
                    bcp = table.getBcp(reference);
                    bcp.setCredits(bcp.getPriority());

                    // Reinsert in queue
                    readyQueue.get(bcp.getCredits()).add(reference);
                }
            }
        }

        readyQueue.get(0).clear();

        if (!queueOfBlocked.empty()) {
            for (int index : queueOfBlocked.getQueue()) {
                bcp = table.getBcp(index);
                bcp.setCredits(bcp.getPriority());
            }
        }
    }

    public static void moveToBlockedQueue(
            List<LinkedList<Integer>> readyQueue,
            QueueOfBlocked queueOfBlocked,
            TableOfProcess tableOfProcess,
            int reference
    ) {
        Bcp bcp = tableOfProcess.getBcp(reference);
        int credit = bcp.getCredits();

        // Add in blocked queue
        queueOfBlocked.addInQueue(reference);

        readyQueue.get(credit).remove();

        if (credit != 0) {
            dispatcher.decreaseCredit(bcp);
        }
    }

    public static void removeDoneProcess(
            List<LinkedList<Integer>> readyQueue,
            TableOfProcess tableOfProcess,
            int reference
    ) {
        Bcp bcp = tableOfProcess.getBcp(reference);
        int credit = bcp.getCredits();

        // Remove process from memory, ready queue, credit queues and table of process
        readyQueue.get(credit).remove();

        // Remove from table of process
        tableOfProcess.removeOfTable(reference);
    }

    public static void moveQueues(
            List<LinkedList<Integer>> readyQueue,
            TableOfProcess tableOfProcess,
            int reference
    ) {
        Bcp bcp = tableOfProcess.getBcp(reference);
        int credit = bcp.getCredits();

        readyQueue.get(credit).remove();

        if (credit != 0) {
            dispatcher.decreaseCredit(bcp);
            credit = bcp.getCredits();

            // if have more than 0 credits put the decrease credit in first of queue
            readyQueue.get(credit).addFirst(reference);
            return;
        }

        // Round-Robin if have 0 credits
        readyQueue.get(credit).addLast(reference);
    }

    public static void returnBlockedInReadyQueue(
            List<LinkedList<Integer>> readyQueue,
            QueueOfBlocked queueOfBlocked,
            TableOfProcess tableOfProcess
    ) {
        // Remove from blocked queue
        int reference = queueOfBlocked.removeOfQueue();

        // Get bcp with reference
        Bcp bcp = tableOfProcess.getBcp(reference);
        dispatcher.returnToDone(bcp);

        int credit = bcp.getCredits();

        // Add in the correct queue of credits
        readyQueue.get(credit).addLast(reference);
    }

    public static Integer verifyBlockedQueue(
            List<LinkedList<Integer>> readyQueue,
            TableOfProcess tableOfProcess,
            QueueOfBlocked queueOfBlocked
    ) {
        Integer reference = null;
        if (!queueOfBlocked.empty()) {
            queueOfBlocked.showQueue(tableOfProcess);
            int index = queueOfBlocked.peek();
            Bcp bcp = tableOfProcess.getBcp(index);
            int time = bcp.getWaittingTime();

            // Remove of blocked queue and insert in credit queue and ready queue if 2 quantum have passed
            if (clock.returnToQueue(time)) {
                returnBlockedInReadyQueue(readyQueue, queueOfBlocked, tableOfProcess);
                reference = readyQueue.get(bcp.getCredits()).getFirst();
            }
        }
        return reference;
    }


    public static Integer getNext(
            List<LinkedList<Integer>> readyQueue,
            QueueOfBlocked queueOfBlocked,
            TableOfProcess tableOfProcess,
            int maxPriority,
            int reference,
            String response
    ) {
        LinkedList<Integer> queue;
        int credit = 0;

        verifyBlockedQueue(readyQueue, tableOfProcess, queueOfBlocked);

        switch (response) {
            case "E/S":
                moveToBlockedQueue(readyQueue, queueOfBlocked, tableOfProcess, reference);
                break;

            case "SAIDA":
                removeDoneProcess(readyQueue, tableOfProcess, reference);
                break;

            default:
                // Move queues
                moveQueues(readyQueue, tableOfProcess, reference);
                break;
        }

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

        Integer address = null;

        if (!readyQueue.get(credit).isEmpty()) {
            address = readyQueue.get(credit).getFirst();
        }

        return address;
    }
}
