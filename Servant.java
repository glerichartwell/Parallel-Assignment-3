import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servant
 */
public class Servant implements Runnable
{
    public LockFreeList<Present> list;
    public AtomicInteger numCards;


    Servant(LockFreeList<Present> list, AtomicInteger numCards)
    {
        this.list = list;
        this.numCards = numCards;
    }

    public boolean minotaurRequestContains()
    {
        Random rand = new Random();

        return list.contains(new Present(rand.nextInt()));
    }
    @Override
    public void run()
    {
        Random rand = new Random();
        Present present = null;
        while (true)
        {   
            if (numCards.get() >= MinotaurPresents.MAX_PRESENTS)
                return;
            if (rand.nextDouble() * 10 > 33)
                minotaurRequestContains();
            
            present = new Present();
            list.add(present);
            list.remove(present);
            numCards.getAndIncrement();
        }
        
    }
}
