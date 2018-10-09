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
}
