package calc;

import java.util.*;

import static java.lang.Double.NaN;
import static java.lang.Double.doubleToLongBits;
import static java.lang.Math.pow;
import static java.lang.Math.toRadians;
import static java.lang.System.out;


/*
 *   A calculator for rather simple arithmetic expressions
 *
 *   This is not the program, it's a class declaration (with methods) in it's
 *   own file (which must be named Calculator.java)
 *
 *   NOTE:
 *   - No negative numbers implemented
 */
class Calculator {

    // Here are the only allowed instance variables!
    // Error messages (more on static later)
    final static String MISSING_OPERAND = "Missing or bad operand";
    final static String DIV_BY_ZERO = "Division with 0";
    final static String MISSING_OPERATOR = "Missing operator or parenthesis";
    final static String OP_NOT_FOUND = "Operator not found";

    // Definition of operators
    final static String OPERATORS = "+-*/^";

    // Method used in REPL
    double eval(String expr) {
        if (expr.length() == 0) {
            return NaN;
        }
        List<String> tokens = tokenize(expr);
        Stack<String> postfix = infix2Postfix(tokens);
        return evalPostfix(postfix);
    }

    // ------  Evaluate RPN expression -------------------

    public double evalPostfix(Stack<String> postfix) {

        Stack<Double> operands = new Stack<Double>();

        for (int i = 0; i < postfix.size(); i++) {

            if(!isNumeric(postfix.get(i))) {
                String operator = postfix.get(i);

                if (operands.size() < 2) {
                    throw new IllegalArgumentException(MISSING_OPERAND);
                }

                double d1 = operands.pop();
                double d2 = operands.pop();

                double result = applyOperator(operator, d1, d2);
                operands.push(result);

            } else {
                // Operand found

                operands.push(Double.parseDouble(postfix.get(i)));
            }

        }

        return operands.pop();
    }

    double applyOperator(String op, double d1, double d2) {

        switch (op) {
            case "+":
                return d1 + d2;
            case "-":
                return d2 - d1;
            case "*":
                return d1 * d2;
            case "/":
                if (d1 == 0) {
                    throw new IllegalArgumentException(DIV_BY_ZERO);
                }
                return d2 / d1;
            case "^":
                return pow(d2, d1);
        }
        throw new RuntimeException(OP_NOT_FOUND);
    }

    // ------- Infix 2 Postfix ------------------------

    public Stack<String> infix2Postfix(List<String> tokens) {

        Stack<String> operatorStack = new Stack<>();
        Stack<String> postfixStack = new Stack<>();

        for (int i = 0; i < tokens.size(); i++) {

            if (isNumeric(tokens.get(i))) {

                postfixStack.push(tokens.get(i));

            } else {

                fixStacks(operatorStack, postfixStack, tokens.get(i));

            }

        }

        for (int i = operatorStack.size(); i > 0; i--) {

            if (operatorStack.get(operatorStack.size() - 1).equalsIgnoreCase("(") || operatorStack.get(operatorStack.size() - 1).equalsIgnoreCase(")")) {

                operatorStack.pop();
            } else {

                postfixStack.push(operatorStack.pop());
            }

        }

        return postfixStack;
    }

    enum OPERATION {

        POW(3),
        MULT_DIV(2),
        PLUS_MINUS(1),

        PARANTESIS(4);

        int priority;

        OPERATION(int i) {
            this.priority = i;
        }
    }

    OPERATION getOperationByString(String operator) {

        if (operator.equalsIgnoreCase("^")) {

            return OPERATION.POW;
        } else if (operator.equalsIgnoreCase("*") || operator.equalsIgnoreCase("/")) {

            return OPERATION.MULT_DIV;
        } else if (operator.equalsIgnoreCase("+") || operator.equalsIgnoreCase("-")) {

            return OPERATION.PLUS_MINUS;
        }

        return OPERATION.PARANTESIS;
    }

    void fixStacks(Stack<String> operatorStack, Stack<String> postfixStack, String addingToken) {

        while (true) {

            if (operatorStack.empty()) {

                operatorStack.push(addingToken);
                break;
            }

            String lastOperator = operatorStack.get(operatorStack.size() - 1);
            OPERATION addingOp = getOperationByString(addingToken);

            if (addingOp.priority == 4) {
                // Add everything between parantesis to postfix.

                if (addingToken.equalsIgnoreCase(")")) {

                    if (operatorStack.get(operatorStack.size() - 1).equalsIgnoreCase("(")) {

                        operatorStack.pop();
                        break;
                    }

                    postfixStack.push(operatorStack.pop());
                } else {

                    operatorStack.push(addingToken);
                    break;
                }


            } else if (shouldPopLastOperator(lastOperator, addingToken)) {
                // Operator too small - push last operator to postfix.

                postfixStack.push(operatorStack.pop());
            } else {
                // Else push to operator stack and break loop.

                operatorStack.push(addingToken);
                break;
            }

        }




    }

    public static boolean isNumeric(String strNum) {

        try {

            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException | NullPointerException nfe) {

            return false;
        }

        return true;
    }


    int getPrecedence(String op) {
        if ("+-".contains(op)) {
            return 2;
        } else if ("*/".contains(op)) {
            return 3;
        } else if ("^".contains(op)) {
            return 4;
        } else {
            throw new RuntimeException(OP_NOT_FOUND);
        }
    }

    boolean shouldPopLastOperator(String lastOperator, String addingOperator) {

        OPERATION lastOp = getOperationByString(lastOperator);
        OPERATION addingOp = getOperationByString(addingOperator);

        if (lastOp.priority == 4) {
            return false;
        }

        if (getAssociativity(addingOperator) == Assoc.RIGHT) {

            return lastOp.priority > addingOp.priority;
        }

        return lastOp.priority >= addingOp.priority;
    }

    Assoc getAssociativity(String op) {
        if ("+-*/".contains(op)) {
            return Assoc.LEFT;
        } else if ("^".contains(op)) {
            return Assoc.RIGHT;
        } else {
            throw new RuntimeException(OP_NOT_FOUND);
        }
    }

    enum Assoc {
        LEFT,
        RIGHT
    }

    // ---------- Tokenize -----------------------

    public List<String> tokenize(String expr) {

        List<String> tokens = new ArrayList<>();

        String tempString = "";

        for(int i = 0; i < expr.length(); i++) {

           // Spaces ignoreras.

            if(isNumeric(Character.toString(expr.charAt(i)))) {
                // Numeric

                tempString += expr.charAt(i);

            } else if(expr.charAt(i) != ' '){
                // Operator

                if(tempString != "") {
                    tokens.add(tempString);
                }

                tokens.add(Character.toString(expr.charAt(i)));
                tempString = "";
            }

            if(i + 1 > expr.length() - 1 && tempString != "") {

                tokens.add(tempString);
            }
        }


        return tokens;
    }

    // TODO Possibly more methods
}
