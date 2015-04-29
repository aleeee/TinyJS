package tinyjs;

import tinyjs.Token.Tokens;
import java.util.ArrayList;
import java.util.Stack;

abstract public class Node {

    public enum InfoResult {

        NULL, CONTINUE, BREAK, CASEEXECUTED
    };

    public Node() {
    }

    abstract public TypeContainer<Evaluate, InfoResult> interpreterNode(Stack<Env> envStack);

    abstract public static class Statement extends Node {

        public Statement() {
        }
    }

    abstract public static class Expression extends Statement {

        public Expression() {
        }
    }

    abstract public static class Operation extends Expression {

        public enum OperationType {

            OR, AND, NOT, EQUAL, INEQUAL, GREATER, GREATEREQ,
            LESS, LESSEQ, PLUS, MINUS, MUL, DIV
        };
        protected OperationType type;

        public Operation(Tokens op) {
            this.type = ConvertType(op);
        }

        public static OperationType ConvertType(Tokens op) {
            switch (op) {
                case OR:
                    return OperationType.OR;
                case AND:
                    return OperationType.AND;
                case NOT:
                    return OperationType.NOT;
                case EQUAL:
                    return OperationType.EQUAL;
                case INEQUAL:
                    return OperationType.INEQUAL;
                case GREATER:
                    return OperationType.GREATER;
                case GREATEREQ:
                    return OperationType.GREATEREQ;
                case LESS:
                    return OperationType.LESS;
                case LESSEQ:
                    return OperationType.LESSEQ;
                case PLUS:
                    return OperationType.PLUS;
                case MINUS:
                    return OperationType.MINUS;
                case MUL:
                    return OperationType.MUL;
                case DIV:
                    return OperationType.DIV;
                default:
                    System.out.println("Failed conversion the operation token.");
                    System.exit(1);
                    return null;
            }
        }
    }

    public static class Assign extends Statement {

        String ide;
        Node.Expression valueNode;

        public Assign(String ide, Node.Expression valueN) {
            this.ide = ide;
            this.valueNode = valueN;
        }

        @Override
        public TypeContainer<Evaluate, InfoResult> interpreterNode(Stack<Env> envStack) {
            Env env = envStack.peek();
            Evaluate value = this.valueNode.interpreterNode(envStack).Item1;
            env.bind(ide, value, false);
            return new TypeContainer<>(null, InfoResult.NULL);
        }
    }

    public static class BinaryOperation extends Operation {

        private Expression leftNode;
        private Expression rightNode;

        public BinaryOperation(Tokens op, Node.Expression left, Node.Expression right) {
            super(op);
            this.leftNode = left;
            this.rightNode = right;
        }

        @Override
        public TypeContainer<Evaluate, InfoResult> interpreterNode(Stack<Env> envStack) {
            Evaluate value = null;
            Evaluate leftResult = this.leftNode.interpreterNode(envStack).Item1;
            Evaluate rightResult = this.rightNode.interpreterNode(envStack).Item1;

            switch (leftResult.type) {
                case INT:
                    int iLV = leftResult.getIValue();
                    int iRV = rightResult.getIValue();

                    switch (this.type) {
                        case EQUAL:
                            value = new Evaluate(iLV == iRV);
                            break;
                        case INEQUAL:
                            value = new Evaluate(iLV != iRV);
                            break;
                        case GREATER:
                            value = new Evaluate(iLV > iRV);
                            break;
                        case GREATEREQ:
                            value = new Evaluate(iLV >= iRV);
                            break;
                        case LESS:
                            value = new Evaluate(iLV < iRV);
                            break;
                        case LESSEQ:
                            value = new Evaluate(iLV <= iRV);
                            break;
                        case PLUS:
                            value = new Evaluate(iLV + iRV);
                            break;
                        case MINUS:
                            value = new Evaluate(iLV - iRV);
                            break;
                        case MUL:
                            value = new Evaluate(iLV * iRV);
                            break;
                        case DIV:
                            if (iRV == 0) {
                                System.out.println(" Error Can not divide by zero ..");
                                System.exit(1);
                            } else {
                                value = new Evaluate(iLV / iRV);
                            }
                            break;
                    }
                    break;

                case BOOL:
                    boolean bLV = leftResult.getBValue();
                    boolean bRV = rightResult.getBValue();

                    switch (this.type) {
                        case OR:
                            value = new Evaluate(bLV || bRV);
                            break;
                        case AND:
                            value = new Evaluate(bLV && bRV);
                            break;
                        case EQUAL:
                            value = new Evaluate(bLV == bRV);
                            break;
                        case INEQUAL:
                            value = new Evaluate(bLV != bRV);
                            break;
                    }
                    break;

                case STRING:
                    String sLV = leftResult.getSValue();
                    String sRV = rightResult.getSValue();

                    if (this.type == OperationType.PLUS) {
                        value = new Evaluate(sLV.concat(sRV));
                    }
                    break;
            }

            return new TypeContainer<>(value, InfoResult.NULL);
        }
    }

