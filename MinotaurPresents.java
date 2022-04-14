import java.util.concurrent.atomic.AtomicInteger;

/**
 * MinotaurPresents
 */
public class MinotaurPresents
{
    final static int MAX_PRESENTS = 500000;

    final static int NUM_SERVANTS = 4;

    public static void main(String[] args) throws InterruptedException
    {
        LockFreeList<Present> list = new LockFreeList<>();
        AtomicInteger numCards = new AtomicInteger(0);

        long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_SERVANTS; i++)
        {
            Servant servant = new Servant(list, numCards);
            Thread thread = new Thread(servant);
            thread.start();
            thread.join();
        }
        long end = System.currentTimeMillis();
        // list.print();
        System.out.println("List complete. Number of thank you cards written: " + numCards.get());
        System.out.println("Exectution time: " + (end - start) + "ms");
    }
    
}