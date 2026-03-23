import java.util.*;

public class MiniCompiler {

    // -------------------- TOKEN --------------------
    // This is an enumeration — a list of all possible types of tokens this language supports.

    enum TokenType {
        IDENT, NUMBER,

        // Keywords
        IF, ELSE, WHILE, PRINT,

        // Operators
        PLUS, MINUS, MUL, DIV,
        ASSIGN,
        GT, LT, GE, LE, EQEQ, NEQ,
        ANDAND, OROR, NOT,

        // Symbols
        SEMI, LPAREN, RPAREN, LBRACE, RBRACE, COMMA,

        EOF
    }

    static class Token {
        TokenType type;
        String lexeme;

        Token(TokenType type, String lexeme) {
            this.type = type;
            this.lexeme = lexeme;
        }

        public String toString() {
            return type + "(" + lexeme + ")";
        }
    }

    // -------------------- LEXER (Lexer class generates tokens)--------------------
    static class Lexer {
        private final String src;
        private int pos = 0;

        Lexer(String src) {
            this.src = src;
        }

        private boolean isAtEnd() {
            return pos >= src.length();
        }

        private char peek() {
            if (isAtEnd()) return '\0';
            return src.charAt(pos);
        }

        private char peekNext() {
            if (pos + 1 >= src.length()) return '\0';
            return src.charAt(pos + 1);
        }

        private char advance() {
            return src.charAt(pos++);
        }

        private void skipWhitespace() {
            while (!isAtEnd()) {
                char c = peek();
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                    advance();
                } else {
                    break;
                }
            }
        }

        List<Token> tokenize() {
            List<Token> tokens = new ArrayList<>();

            while (!isAtEnd()) {
                skipWhitespace();
                if (isAtEnd()) break;

                char c = peek();

                // Identifier/keyword
                if (Character.isLetter(c) || c == '_') {
                    tokens.add(readIdentOrKeyword());
                    continue;
                }

                // Number
                if (Character.isDigit(c)) {
                    tokens.add(readNumber());
                    continue;
                }

                // Multi-char operators
                if (c == '=' && peekNext() == '=') {
                    advance();
                    advance();
                    tokens.add(new Token(TokenType.EQEQ, "=="));
                    continue;
                }
                if (c == '!' && peekNext() == '=') {
                    advance();
                    advance();
                    tokens.add(new Token(TokenType.NEQ, "!="));
                    continue;
                }
                if (c == '>' && peekNext() == '=') {
                    advance();
                    advance();
                    tokens.add(new Token(TokenType.GE, ">="));
                    continue;
                }
                if (c == '<' && peekNext() == '=') {
                    advance();
                    advance();
                    tokens.add(new Token(TokenType.LE, "<="));
                    continue;
                }
                if (c == '&' && peekNext() == '&') {
                    advance();
                    advance();
                    tokens.add(new Token(TokenType.ANDAND, "&&"));
                    continue;
                }
                if (c == '|' && peekNext() == '|') {
                    advance();
                    advance();
                    tokens.add(new Token(TokenType.OROR, "||"));
                    continue;
                }

                // Single-char tokens
                switch (c) {
                    case '+': advance(); tokens.add(new Token(TokenType.PLUS, "+")); break;
                    case '-': advance(); tokens.add(new Token(TokenType.MINUS, "-")); break;
                    case '*': advance(); tokens.add(new Token(TokenType.MUL, "*")); break;
                    case '/': advance(); tokens.add(new Token(TokenType.DIV, "/")); break;
                    case '=': advance(); tokens.add(new Token(TokenType.ASSIGN, "=")); break;
                    case '>': advance(); tokens.add(new Token(TokenType.GT, ">")); break;
                    case '<': advance(); tokens.add(new Token(TokenType.LT, "<")); break;
                    case '!': advance(); tokens.add(new Token(TokenType.NOT, "!")); break;

                    case ';': advance(); tokens.add(new Token(TokenType.SEMI, ";")); break;
                    case '(': advance(); tokens.add(new Token(TokenType.LPAREN, "(")); break;
                    case ')': advance(); tokens.add(new Token(TokenType.RPAREN, ")")); break;
                    case '{': advance(); tokens.add(new Token(TokenType.LBRACE, "{")); break;
                    case '}': advance(); tokens.add(new Token(TokenType.RBRACE, "}")); break;
                    case ',': advance(); tokens.add(new Token(TokenType.COMMA, ",")); break;

                    default:
                        throw new RuntimeException("Unexpected character: '" + c + "'");
                }
            }

            tokens.add(new Token(TokenType.EOF, ""));
            return tokens;
        }

