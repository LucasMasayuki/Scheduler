import java.util.Comparator;

public class PriorityComparator implements Comparator<Bcp> {

    @Override
    public int compare(Bcp x, Bcp y)
    {
        // Assume neither string is null. Real code should
        // probably be more robust
        // You could also just return x.length() - y.length(),
        // which would be more efficient.
        if (x.getPriority() < y.getPriority())
        {
            return y.getPriority();
        }
        if (x.getPriority() > y.getPriority())
        {
            return x.getPriority();
        }
        return x.getPriority();
    }
}
