package tinyjs;

import java.util.Stack;

/**
 * @author Alexy
 */
public class TinyJS {

    public static void main(String[] args) {
        Parser p = new Parser("example.txt");
        Node.Block root = p.createAST();
        System.out.println("Interpreting the program:");
        root.interpreterNode(new Stack<Env>());
    }

}
