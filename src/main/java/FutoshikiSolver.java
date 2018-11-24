package main.java;

import java.io.*;
import java.util.*;

/**
 * @author Jadson Oliveira <jadsonjjmo@gmail.com>
 */

public class FutoshikiSolver {

    private final static StringBuilder stringBuilder = new StringBuilder();
    private static HashMap<String, List<String>> constraints;
    private static BufferedReader bufferedReader;
    private static BufferedWriter bufferedWriter;
    private static int breakPoint;
    private static int dimension;

    public static void main(final String[] args) throws IOException {

        final String inputPath;
        final String outputPath;
        int heuristicType = 0;

        try {
            inputPath = args[0];
            outputPath = args[1];
            heuristicType = Integer.parseInt(args[2]);
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputPath)));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath)));
        } catch (final Exception e) {
            System.err.println("Please use: [0] inputPath, [1] outputPath, [2] heuristic type\n" + e.getMessage());
            System.exit(-1);
        }

        final int quantityOfTests = Integer.parseInt(bufferedReader.readLine());

        for (int i = 0; i < quantityOfTests; i++) {

            StringTokenizer stringTokenizer = new StringTokenizer(bufferedReader.readLine());
            dimension = Integer.parseInt(stringTokenizer.nextToken());
            final int quantityOfConstraints = Integer.parseInt(stringTokenizer.nextToken());

            constraints = new HashMap<>();
            final int[][] board = new int[dimension + 2][dimension];

            for (int j = 0; j < quantityOfConstraints; j++) {
                stringTokenizer = new StringTokenizer(bufferedReader.readLine());
                final String l1 = stringTokenizer.nextToken();
                final String c1 = stringTokenizer.nextToken();
                final String l2 = stringTokenizer.nextToken();
                final String c2 = stringTokenizer.nextToken();

                final List<String> list1 = constraints.getOrDefault(l1 + c1, new ArrayList<>());
                final List<String> list2 = constraints.getOrDefault(l2 + c2, new ArrayList<>());
                list1.add('<' + l2 + c2);
                list2.add('>' + l1 + c1);
                constraints.put(l1 + c1, list1);
                constraints.put(l2 + c2, list2);
            }

            for (int j = 0; j < dimension; j++) {
                stringTokenizer = new StringTokenizer(bufferedReader.readLine());
                for (int k = 0; k < dimension; k++) {
                    final int value = Integer.parseInt(stringTokenizer.nextToken());
                    board[j][k] = value;

                    if (value > 0) {
                        //Set the value as used in column and row
                        //dimension = row
                        board[dimension][j] |= (1 << value);
                        //dimension + 1 = column
                        board[dimension + 1][k] |= (1 << value);
                    }

                }
            }

            breakPoint = 0;

            final int[][] result = recursiveBacktracking(board, heuristicType);

            stringBuilder.append(i + 1).append("\n");
            printBoard(result);

            if (i + 1 < quantityOfTests) {
                bufferedReader.readLine();
            }
        }
        bufferedWriter.write(stringBuilder.toString());
        bufferedWriter.flush();
        bufferedWriter.close();
    }


    /**
     * This is the main method for backtracking approach
     *
     * @param board         A initial or partial board
     * @param heuristicType The heuristic type to be used
     * @return A matrix with the possible solution
     */
    private static int[][] recursiveBacktracking(final int[][] board, final int heuristicType) {

        if (isSolution(board)) {
            return board;
        }

        final Object[] variableAndOrderDomainValues = getVariableAndOrderDomainValues(board, heuristicType);

        final int row = (int) variableAndOrderDomainValues[0];
        final int column = (int) variableAndOrderDomainValues[1];

        //noinspection unchecked
        final ArrayList<Integer> orderDomainValues = (ArrayList<Integer>) variableAndOrderDomainValues[2];

        for (final int value : orderDomainValues) {
            if (breakPoint < 1e6) {
                breakPoint++;
                board[row][column] = value;
                //Set the bit represented by value as used in column and row
                board[dimension][row] |= (1 << value);
                board[dimension + 1][column] |= (1 << value);
                final int[][] result = recursiveBacktracking(board, heuristicType);

                if (result != null) {
                    return result;
                }

                //Unset the bit represented by value as used in column and row
                board[dimension][row] ^= (1 << value);
                board[dimension + 1][column] ^= (1 << value);

                board[row][column] = 0;
            } else {
                break;
            }
        }

        return null;

    }


    /**
     * This method will redirect the getting of variables and domain values following the heuristic informed
     * on parameters.
     *
     * @param board         A matrix with the current board state
     * @param heuristicType The method to be used (BT, BT + FC, BT + FC + MRV, ...)
     * @return An object containing the row and column (variable information) and the domain valid values for that
     * specific variable.
     */
    private static Object[] getVariableAndOrderDomainValues(final int[][] board, final int heuristicType) {

        final Object[] result = new Object[3];

        switch (heuristicType) {
            case 0:
                simpleUnassignedVariable(board, result);
                simpleDomainValues(board, result);
                break;
            case 1:
                simpleUnassignedVariable(board, result);
                simpleDomainValues(board, result);
                forwardChecking(board, result);
                break;
            case 2:
                mrvForwardChecking(board, result);
                simpleDomainValues(board, result);
                break;
            case 3:
                mrvForwardChecking02(board, result);
                simpleDomainValues(board, result);
                break;
            case 4:
                mrvForwardCheckingDegree02(board, result);
                simpleDomainValues(board, result);
                break;
            default:
                System.err.println("Heuristic does not exist!");
                break;
        }

        return result;
    }


    /**
     * Return the first empty variable
     *
     * @param board  A matrix with the current board state
     * @param result An object with [0] and [1] positions filled (row and column information)
     */
    private static void simpleUnassignedVariable(final int[][] board, final Object[] result) {
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                if (board[i][j] == 0) {
                    result[0] = i;
                    result[1] = j;
                    return;
                }
            }
        }
    }


    /**
     * This method checks if there is any variable with no valid values available
     *
     * @param board  A matrix with the current board state
     * @param result An object with the variable and domain values information
     * @return true if there is any variable with no valid values and false otherwise
     */
    private static boolean forwardChecking(final int[][] board, final Object[] result) {
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {

                if (board[i][j] == 0) {

                    if ((board[dimension][i] | board[dimension + 1][j]) == (1 << (dimension + 1)) - 2) {
                        result[0] = i;
                        result[1] = j;
                        result[2] = new ArrayList<>();
                        return false;
                    }

                }

            }
        }
        return true;
    }


    /**
     * This method follows the default MRV rules and return the variable with minimum conflicting values
     *
     * @param board  A matrix with the current board state
     * @param result An object with [0] and [1] positions filled (row and column information)
     */
    private static void mrvForwardChecking(final int[][] board, final Object[] result) {
        int minValidValues = Integer.MAX_VALUE;

        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {

                if (board[i][j] == 0) {
                    //Forward checking
                    if (!forwardChecking(board, result)) {
                        return;
                    }

                    final int validValues = getValidValues02(board, i, j, board[dimension][i] | board[dimension + 1][j]);

                    if (validValues < minValidValues) {
                        minValidValues = validValues;
                        result[0] = i;
                        result[1] = j;
                        if (minValidValues == 1) {
                            return;
                        }
                    }
                }

            }
        }
    }


    /**
     * This method will return the variable with minimum conflicting values (considering row and column)
     *
     * @param board  A matrix with the current board state
     * @param result An object with [0] and [1] positions filled (row and column information)
     */
    private static void mrvForwardChecking02(final int[][] board, final Object[] result) {
        int minValidValues = Integer.MAX_VALUE;

        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {

                if (board[i][j] == 0) {
                    //Forward checking
                    if (!forwardChecking(board, result)) {
                        return;
                    }

                    final int validValues = getValidValues(board[dimension][i] | board[dimension + 1][j]);

                    if (validValues < minValidValues) {
                        minValidValues = validValues;
                        result[0] = i;
                        result[1] = j;
                        if (minValidValues == 1) {
                            return;
                        }
                    }
                }

            }
        }
    }


    /**
     * This method will return the variable with minimum valid values, with degree heuristic tie breaker.
     *
     * @param board  A matrix with the current board state
     * @param result An object with the [0] and [1] positions filled (row and column information)
     */
    private static void mrvForwardCheckingDegree02(final int[][] board, final Object[] result) {
        int minValidValues = Integer.MAX_VALUE;
        int maxDegree = 0;

        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {

                if (board[i][j] == 0) {
                    //Forward checking
                    if (!forwardChecking(board, result)) {
                        return;
                    }

                    final int validValues = getValidValues02(board, i, j, board[dimension][i] | board[dimension + 1][j]);

                    if (validValues < minValidValues) {
                        minValidValues = validValues;
                        maxDegree = getDegree(i, j);
                        result[0] = i;
                        result[1] = j;
                    } else if (validValues == minValidValues) {
                        final int currentDegree = getDegree(i, j);
                        if (currentDegree > maxDegree) {
                            minValidValues = validValues;
                            maxDegree = currentDegree;
                            result[0] = i;
                            result[1] = j;
                        }
                    }
                }

            }
        }
    }


    /**
     * This method will fill the domain valid values fora a specific variable. It will fill the result[2] value.
     *
     * @param board  A matrix with the current board state
     * @param result Result is a variable that contains the row, column and domain values information
     */
    private static void simpleDomainValues(final int[][] board, final Object[] result) {
        final ArrayList<Integer> listOfValidValues = new ArrayList<>();

        final int row = (int) result[0];
        final int column = (int) result[1];

        final int validValues = board[dimension][row] | board[dimension + 1][column];

        for (int value = dimension; value >= 1; value--) {
            if (((1 << value) | validValues) != validValues) {
                if (!breakConstraint(board, row, column, value)) {
                    listOfValidValues.add(value);
                }
            }
        }

        result[2] = listOfValidValues;
    }


    /**
     * This method will compare if the board has any empty space or if is complete.
     *
     * @param board A board with the possible solution
     * @return true case the board is complete or false otherwise
     */
    private static boolean isSolution(final int[][] board) {

        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {

                final int number = board[i][j];

                if (number == 0) {
                    return false;
                }
            }
        }

        return true;
    }


    /**
     * This method returns true if when assigning some constraint is broken or true otherwise.
     *
     * @param board  The current board state
     * @param row    The row to be analysed
     * @param column The column to be analysed
     * @param value  The value to be analysed
     * @return true if constraints are being respected or false, otherwise.
     */
    private static boolean breakConstraint(final int[][] board, final int row, final int column, final int value) {

        final List<String> listOfConstraints = constraints.get((row + 1) + "" + (column + 1));

        if (listOfConstraints != null) {
            for (final String constraint : listOfConstraints) {
                final char operator = constraint.charAt(0);
                final int row2 = Character.getNumericValue(constraint.charAt(1)) - 1;
                final int column2 = Character.getNumericValue(constraint.charAt(2)) - 1;
                final int value2 = board[row2][column2];

                if (operator == '<' && (value2 != 0 && value2 < value)) {
                    return true;
                } else if (operator == '>' && (value2 != 0 && value2 > value)) {
                    return true;
                }

            }
        }

        return false;
    }


    /**
     * This method will return the quantity of degrees for a specific variable. The degrees follow the constraint of
     * smaller and larger value for a specific variable.
     *
     * @param row    The row of the variable to be analysed.
     * @param column The column of the variable to be analysed.
     * @return The corresponding degree.
     */
    private static int getDegree(final int row, final int column) {
        return constraints.getOrDefault((row + "" + column), new ArrayList<>()).size();
    }


    /**
     * This method will return the quantity of valid values (respecting only the exclusivity constraint)
     *
     * @param n The or bit operation between the row and column valid values
     * @return The quantity of valid values respecting the constraints
     */
    private static int getValidValues(int n) {
        //remove the first bit, because it always be zero
        int count = 0;

        while (n > 0) {
            if (n % 2 != 0) {
                count++;
            }
            n /= 2;
        }

        return dimension - count;
    }


    /**
     * This method will return the quantity of valid values to the specific domain, following all the constraints of the
     * problem.
     *
     * @param board  A matrix with the current board state
     * @param row    The row to be analysed
     * @param column The column to be analysed
     * @param n      The or bit operation between the row and column
     * @return The quantity of valid values respecting all constraints
     */
    private static int getValidValues02(final int[][] board, final int row, final int column, int n) {
        //remove the first bit, because it always be zero (1<<1 = 00010 for a 4x4 board)
        int count = 0;
        n >>>= 1;

        for (int i = 1; i <= dimension; i++) {

            if (n % 2 == 0) {
                if (!breakConstraint(board, row, column, i)) {
                    count++;
                }
            }

            n /= 2;

        }

        return count;
    }


    /**
     * Prints the board or a fault message if the board is null
     *
     * @param board A board with the possible solution
     */
    private static void printBoard(final int[][] board) {

        if (board == null) {
            stringBuilder.append(breakPoint >= 1e6 ? "Numero de atribuicoes excede limite maximo\n" : "No solution found\n");
        } else {
            for (int i = 0; i < dimension; i++) {
                for (int j = 0; j < dimension; j++) {
                    stringBuilder.append(board[i][j]);
                    if (j + 1 < dimension) {
                        stringBuilder.append(" ");
                    } else {
                        stringBuilder.append("\n");
                    }
                }
            }
        }

    }

}