import java.util.Comparator;

public class PriorityComparator implements Comparator<Bcp> {

    @Override
    public int compare(Bcp x, Bcp y)
    {
        int past = x.getPriority();
        int current = y.getPriority();

        if (past <= current) {
            return 1;
        }

        return -1;
    }
}
