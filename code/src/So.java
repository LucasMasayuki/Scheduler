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
    private static int numberOfInstructions = 0;
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

            // Insert in process Table (is hash map, first get the address and after increase address)
            indexOfTable = tableOfProcess.getReferenceOfBcp();
            tableOfProcess.insert(bcp);

            // Enque index of bcp in ready queue with the correct credit index
            readyQueue.get(credit).add(indexOfTable);
        }

        _loadProcesses();
    }

    private static void _loadProcesses() {
        int credit = maxPriority;
        while (credit > 0) {
            for (Integer reference : readyQueue.get(credit)) {
                // Write load file in logfile
                lines.add("Carregando " + tableOfProcess.getBcp(reference).getNameOfProcess());
            }
            credit--;
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

        numberOfInstructions = time/maxFiles;

        return numberOfInstructions;
    }

    // Return true if block
    private static String _executeProcess(List<String> process, Bcp bcp) {
        String nameOfProcess = bcp.getNameOfProcess();

        int pc = processor.getPc();

        // Get the line in process by pc
        String line = process.get(pc);

        // End of Process
        if (line.equals("SAIDA")) {
            lines.add(nameOfProcess + " terminado. X=" + bcp.getX() + " y=" + bcp.getY());

            // Remove from memory
            memory.remove(bcp.getProcess());

            return line;
        }

        // Interruption of E/S
        if (line.equals("E/S")) {
            lines.add("E/S iniciada em " + nameOfProcess);

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

    private static void _verifyBlockedQueue() {
        // Decrease the waiting time of blocked processes
        Bcp bcp;
        if (!queueOfBlocked.empty()) {
            for(int referenceOfBlocked : queueOfBlocked.getQueue()){
                bcp = tableOfProcess.getBcp(referenceOfBlocked);
                dispatcher.updateWaittingTimeBcp(bcp);
            }
        }
    }

    private static void _showQueue() {
        for (int i = readyQueue.size() - 1; i >= 0; i--) {
            LinkedList<Integer> list= readyQueue.get(i);
            System.out.println("fila " + i);

            for (int re :list) {
                System.out.println(
                        tableOfProcess.getBcp(re).getNameOfProcess()
                                + " || créditos " +
                                tableOfProcess.getBcp(re).getCredits()
                                + "|| waittingtime " + tableOfProcess.getBcp(re).getWaittingTime()
                                + "|| reference " + re
                                + " || current pc " + tableOfProcess.getBcp(re).getPc()
                );
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        LinkedList<Integer> queue;
        Bcp bcp;
        String response = "";
        String nameOfProcess = "";
        Integer reference = 0;
        boolean blocked = false;
        boolean end = false;
        int quntumquantum = 0;
        int bcpTime = 0;
        int countInstructions = 0;

        // Read All txt files and setup process, priority and quantum
        _readAndSetup();

        // Get the first reference of the queue with higher credit
        for (int i = maxPriority; i > 0; i--) {
            queue = readyQueue.get(i);

            if (!queue.isEmpty()) {
                reference = queue.getFirst();
                break;
            }
        }

        // Loop in credit queues
        while (!tableOfProcess.empty()) {
            tableOfProcess.showTable();
            _showQueue();

            System.out.println();

            // If no have in ready queue, verify blocked queue
            if (reference == null) {
                reference = escalonador.verifyBlockedQueue(readyQueue, tableOfProcess, queueOfBlocked);
                _verifyBlockedQueue();
                continue;
            }

            // Get the bcp in table of processes
            bcp = tableOfProcess.getBcp(reference);
            nameOfProcess = bcp.getNameOfProcess();

            // Recover context
            List<String> process = dispatcher.recoverContext(memory, bcp, processor);

            // Update execute process
            dispatcher.updateBcp(bcp, psw[1], processor, "EXECUTANDO");

            // Write exec in logfile
            lines.add(psw[1] + " " + nameOfProcess);

            bcpTime = bcp.getTimes();

            processor.useCpu();

            // Use processor for quantum time
            while (clock.timeOfProcess(quntumquantum, bcpTime)) {
                response = _executeProcess(process, bcp);

                blocked = response.equals("E/S");
                end = response.equals("SAIDA");

                // Increment pc
                processor.incrementPc();

                quntumquantum = quntumquantum + 1;

                countInstructions++;
                numberOfInstructions++;

                // Over quantum or finish process
                if (blocked || end) {
                    break;
                }
            }

            processor.freeCpu();

            // Update number of changes
            changes++;

            quntumquantum = 0;

            // Logfile
            lines.add("Interrompendo " + nameOfProcess + " após " + countInstructions + " instruções");
            countInstructions = 0;

            // Over quantum or finish process
            if (blocked || end) {
                processor.freeCpu();
                reference = escalonador.getNext(
                        readyQueue,
                        queueOfBlocked,
                        tableOfProcess,
                        maxPriority,
                        reference,
                        response
                );

                blocked = false;
                end = false;

                _verifyBlockedQueue();

                continue;
            }

            // Put ready state and remove executing state
            dispatcher.updateBcp(bcp, psw[0], processor, "OVERQUANTUM");

//             Over the quantum time
//             Put in hashmap
            try {
                groupOfQuantums.put(numberOfInstructions, groupOfQuantums.get(numberOfInstructions) + numberOfInstructions);
            } catch (NullPointerException ex) {
                groupOfQuantums.put(numberOfInstructions,  numberOfInstructions);
            }

            reference = escalonador.getNext(
                    readyQueue,
                    queueOfBlocked,
                    tableOfProcess,
                    maxPriority,
                    reference,
                    response
            );

            _verifyBlockedQueue();
        }

        // Write a logFile
        _writeLogFile();

        System.out.println("Finalizado!!!");
    }
}
