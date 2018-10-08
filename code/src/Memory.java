import java.util.ArrayList;
import java.util.List;

public class Memory {
    private static List<Process> memory = new ArrayList<>();

    public void save(Process process) {
        memory.add(process);
    }

    public void get(int index) {
        memory.get(index);
    }

    public void remove(Process process) {
        memory.remove(process);
    }
}
