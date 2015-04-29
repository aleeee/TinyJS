package tinyjs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import tinyjs.Node.*;
import tinyjs.Token.Tokens;

/**
 * @author Alexy
 */
public class Parser {

    private Tokenizer lookahead;
    private Tokens token;
    private String val;
    private FileReader fr;

    public Parser(String fileName) {
        try {
            fr = new FileReader(fileName);
            BufferedReader in = new BufferedReader(fr);
            lookahead = new Tokenizer(in);
            token = lookahead.getNextToken();
        } catch (Exception ex) {
            System.out.println("Cannot read the file: " + ex.getMessage());
            try {
                fr.close();
            } catch (Exception e) {
                System.out.println("Error in Closing File..." + e.getMessage());
            }
            System.exit(1);
        }
    }

    private void next() {
        token = lookahead.getNextToken();
    }

    private Tokens expect(Tokens t) {
        if (token == t) {
            Tokens tmp = token;
            next();
            return tmp;
        } else {
            System.out.println("Error: Tokens " + t + " expected" + " but found " + token + " line:" + lookahead.getLineNo());
            System.exit(1);
            return null;
        }
    }

    private void mixStatments(Block n) {
        if (Tokenizer.isStm(token)) {
            stmt(n);
            mixStatments(n);
        } else if (token == Tokens.VAR || token == Tokens.FUNCTION) {
            decl(n);
            mixStatments(n);
        }
    }

    private Block block(Block n) {
        Tokens t = expect(Tokens.LPAREN);
        Block bNode = new Block();
        mixStatments(bNode);
        expect(Tokens.RPAREN);
        return bNode;
    }

    private void decl(Block n) {
        if (token == Tokens.VAR) {
            next();
            ArrayList<TypeContainer<String, Expression>> vars = new ArrayList<>();
            varDeclArrayList(n, vars);
            
            expect(Tokens.SEMICOLON);
            for (int i = 0; i < vars.size(); i++) {
                Declaration dNode = new Declaration(vars.get(i).Item1, vars.get(i).Item2);
                n.Children.add(dNode);
            }
        } else {
            n.Children.add(funDecl(n));
        }
    }

    private Function funDecl(Block n) {
        if (token == Tokens.FUNCTION) {
            next();
            String ide = lookahead.getStringValue();
            expect(Tokens.IDE);
            expect(Tokens.LBRAC);
            ArrayList<String> formalParameters = params();
            expect(Tokens.RBRAC);
            Block treeBlock = block(n);
            return new Function(ide, formalParameters, treeBlock);
        }

        return null;
    }

    private void varDeclArrayList(Block n, ArrayList<TypeContainer<String, Expression>> vars) {
        vars.add(varDecl(n));

        if (token == Tokens.COMMA) {
            next();
            varDeclArrayList(n, vars);
        }
    }

    private TypeContainer<String, Expression> varDecl(Block n) {
        Expression treeExp = null;
        String ide = lookahead.getStringValue();
        expect(Tokens.IDE);
        if (token == Tokens.ASSIGN) {
            next();
            treeExp = exp();
            
        }

        return new TypeContainer<>(ide, treeExp);
    }

    private ArrayList<String> params() {
        ArrayList<String> fp = new ArrayList<>();
        if (token == Tokens.IDE) {
            fp.add(lookahead.getStringValue());
            next();
            while (token == Tokens.COMMA) {
                next();
                String v = lookahead.getStringValue();
                expect(Tokens.IDE);
                fp.add(v);
            }
        }

        return fp;
    }

