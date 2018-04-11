import java.io.*;
import java.util.*;

/**
 * @author Jadson Oliveira <jadsonjjmo@gmail.com>
 */

public class FutoshikiSolver {

    private static BufferedReader bufferedReader;
    private static BufferedWriter bufferedWriter;
    private static int breakPoint;
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
            final int dimension = Integer.parseInt(stringTokenizer.nextToken());
            final int quantityOfConstraints = Integer.parseInt(stringTokenizer.nextToken());

            final int[][] constraints = new int[quantityOfConstraints][4];

            for (int j = 0; j < quantityOfConstraints; j++) {
                stringTokenizer = new StringTokenizer(bufferedReader.readLine());
                for (int k = 0; k < 4; k++) {
                    constraints[j][k] = Integer.parseInt(stringTokenizer.nextToken()) - 1;
                }
            }

            final int[][] board = new int[dimension][dimension];

            for (int j = 0; j < dimension; j++) {
                stringTokenizer = new StringTokenizer(bufferedReader.readLine());
                for (int k = 0; k < dimension; k++) {
                    board[j][k] = Integer.parseInt(stringTokenizer.nextToken());
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

        if (breakPoint++ > 1000000) {
            return null;
        }

        if (isSolution(board)) {
            return board;
        }

        final Object[] variableAndOrderDomainValues = getVariableAndOrderDomainValues(board, constraints, heuristicType);

        final int line = (int) variableAndOrderDomainValues[0];
        final int column = (int) variableAndOrderDomainValues[1];
        final ArrayList<Integer> orderDomainValues = (ArrayList<Integer>) variableAndOrderDomainValues[2];

        for (final int value : orderDomainValues) {
            board[line][column] = value;
            final int[][] result = recursiveBacktracking(board, constraints, heuristicType);

            if (result != null) {
                return result;
            }

            board[line][column] = 0;
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

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {

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
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board.length; j++) {
                    stringBuilder.append(board[i][j]);
                    if (j + 1 < board.length) {
                        stringBuilder.append(" ");
                    } else {
                        stringBuilder.append("\n");
                    }
                }
            }
        }

    }

    private static void simpleUnassignedVariable(int[][] board, Object[] result) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j] == 0) {
                    result[0] = i;
                    result[1] = j;
                    return;
                }
            }
        }
    }

    private static void mrvHeuristic(int[][] board, int[][] constraints, Object[] result) {
        int minValidValues = Integer.MAX_VALUE;

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {

                if (board[i][j] == 0) {

                    final ArrayList<Integer> listOfValidValues = new ArrayList<>();

                    for (int value = 1; value <= board.length; value++) {
                        if (isValidValue(board, value, i, j, constraints)) {
                            listOfValidValues.add(value);
                        }
                    }

                    if (listOfValidValues.size() < minValidValues) {
                        minValidValues = listOfValidValues.size();
                        result[0] = i;
                        result[1] = j;
                        result[2] = listOfValidValues;
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
                mrvHeuristic(board, constraints, result);
                break;
        }

        return result;
    }

    private static void simpleDomainValues(int[][] board, int[][] constraints, Object[] result) {
        final ArrayList<Integer> listOfValidValues = new ArrayList<>();

        int line = (int) result[0];
        int column = (int) result[1];

        for (int value = board.length; value >= 1; value--) {
            if (isValidValue(board, value, line, column, constraints)) {
                listOfValidValues.add(value);
            }
        }

        result[2] = listOfValidValues;
    }

    private static boolean isValidValue(final int[][] board, final int value, final int line, final int column, int[][] constraints) {
        for (int i = 0; i < board.length; i++) {
            if (board[line][i] == value || board[i][column] == value) {
                return false;
            }
        }

        board[line][column] = value;
        if (breakConstraint(board, constraints)) {
            board[line][column] = 0;
            return false;
        }
        board[line][column] = 0;

        return true;
    }
}
