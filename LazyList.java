import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.ValueExp;
/**
 * Servant
 */
public class LazyList<T extends Comparable<T>>
{
    Node<T> head;
    Node<T> tail;
    AtomicInteger size;

    LazyList()
    {
        tail = new Node<>(null, Integer.MAX_VALUE, false, null);
        head = new Node<>(null, Integer.MIN_VALUE, false, tail);
        size = new AtomicInteger(0);
    }

    public int size()
    {
        return size.get();
    }

    public boolean validate(Node<T> prev, Node<T> curr)
    {
        return !prev.marked && !curr.marked && prev.next == curr;
    }

    public boolean contains(T item)
    {
        int key = item.hashCode();
        Node<T> curr = head;
        while (curr.key < key)
            curr = curr.next;
        return !curr.marked && curr.key == key;
    }

    public boolean add(T item)
    {
        int key = item.hashCode();
        while (true)
        {
            Node<T> prev = head;
            Node<T> curr = head.next;
            
            while (curr.key < key)
            {
                prev = curr;
                curr = curr.next;
            }
            prev.lock();
            try
            {
                curr.lock();
                try
                {
                    if (validate(prev, curr))
                    {
                        if (curr.key == key)
                            return false;
                        else
                        {
                            Node<T> node = new Node<T>(item, item.hashCode(), false, curr);
                            prev.next = node;
                            size.incrementAndGet();
                            return true;
                        }
                    }
                }
                finally
                {
                    curr.unlock();
                }
            }
            finally
            {
                prev.unlock();
            }
        }
    }

    public boolean remove(T item)
    {
        int key = item.hashCode();
        while (true)
        {
            Node<T> prev = head;
            Node<T> curr = prev.next;

            while (curr.key < key)
            {
                prev = curr;
                curr = curr.next;
            }
            
            prev.lock();
            try
            {   
                curr.lock();
                try
                {
                    if (validate(prev, curr))
                    {
                        if (curr.key != key)
                        {
                            return false;
                        }
                        else
                        {
                            curr.marked = true;
                            prev.next = curr.next;
                            size.decrementAndGet();
                            return true;
                        }
                    }
                    
                }
                finally
                {
                    curr.unlock();
                }
            }
            finally
            {
                prev.unlock();
            }
        }
    }

    public void print()
    {
        Node<T> curr = head;
        System.out.print("List: ");
        while (curr != null)
        {
            if (curr == head || curr == tail)
            {
                curr = curr.next;
            }
            else
            {
                System.out.print(curr.item.toString() + " ");
                curr = curr.next;
            }
        }
        System.out.println();
    }

    public static void main(String[] args) {
        LazyList<Present> list = new LazyList<>();

        Present p1 = new Present();
        Present p2 = new Present();
        Present p3 = new Present();
        Present p4 = new Present();
        Present p5 = new Present();


        list.add(p1);
        list.add(p2);
        list.add(p3);
        list.add(p4);
        list.add(p5);
        System.out.println("Contains 5: " + list.contains(p4));
        list.remove(p4);
        list.print();
        System.out.println("Contains 5: " + list.contains(p4));

    }
}

class Node<T extends Comparable<T>>
{
    T item;
    
    boolean marked;
    int key;

    Node<T> next;
    ReentrantLock lock;

    Node(T item, int key, boolean marked, Node<T> next)
    {
        this.item = item;
        this.marked = marked;
        this.key = key;
        this.next = next;
        this.lock = new ReentrantLock();
    }

    Node(T item, Node<T> next)
    {
        this.marked = false;
        this.item = item;
        this.next = next;
        this.lock = new ReentrantLock();
    }

    void lock()
    {
        try
        {
            lock.lock();
        }
        catch (Exception e) {}
    }

    void unlock()
    {
        try
        {
            lock.unlock();
        }
        catch (Exception e) {}
    }
}