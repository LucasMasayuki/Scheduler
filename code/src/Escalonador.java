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

    private static Queue<Bcp> primaryQueue = new LinkedList<>();
    private static Queue<Bcp> secondaryQueue = new LinkedList<>();
    private static Queue<Bcp> thirtyQueue = new LinkedList<>();
    private static Queue<Bcp> fourthQueue = new LinkedList<>();
    private static ReadyQueue readyQueue = new ReadyQueue();
    private static QueueOfBlocked queueOfBlocked = new QueueOfBlocked();
    private static TableOfProcess tableOfProcess = new TableOfProcess();
    private static List<String> lines = new ArrayList();
    private static List<Queue> priorityQueues = new ArrayList();
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

            fileName = "code/src/" + index + ".txt";
            List<String> readTxt = txtHelper.readStringFiles(fileName);
            String nameOfProcess = readTxt.get(0);
            readTxt.remove(0);

            // Write load file in logfile
            lines.add("Carregando " + nameOfProcess);

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
    }

    private static void _writeLogFile() {
        int averageChanges = _calculateAverage("changes");
        int averageInstructions = _calculateAverage("instructions");

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
        pc = bcp.getPc();
        int time = 1;
        x = bcp.getX();
        y = bcp.getY();
        bcp.setState("EXECUTANDO");

        while (time < bcp.getTimesBlocked()) {
            // Update number of instructions
            numberOfInstructions++;

            String line = process.get(pc);

            // End of Process
            if (line.equals("SAIDA")) {
                lines.add(nameOfProcess + " terminado. X=" + bcp.getX() + " y=" + bcp.getY());
                readyQueue.removeOfQueue(bcp);
                memory.remove(bcp.getProcess());

                _removeFromQueues(bcp, whatQueue);

                return;
            }

            // update pc in schedule
            pc++;

            // Interruption of E/S
            if (line.equals("E/S")) {
                // Update number of changes
                changes++;

                instructions = "(havia apenas a E/S)";

                if (time == 2) {
                    instructions = "(havia um comando antes da E/S)";
                } else if (time == 3){
                    instructions = "(havia dois comando antes da E/S)";
                }

                lines.add("E/S iniciada em " + nameOfProcess);
                lines.add("Interrompendo " + nameOfProcess + " após " + time + " instruções " + instructions);

                _saveInBcp(bcp, "BLOQUEIADO");
                bcp.updateTimes();
                queueOfBlocked.addInQueue(bcp);
                readyQueue.removeOfQueue(bcp);

                _removeFromQueues(bcp, whatQueue);
                _verifyAndRedistributeCredits();

                return;
            }

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

        _saveInBcp(bcp, "PRONTO");

        // Update number of changes
        changes++;

        _addInPreviusCreditQueue(bcp, whatQueue);
        _removeFromQueues(bcp, whatQueue);
        _verifyAndRedistributeCredits();

        lines.add("Interrompendo " + nameOfProcess + " após " + time + " instruções " + instructions);
    }

    private static void _saveInBcp(Bcp bcp, String state) {
        bcp.setX(x);
        bcp.setY(y);
        bcp.setPc(pc);
        bcp.setState(state);
        int credit = bcp.getCredits();
        bcp.setCredits(credit - 1);
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

            if (allZeroCredits) {
                _initializeQueuesOfCredits();
            }
        }
    }

    // Verify credits for blockedQueue
    private static void _verifyCreditsBlocked() {
        boolean allZeroCredits = true;
        if (!queueOfBlocked.empty()) {
            for (Bcp element : queueOfBlocked.getQueue()) {
                if (element.getCredits() > 0) {
                    allZeroCredits = false;
                }
            }
        }
        if (allZeroCredits) {
            _redistributeBlockedQueueCredits();
        }
    }

    private static void _redistributeBlockedQueueCredits() {
        for (Bcp element : queueOfBlocked.getQueue()) {
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
                _verifyCreditsBlocked();
                _verifyAndRedistributeCredits();

            case 1:
                primaryQueue.add(bcp);
                break;

            case 2:
                primaryQueue.add(bcp);
                break;

            case 3:
                secondaryQueue.add(bcp);
                break;

            case 4:
                thirtyQueue.add(bcp);
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
        _initializeQueuesOfCredits();
        String nameOfProcess;

        // Loop in credit queues
        while (!primaryQueue.isEmpty() || !queueOfBlocked.empty()) {
            //queueOfBlocked.showQueue();

            if (!queueOfBlocked.empty()) {
                // Remove of blocked queue and insert in credit queue and ready queue if 3 quantum have passed
                if (queueOfBlocked.checkIfFirstOfQueueOverWaittingTime()) {
                    Bcp bcp = queueOfBlocked.removeOfQueue();
                    bcp.setState("PRONTO");
                    readyQueue.addInQueue(bcp);
                    readyQueue.orderByPriority();
                    _addInPreviusCreditQueue(bcp, bcp.getCredits() + 1);
                }
            }

            if (!fourthQueue.isEmpty()) {
                //showQueue(4);
                Bcp bcp = fourthQueue.remove();
                nameOfProcess = bcp.getNameOfProcess();

                // Write exec in logfile
                lines.add("Executando " + nameOfProcess);
                _executeProcess(bcp, lines, 4);

                // Update clock when pass instruction
                bcp.decreaseWaittingTime();
            } else if (!thirtyQueue.isEmpty()) {
                //showQueue(3);
                Bcp bcp = thirtyQueue.remove();
                nameOfProcess = bcp.getNameOfProcess();

                // Write exec in logfile
                lines.add("Executando " + nameOfProcess);
                _executeProcess(bcp, lines, 3);

                // Update clock when pass instruction
                bcp.decreaseWaittingTime();
            } else if (!secondaryQueue.isEmpty()) {
                //showQueue(2);
                Bcp bcp = secondaryQueue.remove();
                nameOfProcess = bcp.getNameOfProcess();

                // Write exec in logfile
                lines.add("Executando " + nameOfProcess);
                _executeProcess(bcp, lines, 2);

                // Update clock when pass instruction
                bcp.decreaseWaittingTime();
            } else if (!primaryQueue.isEmpty()) {
                //showQueue(1);
                Bcp bcp = primaryQueue.remove();
                nameOfProcess = bcp.getNameOfProcess();

                // Write exec in logfile
                lines.add("Executando " + nameOfProcess);
                _executeProcess(bcp, lines, 1);

                // Update clock when pass instruction
                bcp.decreaseWaittingTime();
            }
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
