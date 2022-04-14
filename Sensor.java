
import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;

public class Sensor implements Runnable
{
    
    LockFreeList<TimeReading> timeSorted;
    LockFreeList<TempReading> tempSorted;
    AtomicReference<String> interval;
    CyclicBarrier barrier;

    Sensor(LockFreeList<TimeReading> timeSorted, LockFreeList<TempReading> tempSorted, AtomicReference<String> interval, CyclicBarrier barrier)
    {
        this.timeSorted = timeSorted;
        this.tempSorted = tempSorted;
        this.interval = interval;
        this.barrier = barrier;
    }

    @Override
    public void run()
    {
        // Take temp every minute

        for (int i = 0; i < Rover.HOURS_RUN; i++)
        {
            for (int j = 0; j < 60; j++)
            {
                // range: -100F to 70F
                int read = new Random().nextInt(171) - 100;
                TempReading temp = new TempReading(read, this.hashCode(), i, (i * 60 + j));
                tempSorted.add(temp);
                TimeReading time = new TimeReading(read, this.hashCode(), i, (i * 60 + j));
                timeSorted.add(time); 
            }
            try
            {
                barrier.await();
            }
            catch (Exception e) {e.printStackTrace();}
        }
    }
}

class TimeReading implements Comparable<TimeReading>
{

    int temp;
    int sensorID;
    int hour;
    int minute;

    TimeReading(int temp, int sensorID, int hour, int minute)
    {
        this.temp = temp;
        this.sensorID = sensorID;
        this.hour = hour;
        this.minute = minute;
    }

    @Override
    public int hashCode()
    {
        return this.minute;
    }

    @Override
    public String toString()
    {
        return "Hour: " + String.valueOf(this.hour) + "\n" + "Minute: " + String.valueOf(this.minute) + "\n" + " Temp: " + String.valueOf(this.temp) + "\n\n";
    }

    @Override
    public int compareTo(TimeReading o)
    {
        return minute - o.minute;
    }

}

class TempReading implements Comparable<TempReading>
{

    int temp;
    int sensorID;
    int hour;
    int minute;

    TempReading(int temp, int sensorID, int hour, int minute)
    {
        this.temp = temp;
        this.sensorID = sensorID;
        this.hour = hour;
        this.minute = minute;
    }

    @Override
    public int hashCode()
    {
        return this.temp;
    }

    @Override
    public String toString()
    {
        return String.valueOf(this.temp) + "F";
    }

    @Override
    public int compareTo(TempReading o)
    {
        return temp - o.temp;
    }

}
