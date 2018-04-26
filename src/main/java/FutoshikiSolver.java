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

    public static void main(String[] args) throws IOException {

        //System.setOut(new PrintStream("output.out"));

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

        stringBuilder.append("test,heuristicType,dimension,constraints,operations,checkMrv,solutionFound,second\n");

        double totalTotalTime = 0;
        int totalAttributions = 0;
        int correctTests = 0;

        final int quantityOfTests = Integer.parseInt(bufferedReader.readLine());

        for (int i = 0; i < quantityOfTests; i++) {

            //System.out.println(i+1);

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

            int quantityOfEmptyVariables = 0;

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
                    } else{
                        quantityOfEmptyVariables ++;
                    }
                }
            }

            int result[][] = null;

            for (int h = 1; h <= 1; h++) {

                heuristicType = h;
                double totalTime = 0;
                int iterations = 1;

                for (int iter = 0; iter < iterations; iter++) {


                    final int tempBoard[][] = new int[dimension + 2][dimension];

                    for (int q = 0; q < dimension + 2; q++) {
                        for (int w = 0; w < dimension; w++) {
                            tempBoard[q][w] = board[q][w];
                        }
                    }

                    final double initialTime = System.currentTimeMillis();

                    breakPoint = 0;
                    result = backtrackingSearch(heuristicType, tempBoard);

                    if (getSolution) {
                        stringBuilder.append(i + 1).append("\n");
                        printBoard(result);
                    }
                    //printBoard(backtrackingSearch(heuristicType, board));
                    //System.out.println(breakPoint);
                    //System.out.println((System.currentTimeMillis() - initialTime) / 1000);


                    //Read break line after each test case, except the last of them
                    final double endTime = System.currentTimeMillis();
                    totalTime += (endTime - initialTime);
                }

                totalTotalTime += ((totalTime / iterations) / 1000);
                totalAttributions += breakPoint;
                correctTests += (result != null) ? 1 : 0;
/*
                stringBuilder.append(i + 1).append(",").append(heuristicType).append(",")
                        .append(dimension).append(",").append(quantityOfConstraints).append(",").append(breakPoint)
                        .append(",").append(breakPoint == quantityOfEmptyVariables).append(",").append(result != null).append(",").append((totalTime / iterations) / 1000).append("\n");
            */
            }
            if (i + 1 < quantityOfTests) {
                bufferedReader.readLine();
            }
        }

        System.out.println("Total time: "+totalTotalTime);
        System.out.println("Correct tests: "+correctTests+" of "+quantityOfTests);
        System.out.println("Total attributions: "+totalAttributions);

        bufferedWriter.write(stringBuilder.toString());
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    private static int[][] backtrackingSearch(final int heuristicType, int[][] board) {
        return recursiveBacktracking(board, heuristicType);
    }

    private static int[][] recursiveBacktracking(int[][] board, int heuristicType) {
        if (isSolution(board)) {
            return board;
        }

        final Object[] variableAndOrderDomainValues = getVariableAndOrderDomainValues(board, heuristicType);

        final int row = (int) variableAndOrderDomainValues[0];
        final int column = (int) variableAndOrderDomainValues[1];
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
            System.out.println("No solution found!");
            stringBuilder.append("No solution found!\n");
        } else {
            for (int i = 0; i < dimension; i++) {
                for (int j = 0; j < dimension; j++) {
                    System.out.print(board[i][j]);
                    stringBuilder.append(board[i][j]);
                    if (j + 1 < dimension) {
                        System.out.print(" ");
                        stringBuilder.append(" ");
                    } else {
                        System.out.println();
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

    private static void mrvForwardChecking(int[][] board, Object[] result) {
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

    private static void mrvForwardChecking02(int[][] board, Object[] result) {
        int minValidValues = Integer.MAX_VALUE;

        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {

                if (board[i][j] == 0) {
                    //Forward checking
                    if (!forwardChecking(board, result)) {
                        return;
                    }

                    final int validValues = getValidValues02(board, i, j, board[dimension][i] | board[dimension + 1][j]);

                    if (validValues <= minValidValues) {
                        minValidValues = validValues;
                        result[0] = i;
                        result[1] = j;
                        if(minValidValues == 1){
                            return;
                        }
                    }
                }

            }
        }
    }

    private static void mrvForwardCheckingDegree02(int[][] board, Object[] result) {
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

    private static Object[] getVariableAndOrderDomainValues(int[][] board, int heuristicType) {

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
                mrvForwardChecking02(board, result);
                simpleDomainValues(board, result);
                break;
            case 3:
                mrvForwardChecking(board, result);
                simpleDomainValues(board, result);
                break;
            case 4:
                mrvForwardCheckingDegree02(board, result);
                simpleDomainValues(board, result);
                break;
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

    private static int getDegree(int row, int column) {
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

    private static int getValidValues02(int[][] board, int row, int column, int n) {
        //remove the first bit, because it always be zero
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

}
