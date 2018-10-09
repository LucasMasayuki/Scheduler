import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Escalonador {
    private static int quantum;
    private static int maxFiles = 10;
    private static int maxPriority;
    private static int x;
    private static int y;
    private static int pc;
    private static int changes = 0;
    private static int numberOfInstructions = 0;

    private static List<Bcp> primaryQueue = new LinkedList<>();
    private static List<Bcp> secondaryQueue = new LinkedList<>();
    private static List<Bcp> thirtyQueue = new LinkedList<>();
    private static List<Bcp> fourthQueue = new LinkedList<>();
    private static ReadyQueue readyQueue = new ReadyQueue();
    private static QueueOfBlocked queueOfBlocked = new QueueOfBlocked();
    private static TableOfProcess tableOfProcess = new TableOfProcess();
    private static List<String> lines = new ArrayList<>();
    private static Memory memory = new Memory();

    private static void _readAndSetup() {
        TxtHelper txtHelper = new TxtHelper();
        String fileName;

        fileName = "code/src/prioridades.txt";
        List<Integer> priority = txtHelper.readIntegerFiles(fileName);
        maxPriority = Collections.max(priority);

        fileName = "code/src/quantum.txt";
        List<Integer> quantumList = txtHelper.readIntegerFiles(fileName);
        quantum = quantumList.get(0);

        for (int i = 1; i < maxFiles + 1; i++) {
            String index = Integer.toString(i);

            if (i < 10) {
                index = "0" + index;
            }

            // Read the processes files
            fileName = "code/src/" + index + ".txt";
            List<String> readTxt = txtHelper.readStringFiles(fileName);

            String nameOfProcess = readTxt.get(0);

            // Remove the first line(processs name)
            readTxt.remove(0);

            // Read file and transform in process object
            Process process = new Process(readTxt);

            Bcp bcp = new Bcp(1, "PRONTO", priority.get(i - 1), process, nameOfProcess, quantum);

            // Save process in the memory
            memory.save(process);

            // Enqueue bcp in ready queue
            readyQueue.addInQueue(bcp);

            // Insert in process Table
            tableOfProcess.insert(bcp);
        }
        readyQueue.orderByPriority();
        _loadProcesses();
    }

    private static void _loadProcesses() {
        for (Bcp element : readyQueue.getQueue()) {
            // Write load file in logfile
            lines.add("Carregando " + element.getNameOfProcess());
        }
    }

    private static void _writeLogFile() {
        // Calculate the stats
        int averageChanges = _calculateAverage("changes");
        int averageInstructions = _calculateAverage("instructions");

        lines.add("TROCAS: "+ changes);
        lines.add("INSTRUCOES: "+ numberOfInstructions);
        lines.add("MEDIA DE TROCAS: "+ averageChanges);
        lines.add("MEDIA DE INSTRUCOES: "+ averageInstructions);
        lines.add("QUANTUM: "+ quantum);

        Path file = Paths.get("log0" + quantum + ".txt");

        try {
            Files.write(file, lines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int _calculateAverage(String what) {
        if (what.equals("changes")) {
            return changes/maxFiles;
        }
        return numberOfInstructions/maxFiles;
    }

    private static void _initializeQueuesOfCredits() {
        List<Bcp> bcpQueue = readyQueue.getQueue();
        for (Bcp element : bcpQueue) {
            switch (element.getPriority()) {
                case 1:
                    element.setCredits(1);
                    primaryQueue.add(element);
                    break;

                case 2:
                    element.setCredits(2);
                    secondaryQueue.add(element);
                    break;

                case 3:
                    element.setCredits(3);
                    thirtyQueue.add(element);
                    break;

                case 4:
                    element.setCredits(4);
                    fourthQueue.add(element);
                    break;
            }
        }
    }

    private static void _executeProcess(Bcp bcp, List<String> lines, int whatQueue) {
        List<String> process = bcp.getProcess().process;
        String nameOfProcess = bcp.getNameOfProcess();
        String instructions = "";

        // Number of instructions executed
        int time = 1;

        // Get the pc and register of bcp
        pc = bcp.getPc();
        x = bcp.getX();
        y = bcp.getY();

        // Execute process
        bcp.setState("EXECUTANDO");

        while (time < bcp.getTimesBlocked()) {

            // Update number of instructions
            numberOfInstructions++;

            // Get the line in process by pc
            String line = process.get(pc);

            // End of Process
            if (line.equals("SAIDA")) {
                lines.add(nameOfProcess + " terminado. X=" + bcp.getX() + " y=" + bcp.getY());

                // Remove process from memory, ready queue, credit queues and table of process
                readyQueue.removeOfQueue(bcp);
                memory.remove(bcp.getProcess());
                tableOfProcess.removeOfTable(bcp);
                _removeFromQueues(bcp, whatQueue);

                // Update number of changes
                changes++;

                return;
            }

            // Update pc in schedule
            pc++;

            // Interruption of E/S
            if (line.equals("E/S")) {

                // Update number of changes
                changes++;

                // For logfile
                instructions = "(havia apenas a E/S)";

                if (time == 2) {
                    instructions = "(havia um comando antes da E/S)";
                } else if (time == 3){
                    instructions = "(havia dois comando antes da E/S)";
                }

                lines.add("E/S iniciada em " + nameOfProcess);
                lines.add("Interrompendo " + nameOfProcess + " após " + time + " instruções " + instructions);

                // Block process
                _saveInBcp(bcp, "BLOQUEIADO");

                // Doubles the quantum for the next time the process runs
                bcp.updateTimes();

                // Starts timer blocked in two
                bcp.setWaittingTime();

                // Add in blocked queue
                queueOfBlocked.addInQueue(bcp);

                // Remove from ready queue
                readyQueue.removeOfQueue(bcp);

                // Remove from credit queue
                _removeFromQueues(bcp, whatQueue);

                return;
            }

            // Update number of commands executed
            time++;

            if (line.equals("COM")) {
                continue;
            }

            // Discover if is assignment and which register is receiving the value
            boolean isXRegister = line.charAt(0) == 'X';
            boolean isYRegister = line.charAt(0) == 'Y';
            boolean passFromEqual = line.charAt(1) == '=';

            if (passFromEqual && isXRegister) {
                x = Integer.parseInt(line.substring(2, line.length()));
            }

            if (passFromEqual && isYRegister) {
                y = Integer.parseInt(line.substring(2, line.length()));
            }
        }

        // Put ready state and remove executing state, decrease the credits
        _saveInBcp(bcp, "PRONTO");

        // Update number of changes
        changes++;

        // Add in inferior credit queue
        _addInPreviusCreditQueue(bcp, whatQueue);

        // Remove from current queue
        _removeFromQueues(bcp, whatQueue);

        // Logfile
        lines.add("Interrompendo " + nameOfProcess + " após " + time + " instruções " + instructions);
    }

    private static void _saveInBcp(Bcp bcp, String state) {
        bcp.setX(x);
        bcp.setY(y);
        bcp.setPc(pc);
        bcp.setState(state);
        int credit = bcp.getCredits();
        if (credit > 0) {
            bcp.setCredits(credit - 1);
        }
    }


    // Redistribute credits
    private static void _verifyAndRedistributeCredits() {
        boolean allZeroCredits = true;
        if (!primaryQueue.isEmpty()) {
            for (Bcp element : primaryQueue) {
                if (element.getCredits() > 0) {
                    allZeroCredits = false;
                }
            }
        }

        if (!queueOfBlocked.empty()) {
            for (Bcp element : queueOfBlocked.getQueue()) {
                if (element.getCredits() > 0) {
                    allZeroCredits = false;
                }
            }
        }

        if (allZeroCredits) {
            _redistributeCredits();
        }
    }

    private static void _redistributeCredits() {
        for (Bcp element : readyQueue.getQueue()) {
            element.setCredits(element.getPriority());
        }
    }

    private static void _removeFromQueues(Bcp bcp, int whatQueue) {
        switch (whatQueue) {
            case 1:
                primaryQueue.remove(bcp);
                break;

            case 2:
                secondaryQueue.remove(bcp);
                break;

            case 3:
                thirtyQueue.remove(bcp);
                break;

            case 4:
                fourthQueue.remove(bcp);
                break;
        }
    }

    private static void _addInPreviusCreditQueue(Bcp bcp, int whatQueue) {
        switch (whatQueue) {
            case 0:
                primaryQueue.add(bcp);
                break;

            case 1:
                primaryQueue.add(bcp);
                break;

            case 2:
                primaryQueue.add(0, bcp);
                break;

            case 3:
                secondaryQueue.add(0, bcp);
                break;

            case 4:
                thirtyQueue.add(0, bcp);
                break;
        }
    }

    public static void showQueue(int what) {
        switch (what) {
            case 1:
                System.out.println("Fila 1 quantum");
                for (Bcp element : primaryQueue) {
                    String nameOfProcess = element.getNameOfProcess();
                    System.out.println(nameOfProcess);
                }
                break;

            case 2:
                System.out.println("Fila 2 quantum");
                for (Bcp element : secondaryQueue) {
                    String nameOfProcess = element.getNameOfProcess();
                    System.out.println(nameOfProcess);
                }
                break;

            case 3:
                System.out.println("Fila 3 quantum");
                for (Bcp element : thirtyQueue) {
                    String nameOfProcess = element.getNameOfProcess();
                    System.out.println(nameOfProcess);
                }
                break;

            case 4:
                System.out.println("Fila 4 quantum");
                for (Bcp element : fourthQueue) {
                    String nameOfProcess = element.getNameOfProcess();
                    System.out.println(nameOfProcess);
                }
                break;
        }
    }

    public static void execute() {
        String nameOfProcess;

        // Distribute credits
        _initializeQueuesOfCredits();

        // Loop in credit queues
        while (!primaryQueue.isEmpty() || !queueOfBlocked.empty()) {
            queueOfBlocked.showQueue();

            if (!queueOfBlocked.empty()) {

                // Remove of blocked queue and insert in credit queue and ready queue if 2 quantum have passed
                if (queueOfBlocked.checkIfFirstOfQueueOverWaittingTime()) {

                    // Remove from blocked queue
                    Bcp bcp = queueOfBlocked.removeOfQueue();

                    // Put in ready queue and reorder the queue
                    bcp.setState("PRONTO");
                    readyQueue.addInQueue(bcp);
                    readyQueue.orderByPriority();

                    // Add in credit queue
                    _addInPreviusCreditQueue(bcp, bcp.getCredits());
                }
            }

            // Starts by checking if the queue of four credits is decreasing
            if (!fourthQueue.isEmpty()) {
                showQueue(4);
                Bcp bcp = fourthQueue.remove(0);
                nameOfProcess = bcp.getNameOfProcess();

                // Write exec in logfile
                lines.add("Executando " + nameOfProcess);
                _executeProcess(bcp, lines, 4);
            } else if (!thirtyQueue.isEmpty()) {
                showQueue(3);
                Bcp bcp = thirtyQueue.remove(0);
                nameOfProcess = bcp.getNameOfProcess();

                // Write exec in logfile
                lines.add("Executando " + nameOfProcess);
                _executeProcess(bcp, lines, 3);
            } else if (!secondaryQueue.isEmpty()) {
                showQueue(2);
                Bcp bcp = secondaryQueue.remove(0);
                nameOfProcess = bcp.getNameOfProcess();

                // Write exec in logfile
                lines.add("Executando " + nameOfProcess);
                _executeProcess(bcp, lines, 2);
            } else if (!primaryQueue.isEmpty()) {
                showQueue(1);
                Bcp bcp = primaryQueue.remove(0);
                nameOfProcess = bcp.getNameOfProcess();

                // Write exec in logfile
                lines.add("Executando " + nameOfProcess);
                _executeProcess(bcp, lines, 1);
            }

            // Decrease the waiting time of blocked processes
            if (!queueOfBlocked.empty()) {
                Bcp bcp = queueOfBlocked.getQueue().peek();
                // Update clock when pass instruction
                bcp.decreaseWaittingTime();
            }

            // Verify if all elements in queue have credit 0
            _verifyAndRedistributeCredits();
        }
    }

    public static void main(String[] args) {
        // Read All txt files and setup process, priority and quantum
        _readAndSetup();

        // Execute round robin and credit algorithm
        execute();

        // Write a logFile
        _writeLogFile();
    }
}
