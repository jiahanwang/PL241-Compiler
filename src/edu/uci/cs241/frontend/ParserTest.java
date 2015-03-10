package edu.uci.cs241.frontend;

import edu.uci.cs241.ir.*;
import edu.uci.cs241.ir.types.InstructionType;
import edu.uci.cs241.optimization.CP;
import edu.uci.cs241.optimization.CSE;
import edu.uci.cs241.optimization.RegisterAllocator;
import org.java.algorithm.graph.basics.Node;
import org.java.algorithm.graph.basics.SimpleGraph;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ivan on 2/9/2015.
 */
public class ParserTest {

    public static void main(String[] args) {
        try {
            Parser parser;
            for(int i = 1; i <= 1; i++) {
                PrintWriter pw = new PrintWriter("viz/test0"+String.format("%02d", i)+".cse.cfg.dot");
                pw.println("digraph test0"+String.format("%02d", i)+" {");
                pw.println("node [shape=box]");

                PrintWriter pw_dom = new PrintWriter("viz/test0"+String.format("%02d", i)+".cse.dom.dot");
                pw_dom.println("digraph test0"+String.format("%02d", i)+" {");
                pw_dom.println("node [shape=box]");

                PrintWriter pw_ig = new PrintWriter("viz/test0"+String.format("%02d", i)+".cse.ig.dot");
                pw_ig.println("graph test0"+String.format("%02d", i)+" {");
                pw_ig.println("node [shape=circle]");

                parser = new Parser("tests/test0"+String.format("%02d", i)+".txt");
                /** Get all functions **/
                Function main = parser.computation();
                List<Function> funcs = new ArrayList<Function>();
                funcs.add(main);
                funcs.addAll(main.getFunctions());
                boolean[] explored = new boolean[1000000];
                boolean[] explored_dom = new boolean[1000000];
                /** Print out all functions **/
                System.out.print("test0" + String.format("%02d", i) + ".txt" + "\n======================\n");
                for(Function func : funcs){
                    // No optimizations
//                    System.out.print(func.name + ":\n");
//                    System.out.print(func.ir);
//                    System.out.print("-----------------------\n");
                    /* Copy Propagation */
                    CP.performCP(func);
                    /* print out ir*/
//                    System.out.print(func.name + ":\n");
//                    System.out.print(func.ir);
//                    System.out.print("-----------------------\n");
                    // Apply CSE
                    HashMap<InstructionType, ArrayList<Instruction>> anchor = new HashMap<InstructionType, ArrayList<Instruction>>();
                    CSE.recursiveCSE(func.entry, anchor);
                    for(Integer num : CSE.remove) {
                        func.ir.deleteInstruction(num);
                    }
                    CSE.reset();

                    //if(func.predefined) continue;
                    // print out ir
                    System.out.print(func.name + ":\n");
                    System.out.print(func.ir);
                    System.out.print("-----------------------\n");

                    // print out CFG
                    DFS_buildCFG(func.entry, func.ir, pw, explored);
                    DFS_buildDom(func.entry, func.ir, pw_dom, explored_dom);

                    DefUseChain du = func.getDu();
                    System.out.println(du.toString());

                    // Register Allocator
                    RegisterAllocator reg = new RegisterAllocator(func);
                    reg.buildLiveRanges();
                    reg.printLiveRanges();
                    reg.buildIG();
                    SimpleGraph<Node, String> sg = reg.getIG();
                    for(Node n : sg.getVertices()) {
                        pw_ig.println(n.getId() + "[label=\"[" + n.getId() +
                                "]\ncost: "+n.cost+
                                "\ndegree: "+sg.adjacentVertices(n).size()+
                                "\"]");
                    }
                    pw_ig.println("}");
                    pw_ig.close();
                    for(String edge : sg.getEdges()) {
                        pw_ig.println(edge);
                    }
                    reg.allocateRegisters();
                    reg.printRegMap();
                    reg.replaceInstructions();
                    System.out.print(func.name + ":\n");
                    System.out.print(func.ir);
                    System.out.print("-----------------------\n");
                    reg.reset();
                    //TODO: reset for reg alloc

                }
                System.out.print("======================\n\n\n");
                pw.println("}");
                pw.close();
                pw_dom.println("}");
                pw_dom.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void DFS_buildCFG(BasicBlock b, IR ir, PrintWriter pw, boolean[] explored) {
        if(explored[b.id]) {
            return;
        }
        if (b != null) {
            String output = b.getStart() == Integer.MIN_VALUE ? b.name : b.toStringOfInstructions();
            pw.println(b.id + "[label=\"" + output + "\"]");
            explored[b.id] = true;
        }
        if (b.left != null) {
            pw.println(b.id + " -> " + b.left.id);
            DFS_buildCFG(b.left, ir, pw, explored);
        }
        if (b.right != null) {
            pw.println(b.id + " -> " + b.right.id);
            DFS_buildCFG(b.right, ir, pw, explored);
        }
    }

    public static void DFS_buildDom(BasicBlock b, IR ir, PrintWriter pw, boolean[] explored) {
        if(explored[b.id]) {
            return;
        }
        if (b != null) {
            String output = b.getStart() == Integer.MIN_VALUE ? b.name : b.toStringOfInstructions();
            pw.println(b.id + "[label=\"" + output + "\"]");
            explored[b.id] = true;

            for(BasicBlock d : b.dom) {
                pw.println(b.id + " -> " + d.id);
                DFS_buildDom(d, ir, pw, explored);
            }
        }

    }
}
