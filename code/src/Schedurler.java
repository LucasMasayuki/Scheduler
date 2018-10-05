import java.util.ArrayList;
import java.util.List;

public class Schedurler {
    private static int quantum;
    private static Memory memory = new Memory();

    private static void _readAndSetup() {
        int maxFiles = 10;
        String fileName;
        ReadyQueue readyQueue = new ReadyQueue();
        TxtHelper txtHelper = new TxtHelper();

        fileName = "code/src/prioridades.txt";
        List<Integer> priority = txtHelper.readIntegerFiles(fileName);

        fileName = "code/src/quantum.txt";
        List<Integer> quantumList = txtHelper.readIntegerFiles(fileName);
        quantum = quantumList.get(0);

        for (int i = 1; i < maxFiles; i++) {
            String index = Integer.toString(i);
            fileName = "code/src/0" + index + ".txt";
            List<String> readTxt = txtHelper.readStringFiles(fileName);

            // read file and transform in process object
            Process process = new Process(readTxt);

            Bcp bcp = new Bcp(0, "PRONTO", priority.get(i), priority.get(i), process);

            // Save process in the memory
            memory.save(process);

            // Enqueue bcp in ready queue
            readyQueue.addInQueue(bcp);
        }
    }

    public static void main(String[] args) {
        // Read All txt files and setup process, priority and quantum
        _readAndSetup();
    }
}