        private Token readIdentOrKeyword() {
            int start = pos;
            while (!isAtEnd()) {
                char c = peek();
                if (Character.isLetterOrDigit(c) || c == '_') advance();
                else break;
            }
            String text = src.substring(start, pos);

            switch (text) {
                case "if": return new Token(TokenType.IF, text);
                case "else": return new Token(TokenType.ELSE, text);
                case "while": return new Token(TokenType.WHILE, text);
                case "print": return new Token(TokenType.PRINT, text);
                default: return new Token(TokenType.IDENT, text);
            }
        }

        private Token readNumber() {
            int start = pos;
            while (!isAtEnd() && Character.isDigit(peek())) advance();
            return new Token(TokenType.NUMBER, src.substring(start, pos));
        }
    }


    // -------------------- PARSER (Parser class builds an AST)--------------------
    static class Parser {
        private final List<Token> tokens;
        private int current = 0;

        Parser(List<Token> tokens) {
            this.tokens = tokens;
        }

        private Token peek() { return tokens.get(current); }
        private Token previous() { return tokens.get(current - 1); }
        private boolean isAtEnd() { return peek().type == TokenType.EOF; }

        private Token advance() {
            if (!isAtEnd()) current++;
            return previous();
        }

        private boolean check(TokenType type) {
            if (isAtEnd()) return false;
            return peek().type == type;
        }

        private boolean match(TokenType... types) {
            for (TokenType t : types) {
                if (check(t)) {
                    advance();
                    return true;
                }
            }
            return false;
        }
        
        // Used for checking grammar rules like "(" after if"

        private Token consume(TokenType type, String msg) {
            if (check(type)) return advance();
            throw new RuntimeException("Parse error: " + msg + " Found: " + peek());
        }

        List<Stmt> parseProgram() {
            List<Stmt> stmts = new ArrayList<>();
            while (!isAtEnd()) {
                stmts.add(statement());
            }
            return stmts;
        }

        private Stmt statement() {
            if (match(TokenType.PRINT)) {
                consume(TokenType.LPAREN, "Expected '(' after print");
                Expr e = expression();
                consume(TokenType.RPAREN, "Expected ')' after print(expr)");
                consume(TokenType.SEMI, "Expected ';' after print statement");
                return new PrintStmt(e);
            }

            if (match(TokenType.IF)) {
                consume(TokenType.LPAREN, "Expected '(' after if");
                Expr cond = expression();
                consume(TokenType.RPAREN, "Expected ')' after if condition");

                BlockStmt thenBlock = block();
                BlockStmt elseBlock = null;

                if (match(TokenType.ELSE)) {
                    elseBlock = block();
                }
                return new IfStmt(cond, thenBlock, elseBlock);
            }

            if (match(TokenType.WHILE)) {
                consume(TokenType.LPAREN, "Expected '(' after while");
                Expr cond = expression();
                consume(TokenType.RPAREN, "Expected ')' after while condition");
                BlockStmt body = block();
                return new WhileStmt(cond, body);
            }

            Token name = consume(TokenType.IDENT, "Expected variable name");
            consume(TokenType.ASSIGN, "Expected '=' after variable");

            Expr val = expression();
            consume(TokenType.SEMI, "Expected ';' after assignment");
            return new AssignStmt(name.lexeme, val);
        }

        private BlockStmt block() {
            consume(TokenType.LBRACE, "Expected '{' to start block");
            List<Stmt> stmts = new ArrayList<>();
            while (!check(TokenType.RBRACE)) {
                if (isAtEnd()) throw new RuntimeException("Parse error: Missing '}'");
                stmts.add(statement());
            }
            consume(TokenType.RBRACE, "Expected '}' to end block");
            return new BlockStmt(stmts);
        }

        // precedence: or -> and -> equality -> comparison -> term -> factor -> unary -> primary
        private Expr expression() { return or(); }

        private Expr or() {
            Expr expr = and();
            while (match(TokenType.OROR)) {
                TokenType op = previous().type;
                Expr right = and();
                expr = new BinaryExpr(expr, op, right);
            }
            return expr;
        }

        private Expr and() {
            Expr expr = equality();
            while (match(TokenType.ANDAND)) {
                TokenType op = previous().type;
                Expr right = equality();
                expr = new BinaryExpr(expr, op, right);
            }
            return expr;
        }

        private Expr equality() {
            Expr expr = comparison();
            while (match(TokenType.EQEQ, TokenType.NEQ)) {
                TokenType op = previous().type;
                Expr right = comparison();
                expr = new BinaryExpr(expr, op, right);
            }
            return expr;
        }

        private Expr comparison() {
            Expr expr = term();
            while (match(TokenType.GT, TokenType.LT, TokenType.GE, TokenType.LE)) {
                TokenType op = previous().type;
                Expr right = term();
                expr = new BinaryExpr(expr, op, right);
            }
            return expr;
        }

        private Expr term() {
            Expr expr = factor();
            while (match(TokenType.PLUS, TokenType.MINUS)) {
                TokenType op = previous().type;
                Expr right = factor();
                expr = new BinaryExpr(expr, op, right);
            }
            return expr;
        }

        private Expr factor() {
            Expr expr = unary();
            while (match(TokenType.MUL, TokenType.DIV)) {
                TokenType op = previous().type;
                Expr right = unary();
                expr = new BinaryExpr(expr, op, right);
            }
            return expr;
        }

        private Expr unary() {
            if (match(TokenType.NOT, TokenType.MINUS)) {
                TokenType op = previous().type;
                Expr right = unary();
                return new UnaryExpr(op, right);
            }
            return primary();
        }

        private Expr primary() {
            if (match(TokenType.NUMBER)) return new NumberExpr(Integer.parseInt(previous().lexeme));
            if (match(TokenType.IDENT)) return new VarExpr(previous().lexeme);

            if (match(TokenType.LPAREN)) {
                Expr e = expression();
                consume(TokenType.RPAREN, "Expected ')'");
                return e;
            }

            throw new RuntimeException("Parse error: Unexpected token " + peek());
        }
    }

    // -------------------- AST (Abstract Syntax Tree)--------------------
    interface Stmt {}
    interface Expr {}

    static class BlockStmt implements Stmt {
        List<Stmt> statements;
        BlockStmt(List<Stmt> statements) {
            this.statements = statements;
        }
    }

    static class AssignStmt implements Stmt {
        String name;
        Expr value;
        AssignStmt(String name, Expr value) {
            this.name = name;
            this.value = value;
        }
    }

    static class PrintStmt implements Stmt {
        Expr value;
        PrintStmt(Expr value) {
            this.value = value;
        }
    }

    static class IfStmt implements Stmt {
        Expr condition;
        BlockStmt thenBlock;
        BlockStmt elseBlock; // can be null

        IfStmt(Expr condition, BlockStmt thenBlock, BlockStmt elseBlock) {
            this.condition = condition;
            this.thenBlock = thenBlock;
            this.elseBlock = elseBlock;
        }
    }

    static class WhileStmt implements Stmt {
        Expr condition;
        BlockStmt body;

        WhileStmt(Expr condition, BlockStmt body) {
            this.condition = condition;
            this.body = body;
        }
    }

    static class NumberExpr implements Expr {
        int value;
        NumberExpr(int value) { this.value = value; }
    }

    static class VarExpr implements Expr {
        String name;
        VarExpr(String name) { this.name = name; }
    }

    static class UnaryExpr implements Expr {
        TokenType op;
        Expr expr;
        UnaryExpr(TokenType op, Expr expr) {
            this.op = op;
            this.expr = expr;
        }
    }

    static class BinaryExpr implements Expr {
        Expr left;
        TokenType op;
        Expr right;
        BinaryExpr(Expr left, TokenType op, Expr right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }
    }



    // -------------------- SYMBOL TABLE (25 vars) --------------------
    static class SymbolTable {
        private final Map<String, Integer> map = new HashMap<>();
        private int nextIndex = 0;
        private final int MAX = 25;

        int getOrAllocate(String name) {
            if (map.containsKey(name)) return map.get(name);
            if (nextIndex >= MAX) throw new RuntimeException("Too many variables. Max = " + MAX);

            map.put(name, nextIndex);
            nextIndex++; // ✅ FIXED: increment index for next new variable
            return map.get(name);
        }

        int get(String name) {
            if (!map.containsKey(name)) {
                throw new RuntimeException("Variable used before assignment: " + name);
            }
            return map.get(name);
        }
    }

    // -------------------- VM GENERATOR --------------------
    static class VMGenerator {
        private final SymbolTable table = new SymbolTable();
        private final List<String> out = new ArrayList<>();
        private int labelCounter = 0;

        List<String> generate(List<Stmt> program) {
            for (Stmt s : program) genStmt(s);
            return out;
        }

        private String newLabel(String base) {
            labelCounter++;
            return base + "_" + labelCounter;
        }

        private void genStmt(Stmt s) {
            if (s instanceof AssignStmt) {
                AssignStmt a = (AssignStmt) s;
                genExpr(a.value);
                int idx = table.getOrAllocate(a.name);
                out.add("pop local " + idx);
                return;
            }

            if (s instanceof PrintStmt) {
                PrintStmt p = (PrintStmt) s;
                genExpr(p.value);
                out.add("call print 1");
                return;
            }

            if (s instanceof BlockStmt) {
                BlockStmt b = (BlockStmt) s;
                for (Stmt st : b.statements) genStmt(st);
                return;
            }

            if (s instanceof IfStmt) {
                IfStmt i = (IfStmt) s;

                String L_TRUE = newLabel("IF_TRUE");
                String L_FALSE = newLabel("IF_FALSE");
                String L_END = newLabel("IF_END");

                genExpr(i.condition);
                out.add("if-goto " + L_TRUE);
                out.add("goto " + L_FALSE);

                out.add("label " + L_TRUE);
                genStmt(i.thenBlock);
                out.add("goto " + L_END);

                out.add("label " + L_FALSE);
                if (i.elseBlock != null) genStmt(i.elseBlock);
                out.add("label " + L_END);
                return;
            }

            if (s instanceof WhileStmt) {
                WhileStmt w = (WhileStmt) s;

                String L_EXP = newLabel("WHILE_EXP");
                String L_END = newLabel("WHILE_END");

                out.add("label " + L_EXP);
                genExpr(w.condition);
                out.add("not");
                out.add("if-goto " + L_END);

                genStmt(w.body);
                out.add("goto " + L_EXP);
                out.add("label " + L_END);
                return;
            }

            throw new RuntimeException("Unknown statement type");
        }

        private void genExpr(Expr e) {
            if (e instanceof NumberExpr) {
                out.add("push constant " + ((NumberExpr) e).value);
                return;
            }

            if (e instanceof VarExpr) {
                String name = ((VarExpr) e).name;
                int idx = table.get(name);
                out.add("push local " + idx);
                return;
            }

            if (e instanceof UnaryExpr) {
                UnaryExpr u = (UnaryExpr) e;
                genExpr(u.expr);

                if (u.op == TokenType.MINUS) out.add("neg");
                else if (u.op == TokenType.NOT) out.add("not");
                else throw new RuntimeException("Unsupported unary op");

                return;
            }

            if (e instanceof BinaryExpr) {
                BinaryExpr b = (BinaryExpr) e;

                genExpr(b.left);
                genExpr(b.right);

                switch (b.op) {
                    // arithmetic
                    case PLUS: out.add("add"); break;
                    case MINUS: out.add("sub"); break;
                    case MUL: out.add("call Math.multiply 2"); break;
                    case DIV: out.add("call Math.divide 2"); break;

                    // comparisons
                    case GT: out.add("gt"); break;
                    case LT: out.add("lt"); break;
                    case EQEQ: out.add("eq"); break;

                    case NEQ:
                        out.add("eq");
                        out.add("not");
                        break;

                    case GE:
                        out.add("lt");
                        out.add("not");
                        break;

                    case LE:
                        out.add("gt");
                        out.add("not");
                        break;

                    // logical
                    case ANDAND: out.add("and"); break;
                    case OROR: out.add("or"); break;

                    default:
                        throw new RuntimeException("Unsupported operator: " + b.op);
                }
                return;
            }

            throw new RuntimeException("Unknown expression type");
        }
    }
    
    // -------------------- MAIN --------------------
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter your program (end with line: END):");
        StringBuilder sb = new StringBuilder();

        while (true) {
            String line = sc.nextLine();
            if (line.equals("END")) break;
            sb.append(line).append("\n");
        }

        String source = sb.toString();

        try {
            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.tokenize();

            Parser parser = new Parser(tokens);
            List<Stmt> program = parser.parseProgram();

            VMGenerator gen = new VMGenerator();
            List<String> vm = gen.generate(program);

            System.out.println("\n--- VM OUTPUT ---");
            for (String cmd : vm) System.out.println(cmd);

        } catch (Exception e) {
            System.out.println("\nERROR: " + e.getMessage());
        }
    }
}
