import sun.awt.image.ImageWatched;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class So {
    private static int quantum;
    private static int maxFiles = 10;
    private static int changes = 0;
    private static int maxPriority = 0;
    private static int time = 1;
    private static String[] psw = { "PRONTO", "EXECUTANDO", "BLOQUEIADO" };

    private static List<LinkedList<Integer>> readyQueue = new ArrayList<>();

    private static QueueOfBlocked queueOfBlocked = new QueueOfBlocked();
    private static TableOfProcess tableOfProcess = new TableOfProcess();
    private static List<String> lines = new ArrayList<>();
    private static Map<Integer, Integer> groupOfQuantums = new HashMap<Integer, Integer>();
    private static Memory memory = new Memory();
    private static Processor processor = new Processor();
    private static Dispatcher dispatcher = new Dispatcher();
    private static Escalonador escalonador = new Escalonador();
    private static Clock clock = new Clock();

    private static void _readAndSetup() {
        TxtHelper txtHelper = new TxtHelper();
        String fileName;
        int credit;
        int indexOfTable;

        fileName = "code/src/prioridades.txt";
        List<Integer> priority = txtHelper.readIntegerFiles(fileName);

        maxPriority = Collections.max(priority);

        for (int i = 0; i < maxPriority + 1; i++) {
            readyQueue.add(i, new LinkedList<>());
        }

        fileName = "code/src/quantum.txt";
        List<Integer> quantumList = txtHelper.readIntegerFiles(fileName);

        try {
            quantum = quantumList.get(0);
        } catch (NullPointerException e) {
            System.out.println("Erro quantum.txt está vazio ou em formato incorreto, quantum setado para o padrão 3");
            quantum = 3;
        }

        for (int i = 1; i < maxFiles + 1; i++) {
            String index = Integer.toString(i);

            if (i < 10) {
                index = "0" + index;
            }

            // Read the processes files
            fileName = "code/src/" + index + ".txt";
            List<String> readTxt = txtHelper.readStringFiles(fileName);

            String nameOfProcess;

            try {
                nameOfProcess = readTxt.get(0);
            } catch (NullPointerException e) {
                System.out.println("Erro Não possui nome do process! nome generico teste-x adicionado");
                nameOfProcess = "teste-x";
            }

            // Remove the first line(processs name)
            readTxt.remove(0);

            // Save process in the memory
            memory.save(readTxt);

            int reference = memory.getReference(readTxt);

            // Read file and transform in process object
            Process process = new Process(reference);

            Bcp bcp = new Bcp(0, "PRONTO", priority.get(i - 1), process, nameOfProcess, quantum);

            // Add to the correct credit list
            credit = bcp.getPriority();

            // Insert in process Table
            tableOfProcess.insert(bcp);
            indexOfTable = tableOfProcess.getReferenceOfBcp(bcp);

            // Enque index of bcp in ready queue with the correct credit index
            readyQueue.get(credit).add(indexOfTable);
        }

        _loadProcesses();
    }

    private static void _loadProcesses() {
        for (Bcp element : tableOfProcess.getTable()) {
            // Write load file in logfile
            lines.add("Carregando " + element.getNameOfProcess());
        }
    }

    private static void _writeLogFile() {
        // Calculate the stats
        int averageChanges = _calculateAverage("changes");
        int averageInstructions = _calculateAverage("instructions");

        lines.add("MEDIA DE TROCAS: "+ averageChanges);
        lines.add("MEDIA DE INSTRUCOES: "+ averageInstructions);
        lines.add("QUANTUM: "+ quantum);

        String logName;

        if (quantum < 10) {
            logName = "log0" + quantum + ".txt";
        } else {
            logName = "log" + quantum + ".txt";
        }

        Path file = Paths.get(logName);

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
        int numberOfInstructions = 0;
        int instructions;
        int time = 0;

        // Makes sum of averages of the number of instructions grouped by quantum
        for (Map.Entry<Integer, Integer> group : groupOfQuantums.entrySet()) {
            time = group.getKey();
            instructions = group.getValue();

            numberOfInstructions += (instructions/time);
        }

        return numberOfInstructions;
    }

    // Return true if block
    private static String _executeProcess(List<String> process, Bcp bcp, int reference) {
        String nameOfProcess = bcp.getNameOfProcess();

        int pc = processor.getPc();

        // Get the line in process by pc
        String line = process.get(pc);

        // Update number of commands executed
        time++;

        // End of Process
        if (line.equals("SAIDA")) {
            lines.add(nameOfProcess + " terminado. X=" + bcp.getX() + " y=" + bcp.getY());

            escalonador.removeDoneProcess(readyQueue, tableOfProcess, reference, memory);

            return line;
        }

        // Interruption of E/S
        if (line.equals("E/S")) {
            lines.add("E/S iniciada em " + nameOfProcess);
            lines.add("Interrompendo " + nameOfProcess + " após " + time + " instruções");

            escalonador.moveToBlockedQueue(readyQueue, queueOfBlocked, tableOfProcess, reference);

            // Block process
            dispatcher.updateBcp(bcp, psw[2], processor, line);

            return line;
        }

        if (line.equals("COM")) {
            return line;
        }

        processor.process(bcp, line);

        return line;
    }

    public static void main(String[] args) {
        LinkedList<Integer> queue;
        List<Bcp> table = tableOfProcess.getTable();
        Bcp bcp;
        int reference = 0;
        String response = "";
        boolean blocked = false;
        boolean end = false;
        String nameOfProcess = "";
        int index;
        int credit = maxPriority;
        int quntumquantum = 0;
        int bcpTime = 0;

        // Read All txt files and setup process, priority and quantum
        _readAndSetup();

        // Get the first reference of the queue with higher credit
        for (int i = maxPriority; i > 0; i--) {
            queue = readyQueue.get(i);

            if (!queue.isEmpty()) {
                reference = queue.get(0);
                break;
            }
        }

        // Loop in credit queues
        while (!tableOfProcess.empty()) {
//            for (int i = readyQueue.size() - 1; i > 0; i--) {
//                LinkedList<Integer> list= readyQueue.get(i);
//                System.out.println("fila" + i);
//
//                for (int re :list) {
//                    System.out.println(tableOfProcess.getBcp(re).getNameOfProcess());
//                }
//            }

            if (!queueOfBlocked.empty()) {
                queueOfBlocked.showQueue(tableOfProcess);
                index = queueOfBlocked.getQueue().peek();
                bcp = tableOfProcess.getBcp(index);

                // Remove of blocked queue and insert in credit queue and ready queue if 2 quantum have passed
                if (clock.returnToQueue(bcp.getWaittingTime())) {
                    escalonador.returnBlockedInReadyQueue(readyQueue, queueOfBlocked, tableOfProcess);
                }
            }

            // Get the bcp in table of processes
            bcp = table.get(reference);
            nameOfProcess = bcp.getNameOfProcess();

            // Recover context
            List<String> process = dispatcher.recoverContext(memory, bcp, processor);

            // Update execute process
            dispatcher.updateBcp(bcp, psw[1], processor, "EXECUTANDO");

            // Write exec in logfile
            lines.add(psw[1] + " " + nameOfProcess);

            bcpTime = bcp.getTimes();

            // Use processor for quantum time
            while (clock.timeOfProcess(quntumquantum, bcpTime)) {
                response = _executeProcess(process, bcp, reference);

                blocked = response == "E/S";
                end = response == "SAIDA";

                processor.useCpu();

                // Over quantum or finish process
                if (blocked || end) {
                    break;
                }

                quntumquantum = quntumquantum + 1;
            }

            // Update number of changes
            changes++;

            processor.freeCpu();

            quntumquantum = 0;

            // Over quantum or finish process
            if (blocked || end) {
                blocked = false;
                end = false;
                continue;
            }

            // Move queues
            escalonador.moveQueues(readyQueue, table, reference);

            // Put ready state and remove executing state, decrease the credits
            dispatcher.updateBcp(bcp, psw[0], processor, "OVERQUANTUM");

            // Over the quantum time
            // Put in hashmap
//            try {
//                groupOfQuantums.put(time, groupOfQuantums.get(time) + numberOfInstructions);
//            } catch (NullPointerException ex) {
//                groupOfQuantums.put(time,  numberOfInstructions);
//            }

            // Logfile
            lines.add("Interrompendo " + nameOfProcess + " após " + (time - 1) + " instruções");

            // Decrease the waiting time of blocked processes
            if (!queueOfBlocked.empty()) {
                for(int referenceOfBlocked : queueOfBlocked.getQueue()){
                    bcp = tableOfProcess.getBcp(referenceOfBlocked);
                    dispatcher.updateWaittingTimeBcp(bcp);
                }
            }

            reference = escalonador.getNext(readyQueue, queueOfBlocked, tableOfProcess, maxPriority, lines);
        }

        // Write a logFile
        _writeLogFile();

        System.out.println("Finalizado!!!");
    }
}
