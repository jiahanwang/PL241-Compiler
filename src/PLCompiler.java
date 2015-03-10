import edu.uci.cs241.frontend.Parser;
import edu.uci.cs241.ir.BasicBlock;
import edu.uci.cs241.ir.Function;
import edu.uci.cs241.ir.Instruction;
import edu.uci.cs241.ir.types.InstructionType;
import edu.uci.cs241.optimization.CP;
import edu.uci.cs241.optimization.CSE;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hanplusplus on 3/6/15.
 */
public class PLCompiler {

    public static void main(String[] args) {
        try {
            for(int i = 1; i <= 32; i++) {

                // CFG Visualization for unoptimized IR
                PrintWriter unoptimized_pw = new PrintWriter("viz/unoptimized/test0"+String.format("%02d", i)+".dot");
                unoptimized_pw.println("digraph test0" + String.format("%02d", i) + " {");
                unoptimized_pw.println("node [shape=box]");

                // CFG Visualization for optimized IR
                PrintWriter optimized_cp_pw = new PrintWriter("viz/cp/test0"+String.format("%02d", i)+".cp.dot");
                optimized_cp_pw.println("digraph test0" + String.format("%02d", i) + " {");
                optimized_cp_pw.println("node [shape=box]");


                // CFG Visualization for optimized IR
                PrintWriter optimized_cse_pw = new PrintWriter("viz/cse/test0"+String.format("%02d", i)+".cse.dot");
                optimized_cse_pw.println("digraph test0" + String.format("%02d", i) + " {");
                optimized_cse_pw.println("node [shape=box]");

                /**
                 *
                 * STEP 1 : Parsing
                 *
                 * **/
                Parser parser = new Parser("tests/test0"+String.format("%02d", i)+".txt");
                /** Get all functions **/
                Function main = parser.computation();
                List<Function> funcs = new ArrayList<Function>();
                funcs.add(main);
                funcs.addAll(main.getFunctions());
                /** Print out all functions **/
                System.out.print("test0" + String.format("%02d", i) + ".txt" + "\n======================\n");
                for(Function func : funcs){
                    /* print out ir*/
                    System.out.print(func.name + ":\n");
                    System.out.print(func.ir);
                    System.out.print("-----------------------\n");
                    /* print out CFG */
                    boolean[] explored = new boolean[10000];
                    DFS_buildCFG(func.entry, unoptimized_pw, explored);
                    /* print out Def-Use Chain */
                    System.out.print(func.getDu());
                    System.out.print("***********************\n");

                    /**
                     *
                     * STEP 2 : Optimization
                     *
                     * **/
                    /** Copy Propagation **/
                    CP.performCP(func);
                    /* print out ir*/
                    System.out.print(func.name + ":\n");
                    System.out.print(func.ir);
                    System.out.print("-----------------------\n");
                    /* print out CFG */
                    explored = new boolean[10000];
                    DFS_buildCFG(func.entry, optimized_cp_pw, explored);
                    /* print out Def-Use Chain */
                    System.out.print(func.getDu());
                    System.out.print("***********************\n");

                    /** Common Subexpression Elimination **/
                    HashMap<InstructionType, ArrayList<Instruction>> anchor = new HashMap<InstructionType, ArrayList<Instruction>>();
                    CSE.recursiveCSE(func.entry, anchor);
                    for(Integer num : CSE.remove) {
                        func.ir.deleteInstruction(num);
                    }
                    CSE.reset();
                    /* print out ir*/
                    System.out.print(func.name + ":\n");
                    System.out.print(func.ir);
                    System.out.print("-----------------------\n");
                    /* print out CFG */
                    explored = new boolean[10000];
                    DFS_buildCFG(func.entry, optimized_cse_pw, explored);
                    /* print out Def-Use Chain */
                    System.out.print(func.getDu());
                    System.out.print("***********************\n");

                }
                System.out.print("======================\n\n\n");
                optimized_cp_pw.println("}");
                optimized_cp_pw.close();
                optimized_cse_pw.println("}");
                optimized_cse_pw.close();
                unoptimized_pw.println("}");
                unoptimized_pw.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void DFS_buildCFG(BasicBlock b, PrintWriter pw, boolean[] explored) {
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
            DFS_buildCFG(b.left, pw, explored);
        }
        if (b.right != null) {
            pw.println(b.id + " -> " + b.right.id);
            DFS_buildCFG(b.right, pw, explored);
        }
    }
}
