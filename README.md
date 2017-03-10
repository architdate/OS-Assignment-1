Process Management (DAG Traversal)
========================================================================

Contents: **Purpose of the program** | **How to compile the program** | **What exactly the program does** 


Introduction
------------------------------------------------------------------------

This is the source code of a process management tree for traversing a DAG of processes (Directed Acyclic Graph).

This repository contains the files needed to be able to traverse through a DAG of processes. Note that you will require an Ubuntu OS to run the java program since the system calls are ones which are based of a UNIX shell.

You can use your own computer as an operating system provided that you have a [virtual machine][1] setup for Ubuntu.

  [1]: https://betanews.com/2012/08/29/how-to-install-ubuntu-on-vmware-workstation/


Installing
------------------------------------------------------------------------

In bash:

    $javac processmgt.java

(Requires bash)


Detailed installation instructions
------------------------------------------------------------------------

To run this program you need to have an Ubuntu system or an Ubuntu virtual machine

Next, obtain a copy of this code and make sure that you keep the processmgt.java file in the same location as your graph file

Now in bash type the following command:

    cd LOCATION

Replace `LOCATION` with the location processmgt.java is in. (for example : "cd Desktop")

This will set your command line's location to processmgt.java's folder. You'll have to do this each time you open a command line to run commands for processmgt.java


Congratulations, you're done setting up processmgt.java

Now, to compile processmgt.java, run the command:

    javac processmgt.java


You can run the graph-file by running:

    java processmgt graph-file



Setting up a graph-file to run
------------------------------------------------------------------------

Once your processmgt.java has compiled, you will need a graph-file for it to run.

To make a proper graph file as shown below:

    sleep 10:1:stdin:stdout
    echo "Process P1 running. Dependency to P4 is cleared.":4:stdin:out1.txt
    sleep 15:3:stdin:stdout
    echo "Process P3 running. Dependency to P4 is cleared.":4:stdin:out2.txt
    cat out1.txt out2.txt:5:stdin:cat-out.txt
    grep 3:6:cat-out.txt:grep-out.txt
    wc -l:none:grep-out.txt:wc-out.txt

Make sure that your graph-file is in the format of `<program name with arguments:list of children ID's:input file:output file>`.

Make sure that your graph file is legal according to the above format. Otherwise the program will not run properly.

Once that is done you can now run the above program with the `java processmgt graph-file` command.


Program Working
------------------------------------------------------------------------

There are 3 major processes that go on to make the program work as it does.

The program reads the `graph-file` line by line separating the node arguments by `:` and identifying the inputs, outputs, commands that are supposed to be run at every stage as well as the child nodes.

Keeping this in mind it forms an Array of nodes based on the graph-file. These nodes have attributes of parent node id's, child node id's so that the functioning of the DAG is maintained intact.

Now every node in the array is iterated through and checked for "RUNNABLE" as its state and is run using pbThreads (an extention of the java thread class)

Once all nodes have the state as "DONE" the process is declared completed and the program ends.



Credits
------------------------------------------------------------------------

Author

- Archit Date [1001695]