    public static class Block extends Node {

        public ArrayList<Node.Statement> Children = new ArrayList<>();

        public Block() {
        }

        @Override
        public TypeContainer<Evaluate, InfoResult> interpreterNode(Stack<Env> envStack) {
            TypeContainer<Evaluate, InfoResult> pair = new TypeContainer<>();
            Block.newActivationRecord(envStack);
            for (Node.Statement child : this.Children) {
                pair = child.interpreterNode(envStack);
                if ((pair.Item1 != null) || (pair.Item2 == InfoResult.CONTINUE) || (pair.Item2 == InfoResult.BREAK)) {
                    break;
                }
            }
            envStack.pop();
            return pair;
        }

        public static void newActivationRecord(Stack<Env> envStack) {
            boolean created = false;
            if (!envStack.isEmpty() && envStack.peek().alreadyBeenCreated) {
                envStack.peek().alreadyBeenCreated = false;
                created = true;
            }
            if (!created) {
                Env env = new Env();
                if (envStack.isEmpty()) {
                    env.Parent = null;
                } else {
                    env.Parent = envStack.peek();
                }
                envStack.push(env);
            }
        }
    }

    public static class Break extends Statement {

        public Break() {
        }

        @Override
        public TypeContainer<Evaluate, InfoResult> interpreterNode(Stack<Env> envStack) {
            return new TypeContainer<>(null, InfoResult.BREAK);
        }
    }

    public static class Call extends Expression {

        private String ide;
        private ArrayList<Node.Expression> actualParameters;
        private boolean itIsStm = false;

        public Call(String ide, ArrayList<Node.Expression> ap, boolean itIsStm) {
            this.ide = ide;
            this.actualParameters = ap;
            this.itIsStm = itIsStm;
        }

        @Override
        public TypeContainer<Evaluate, InfoResult> interpreterNode(Stack<Env> envStack) {
            Env env = envStack.peek();
            ArrayList<Evaluate> actualValues = new ArrayList<>();

            for (Node.Expression e : this.actualParameters) {
                actualValues.add(e.interpreterNode(envStack).Item1);
            }

            Evaluate funVal = env.apply(this.ide);
            Node.Function fun = funVal.getFValue();
            Evaluate res = fun.call(envStack, actualValues);
            if (this.itIsStm) {
                return new TypeContainer<>(null, InfoResult.NULL);
            } else {
                return new TypeContainer<>(res, InfoResult.NULL);
            }
        }
    }

    public static class Case extends Statement {

        private String ide;
        private Constant valueNode;
        private Block bodyNode;
        public boolean AlwaysExecute = false;

        public Case(String ide, Node.Constant v, Node.Block body) {
            this.ide = ide;
            this.valueNode = v;
            this.bodyNode = body;
        }

        @Override
        public TypeContainer<Evaluate, InfoResult> interpreterNode(Stack<Env> envStack) {
            
            if (this.valueNode == null) {
                return this.bodyNode.interpreterNode(envStack);
            } else {
                Env env = envStack.peek();
                Evaluate v = env.apply(this.ide);
                if (v.value == null) {
                    System.out.println("Variable \"" + this.ide + "is UNDEFINED");
                    System.exit(1);
                  
                }

                if (AlwaysExecute || Evaluate.equal(this.valueNode.interpreterNode(envStack).Item1, v)) {
                    TypeContainer<Evaluate, InfoResult> p = this.bodyNode.interpreterNode(envStack);
                    if (p.Item2 != InfoResult.BREAK) {
                        p.Item2 = InfoResult.CASEEXECUTED;
                    }
                    return p;
                } else {
                    return new TypeContainer<>(null, InfoResult.NULL);
                }
            }
        }
    }

    public static class Constant extends Expression {

        private Evaluate value;

        public Constant(Tokens t, String v) {
            switch (t) {
                case NUMBER:
                    this.value = new Evaluate(Integer.parseInt(v));
                    break;
                case TRUE:
                    this.value = new Evaluate(true);
                    break;
                case FALSE:
                    this.value = new Evaluate(false);
                    break;
                case STRING:
                    this.value = new Evaluate(v);
                    break;
            }
        }

