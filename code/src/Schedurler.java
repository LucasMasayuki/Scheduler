import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Schedurler {
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

            // Write load file in logfile
            lines.add("Carregando " +readTxt.get(0));

            // Read file and transform in process object
            Process process = new Process(readTxt);

            Bcp bcp = new Bcp(1, "PRONTO", priority.get(i - 1), priority.get(i - 1), process);

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
            switch (element.getCredits()) {
                case 1:
                    primaryQueue.add(element);
                    break;

                case 2:
                    secondaryQueue.add(element);
                    break;

                case 3:
                    thirtyQueue.add(element);
                    break;

                case 4:
                    fourthQueue.add(element);
                    break;
            }
        }
    }

    private static void _executeProcess(Bcp bcp, List<String> lines, int whatQueue) {
        List<String> process = bcp.getProcess().process;
        String instructions = "";
        pc = bcp.getPc();
        int maxLengthProcess = process.size();
        int time = 1;
        x = bcp.getX();
        y = bcp.getY();
        bcp.setState("EXECUTANDO");

        while (time < quantum + 1) {
            // Update number of instructions
            numberOfInstructions++;

            String line = process.get(pc);

            // End of Process
            if (line.equals("SAIDA")) {
                lines.add(process.get(0)+ " terminado. X=" + bcp.getX() + "y=" + bcp.getY());
                readyQueue.removeOfQueue(bcp);
                memory.remove(bcp.getProcess());

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

                return;
            }

            // update pc in schedule
            pc++;

            // Interruption of E/S
            if (line.equals("E/S")) {
                instructions = "(havia apenas a E/S)";

                if (time == 2) {
                    instructions = "(havia um comando antes da E/S)";
                } else if (time == 3){
                    instructions = "(havia dois comando antes da E/S)";
                }

                lines.add("E/S iniciada em " +process.get(0));
                lines.add("Interrompendo " + process.get(0) + "após " + time + "instruções " + instructions);

                _saveInBcp(bcp, "BLOQUEIADO");
                queueOfBlocked.addInQueue(bcp);
                readyQueue.removeOfQueue(bcp);

                switch (whatQueue) {
                    case 2:
                        primaryQueue.add(bcp);

                    case 3:
                        secondaryQueue.add(bcp);

                    case 4:
                        thirtyQueue.add(bcp);
                }

                return;
            }

            time++;

            if (line.equals("COM")) {
                continue;
            }

            boolean isXRegister;
            boolean isYRegister;
            boolean passFromEqual = false;

            // Discover if is assignment and which register is receiving the value
            for (int i = 0; i < line.length(); i++) {
                isXRegister = line.charAt(i) == 'x';
                isYRegister = line.charAt(i) == 'y';

                if (passFromEqual && isXRegister) {
                    x = Integer.parseInt(line.substring(i));
                    break;
                }

                if (passFromEqual && isYRegister) {
                    y = Integer.parseInt(line.substring(i));
                    break;
                }

                passFromEqual = line.charAt(i) == '=';
            }
        }

        _saveInBcp(bcp, "PRONTO");

        // Update number of changes
        changes++;

        lines.add("Interrompendo " + process.get(0) + "após " + time + "instruções " + instructions);
    }

    private static void _saveInBcp(Bcp bcp, String state) {
        bcp.setX(x);
        bcp.setY(y);
        bcp.setPc(pc);
        bcp.setState(state);
        int credit = bcp.getCredits();
        bcp.setCredits(credit - 1);
    }

    public static void showQueue(int what) {
        switch (what) {
            case 1:
                System.out.println("Fila 1 quantum");
                for (Bcp element : primaryQueue) {
                    System.out.println(element.getProcess().process.get(0));
                }
                break;

            case 2:
                System.out.println("Fila 2 quantum");
                for (Bcp element : secondaryQueue) {
                    System.out.println(element.getProcess().process.get(0));
                }
                break;

            case 3:
                System.out.println("Fila 3 quantum");
                for (Bcp element : thirtyQueue) {
                    System.out.println(element.getProcess().process.get(0));
                }
                break;

            case 4:
                System.out.println("Fila 4 quantum");
                for (Bcp element : fourthQueue) {
                    System.out.println(element.getProcess().process.get(0));
                }
                break;
        }
    }

    public static void execute() {
        int credits = 4;
        _initializeQueuesOfCredits();

        while (!primaryQueue.isEmpty()) {
            if (!fourthQueue.isEmpty()) {
                Bcp bcp = fourthQueue.poll();

                // Write exec in logfile
                lines.add("Executando " +bcp.getProcess().process.get(0));
                _executeProcess(bcp, lines, 4);
                showQueue(4);
            } else if (!thirtyQueue.isEmpty()) {
                Bcp bcp = thirtyQueue.remove();

                // Write exec in logfile
                lines.add("Executando " +bcp.getProcess().process.get(0));
                _executeProcess(bcp, lines, 3);
                showQueue(3);
            } else if (!secondaryQueue.isEmpty()) {
                Bcp bcp = secondaryQueue.remove();

                // Write exec in logfile
                lines.add("Executando " +bcp.getProcess().process.get(0));
                _executeProcess(bcp, lines, 2);
                showQueue(3);
            } else if (!primaryQueue.isEmpty()) {
                Bcp bcp = primaryQueue.remove();

                // Write exec in logfile
                lines.add("Executando " +bcp.getProcess().process.get(0));
                _executeProcess(bcp, lines, 1);
                showQueue(1);
            }
        }
    }

    public static void main(String[] args) {
        // Read All txt files and setup process, priority and quantum
        _readAndSetup();
        readyQueue.showQueue();

        execute();

        _writeLogFile();
    }
}
