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

    public static void main(String[] args) throws IOException {

        final String inputPath;
        final String outputPath;
        int heuristicType = 0;
        boolean getSolution = true;

        try {
            inputPath = args[0];
            outputPath = args[1];
            heuristicType = Integer.parseInt(args[2]);
            getSolution = Boolean.parseBoolean(args[3]);
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputPath)));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath)));
        } catch (Exception e) {
            System.err.println("Please use: [0] inputPath, [1] outputPath, [2] heuristic type, [3] getSolution\n" + e.getMessage());
            System.exit(-1);
        }

        stringBuilder.append("test,dimension,constraints,operations,solutionFound,second");


        final int quantityOfTests = Integer.parseInt(bufferedReader.readLine());

        for (int i = 0; i < quantityOfTests; i++) {

            double totalTime = 0;
            int iterations = 5;
            breakPoint = 1;

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


            stringBuilder.append(i + 1).append("\n");

            int result[][] = null;

            for (int h = 0; h <= 3; h++) {

                heuristicType = h;

                while (iterations-- > 0) {

                    final int tempBoard[][] = new int[dimension + 2][dimension];

                    for (int q = 0; q < dimension + 2; q++) {
                        System.arraycopy(board[q], 0, tempBoard[q], 0, dimension);
                    }

                    final double initialTime = System.currentTimeMillis();

                    result = backtrackingSearch(heuristicType, tempBoard);

                    if (getSolution) {
                        printBoard(result);
                    }
                    //printBoard(backtrackingSearch(heuristicType, board));
                    //System.out.println(breakPoint);
                    //System.out.println((System.currentTimeMillis() - initialTime) / 1000);


                    //Read break line after each test case, except the last of them
                    final double endTime = System.currentTimeMillis();
                    totalTime += (endTime - initialTime);
                }

                stringBuilder.append(i + 1).append(",").append(dimension).append(",")
                        .append(quantityOfConstraints).append(",").append(breakPoint)
                        .append(totalTime / iterations).append(",").append(result != null);

            }
            if (i + 1 < quantityOfTests) {
                bufferedReader.readLine();
            }
        }

        bufferedWriter.write(stringBuilder.toString());
        bufferedWriter.flush();
        bufferedWriter.close();
    }


    private static int[][] backtrackingSearch(final int heuristicType, int[][] board) {
        return recursiveBacktracking(board, heuristicType);
    }


    private static int[][] recursiveBacktracking(int[][] board, int heuristicType) {

        if (breakPoint++ > 1e6) {
            return null;
        }

        if (isSolution(board)) {
            return board;
        }

        final Object[] variableAndOrderDomainValues = getVariableAndOrderDomainValues(board, heuristicType);

        final int row = (int) variableAndOrderDomainValues[0];
        final int column = (int) variableAndOrderDomainValues[1];
        final ArrayList<Integer> orderDomainValues = (ArrayList<Integer>) variableAndOrderDomainValues[2];

        for (final int value : orderDomainValues) {
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

    private static boolean breakConstraint(int[][] board, int row, int column, int value) {

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

    private static boolean forwardChecking(int[][] board, Object[] result) {

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

    private static void mrvForwardChecking(int[][] board, Object[] result) {
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

    private static void mrvForwardCheckingDegree(int[][] board, Object[] result) {
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
                        maxDegree = getDegree(board, i, j);
                        result[0] = i;
                        result[1] = j;
                    } else if (validValues == minValidValues) {
                        final int currentDegree = getDegree(board, i, j);
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

    private static Object[] getVariableAndOrderDomainValues(int[][] board, int heuristicType) {

        final Object[] result = new Object[3];

        switch (heuristicType) {
            case 0:
                simpleUnassignedVariable(board, result);
                simpleDomainValues(board, result);
                break;
            case 1:
                simpleUnassignedVariable(board, result);
                if (forwardChecking(board, result)) {
                    simpleDomainValues(board, result);
                }
                break;
            case 2:
                mrvForwardChecking(board, result);
                simpleDomainValues(board, result);
                break;
            case 3:
                mrvForwardCheckingDegree(board, result);
        }

        return result;
    }

    private static void simpleDomainValues(int[][] board, Object[] result) {
        final ArrayList<Integer> listOfValidValues = new ArrayList<>();

        int row = (int) result[0];
        int column = (int) result[1];

        int validValues = board[dimension][row] | board[dimension + 1][column];

        for (int value = dimension; value >= 1; value--) {
            if (((1 << value) | validValues) != validValues) {
                if (!breakConstraint(board, row, column, value)) {
                    listOfValidValues.add(value);
                }
            }
        }

        result[2] = listOfValidValues;
    }

    private static int getDegree(final int[][] board, int row, int column) {
        return constraints.getOrDefault((row + "" + column), new ArrayList<>()).size();
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