        @Override
        public TypeContainer<Evaluate, InfoResult> interpreterNode(Stack<Env> envStack) {
            return new TypeContainer<>(this.value, InfoResult.NULL);
        }
    }

    public static class Continue extends Statement {

        public Continue() {
        }

        @Override
        public TypeContainer<Evaluate, InfoResult> interpreterNode(Stack<Env> envStack) {
            return new TypeContainer<>(null, InfoResult.CONTINUE);
        }
    }

    public static class Declaration extends Statement {

        private String ide;
        private Node.Expression valueNode;

        public Declaration(String ide, Node.Expression valueN) {
            this.ide = ide;
            this.valueNode = valueN;
        }

        @Override
        public TypeContainer<Evaluate, InfoResult> interpreterNode(Stack<Env> envStack) {
            Env env = envStack.peek();
            Evaluate value;

            if (this.valueNode != null) {
                value = this.valueNode.interpreterNode(envStack).Item1;
            } else {
                value = new Evaluate();
            }

            env.bind(this.ide, value, true);
            return new TypeContainer<>(null, InfoResult.NULL);
        }
    }

    public static class EvalN extends Expression {

        private Node val = null;

        public EvalN(Node v) {
            this.val = v;
        }

        @Override
        public TypeContainer<Evaluate, InfoResult> interpreterNode(Stack<Env> envStack) {
            return new TypeContainer<>(null, InfoResult.NULL);
        }
    }

    public static class For extends Statement {

        private Statement stmNode1;
        private Expression condNode;
        private Statement stmNode2;
        private Block bodyNode;

        public For(Statement stm1, Expression condN, Statement stm2, Block body) {
            this.stmNode1 = stm1;
            this.condNode = condN;
            this.stmNode2 = stm2;
            this.bodyNode = body;
        }

        @Override
        public TypeContainer<Evaluate, InfoResult> interpreterNode(Stack<Env> envStack) {
            this.stmNode1.interpreterNode(envStack);
            return InterpreterNode2(envStack);
        }

        private TypeContainer<Evaluate, InfoResult> InterpreterNode2(Stack<Env> envStack) {
            if (this.condNode.interpreterNode(envStack).Item1.getBValue()) {
                TypeContainer<Evaluate, InfoResult> v = this.bodyNode.interpreterNode(envStack);

                if (v.Item2 == InfoResult.CONTINUE) {
                    v.Item2 = InfoResult.NULL;
                }
                if ((v.Item1 != null) || (v.Item2 == InfoResult.BREAK)) {
                    return new TypeContainer<>(v.Item1, InfoResult.NULL);
                } else {
                    this.stmNode2.interpreterNode(envStack);
                    return this.InterpreterNode2(envStack);
                }
            } else {
                return new TypeContainer<>(null, InfoResult.NULL);
            }
        }
    }

    public static class Function extends Statement {

        private String ide;
        public ArrayList<String> FormalParameters;
        public Node.Block BodyNode;
        public Evaluate ReturnValue = null;

        public Function(String ide, ArrayList<String> fp, Block bodyN) {
            this.ide = ide;
            this.FormalParameters = fp;
            this.BodyNode = bodyN;
        }

        public Evaluate call(Stack<Env> envStack, ArrayList<Evaluate> args) {
            Env env = new Env();
            env.alreadyBeenCreated = true;
            if (envStack.isEmpty()) {
                env.Parent = null;
            } else {
                env.Parent = envStack.peek();
            }
            for (int i = 0; i < args.size(); i++) {
                env.bind(this.FormalParameters.get(i), args.get(i), true);
            }
            envStack.push(env);

            return this.BodyNode.interpreterNode(envStack).Item1;
        }

        @Override
        public TypeContainer<Evaluate, InfoResult> interpreterNode(Stack<Env> envStack) {
            Env env = envStack.peek();
            env.bind(this.ide, new Evaluate(this), true);
            return new TypeContainer<>(null, InfoResult.NULL);
        }
    }

    public static class IF extends Expression {

        private Node.Expression condNode;
        private Node.Block thenNode;
        private Node.Block elseNode;

        public IF(Node.Expression condN, Node.Block thenN, Node.Block elseN) {
            this.condNode = condN;
            this.thenNode = thenN;
            this.elseNode = elseN;
        }

