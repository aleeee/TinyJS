package tinyjs;

/**
 * @author Alexy
 */
public class Token {
    public enum Tokens{ 
        VAR, // "var"
	FUNCTION, // "function"
        LPAREN, // "{"
        RPAREN, // "}"
        LBRAC, // "("
        RBRAC, // ")"
        SEMICOLON, // ";"
        COLON, // ":"
        COMMA, // ","
        INCR, // "++"
        DECR, // "--"
        ASSIGN, // "="
        ASSIGN_PLUS, // "+="
        ASSIGN_MINUS, // "-="
        IF, // "if"
        ELSE, // "else"
        WHILE, // "while"
        FOR, // "for"
	SWITCH, //"switch"
	CASE, // "case"
	DEFAULT, // "default"
        RETURN, // "return"
	CONTINUE, // "continue"
	BREAK, // "break"
	PRINTLN, // "println"
	EVAL, // "eval"
	OR, // "||"
	AND, // "&&"
	NOT, // "!"
	EQUAL, // "=="
	INEQUAL, // "!="
	GREATER, // ">"
	GREATEREQ, // ">="
	LESS, // "<"
	LESSEQ, // "<="
	PLUS, // "+"
	MINUS, // "-"
	MUL, // "*"
	DIV, // "/"
        NUMBER,
        STRING,
        IDE,
	TRUE, // "true"
	FALSE, // "false"
	COMMENT,
        UNKNOWN,
        UNDEFINED,
        EOF
    }
}
