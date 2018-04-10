import java.io.*;
import java.util.*;

/**
 * @author Jadson Oliveira <jadsonjjmo@gmail.com>
 */

public class FutoshikiSolver {

    static BufferedReader bufferedReader;
    static BufferedWriter bufferedWriter;
    static StringBuilder stringBuilder = new StringBuilder();
    static int breakPoint;


    public static void main(String[] args) throws IOException {

        String inputPath;
        String outputPath;
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
        return recursiveBacktracking(board, constraints);
    }


    private static int[][] recursiveBacktracking(int[][] board, int[][] constraints) {

        if (breakPoint++ > 1000000) return null;

        if (isSolution(board, constraints)) {
            return board;
        }

        final int[] variable = selectUnassignedVariable(board);

        for (int i = board.length; i >= 1; i--) {

            if (!checkValue(board, i, variable[0], variable[1])) continue;

            board[variable[0]][variable[1]] = i;

            if (!breakConstraint(board, constraints)) {

                final int[][] result = recursiveBacktracking(board, constraints);

                if (result != null) {
                    return result;
                }
            }

            board[variable[0]][variable[1]] = 0;

        }

        return null;

    }


    private static boolean isSolution(int[][] board, int[][] constraints) {

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

    private static int[] selectUnassignedVariable(int[][] board) {

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j] == 0) {
                    return new int[]{i, j};
                }
            }
        }

        return new int[]{};
    }


    private static boolean checkValue(int[][] board, int value, int l, int c) {

        for (int i = 0; i < board.length; i++) {
            if (board[l][i] == value || board[i][c] == value) {
                return false;
            }
        }

        return true;

    }

}
