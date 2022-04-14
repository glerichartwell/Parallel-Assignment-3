# Assignment 3 #
Glenn Hartwell  
COP4520
Spring 2022

## How to compile and run ##

### Problem 1 ###
You can compile and run the solution for problem 1 by running the following commands

    $ javac MinotaurPresents.java 
    $ java MinotaurPresents

The program will terminate and print the number of thank you cards written by Servants.

### Problem 2 ###

You can compile and run the solution for problem 1 by running the following commands

    $ javac Rover.java 
    $ java Rover

The program will terminate and print the reports for each hour (24 hours).

## Solution Descriptions ##

### Problem 1 ###

The original linked list that the servants used is not suited for concurrent work. If they knew there were more presents than thank you cards written then that means that some presents were lost during the adding or removal process. This would be possible if two servants try to make changes to the same node at the same time. This problem can be solved by creating a linked list that can edited concurrently.

I used the `LockFreeList` implementation from the Art of Multiprocessor Programming giving it the abilty to take any class that is `Comparable`. This allowed me to make my own `Comparable` `Present` class which I used to simulate this scenario. I also create a `Servant` class as my `Runnable` since each thread is supposed to represent a `Servant`. Each `Servant` spawned will continue to create `Present`s (this has the same unordered property as pulling them from the bag) add them to the chain, and then remove that `Present` from the chain until the number of thank you cards written is equal to the total number of `Presents` that were supposed to be there. The number of thank you cards is tracked by an `AtomicInteger` that is shared by all of the `Servants`. Since the Minotaur is able to call on his `Servants` to see if a present with a specific tag is in the list, I wrote each `Servant` to have a 33% chance of running the `contains` method to search for a random `Present` to simulate the request.

The `LockFreeList` from the book has a lock-free progress guarantee for `add` and `remove` and a `wait-free` progress guarantee for `contains`.

### Problem 2 ###

My solution for problem 2 also uses the `LockFreeList` implementation from the textbook. I created two class files `Rover` and `Sensor`, a `Runnable`, to simulate the purpose of the problem we're solving. The `main` method of `Rover` is where the `Sensors` are created and begin doing their work collecting temperatures. The `Sensors` are set to run for `NUM_HOURS` iterations determined by the `Rover` to simulate hours with another loop that creates random temperatures 60 times to simulate the 60 minutes in an hour. Two classes `TempReading` and `TimeReading` are used to hold these temperatures as well as the hour they were recorded and the minute of the day they were recorded. These classes are in the `Sensor` file because they are specific to the `Sensor`. 

Once a `Sensor` has completed its temperature readings a `CyclicBarrier` is used to make sure a `Report` is not generated unless all temperature `Sensor`s have finished recording for the hour. The `Rover` generates a report by accessing the different `LockFreeList`s used to store readings ordered by temperature and time. These lists will not alter past readings so the `Rover` does not need to worry about accessing a node that does not exist. The data structures that are used to generate the reports do not need to be thread safe as they are never accessed by anything other than `main`. An `ArrayDeque` was chosen because of its O(1) access to the head and tail. Once a report has begun being generated the `Sensor`s are free to continue taking temperature measurements for the next hour.

The `LockFreeList` from the book has a lock-free progress guarantee for `add` and `remove` and a `wait-free` progress guarantee for `contains`.

Finding `CyclicBarrier` to use to make sure that `Sensor`s did not wait for `main` to finish generating a `Report` was the easiest method. I also attempted to use `AtomicReferenceArray`s holding `Boolean` objects but this proved to be much more difficult.

I also implemented the `LazyList` from the textbook, but I got the `LockFreeList` working before the `LazyList` because I noticed a bug in my `Node` class.