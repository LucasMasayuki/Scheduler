import java.util.*;

public class TableOfProcess {
    private Map<Integer, Bcp> table = new HashMap<Integer, Bcp>();
    private int line = 0;

    public void insert(Bcp bcp) {
        table.put(line, bcp);
        line++;
    }

//    public void orderByPriority() {
//        if (table.size() > 0) {
//            Comparator<Bcp> comparator = new PriorityComparator();
//            Collections.sort(table, comparator);
//        }
//    }

    public void removeOfTable(int reference) {
        table.remove(reference);
    }

    public Bcp getBcp(int i) {
        return table.get(i);
    }

    public int getReferenceOfBcp() {
        return line;
    }

    public void showTable() {
        System.out.println("tabela");
        for (Integer reference : table.keySet()) {
            System.out.println(table.get(reference).getNameOfProcess() + " || reference " + reference);
        }
        System.out.println();
    }

    public Map<Integer, Bcp> getTable() {
        return table;
    }

    public boolean empty() {
        return table.isEmpty();
    }
}