    private void stmt(Block n) {
        String ide;
        Statement sNode = null;
        Expression treeExp;
        Block bodyNode;

        switch (token) {
            case IDE:
                ide = lookahead.getStringValue();
                expect(Tokens.IDE);
                if (token == Tokens.LBRAC) {
                    sNode = call(ide, true);
                    expect(Tokens.SEMICOLON);
                } else {
                    sNode = stmtIDE(n, ide, true);
                }
                break;

            case IF:
                next();
                expect(Tokens.LBRAC);
                treeExp = exp();
                expect(Tokens.RBRAC);

                Block thenNode = block(n);
                Block elseNode = null;
                if (token == Tokens.ELSE) {
                    next();
                    elseNode = block(n);
                }
                sNode = new IF(treeExp, thenNode, elseNode);
                break;

            case WHILE:
                next();

                expect(Tokens.LBRAC);
                treeExp = exp();
                expect(Tokens.RBRAC);

                bodyNode = block(n);
                sNode = new While(treeExp, bodyNode);
                break;

            case FOR:
                next();
                expect(Tokens.LBRAC);
                String ide1 = lookahead.getStringValue();
                expect(Tokens.IDE);
                expect(Tokens.ASSIGN);
                Statement s1 = assign(Tokens.ASSIGN, n, ide1, exp(), true);

                treeExp = exp();
                expect(Tokens.SEMICOLON);

                String ide2 = lookahead.getStringValue();
                expect(Tokens.IDE);
                Statement s2 = stmtIDE(n, ide2, false);
                expect(Tokens.RBRAC);

                bodyNode = block(n);
                sNode = new For(s1, treeExp, s2, bodyNode);
                break;

            case SWITCH:
                next();
                expect(Tokens.LBRAC);
                ide = lookahead.getStringValue();
                expect(Tokens.IDE);
                expect(Tokens.RBRAC);
                expect(Tokens.LPAREN);
                ArrayList<Case> cases = new ArrayList<>();
                caseBlock(ide, cases);
                sNode = new Switch(ide, cases);
                expect(Tokens.RPAREN);
                break;

            case RETURN:
                next();
                sNode = new Return(exp());
                expect(Tokens.SEMICOLON);
                break;

            case CONTINUE:
                next();
                sNode = new Continue();
                expect(Tokens.SEMICOLON);
                break;

            case BREAK:
                next();
                sNode = new Break();
                expect(Tokens.SEMICOLON);
                break;

            case PRINTLN:
                next();
                expect(Tokens.LBRAC);
                sNode = new Print(exp());
                expect(Tokens.RBRAC);
                expect(Tokens.SEMICOLON);
                break;

            case EVAL:
                sNode = eval(n);
                expect(Tokens.SEMICOLON);
                break;
            case NUMBER:
                String num = lookahead.getStringValue();
                expect(Tokens.NUMBER);
                if (token == Tokens.PLUS || token == Tokens.MINUS || token == Tokens.MUL || token == Tokens.DIV) {
                    sNode = sNode = stmtIDE(n, num, true);
                } else {
                    sNode = new Constant(token, val);
                }
        }

        if (sNode != null) {
            n.Children.add(sNode);
        }
    }

    private void caseBlock(String ide, ArrayList<Case> cases) {
        if ((token == Tokens.CASE) || (token == Tokens.DEFAULT)) {
            if (token == Tokens.CASE) {
                caseClause(ide, cases);
                if ((token == Tokens.CASE) || (token == Tokens.DEFAULT)) {
                    caseBlock(ide, cases);
                }
            } else {
                expect(Tokens.DEFAULT);
                expect(Tokens.COLON);
                Block bNode = new Block();
                mixStatments(bNode);
                cases.add(new Case(ide, null, bNode));
            }
        } else {
            System.out.println("Expected CASE or DEFAULT tokens, but found token: " + token + ".");
            System.exit(1);
        }
    }

    private void caseClause(String ide, ArrayList<Case> cases) {
        expect(Tokens.CASE);
        if ((token == Tokens.NUMBER) || (token == Tokens.STRING) || (token == Tokens.TRUE) || (token == Tokens.FALSE)) {
            Constant c = new Constant(token, lookahead.getStringValue());
            next();
            expect(Tokens.COLON);
            Block bNode = new Block();
            mixStatments(bNode);
            cases.add(new Case(ide, c, bNode));
        } else {
            System.out.println("Expected a constant value after the CASE, but found token: " + token + ".");
            System.exit(1);
        }
    }

