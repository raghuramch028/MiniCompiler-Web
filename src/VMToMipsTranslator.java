import java.util.*;

public class VMToMipsTranslator {

    static class Translator {
        private final List<String> out = new ArrayList<>();
        private int cmpCounter = 0;

        public List<String> translate(List<String> vmLines) {
            // Header
            out.add(".data");
            out.add("LOCAL_BASE: .space 400      # 25 locals * 4 bytes");
            out.add("STACK_MEM:  .space 4000     # stack memory");
            out.add("NL: .asciiz \"\\n\"");
            out.add("");

            out.add(".text");
            out.add(".globl main");
            out.add("main:");
            out.add("  la $s1, LOCAL_BASE        # $s1 = base address for local segment");
            out.add("  la $sp, STACK_MEM");
            out.add("  addi $sp, $sp, 4000       # initialize $sp to top of stack");
            out.add("");

            for (String line : vmLines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.startsWith("//")) continue;

                int idx = line.indexOf("//");
                if (idx != -1) line = line.substring(0, idx).trim();
                if (line.isEmpty()) continue;

                translateCommand(line);
                out.add("");
            }

            // Exit
            out.add("  li $v0, 10");
            out.add("  syscall");
            return out;
        }

        private void translateCommand(String line) {
            String[] parts = line.split("\\s+");
            String cmd = parts[0];

            out.add("  # " + line);

            switch (cmd) {
                case "push":
                    translatePush(parts);
                    break;
                case "pop":
                    translatePop(parts);
                    break;

                case "add":
                    binaryOp("add");
                    break;
                case "sub":
                    binaryOp("sub");
                    break;
                case "and":
                    binaryOp("and");
                    break;
                case "or":
                    binaryOp("or");
                    break;

                case "neg":
                    unaryNeg();
                    break;
                case "not":
                    unaryNot();
                    break;

                case "eq":
                    compareOp("eq");
                    break;
                case "lt":
                    compareOp("lt");
                    break;
                case "gt":
                    compareOp("gt");
                    break;

                case "label":
                    out.add(parts[1] + ":");
                    break;
                case "goto":
                    out.add("  j " + parts[1]);
                    break;
                case "if-goto":
                    ifGoto(parts[1]);
                    break;

                case "call":
                    translateCall(parts);
                    break;

                default:
                    out.add("  # Unsupported VM command: " + line);
                    break;
            }
        }

        // ---------------- PUSH/POP ----------------
        private void translatePush(String[] parts) {
            String segment = parts[1];
            int index = Integer.parseInt(parts[2]);

            if (segment.equals("constant")) {
                out.add("  addi $sp, $sp, -4");
                out.add("  li   $t0, " + index);
                out.add("  sw   $t0, 0($sp)");
            } else if (segment.equals("local")) {
                out.add("  lw   $t0, " + (index * 4) + "($s1)");
                out.add("  addi $sp, $sp, -4");
                out.add("  sw   $t0, 0($sp)");
            } else {
                out.add("  # Unsupported push segment: " + segment);
            }
        }

        private void translatePop(String[] parts) {
            String segment = parts[1];
            int index = Integer.parseInt(parts[2]);

            if (segment.equals("local")) {
                out.add("  lw   $t0, 0($sp)");
                out.add("  addi $sp, $sp, 4");
                out.add("  sw   $t0, " + (index * 4) + "($s1)");
            } else {
                out.add("  # Unsupported pop segment: " + segment);
            }
        }

        // ---------------- STACK OPS ----------------
        private void binaryOp(String op) {
            out.add("  lw   $t0, 0($sp)      # y");
            out.add("  addi $sp, $sp, 4");
            out.add("  lw   $t1, 0($sp)      # x");
            out.add("  addi $sp, $sp, 4");

            if (op.equals("add")) out.add("  add  $t2, $t1, $t0");
            else if (op.equals("sub")) out.add("  sub  $t2, $t1, $t0");
            else if (op.equals("and")) out.add("  and  $t2, $t1, $t0");
            else if (op.equals("or"))  out.add("  or   $t2, $t1, $t0");

            out.add("  addi $sp, $sp, -4");
            out.add("  sw   $t2, 0($sp)");
        }

        private void unaryNeg() {
            out.add("  lw   $t0, 0($sp)");
            out.add("  sub  $t0, $zero, $t0");
            out.add("  sw   $t0, 0($sp)");
        }

