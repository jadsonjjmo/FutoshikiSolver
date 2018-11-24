# FutoshikiSolver

This project consists of a tool that uses a backtracking approach to solve Futoshiki,
which is a board game whose purpose is to fill all board fields with numbers, such that no digit is repeated in a row or column,
respecting some restrictions imposed, which inform if a certain board field number should be smaller or larger than another adjacent board field number.
To solve this problem, 3 methodologies, using the backtrack approach, were implemented and tested.

## Backtracking simples (BT)

The first approach consists of the basic implementation of a BT algorithm,
which does not use any heuristics to perform pruning methods in the tree of possibilities.
For this, the board was represented as an array **N**, where **N** is the number of rows
and columns of the board game.
To store the constraints, a Hash structure was used, in which the search and storage
complexity tends to be constant (*O(1)* and if there is a hash collision,
the programming language treats it efficiently, where the worst case becomes *O(log n)*).

The process of solving the problem using this approach is recursive,
traversing the columns of the matrix and performing assignments to the first empty board field (called here as variable),
being that the values ​​of the domain are always retrieved in descending order.
When a row is completely filled the search is performed on the next row,
and it should be noted that the valid values ​​for assignment respect all constraints imposed,
so it is not possible to fill the entire board with some broken constraint.
The state of the board is returned to a previous one whenever the tree of possibilities has
reached an unresolved leaf node. The procedure is terminated when a valid solution is found,
when the amount of assignments exceeds a limit of *10⁶* or when there is no valid solution
for the defined board game, i.e. all possibilities have been tested within the space of
*10⁶* assignments.

## BT + Forward Checking (FC)
FC is a pruning technique in the tree of possibilities that verifies the existence of 
variables with no valid domain value for each assignment (for any variable), if positive, 
performs the backtrack process, testing another domain value for the parent variable 
(previous variable). This procedure is performed until a solution is found, 
or all possibilities are tested (with pruning done), within a set limit.

For the implementation of this method of pruning, an auxiliary structure was used to update 
and recover the valid domain values ​​for each row and each column. 
This auxiliary structure is a set of bits to represent each row and each column. 
Therefore, for the representation of the state of the board, it used an 
array **N + 2** x **N**, where the row **N + 1** sets of bits, 
each representing the values ​​used in each row and the row **N + 2** of the array, 
also composed of **N** sets of bits, representing the values ​​used in each column.

![alt text](/latex/images/bitmask.png "Auxiliary structure working with bits for retrieval of valid values in the row and column.")

The figure above pictures the process of verifying valid values for a specific variable, 
where the red square is the variable that we want to know which values have not yet been used in any row or column, 
for this, an *or* operation is performed between the row and column bit of the given variable, 
the end result is the exclusion of the values that are already used. This procedure optimizes 
the valid domain verification process by performing constant time comparisons.

## BT + FC + Minimum Remaining Values (MRV)

MRV is another pruning technique, which aims to decrease the *branching factor* of the
possibilities. The technique is applied in the step of choosing the variable to be filled,
choosing the variable that has the least amount of valid values ​​in the domain.
In this project, 3 MRV variations were implemented and tested.

The first MRV implementation (MRV 1), was based on the standard definition of heuristics,
where the chosen variable must be the one that has the least amount of valid values ​​in the
domain, by checking both the unique digit condition in the row and column as
respecting the minor and major constraints. In the event of a tie in the amount of
values, the first variable (in the row and column sequence) is chosen.

The second MRV implementation (MRV 2), used only the condition of exclusivity
for the row and column, for counting valid values. In the event of a tie in the
number of valid values, the first variable (in the row and column sequence) is chosen.

The third MRV implementation (MRV 3) used the first implementation with addition
of the degree heuristic as a tie-breaking criterion, the greater the degree of the 
variable (higher amount of restrictions), the higher your priority in choosing.

## Experimental results

Several experiments were executed with the purpose of answering the following questions:
* Is the implementation of BT + MRV 1 + FC correct?
* Taking into account the execution time and the number 
of allocations for each algorithm, what is the cost/benefit of using heuristics 
to perform pruning in the tree of possibilities?
* Is the amount of assignments needed to solve the problem influenced by the 
amount of problem constraints?

The results can be found in this [Report file](/latex/futoshiki_report.pdf).
