
package tinyjs;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import tinyjs.Token.Tokens;

/**
 * @author Alexy
 */
public class Tokenizer {
    private StreamTokenizer stream;
    private Tokens nextToken;

    Tokenizer(Reader r) {
        stream = new StreamTokenizer(r);
        stream.resetSyntax();
        stream.eolIsSignificant(false);
        stream.parseNumbers();
        
        stream.quoteChar('"');
        stream.slashSlashComments(true);
        stream.slashStarComments(true);
        stream.wordChars('a', 'z');
        stream.wordChars('A', 'Z');
        stream.wordChars('0', '9');
        
        stream.whitespaceChars('\u0000', '\u0020');

        stream.ordinaryChar(',');
        stream.ordinaryChar(':');
        stream.ordinaryChar(';');
        stream.ordinaryChar('+');
        stream.ordinaryChar('-');
        stream.ordinaryChar('*');
        stream.ordinaryChar('/');
        stream.ordinaryChar('{');
        stream.ordinaryChar('}');
        stream.ordinaryChar('(');
        stream.ordinaryChar(')');
        stream.ordinaryChar('&');
        stream.ordinaryChar('|');
        stream.ordinaryChar('=');
        stream.ordinaryChar('!');
        stream.ordinaryChar('\n');
    }
    
    String getStringValue() {
        if (stream.ttype == '"' || stream.ttype == StreamTokenizer.TT_WORD)
            return stream.sval;
        
        else if (stream.ttype == StreamTokenizer.TT_NUMBER)
            return Integer.toString((int)stream.nval);
        else
            return String.valueOf((char) stream.ttype);
    }

    Tokens getNextToken() {
        try {
            switch (stream.nextToken()) {
                case StreamTokenizer.TT_EOF:
                    nextToken = Tokens.EOF;
                    break;
                    
                case StreamTokenizer.TT_WORD:
                    String value = getStringValue();
                    if (value.equals("if")) nextToken = Tokens.IF;
                    else if (value.equals("else")) nextToken = Tokens.ELSE;
                    else if (value.equals("while")) nextToken = Tokens.WHILE;
                    else if (value.equals("for")) nextToken = Tokens.FOR;
                    else if (value.equals("switch")) nextToken = Tokens.SWITCH;
                    else if (value.equals("case")) nextToken = Tokens.CASE;
                    else if (value.equals("default")) nextToken = Tokens.DEFAULT;
                    else if (value.equals("break")) nextToken = Tokens.BREAK;
                    else if (value.equals("return")) nextToken = Tokens.RETURN;
                    else if (value.equals("continue")) nextToken = Tokens.CONTINUE;
                    else if (value.equals("eval")) nextToken = Tokens.EVAL;
                    else if (value.equals("println")) nextToken = Tokens.PRINTLN;
                    else if (value.equals("var")) nextToken = Tokens.VAR;
                    else if (value.equals("function")) nextToken = Tokens.FUNCTION;
                    else if (value.equals("true")) nextToken = Tokens.TRUE;
                    else if (value.equals("false")) nextToken = Tokens.FALSE;
                    else nextToken = Tokens.IDE;
                    
                    break;
                    
                case StreamTokenizer.TT_NUMBER: nextToken = Tokens.NUMBER;  break;
                   
                case '{': nextToken = Tokens.LPAREN; break;  
                case '}': nextToken = Tokens.RPAREN; break;
                case '(': nextToken = Tokens.LBRAC; break;
                case ')': nextToken = Tokens.RBRAC; break;
                case ':': nextToken = Tokens.COLON; break;
                case '=':
                    if (stream.nextToken() == '=') nextToken = Tokens.EQUAL;
                    else {
                        stream.pushBack();
                        nextToken = Tokens.ASSIGN;
                    }
                    break;
                case '>':
                    if (stream.nextToken() == '=') nextToken = Tokens.GREATEREQ;
                    else {
                        stream.pushBack();
                        nextToken = Tokens.GREATER;
                    }
                    break;
                case '<':
                    if (stream.nextToken() == '=') nextToken = Tokens.LESSEQ;
                    else {
                        stream.pushBack();
                        nextToken = Tokens.LESS;
                    }
                    break;
                case '!':
                    if (stream.nextToken() == '=') nextToken = Tokens.INEQUAL;
                    else {
                        stream.pushBack();
                        nextToken = Tokens.NOT;
                    }
                    break;
                case '&':
                    if (stream.nextToken() == '&') nextToken = Tokens.AND;
                    break;
                case '|':
                    if (stream.nextToken() == '|') nextToken = Tokens.OR;
                    break;
                case '+':
                    int newValToken = stream.nextToken();
                    if (newValToken == '+')
                        nextToken = Tokens.INCR;
                    else if (newValToken == '=')
                        nextToken = Tokens.ASSIGN_PLUS;
                    else {
                        nextToken = Tokens.PLUS;
                        stream.pushBack();
                    }
                    break;
                case '-':
                    int newValTokenMinus = stream.nextToken();
                    if (newValTokenMinus == '-')
                        nextToken = Tokens.DECR;
                    else if (newValTokenMinus == '=')
                        nextToken = Tokens.ASSIGN_MINUS;
                    else {
                        stream.pushBack();
                        nextToken = Tokens.MINUS;
                    }
                    break;
                case '*': nextToken = Tokens.MUL; break;
                case '/': nextToken = Tokens.DIV; break;
                case ',': nextToken = Tokens.COMMA; break;
                case ';': nextToken = Tokens.SEMICOLON; break;
                case '"': nextToken = Tokens.STRING; break;
                default: nextToken = Tokens.UNKNOWN; break;
            }
        }
        catch (IOException e) {
            nextToken = Tokens.EOF;
        }
        return nextToken;
    }
    
    int getLineNo(){
        return stream.lineno();
    }
    
    public static boolean isStm(Tokens t) {
        switch (t) {
            case IF: case WHILE: case FOR: case BREAK: case CONTINUE:
            case PRINTLN: case RETURN: case SWITCH:case EVAL: case IDE: case NUMBER: //Needs revision
                return true;
            default:
                return false;
        }
    }
}
