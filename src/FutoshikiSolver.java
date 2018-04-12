import java.io.*;
import java.util.*;

/**
 * @author Jadson Oliveira <jadsonjjmo@gmail.com>
 */

public class FutoshikiSolver {

    private static BufferedReader bufferedReader;
    private static BufferedWriter bufferedWriter;
    private static int breakPoint;
    private static int dimension;
    private final static StringBuilder stringBuilder = new StringBuilder();


    public static void main(String[] args) throws IOException {

        final String inputPath;
        final String outputPath;
        int heuristicType = 0;

        try {
            inputPath = args[0];
            outputPath = args[1];
            heuristicType = Integer.parseInt(args[2]);
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputPath)));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath)));
        } catch (Exception e) {
            System.err.println("Please use: [0] inputPath, [1] heuristic type\n" + e.getMessage());
            System.exit(-1);
        }


        double initialTime = System.currentTimeMillis();

        final int quantityOfTests = Integer.parseInt(bufferedReader.readLine());

        for (int i = 0; i < quantityOfTests; i++) {

            breakPoint = 1;

            StringTokenizer stringTokenizer = new StringTokenizer(bufferedReader.readLine());
            dimension = Integer.parseInt(stringTokenizer.nextToken());
            final int quantityOfConstraints = Integer.parseInt(stringTokenizer.nextToken());

            final int[][] constraints = new int[quantityOfConstraints][4];
            final int[][] board = new int[dimension + 2][dimension];

            for (int j = 0; j < quantityOfConstraints; j++) {
                stringTokenizer = new StringTokenizer(bufferedReader.readLine());
                for (int k = 0; k < 4; k++) {
                    constraints[j][k] = Integer.parseInt(stringTokenizer.nextToken()) - 1;
                }
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

            stringBuilder.append(i + 1).append("\n");
            printBoard(backtrackingSearch(heuristicType, board, constraints));
            //System.out.println(breakPoint);
            //System.out.println((System.currentTimeMillis() - initialTime) / 1000);


            //Read break line after each test case, except the last of them
            if (i + 1 < quantityOfTests) {
                bufferedReader.readLine();
            }
        }

        stringBuilder.append("Total time: ").append((System.currentTimeMillis() - initialTime) / 1000);

        bufferedWriter.write(stringBuilder.toString());
        bufferedWriter.flush();
        bufferedWriter.close();
    }


    private static int[][] backtrackingSearch(final int heuristicType, int[][] board, int[][] constraints) {
        return recursiveBacktracking(board, constraints, heuristicType);
    }


    private static int[][] recursiveBacktracking(int[][] board, int[][] constraints, int heuristicType) {

        if (breakPoint++ > 1e6) {
            return null;
        }

        if (isSolution(board)) {
            return board;
        }

        final Object[] variableAndOrderDomainValues = getVariableAndOrderDomainValues(board, constraints, heuristicType);

        final int row = (int) variableAndOrderDomainValues[0];
        final int column = (int) variableAndOrderDomainValues[1];
        final ArrayList<Integer> orderDomainValues = (ArrayList<Integer>) variableAndOrderDomainValues[2];

        for (final int value : orderDomainValues) {
            board[row][column] = value;
            //Set the value as used in column and row
            //dimension = row
            board[dimension][row] |= (1 << value);
            //dimension + 1 = column
            board[dimension + 1][column] |= (1 << value);
            final int[][] result = recursiveBacktracking(board, constraints, heuristicType);

            if (result != null) {
                return result;
            }

            //Set the value as used in column and row
            //dimension = row
            board[dimension][row] ^= (1 << value);
            //dimension + 1 = column
            board[dimension + 1][column] ^= (1 << value);

            board[row][column] = 0;
        }

        return null;

    }


    /**
     * This method will compare if the board has any empty space or if is complete.
     *
     * @param board A board with the possible solution
     * @return true case the board is complete or false otherwise
     */
    private static boolean isSolution(int[][] board) {

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

    private static boolean breakConstraint(int[][] board, int[][] constraints) {

        for (int i = 0; i < constraints.length; i++) {
            if (board[constraints[i][0]][constraints[i][1]] >= board[constraints[i][2]][constraints[i][3]] &&
                    (board[constraints[i][0]][constraints[i][1]] != 0 && board[constraints[i][2]][constraints[i][3]] != 0)) {
                return true;
            }
        }

        return false;
    }

    private static void printBoard(int[][] board) {

        if (board == null) {
            stringBuilder.append("No solution found!\n");
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

    private static void simpleUnassignedVariable(int[][] board, Object[] result) {
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

    private static boolean forwardChecking(int[][] board, int[][] constraints, Object[] result) {

        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {

                if (board[i][j] == 0) {

                    if ((board[dimension][i] | board[dimension + 1][j]) == (1 << dimension + 1) - 1) {
                        result[2] = new ArrayList<>();
                        return false;
                    }

                }

            }
        }

        return true;
    }

    private static void mrvForwardChecking(int[][] board, int[][] constraints, Object[] result) {
        int minValidValues = Integer.MAX_VALUE;

        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {

                if (board[i][j] == 0) {
                    //Forward checking
                    if ((board[dimension][i] | board[dimension + 1][j]) == ((1 << dimension + 1) - 1)) {
                        result[0] = i;
                        result[1] = j;
                        result[2] = new ArrayList<>();
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

    private static void mrvForwardCheckingDegree(int[][] board, int[][] constraints, Object[] result) {
        int minValidValues = Integer.MAX_VALUE;
        int maxDegree = 0;

        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {

                if (board[i][j] == 0) {
                    //Forward checking
                    if ((board[dimension][i] | board[dimension + 1][j]) == ((1 << dimension + 1) - 1)) {
                        result[0] = i;
                        result[1] = j;
                        result[2] = new ArrayList<>();
                        return;
                    }

                    final int validValues = getValidValues(board[dimension][i] | board[dimension + 1][j]);

                    if (validValues < minValidValues) {
                        minValidValues = validValues;
                        maxDegree = getDegree(board, constraints, i, j);
                        result[0] = i;
                        result[1] = j;
                    } else if (validValues == minValidValues) {
                        int currentDegree = getDegree(board, constraints, i, j);
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

    private static Object[] getVariableAndOrderDomainValues(int[][] board, int[][] constraints, int heuristicType) {

        Object[] result = new Object[3];

        switch (heuristicType) {
            case 0:
                simpleUnassignedVariable(board, result);
                simpleDomainValues(board, constraints, result);
                break;
            case 1:
                simpleUnassignedVariable(board, result);
                if (forwardChecking(board, constraints, result)) {
                    simpleDomainValues(board, constraints, result);
                }
                break;
            case 2:
                mrvForwardChecking(board, constraints, result);
                simpleDomainValues(board, constraints, result);
                break;
        }

        return result;
    }

    private static void simpleDomainValues(int[][] board, int[][] constraints, Object[] result) {
        final ArrayList<Integer> listOfValidValues = new ArrayList<>();

        int row = (int) result[0];
        int column = (int) result[1];

        int validValues = board[dimension][row] | board[dimension + 1][column];

        for (int value = dimension; value >= 1; value--) {
            if (((1 << value) | validValues) != validValues) {
                board[row][column] = value;
                if (!breakConstraint(board, constraints)) {
                    listOfValidValues.add(value);
                }
                board[row][column] = 0;
            }
        }

        result[2] = listOfValidValues;
    }


    private static int getDegree(final int[][] board, int[][] constraints, int row, int column) {
        int validValues = board[dimension][row] | board[dimension + 1][column];
        int breakConstraint = 0;

        for (int value = dimension; value >= 1; value--) {
            if (((1 << value) | validValues) != validValues) {
                board[row][column] = value;
                if (breakConstraint(board, constraints)) {
                    breakConstraint++;
                }
                board[row][column] = 0;
            }
        }

        return breakConstraint;
    }


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


}
