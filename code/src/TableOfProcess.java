import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TableOfProcess {
    private List<Bcp> table = new ArrayList<>();

    public void insert(Bcp bcp) {
        table.add(bcp);
    }

    public void orderByPriority() {
        if (table.size() > 0) {
            Comparator<Bcp> comparator = new PriorityComparator();
            Collections.sort(table, comparator);
        }
    }

    public void removeOfTable(Bcp bcp) {
        table.remove(bcp);
    }

    public Bcp getProcess(int i) {
        return table.get(i);
    }

    public int getReferenceOfBcp(Bcp bcp) {
        return table.indexOf(bcp);
    }

    public void showTable() {
        System.out.println("teste");
        for (Bcp element : table ) {
            System.out.println(element.getNameOfProcess());
        }

    }

    public boolean empty() {
        return table.isEmpty();
    }
}
