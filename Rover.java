
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class Rover implements Runnable
{
    final static public int HOURS_RUN = 24;
    final static public int NUM_SENSORS = 8;

    ArrayList<Sensor> sensors = new ArrayList<>();

    LockFreeList<TimeReading> timeSorted = new LockFreeList<>();
    LockFreeList<TempReading> tempSorted = new LockFreeList<>();
    
    AtomicReference<String> interval = new AtomicReference<>();

    @Override
    public void run() {
        
    }

    private ArrayDeque<TempReading> getTopLow(int hour)
    {
        ArrayDeque<TempReading> topLow = new ArrayDeque<>();

        Node<TempReading> curr = tempSorted.head;

         int maxLow = Integer.MIN_VALUE;

        while (curr != null)
        {
            if (curr == tempSorted.head || 
                curr == tempSorted.tail || 
                curr.item.temp == maxLow || 
                curr.item.hour != hour)
            {
                curr = curr.next.getReference();
            }
            else if (topLow.size() == 5)
            {
                return topLow;
            }
            else
            {
                topLow.add(curr.item);
                maxLow = topLow.peekLast().temp;
                curr = curr.next.getReference();
            }
        }
        return topLow;
    }

    private ArrayDeque<TempReading> getTopHigh(int hour)
    {
        ArrayDeque<TempReading> topHigh = new ArrayDeque<>();
        
        Node<TempReading> curr = tempSorted.head;

         int maxHigh = Integer.MIN_VALUE;

        while (curr != null)
        {
            if (curr == tempSorted.head || 
                curr == tempSorted.tail || 
                curr.item.temp <= maxHigh || 
                curr.item.hour != hour)
            {
                curr = curr.next.getReference();
            }
            else
            {
                
                topHigh.add(curr.item);
                if (topHigh.size() > 5)
                {
                    topHigh.remove();
                }
                maxHigh = topHigh.peekLast().temp;
                curr = curr.next.getReference();
            }
        }
        return topHigh;
    }

    private String getInterval(int hour)
    {

        Node<TimeReading> curr = timeSorted.head.next.getReference();
        Node<TimeReading> compare = timeSorted.head.next.getReference();
        int maxDiff = 0;
    
        Node<TimeReading> save = compare;
        while (curr != timeSorted.tail)
        {
            if (curr.item.hour != hour)
            {
                // System.out.println("Hour: " + curr.item.hour);
                curr = curr.next.getReference();
                compare = curr;
                save = compare;
                continue;
            }

            
            int temp = maxDiff;
            String newInterval;
            maxDiff = Math.max(maxDiff, Math.abs(curr.item.temp - compare.item.temp));
            if (Math.max(curr.item.minute, compare.item.minute) - 10 < 0)
            {
                newInterval = "Minute " + 
                                String.valueOf(0) + 
                                " to Minute " +
                                String.valueOf(10);
            }
            else
            {
                newInterval = "Minute " + 
                                String.valueOf(Math.max(curr.item.minute, compare.item.minute) - 10) + 
                                " to Minute " +
                                String.valueOf(Math.max(curr.item.minute, compare.item.minute));
            }

            if (temp != maxDiff)
            {
                // System.out.println("temp: " + curr.item.temp + " compare: " + compare.item.temp);
                // System.out.println("minute: " + curr.item.minute + " compare: " + compare.item.minute);
                // System.out.println("hour: " + curr.item.hour + " compare: " + compare.item.hour);
                // System.out.println("new interval: " + newInterval);
                // System.out.println();
                interval.set(newInterval);
            }

            compare = compare.next.getReference();
            if (Math.abs(curr.item.minute - compare.item.minute) > 10)
            {
                curr = curr.next.getReference();
                compare = save;
            }
            
        }
        // System.out.println("MAX DIFF: " + maxDiff);
        return interval.get();
    }

    public void runReport(ArrayList<Report> reports, int hour)
    {
        reports.add(new Report(getTopHigh(hour), getTopLow(hour), getInterval(hour)));
    }

    public void printReports(ArrayList<Report> reports)
    {
        for (Report report: reports)
        {
            System.out.println("======== Report for Hour " + report.topHigh.peek().hour + " ========");
            System.out.print("Top 5 Highest Temperatures: ");
            for (TempReading read: report.topHigh)
            {
                System.out.print(read.temp + " ");
            }
            System.out.println();
            System.out.print("Top 5 Lowest Temperatures: ");
            for (TempReading read: report.topLow)
            {
                System.out.print(read.temp + " ");
            }
            System.out.println();
            System.out.println("Largest Temperature Difference Interval: " + report.interval);
            System.out.println();

        }
    }

    public static void main(String[] args) throws InterruptedException
    {
        Rover rover = new Rover();
        ArrayList<Thread> threads = new ArrayList<>();
        ArrayList<Report> reports = new ArrayList<>();
        CyclicBarrier barrier = new CyclicBarrier(NUM_SENSORS + 1);
        
    
        // Rover controls hour and runs report when threads terminate
        // Start new threads for next hour
        
        for (int i = 0; i < NUM_SENSORS; i++)
        {
            Sensor sensor = new Sensor(rover.timeSorted, rover.tempSorted, rover.interval, barrier);
            Thread thread = new Thread(sensor);
            threads.add(thread);
            thread.start();
        }
        int hour = 0;
        while (hour < HOURS_RUN)
        {
            try
            {
                barrier.await();
            }
            catch (Exception e) {e.printStackTrace();}
            rover.runReport(reports, hour);
            hour++;

        }

        rover.printReports(reports);
        // rover.timeSorted.print();

    }
}

class Report
{
    ArrayDeque<TempReading> topHigh;
    ArrayDeque<TempReading> topLow;
    String interval;

    Report(ArrayDeque<TempReading> topHigh, ArrayDeque<TempReading> topLow, String interval)
    {
        this.topHigh = topHigh;
        this.topLow = topLow;
        this.interval = interval;
    }
}

