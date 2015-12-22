# 3SAT_Solver_Complete

This is a complete 3-SAT solver written in Java and Parallel Java 2 using the DPLL algorithm.

About this program :

This program uses the DPLL algorithm to solve for satisfiability of a Boolean Formula in the Conjunctive Normal Form, often referred to as the CNF.

A boolean formula is said to be in a conjunctive normal form if it has one or more clauses logically in an AND relation to one another. And each clause is a collection of literals (Any logical variable which can have two possible states/values i.e. True(ON) or False(OFF)) in a OR relation to one another.

A boolean formula is said to be satisfiable if for some valid assignment of literals, the formula has an outcome of True.

Example Clause :   
(A \/ B \/ C) 
where A, B, C are literals and \/ represents the OR symbol

Example Formula :   
(A \/ B \/ C) ^ (~A \/ D \/ C) ^ (E \/ F \/ ~G) ^ (A \/ B \/ ~C) 
where ^ represents the AND symbol and ~ represents the logical NOT operation 

The ~ operator inverses the variables value, example changes True to False and False to True.

Satisfiable Fomula  :  
(A \/ B \/ C) ^ (D \/ B \/ ~E) ^ (D \/ ~B \/ C)

Assignment :   
B - True (Makes clause 1 and 2 True)
D - True (Makes clause 3 True)
All other literals can be unassigned or set to false and the formula will still be True.

Also, I have used the Parallel Java 2 Library Developed by Prof. Alan Kaminsky at RIT, to make this a parallel application, a solution that works on a multicore architecture.

Applications of Boolean Satisfiability in Real world :
1) Automatic Test-Pattern Generation
2) Combinational Equivalence Checking
3) Model Checking
4) Circuit Verification/Validation

References : 

1) Problem explanation : https://en.wikipedia.org/wiki/Boolean_satisfiability_problem
2) For Parallel Java2 usage and documentation, refer to http://www.cs.rit.edu/~ark/pj2.shtml
