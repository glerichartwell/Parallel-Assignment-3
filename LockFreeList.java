import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class LockFreeList<T extends Comparable<T>>
{
    Node<T> head;
    Node<T> tail;

    LockFreeList()
    {
        head = new Node<>(Integer.MIN_VALUE);
        tail = new Node<>(Integer.MAX_VALUE);
        head.next.set(tail, false); 
    }

    public Window<T> find(Node<T> head, int key)
    {
        Node<T> prev = null, curr = null, succ = null;
        boolean[] marked = {false};
        boolean snip;

        retry: while (true)
        {
            prev = head;
            curr = prev.next.getReference();

            while (true)
            {
                succ = curr.next.get(marked);
                while (marked[0])
                {
                    snip = prev.next.compareAndSet(curr, succ, false, false);
                    if (!snip)
                    {
                        continue retry;
                    }
                    curr = succ;
                    succ = curr.next.get(marked);
                }
                if (curr.key >= key)
                {
                    return new Window<T>(prev, curr);
                }
                prev = curr;
                curr = succ;
            }
        }
    }

    public boolean add(T item)
    {
        int key = item.hashCode();
        while (true)
        {
            Window<T> window = find(head, key);
            Node<T> prev = window.prev, curr = window.curr;
            
            Node<T> node = new Node<>(item);
            node.next = new AtomicMarkableReference<Node<T>>(curr, false);
            if (prev.next.compareAndSet(curr, node, false, false))
            {
                // System.out.println("Adding " + item.toString()+ "...");
                return true;
            }
            
        }
    }

    public boolean remove(T item)
    {
        int key = item.hashCode();
        boolean snip;

        while (true)
        {
            Window<T> window = find(head, key);
            Node<T> prev = window.prev, curr = window.curr;
            if (curr.key != key)
            {
                return false;
            }
            else
            {
                Node<T> succ = curr.next.getReference();
                snip = curr.next.attemptMark(succ, true);
                if (!snip)
                    continue;
                prev.next.compareAndSet(curr, succ, false, false);
                return true;
            }
        }
    }

    public boolean contains(T item)
    {
        boolean[] marked = {false};
        int key = item.hashCode();
        Node<T> curr = head;
        while (curr.key < key)
        {
            curr = curr.next.getReference();
            Node<T> succ = curr.next.get(marked);
        }
        return curr.key == key && !marked[0];
    }

    public void print()
    {
        Node<T> curr = head;
        System.out.print("List: ");
        while (curr != null)
        {
            if (curr == head || curr == tail)
            {
                curr = curr.next.getReference();
            }
            else
            {
                System.out.print(curr.item.toString() + " ");
                curr = curr.next.getReference();
            }
        }
        System.out.println();
    }
}

class Window<T extends Comparable<T>>
{
    public Node<T> prev, curr;

    Window (Node<T> prev, Node<T> curr)
    {
        this.prev = prev;
        this.curr = curr;
    }
}


class Node<T extends Comparable<T>>
{
    T item;
    
    boolean marked = false;
    int key;

    AtomicMarkableReference<Node<T>> next = new AtomicMarkableReference<Node<T>>(null, false);

    Node(int key)
    {
        this.key = key;
    }

    Node(T item)
    {
        this.item = item;
        this.key = item.hashCode();
    }
}