    // StmtIDE → = Exp ; | += Exp ; | -= Exp ; | ++ ; | --
    private Assign stmtIDE(Block n, String ide, boolean semicolons) {
        Assign sNode = null;
        Expression treeExp;
        Tokens t = token;
        switch (token) {
            case ASSIGN:
                next();
                treeExp = exp();
                sNode = assign(t, n, ide, treeExp, semicolons);
                break;

            case ASSIGN_MINUS:
            case ASSIGN_PLUS:
                next();
                treeExp = exp();
                sNode = assign(t, n, ide, treeExp, semicolons);
                break;

            case INCR:
            case DECR:
                next();
                Constant one = new Constant(Tokens.NUMBER, "1");
                sNode = assign(t, n, ide, one, semicolons);
                break;
            case PLUS:
            case MINUS:
            case MUL:
            case DIV:
                next();
                treeExp = exp();
                Statement st = new BinaryOperation(t, treeExp, treeExp);
                break;

            default:
                System.out.println("Invalid statement, token: " + token + ".");
                System.exit(1);
                break;
        }
        return sNode;
    }

    private Assign assign(Tokens t, Block n, String ide, Expression rightNode, boolean semicolons) {
        Var copyide;
        BinaryOperation opNode;
        Expression tree = null;
        switch (t) {
            case ASSIGN:
                tree = rightNode;
                break;

            case INCR:
            case ASSIGN_PLUS:
            case DECR:
            case ASSIGN_MINUS:
                Tokens op;
                if (t == Tokens.INCR || t == Tokens.ASSIGN_PLUS) {
                    op = Tokens.PLUS;
                } else {
                    op = Tokens.MINUS;
                }

                copyide = new Var(ide);
                opNode = new BinaryOperation(op, copyide, rightNode);
                tree = opNode;
                break;
            default:
                break;
        }

        Assign sNode = new Assign(ide, tree);
        if (semicolons) {
            expect(Tokens.SEMICOLON);
        }
        return sNode;
    }

    private EvalN eval(Block b) {
        EvalN n = null;

        String evalString;
        Tokens t = expect(Tokens.EVAL);
        expect(Tokens.LBRAC);

        if (token == Tokens.IDE) {
            // n=new EvalN(new Var(val));
            evalString = val;
            StringReader reader = new StringReader(evalString);
            Tokenizer temp = lookahead;
            lookahead = new Tokenizer(reader);
            next();
            mixStatments(b);
            lookahead = temp;

        } else if (token == Tokens.STRING) {

            evalString = lookahead.getStringValue();
            StringReader reader = new StringReader(evalString);
            Tokenizer temp = lookahead;
            lookahead = new Tokenizer(reader);
            next();
            mixStatments(b);
            lookahead = temp;

        } else {
            System.out.println("Eval statemant expects an ide or a String, instead we found " + token + ".");
            System.exit(1);
        }

        next();
        expect(Tokens.RBRAC);
        return n;

    }

    private Call call(String ide, boolean itIsStm) {
        ArrayList<Expression> actualParameters = new ArrayList<>();

        this.expect(Tokens.LBRAC);
        if (token != Tokens.RBRAC) {
            actualParameters.add(exp());
            while (token == Tokens.COMMA) {
                next();
                actualParameters.add(exp());
            }
        }
        this.expect(Tokens.RBRAC);

        return new Call(ide, actualParameters, itIsStm);
    }

    private Expression exp() {
        Expression treeElement = andExp();
        Expression treeMoreElements = moreAndExps(treeElement);
        return treeMoreElements;
    }

    private Expression moreAndExps(Expression n) {
        if (token == Tokens.OR) {
            Tokens op = token;
            next();
            Expression treeElement = andExp();
            BinaryOperation opNode = new BinaryOperation(op, n, treeElement);
            Expression treeMoreElements = moreAndExps(opNode);

            return treeMoreElements;
        } else {
            return n;
        }
    }

    private Expression andExp() {
        Expression treeElement = unaryRelExp();
        Expression treeMoreElements = moreUnaryRelExps(treeElement);
        return treeMoreElements;
    }

    private Expression moreUnaryRelExps(Expression n) {
        if (token == Tokens.AND) {
            Tokens op = token;
            next();
            Expression treeElement = unaryRelExp();
            BinaryOperation opNode = new BinaryOperation(op, n, treeElement);
            Expression treeMoreElements = moreUnaryRelExps(opNode);

            return treeMoreElements;
        } else {
            return n;
        }
    }

