import java.util.ArrayList;
import java.util.List;

public class Memory {
    private List<List<String>> memory = new ArrayList<List<String>>();

    public void save(List<String> process) {
        this.memory.add(process);
    }

    public List<String> get(int index) {
        return memory.get(index);
    }

    public int getReference(List<String> process) { return memory.indexOf(process); }

    public void remove(Process process) {
        memory.remove(process);
    }
}
