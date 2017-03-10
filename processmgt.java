/* Programming Assignment 1
 * Author : Archit Atul Date
 * ID: 1001695
 * Date: 08/03/2017 */

package com.example;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class processmgt {

    static int finishedNodeNum = 0; // number of finished nodes

    public static void main(String[] args) {
        String graphFile = args[0]; // processmgt <graphFile argument>
        ArrayList<Node> nodes = generateNodeList(graphFile); // generate a node list
        runNodeList(nodes); // execute the nodes.
    }


    /**
     * Creating an array list of the Node Class
     */
    private static ArrayList<Node> generateNodeList(String graphFile) {

        ArrayList<Node> nodes = new ArrayList<>();
        try {
            FileReader input = new FileReader(graphFile); // read into the graph file
            BufferedReader br = new BufferedReader(input);
            String line;

            int id = 0; // id to be iterated every line in the while loop

            while ((line = br.readLine()) != null) { // while the line isn't empty (read the whole graph file to make a node array)
                String[] nodeArgs = line.split(":");

                ArrayList<Integer> childID = new ArrayList<>(); // integer array for the id's of every child node

                for (String child: nodeArgs[1].split(" ")) { // child node id separated by a space in nodeArgs[1]
                    if (child.equals("none")) {
                        childID = null; // null will be passed if the string in graph is none
                        break;
                    } else {
                        try {
                            childID.add(Integer.parseInt(child)); // add integer to the child array
                        } catch (NumberFormatException e) {
                            System.out.println("Child ID is not an integer"); // file is not made properly since the child node id is not an integer
                        }
                    }
                } // all children for all nodes written

                try {
                    String program = nodeArgs[0];
                    String in = nodeArgs[2];
                    String out = nodeArgs[3];
                    nodes.add(new Node(id, program, childID, in, out)); // id , command , child id Array , input , output
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Node args array has some info missing");
                } catch (Exception e) {
                    System.out.println("Some other error while making a node at id: "+ id); // error debugging
                }
                id++; // increment id for the next node in graph file
            }
            input.close(); // close the reader

        } catch (FileNotFoundException e) {
            System.out.println("Directory does not exist");
        } catch (IOException e) {
            System.out.println("IO Exception while generating node list");
            // e.printStackTrace();
        }

        try {
            for (Node node: nodes) {
                if (node.getchildID() != null) {
                    for (Integer childrenID: node.getchildID()) {
                        nodes.get(childrenID).addParent(node.getId()); // iterate through nodes and add the node as the parent to its children.
                    }
                }
            } // adding parent to ever node
        } catch (Exception e) {
            System.out.println("There exist no children for this node"); // if there are no child nodes
        }

        for (Node node: nodes) {
            if (node.getParentNumber() == 0) { // number of parents
                node.setState(Node.RUNNABLE); // if there are no parents its the root node (runnable by default)
            }
        }
        return nodes;
    }


    /**
     * run the Node Array list
     */
    private static void runNodeList(ArrayList<Node> nodes) {

        ArrayList<pbThread> threadList = new ArrayList<>();

        while (finishedNodeNum < nodes.size()) { // loop run till every node is finished
            for (Iterator<pbThread> i = threadList.iterator(); i.hasNext();) { // check every pbThread for the flag
                pbThread thread = i.next();
                if (thread.getFlag()) {
                    try {
                        thread.join(); // join thread if flag is true
                    } catch (InterruptedException e) {
                        System.out.println("Thread process interrupted"); // interrupting thread process exception
                    }
                    i.remove(); // remove i
                }
            }

            for (Node node: nodes) {
                node.checkState(); // check state of nodes (if executed or not)
            }

            for (Node node: nodes) {
                if (node.getState() == Node.RUNNABLE) { // check if a node is RUNNABLE
                    System.out.println("Node ID: " + node.getId() + " running."); // start running the node
                    pbThread newThread = new pbThread(node, nodes); // new pbThread constructor with the runnable node, node list and current directory
                    newThread.start(); // start Thread and add it to the threadList
                    threadList.add(newThread);
                    node.setState(Node.RUNNING); // set state of running
                }
            }
        }

        System.out.println("Finished traversing the DAG nodes"); // running finished after while loop
    }
}


/**
 * pbThread extending Thread to run Nodes
 */
class pbThread extends Thread {

    private ArrayList<Node> nodes;  // set a reference to the list of nodes
    private Node currentNode;
    private boolean flag = false;   // finished flag, being checked before thread is joined (true for thread finishing)

    /**
     * Constructor
     */
    public pbThread(Node currentNode, ArrayList<Node> nodes) {
        this.currentNode = currentNode;
        this.nodes = nodes;
    }

    public void run() {
        try {
            /**
             * Process builder commands.
             */
            
            String input = currentNode.getInput();
            String command = currentNode.getCommand();
            if (!input.equals("stdin")){
                command = currentNode.getCommand() + " " + input;
            }
            ProcessBuilder pb = new ProcessBuilder();
            pb.command(command.split(" "));
            pb.redirectOutput(new File(currentNode.getOutput()));
            pb.start();

            System.out.println("Node ID: " + currentNode.getId() + " process done."); // print every time the process finishes

            synchronized (this) { // setting states to finished and incrementing number of parents done for the node must be atomic
                currentNode.setState(Node.DONE);
                if (currentNode.getchildID() != null) {
                    for (Integer childrenID: currentNode.getchildID()) {
                        nodes.get(childrenID).incrementParentNumberDone(); // informing the child that one more parent is done
                    }
                }

                processmgt.finishedNodeNum++; // add to the total number of finished nodes
                System.out.println("Finished Nodes: " + processmgt.finishedNodeNum);

                flag = true; // setting the finished flag to true (for pbThread to be joined)
            }

        } catch (Exception ex) {
            System.out.println("Program did not run successfully. This may impede other nodes from executing.");
        }
    }

    public boolean getFlag() {
        return flag;
    }
}


/**
 * This class simulates a node in a graph.
 * It contains relevant information regarding its relationship with other nodes such as parent nodes, child nodes.
 * It also contains program information such as the command, input file, output file and most importantly, its current State.
 */
class Node {

    public static final char IDLE = 'i'; // i = idle
    public static final char RUNNABLE = 'q'; // q = in queue
    public static final char RUNNING = 'r'; // r = running
    public static final char DONE = 'f'; // f = finish


    private int id;
    private String command; // program name with arguments
    private String input;   // input file
    private String output;  // output file

    private ArrayList<Integer> parentIDs;   // ID of parent nodes
    private ArrayList<Integer> childID; // ID of children nodes

    private int ParentNumberDone = 0;    // number of parent nodes done executing

    private int State = 0; // set state as idle state at start

    public Node(int id, String command, ArrayList<Integer> childID, String input, String output) {
        this.id = id;
        this.command = command;
        this.input = input;
        this.output = output;
        this.childID = childID;
        this.parentIDs = new ArrayList<>();
    }

    public void checkState() {
        if (ParentNumberDone == parentIDs.size() && State == IDLE) {
            setState(RUNNABLE);
        }
    }

    public void addParent(int parentID) {
        parentIDs.add(parentID);
    }

    public void incrementParentNumberDone() {
        ParentNumberDone++;
    }

    public int getId() {
        return id;
    }

    public String getCommand() {
        return command;
    }

    public int getParentNumber() {
        return parentIDs.size();
    }

    public ArrayList<Integer> getchildID() {
        return childID;
    }

    public String getInput() {
        return input;
    }

    public String getOutput() {
        return output;
    }

    public int getState() {
        return State;
    }

    public void setState(char State) {
        this.State = State;
    }
}