        @Override
        public TypeContainer<Evaluate, InfoResult> interpreterNode(Stack<Env> envStack) {
            if (this.condNode.interpreterNode(envStack).Item1.getBValue()) {
                return this.thenNode.interpreterNode(envStack);
            } else if (this.elseNode != null) {
                return this.elseNode.interpreterNode(envStack);
            } else {
                return new TypeContainer<>(null, InfoResult.NULL);
            }
        }
    }

    public static class Print extends Statement {

        private Node.Expression valueNode;

        public Print(Node.Expression valueN) {
            this.valueNode = valueN;
        }

        @Override
        public TypeContainer<Evaluate, InfoResult> interpreterNode(Stack<Env> envStack) {
            Evaluate result = this.valueNode.interpreterNode(envStack).Item1;
            System.out.println(result.value.toString());
            return new TypeContainer<>(null, InfoResult.NULL);
        }
    }

    public static class Return extends Statement {

        private Expression valueNode;

        public Return(Expression valueN) {
            this.valueNode = valueN;
        }

        @Override
        public TypeContainer<Evaluate, InfoResult> interpreterNode(Stack<Env> envStack) {
            return this.valueNode.interpreterNode(envStack);
        }
    }

    public static class Switch extends Statement {

        private String ide;
        private ArrayList<Node.Case> caseNodes;

        public Switch(String ide, ArrayList<Case> cases) {
            this.ide = ide;
            this.caseNodes = cases;
        }

        @Override
        public TypeContainer<Evaluate, InfoResult> interpreterNode(Stack<Env> envStack) {
            TypeContainer<Evaluate, InfoResult> pair = new TypeContainer<>();
            boolean executed = false;
            for (Node.Case caseNode : this.caseNodes) {
                if (executed) {
                    caseNode.AlwaysExecute = true;
                    executed = false;
                }
                pair = caseNode.interpreterNode(envStack);
                if (pair.Item2 == InfoResult.CASEEXECUTED) {
                    executed = true;
                }
                if ((pair.Item1 != null) || (pair.Item2 == InfoResult.BREAK)) {
                    break;
                }
            }
            pair.Item2 = InfoResult.NULL;
            return pair;
        }
    }

    public static class UnaryOperation extends Operation {

        private Expression node;

        public UnaryOperation(Tokens op, Expression v) {
            super(op);
            this.node = v;
        }

        @Override
        public TypeContainer<Evaluate, InfoResult> interpreterNode(Stack<Env> envStack) {
            Evaluate value = null;
            Evaluate childResult = this.node.interpreterNode(envStack).Item1;
            switch (childResult.type) {
                case INT:
                    int iV = childResult.getIValue();
                    if (this.type == OperationType.MINUS) {
                        value = new Evaluate(-iV);
                    }
                    break;

                case BOOL:
                    boolean bV = childResult.getBValue();
                    if (this.type == OperationType.NOT) {
                        value = new Evaluate(!bV);
                    }
                    break;
            }
            return new TypeContainer<>(value, InfoResult.NULL);
        }
    }

    public static class Var extends Expression {

        String ide;

        public Var(String ide) {
            this.ide = ide;
        }

        @Override
        public TypeContainer<Evaluate, InfoResult> interpreterNode(Stack<Env> envStack) {
            Env env = envStack.peek();
            Evaluate v = env.apply(this.ide);
            if (v.value == null) {
                System.out.println("Variable \"" + this.ide + "\" is UNDEFINED");
                System.exit(1);
            }
            return new TypeContainer<>(v, InfoResult.NULL);
        }
    }

    public static class While extends Statement {

        private Expression condNode;
        private Block bodyNode;

        public While(Node.Expression condN, Node.Block bodyN) {
            this.condNode = condN;
            this.bodyNode = bodyN;
        }

        @Override
        public TypeContainer<Evaluate, InfoResult> interpreterNode(Stack<Env> envStack) {
            if (this.condNode.interpreterNode(envStack).Item1.getBValue()) {
                TypeContainer<Evaluate, InfoResult> v = this.bodyNode.interpreterNode(envStack);

                if (v.Item2 == InfoResult.CONTINUE) {
                    v.Item2 = InfoResult.NULL;
                }
                if ((v.Item1 != null) || (v.Item2 == InfoResult.BREAK)) {
                    return new TypeContainer<>(v.Item1, InfoResult.NULL);
                } else {
                    return this.interpreterNode(envStack);
                }
            } else {
                return new TypeContainer<>(null, InfoResult.NULL);
            }
        }
    }
}