    private Expression unaryRelExp() {
        if (token == Tokens.NOT) {
            Tokens op = token;
            next();
            Expression treeElement1 = unaryRelExp();
            UnaryOperation opNode = new UnaryOperation(op, treeElement1);
            return opNode;
        } else {
            Expression treeElement2 = relExp();
            return treeElement2;
        }
    }

    private Expression relExp() {
        Expression treeElement = sumExp();
        Expression treeMoreElements = moreSumExps(treeElement);
        return treeMoreElements;
    }

    private Expression moreSumExps(Expression n) {
        boolean cond = ((token == Tokens.EQUAL) || (token == Tokens.INEQUAL));
        cond = (cond || (token == Tokens.GREATER) || (token == Tokens.GREATEREQ));
        cond = (cond || (token == Tokens.LESS) || (token == Tokens.LESSEQ));
        if (cond) {
            Tokens op = token;
            next();
            Expression treeElement = sumExp();
            BinaryOperation opNode = new BinaryOperation(op, n, treeElement);
            Expression treeMoreElements = moreSumExps(opNode);

            return treeMoreElements;
        } else {
            return n;
        }
    }

    private Expression sumExp() {
        Expression treeElement = term();
        Expression treeMoreElements = moreTerms(treeElement);
        return treeMoreElements;
    }

    private Expression moreTerms(Expression n) {
        if ((token == Tokens.PLUS) || (token == Tokens.MINUS)) {
            Tokens op = token;
            next();
            Expression treeElement = term();
            BinaryOperation opNode = new BinaryOperation(op, n, treeElement);
            Expression treeMoreElements = moreTerms(opNode);

            return treeMoreElements;
        } else {
            return n;
        }
    }

    private Expression term() {
        Expression treeElement = unaryExp();
        Expression treeMoreElements = MoreUnaryExps(treeElement);
        return treeMoreElements;
    }

    private Expression MoreUnaryExps(Expression n) {
        if ((token == Tokens.MUL) || (token == Tokens.DIV)) {
            Tokens op = token;
            next();
            Expression treeElement = unaryExp();
            BinaryOperation opNode = new BinaryOperation(op, n, treeElement);
            Expression treeMoreElements = MoreUnaryExps(opNode);

            return treeMoreElements;
        } else {
            return n;
        }
    }

    private Expression unaryExp() {
        if (token == Tokens.MINUS) {
            Tokens op = token;
            next();
            Expression treeElement1 = unaryExp();
            UnaryOperation opNode = new UnaryOperation(op, treeElement1);
            return opNode;
        } else {
            Expression treeElement2 = factor();
            return treeElement2;
        }
    }

    private Expression factor() {
        Block n = null;
        Expression result = null;
        switch (token) {
            case LBRAC:
                next();
                result = exp();
                this.expect(Tokens.RBRAC);
                break;

            case IDE:
                String ide = lookahead.getStringValue();
                expect(Tokens.IDE);
                if (token == Tokens.LBRAC) {
                    result = call(ide, false);
                } else {
                    result = new Var(ide);
                }
                break;

            case NUMBER:
            case STRING:
            case TRUE:
            case FALSE:
                result = new Constant(token, lookahead.getStringValue());
                val = lookahead.getStringValue();
                next();
                break;

            case EVAL:
                next();
                expect(Tokens.LBRAC);
                result = new Var(lookahead.getStringValue());
                val = lookahead.getStringValue();
                next();
                expect(Tokens.RBRAC);
                break;

            default:
                System.out.println("Invalid factor, token: " + token + ".");
                System.exit(1);
                break;
        }

        return result;
    }

    public Block createAST() {
        Block root = new Block();
        mixStatments(root);
        if (fr != null) {
            try {
                fr.close();
            } catch (IOException ex) {
                System.out.println("Error ... in CreateASt");
            }
        }
        if (token != Tokens.EOF) {
            System.out.println("Error: unexpected token at " + lookahead.getLineNo());
            System.exit(1);
            return null;
        }
        return root;

    }
}
