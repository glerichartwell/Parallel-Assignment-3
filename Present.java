import java.util.Random;

/**
 * Present
 */
public class Present implements Comparable<Present>
{
    private int tag;

    Present()
    {
        tag = this.hashCode();
    }

    Present(int tag)
    {
        this.tag = tag;
    }

    @Override
    public String toString() {
        
        return Integer.valueOf(tag).toString();
    }

    @Override
    public int compareTo(Present o)
    {
        return o.getTag() - this.tag;
    }

    public int getTag()
    {
        return tag;
    }
}