        private void unaryNot() {
            // if top==0 -> push 1 else push 0
            String L_TRUE = "NOT_TRUE_" + (++cmpCounter);
            String L_END  = "NOT_END_" + (cmpCounter);

            out.add("  lw   $t0, 0($sp)");
            out.add("  beq  $t0, $zero, " + L_TRUE);
            out.add("  li   $t0, 0");
            out.add("  j    " + L_END);
            out.add(L_TRUE + ":");
            out.add("  li   $t0, 1");
            out.add(L_END + ":");
            out.add("  sw   $t0, 0($sp)");
        }

        // ---------------- COMPARISONS ----------------
        private void compareOp(String kind) {
            String L_TRUE = "CMP_TRUE_" + (++cmpCounter);
            String L_END  = "CMP_END_" + (cmpCounter);

            out.add("  lw   $t0, 0($sp)   # y");
            out.add("  addi $sp, $sp, 4");
            out.add("  lw   $t1, 0($sp)   # x");
            out.add("  addi $sp, $sp, 4");

            if (kind.equals("eq")) {
                out.add("  beq  $t1, $t0, " + L_TRUE);
            } else if (kind.equals("lt")) {
                out.add("  slt  $t2, $t1, $t0");
                out.add("  bne  $t2, $zero, " + L_TRUE);
            } else if (kind.equals("gt")) {
                out.add("  slt  $t2, $t0, $t1"); // y < x
                out.add("  bne  $t2, $zero, " + L_TRUE);
            }

            out.add("  li   $t3, 0");
            out.add("  j    " + L_END);
            out.add(L_TRUE + ":");
            out.add("  li   $t3, 1");
            out.add(L_END + ":");

            out.add("  addi $sp, $sp, -4");
            out.add("  sw   $t3, 0($sp)");
        }

        // ---------------- CONTROL FLOW ----------------
        private void ifGoto(String label) {
            out.add("  lw   $t0, 0($sp)");
            out.add("  addi $sp, $sp, 4");
            out.add("  bne  $t0, $zero, " + label);
        }

        // ---------------- CALL SUPPORT ----------------
        private void translateCall(String[] parts) {
            // format: call <name> <nArgs>
            String name = parts[1];
            int nArgs = Integer.parseInt(parts[2]);

            if (name.equals("print") && nArgs == 1) {
                // pop value and print integer
                out.add("  lw   $a0, 0($sp)");
                out.add("  addi $sp, $sp, 4");
                out.add("  li   $v0, 1");
                out.add("  syscall");

                // newline
                out.add("  li   $v0, 4");
                out.add("  la   $a0, NL");
                out.add("  syscall");

                // print returns nothing, but to keep stack stable we push 0 (optional)
                out.add("  addi $sp, $sp, -4");
                out.add("  li   $t0, 0");
                out.add("  sw   $t0, 0($sp)");
                return;
            }

            if (name.equals("Math.multiply") && nArgs == 2) {
                // pop y, pop x, push x*y
                out.add("  lw   $t0, 0($sp)   # y");
                out.add("  addi $sp, $sp, 4");
                out.add("  lw   $t1, 0($sp)   # x");
                out.add("  addi $sp, $sp, 4");
                out.add("  mul  $t2, $t1, $t0");
                out.add("  addi $sp, $sp, -4");
                out.add("  sw   $t2, 0($sp)");
                return;
            }

            if (name.equals("Math.divide") && nArgs == 2) {
                // pop y, pop x, push x/y
                // MIPS div gives quotient in LO
                out.add("  lw   $t0, 0($sp)   # y");
                out.add("  addi $sp, $sp, 4");
                out.add("  lw   $t1, 0($sp)   # x");
                out.add("  addi $sp, $sp, 4");
                out.add("  div  $t1, $t0");
                out.add("  mflo $t2");
                out.add("  addi $sp, $sp, -4");
                out.add("  sw   $t2, 0($sp)");
                return;
            }

            out.add("  # Unsupported call: " + name + " " + nArgs);
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Paste VM code (end with line: END):");

        List<String> vm = new ArrayList<>();
        while (true) {
            String line = sc.nextLine();
            if (line.equals("END")) break;
            vm.add(line);
        }

        Translator t = new Translator();
        List<String> mips = t.translate(vm);

        System.out.println("\n--- MIPS OUTPUT ---");
        for (String s : mips) System.out.println(s);
    }
}
