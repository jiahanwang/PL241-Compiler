import edu.uci.cs241.frontend.Parser;
import edu.uci.cs241.ir.BasicBlock;
import edu.uci.cs241.ir.Function;
import edu.uci.cs241.ir.Instruction;
import edu.uci.cs241.ir.types.InstructionType;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hanplusplus on 3/6/15.
 */
public class PLCompiler {

    public static void main(String[] args) {
        try {
            for(int i = 0; i <= 0; i++) {
                PrintWriter pw = new PrintWriter("viz/test0"+String.format("%02d", i)+".dot");
                pw.println("digraph test0"+String.format("%02d", i)+" {");
                pw.println("node [shape=box]");
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
                boolean[] explored = new boolean[1000000];
                /** Print out all functions **/
                System.out.print("test0" + String.format("%02d", i) + ".txt" + "\n======================\n");
                for(Function func : funcs){

//                    Instruction in = new Instruction(InstructionType.ADD);
//                    in.parent = func.entry.left.left.right;
//                    func.ir.insertInstruction(in, 13);
//                    func.ir.deleteInstruction(14);

                    // print out ir
                    System.out.print(func.name + ":\n");
                    System.out.print(func.ir);
                    System.out.print("-----------------------\n");

                    // print out CFG
                    DFS_buildCFG(func.entry, pw, explored);
                }
                System.out.print("======================\n\n\n");
                pw.println("}");
                pw.close();

                /**
                 *
                 * STEP 2 : Optimization
                 *
                 * **/
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
