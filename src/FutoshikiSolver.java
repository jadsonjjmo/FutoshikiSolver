import java.io.*;
import java.util.*;

/**
 * @author Jadson Oliveira <jadsonjjmo@gmail.com>
 */

public class FutoshikiSolver {

    static BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    static BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(System.out));
    static StringBuilder stringBuilder = new StringBuilder();


    public static void main(String[] args) throws IOException {

        String inputPath = "";
        int heuristicType = 0;

        try {
            inputPath = args[0];
            heuristicType = Integer.parseInt(args[1]);
        } catch (Exception e) {
            System.err.println("Please use: [0] inputPath, [1] heuristic type");
            System.exit(-1);
        }


        final int quantityOfTests = Integer.parseInt(bufferedReader.readLine());

        for (int i = 0; i < quantityOfTests; i++) {

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

            final int[][] initialBoard = new int[dimension][dimension];

            for (int j = 0; j < dimension; j++) {
                stringTokenizer = new StringTokenizer(bufferedReader.readLine());
                for (int k = 0; k < dimension; k++) {
                    initialBoard[j][k] = Integer.parseInt(stringTokenizer.nextToken());
                }
            }


            stringBuilder.append(i).append("\n");
            printBoard(backtrackingSearch(heuristicType, initialBoard, constraints));

            //Read break line after each test case, except the last of them
            if (i + 1 < quantityOfTests) {
                bufferedReader.readLine();
            }
        }

        bufferedWriter.write(stringBuilder.toString());
        bufferedWriter.flush();
        bufferedWriter.close();
    }


    private static int[][] backtrackingSearch(final int heuristicType, int[][] board, int[][] constraints) {

        return backtracking(board, constraints);

    }

    private static int[][] backtracking(final int[][] board, final int[][] constraints) {
        final Stack<int[][]> tree = new Stack<>();

        tree.add(board);

        while (!tree.isEmpty()) {

            final int[][] currentBoard = tree.pop();

            if (isSolution(currentBoard, constraints)) {
                return currentBoard;
            }

            final ArrayList<int[][]> listOfChildren = getChildren(currentBoard, constraints);

            for (final int[][] nextChildren : listOfChildren) {
                tree.push(nextChildren);
            }

        }

        return null;
    }


    private static boolean isSolution(int[][] board, int[][] constraints) {

        //possible number 0 >= x <= board.length
        final boolean[][][] visited = new boolean[2][board.length][board.length + 1];

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {

                final int number = board[i][j];

                if (visited[0][i][number] || visited[1][j][number] || number == 0) {
                    return false;
                }

                visited[0][i][number] = true;
                visited[1][j][number] = true;

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

    private static ArrayList<int[][]> getChildren01(final int[][] board, final int[][] constraints) {

        ArrayList<int[][]> listOfChildren = new ArrayList<>();

        final boolean[][][] visited = new boolean[2][board.length][board.length + 1];

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                final int number = board[i][j];

                visited[0][i][number] = true;
                visited[1][j][number] = true;
            }
        }

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                for (int k = 1; k <= board.length; k++) {
                    if (board[i][j] == 0) {
                        if (!visited[0][i][k] && !visited[1][j][k]) {
                            final int[][] boardClone = cloneBoard(board);
                            boardClone[i][j] = k;
                            if (!breakConstraint(boardClone, constraints)) {
                                listOfChildren.add(boardClone);
                            }
                        }
                    }
                }
            }
        }

        return listOfChildren;
    }

    /**
     * getChildren search the next empty space and return all the possibilities for fill that space,
     * following the constraints.
     *
     * @param board       The board to be analysed
     * @param constraints The constraints of the problem
     * @return a list containing the possibilities for fill the next empty space
     */
    private static ArrayList<int[][]> getChildren(final int[][] board, final int[][] constraints) {

        ArrayList<int[][]> listOfChildren = new ArrayList<>();

        final boolean[][][] visited = new boolean[2][board.length][board.length + 1];

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                final int number = board[i][j];

                visited[0][i][number] = true;
                visited[1][j][number] = true;
            }
        }

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j] == 0) {
                    for (int k = 1; k <= board.length; k++) {
                        if (!visited[0][i][k] && !visited[1][j][k]) {
                            final int[][] boardClone = cloneBoard(board);
                            boardClone[i][j] = k;
                            if (!breakConstraint(boardClone, constraints)) {
                                listOfChildren.add(boardClone);
                            }
                        }
                    }
                    break;
                }
            }
        }

        return listOfChildren;
    }

    private static int[][] cloneBoard(final int[][] board) {

        int[][] newBoard = new int[board.length][board.length];

        for (int i = 0; i < board.length; i++) {
            newBoard[i] = Arrays.copyOf(board[i], board[i].length);
        }

        return newBoard;
    }

    private static String boardToString(final int[][] board) {
        String s = "";
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                s += board[i][j];
            }
        }

        return s;
    }

}
