import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
public class Truth_Table{
     public static void main(String[] args) {
        
        // Frame
         JFrame frame = new JFrame("Truth Table Generator");
        frame.setSize(1200, 1000);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());
        frame.getContentPane().setBackground(Color.WHITE);

        // Panel 
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(800, 600));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createLineBorder(new Color(120, 120, 180), 4));
        panel.setLayout(null);

        // Title
        JLabel title = new JLabel("Truth Table Generator");
        title.setFont(new Font("Arial", Font.BOLD, 30));
        title.setForeground(new Color(70, 70, 150));
        title.setBounds(50, 20, 600, 40);

        // Description
        JTextArea desc = new JTextArea(
                "This tool generates truth tables for logical expressions.\n" +
                "Use operators: &, |, !, ->, <-> and brackets ().\n" +
                "For Example:(A & B)-> !C"
        );
        desc.setFont(new Font("Arial", Font.PLAIN, 14));
        desc.setWrapStyleWord(true);
        desc.setLineWrap(true);
        desc.setEditable(false);
        desc.setBackground(panel.getBackground());
        desc.setBounds(50, 80, 600, 60);

        // Input field
        JTextField input = new JTextField();
        input.setFont(new Font("Arial", Font.PLAIN, 16));
        input.setHorizontalAlignment(JTextField.CENTER);
        input.setBounds(220, 150, 250, 35);

        // Button
        JButton button = new JButton("Generate");
        button.setBounds(220, 200, 120, 30);

        // Output area
        JTextArea output = new JTextArea();
        output.setFont(new Font("Monospaced", Font.PLAIN, 20));
        output.setEditable(false);

        JScrollPane scroll = new JScrollPane(output);
        scroll.setBounds(50, 250, 600, 300);

         JButton clearBtn = new JButton("Clear");
        clearBtn.setBounds(350, 200, 120, 30);

        // Add components
        panel.add(title);
        panel.add(desc);
        panel.add(input);
        panel.add(button);
        panel.add(scroll);
        panel.add(clearBtn);

        frame.add(panel);
        frame.setVisible(true);
         // Generate button
        button.addActionListener(e -> {
            String logical_exp = input.getText().replaceAll(" ", "").toUpperCase();

            if (!isValidExpression(logical_exp)) {
                JOptionPane.showMessageDialog(frame, "Invalid Expression!");
                return;
            }

            String result = generates_table(logical_exp);
            output.setText(result);
        });

        // Clear button
        clearBtn.addActionListener(e -> {
            input.setText("");
            output.setText("");
        });
    }

    // Check for invalid expression by trying to parse it
    static boolean isValidExpression(String logical_exp) {
        if (logical_exp.trim().isEmpty()) return false;
        try {
            new Parser(logical_exp).parse();
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // Generates the truth table
    static String generates_table(String logical_exp) {
        List<Character> vars = new ArrayList<>();
        for (char c : logical_exp.toCharArray()) {
            if (Character.isUpperCase(c) && !vars.contains(c)) {
                vars.add(c);
            }
        }
        // Sort variables alphabetically
        Collections.sort(vars);

        Node root;
        try {
            root = new Parser(logical_exp).parse();
        } catch (IllegalArgumentException e) {
            return "Invalid Expression: " + e.getMessage();
        }

        StringBuilder result_text = new StringBuilder();
        int n = vars.size();
        int rows = 1 << n;

        // Header of table
        for (char v : vars) {
            result_text.append(v).append(" ");
        }
        result_text.append("| Result\n");

        Map<Character, Boolean> values = new HashMap<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < n; j++) {
                boolean val = ((i >> (n - j - 1)) & 1) == 1;
                values.put(vars.get(j), val);
                result_text.append(val ? "1 " : "0 ");
            }

            boolean result = root.eval(values);
            result_text.append("| ").append(result ? "1" : "0").append("\n");
        }

        return result_text.toString();
    }

    // AST Node implementations
    interface Node {
        boolean eval(Map<Character, Boolean> values);
    }

    static class VarNode implements Node {
        private final char name;
        VarNode(char name) { this.name = name; }
        public boolean eval(Map<Character, Boolean> values) {
            return values.getOrDefault(name, false);
        }
    }

    static class NotNode implements Node {
        private final Node child;
        NotNode(Node child) { this.child = child; }
        public boolean eval(Map<Character, Boolean> values) {
            return !child.eval(values);
        }
    }

    static class AndNode implements Node {
        private final Node left, right;
        AndNode(Node left, Node right) { this.left = left; this.right = right; }
        public boolean eval(Map<Character, Boolean> values) {
            return left.eval(values) && right.eval(values);
        }
    }

    static class OrNode implements Node {
        private final Node left, right;
        OrNode(Node left, Node right) { this.left = left; this.right = right; }
        public boolean eval(Map<Character, Boolean> values) {
            return left.eval(values) || right.eval(values);
        }
    }

    static class ImpliesNode implements Node {
        private final Node left, right;
        ImpliesNode(Node left, Node right) { this.left = left; this.right = right; }
        public boolean eval(Map<Character, Boolean> values) {
            return !left.eval(values) || right.eval(values);
        }
    }

    static class EquivNode implements Node {
        private final Node left, right;
        EquivNode(Node left, Node right) { this.left = left; this.right = right; }
        public boolean eval(Map<Character, Boolean> values) {
            return left.eval(values) == right.eval(values);
        }
    }

    // Recursive descent parser
    static class Parser {
        private final String src;
        private int pos = 0;

        Parser(String src) {
            // Remove all whitespace characters
            this.src = src.replaceAll("\\s+", "");
        }

        private char peek() {
            if (pos >= src.length()) return '\0';
            return src.charAt(pos);
        }

        private char consume() {
            if (pos >= src.length()) return '\0';
            return src.charAt(pos++);
        }

        private void expect(String s) {
            for (int i = 0; i < s.length(); i++) {
                if (pos >= src.length() || src.charAt(pos) != s.charAt(i)) {
                    throw new IllegalArgumentException("Expected '" + s + "' at position " + pos);
                }
                pos++;
            }
        }

        Node parse() {
            Node node = parseEquivalence();
            if (pos < src.length()) {
                throw new IllegalArgumentException("Unexpected character '" + src.charAt(pos) + "' at position " + pos);
            }
            return node;
        }

        private Node parseEquivalence() {
            Node node = parseImplication();
            while (peek() == '<') {
                expect("<->");
                Node right = parseImplication();
                node = new EquivNode(node, right);
            }
            return node;
        }

        private Node parseImplication() {
            Node node = parseDisjunction();
            if (peek() == '-') {
                expect("->");
                Node right = parseImplication();
                node = new ImpliesNode(node, right);
            }
            return node;
        }

        private Node parseDisjunction() {
            Node node = parseConjunction();
            while (peek() == '|') {
                consume();
                Node right = parseConjunction();
                node = new OrNode(node, right);
            }
            return node;
        }

        private Node parseConjunction() {
            Node node = parseUnary();
            while (peek() == '&') {
                consume();
                Node right = parseUnary();
                node = new AndNode(node, right);
            }
            return node;
        }

        private Node parseUnary() {
            if (peek() == '!') {
                consume();
                return new NotNode(parseUnary());
            }
            return parsePrimary();
        }

        private Node parsePrimary() {
            char c = peek();
            if (Character.isUpperCase(c)) {
                consume();
                return new VarNode(c);
            } else if (c == '(') {
                consume();
                Node node = parseEquivalence();
                if (consume() != ')') {
                    throw new IllegalArgumentException("Expected ')' at position " + pos);
                }
                return node;
            } else {
                throw new IllegalArgumentException("Unexpected character '" + (c == '\0' ? "EOF" : c) + "'");
            }
        }
    }
}
