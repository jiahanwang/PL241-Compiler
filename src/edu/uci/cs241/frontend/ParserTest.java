package edu.uci.cs241.frontend;

import edu.uci.cs241.ir.*;
import edu.uci.cs241.ir.types.InstructionType;
import edu.uci.cs241.optimization.CSE;

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
            for(int i = 0; i <= 31; i++) {
                PrintWriter pw = new PrintWriter("viz/test0"+String.format("%02d", i)+".cfg.dot");
                pw.println("digraph test0"+String.format("%02d", i)+" {");
                pw.println("node [shape=box]");

                PrintWriter pw_dom = new PrintWriter("viz/test0"+String.format("%02d", i)+".dom.dot");
                pw_dom.println("digraph test0"+String.format("%02d", i)+" {");
                pw_dom.println("node [shape=box]");

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
                    // Apply CSE
                    HashMap<InstructionType, ArrayList<Instruction>> anchor = new HashMap<InstructionType, ArrayList<Instruction>>();
                    CSE.recursiveCSE(func.entry, anchor);

                    //if(func.predefined) continue;
                    // print out ir
                    System.out.print(func.name + ":\n");
                    System.out.print(func.ir);
                    System.out.print("-----------------------\n");

                    // print out CFG
                    DFS_buildCFG(func.entry, func.ir, pw, explored);
                    DFS_buildDom(func.entry, func.ir, pw_dom, explored_dom);
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
