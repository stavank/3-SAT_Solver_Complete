// DPLL Sat Solver Parallel
// @author Stavan Karia
// CSCI 654 : Foundations of Parallel Computing Project

// Self made test cases : 
// Test case 1 : 5 [1,3,5,4] [-2,1,5,2] [3,-1,-2] [1] [2]
//  -- Satisfiable

// Test case 2 : 5 [1,3,5,4] [-2,1,5,2] [3,1,2] [1] [2]
//  -- Satisfiable
//  -- Derived from 1 showing importance of unit clause and its assignment

// Test case 3 : 2 [1] [-1]
//  -- Not Satisfiable

// Test case 4 : 10 [1,2,4,6,7,9,-9] [-1,-4,-3,9,-2] [4,8,-6,2] [1,10] [3,8,6,4,1] [1,2,3,4,5,6] [-2,-4-6-8] [1,-2,3,-4] [8,-10] [1]
//  -- Satisfiable


// necessary imports for the program
import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import edu.rit.pj2.Job;
import edu.rit.pj2.Task;
import edu.rit.io.InStream;
import edu.rit.io.OutStream;
import edu.rit.pj2.Loop;
import edu.rit.pj2.Tuple;
import edu.rit.pj2.Vbl;


// DPLLParallel class extends Task
// Class with the main method for execution 
public class DPLLParallel extends Task{
	
	// Formula Class implements Vbl
	// This class's insinstances hold the formula under investigation 
	private static class Formula implements Vbl{

		// Formula under investigation
		List<Clause> F ;
		int [] assignment;

		// Start and End points
		// These points are used to mark points in the thread local formulae
		// Clauses from these mark points are copied back to the Global Formula 
		int endAt,uorp;

		// Empty Constructor
		public Formula(){
			this.F          = new LinkedList<Clause>();
			this.assignment = new int[0];
		}

		// Constructor with arguments
		public Formula(List<Clause> F,int endAt,int[] assignment,int uorp){
			this.F          = new LinkedList<Clause>(F);
			this.endAt      = endAt;
			this.assignment = Arrays.copyOf(assignment,assignment.length);
			this.uorp       = uorp;
		}	
		
		// Clone method - To make Deep Copy
		public Object clone(){
			return new Formula(F, endAt,assignment,uorp);
	    	}
		
		// Set method - Needs to be implemented because of Vbl
		public void set(Vbl FormulaObj){
			this.F          = new LinkedList<Clause>(((Formula)FormulaObj).F);
			this.endAt      = ((Formula)FormulaObj).endAt;
			this.assignment = Arrays.copyOf(((Formula)FormulaObj).assignment,((Formula)FormulaObj).assignment.length);
			this.uorp       = ((Formula)FormulaObj).uorp;
		}

		// Reduce method
		// Called by threads to reduce values among the threads
		// called for pure literal assignment and unit propagation
		// variable uorp decides what part of reduce must be executed
		// finds intersection of formula 
		// sets the assignment array values
		// updates the formula
		public void reduce(Vbl FormulaObj){

			// for unit propagation
			if(this.uorp == 0){
				for(int i = 0; i < ((Formula)FormulaObj).assignment.length; i++){
					if(((Formula)FormulaObj).assignment[i] != 2){
						this.assignment[i] = ((Formula)FormulaObj).assignment[i];
					}
				}		

				this.F.retainAll(((Formula)FormulaObj).F);	
			}

			// for pure literal assignment
			else if(this.uorp == 1){				
				this.F.addAll(((Formula)FormulaObj).F.subList(((Formula)FormulaObj).endAt,((Formula)FormulaObj).F.size()));
			}
		}

	}

	Formula FormulaForReduction = new Formula();
	int nLiterals, coresUsed, endAt = 0, uorp=0;
	float threadsUsed;
	double chunk;
	int glob = 0;

	// main method 
	public void main(String[] args) throws Exception{
		try{
			if(args.length != 3)
				helpMessage();

			long endTime;

			// store clauses from file to objects list
			parseClauses(FormulaForReduction.F,args[0]);
			int literalSize = Integer.parseInt(args[2]);
			coresUsed=Integer.parseInt(args[1]);
			FormulaForReduction.assignment = new int[literalSize];

			// set all assignment values to unassigned
			// 0 -> false
			// 1 -> true
			// 2 -> unassigned
 			for(int i = 0; i < FormulaForReduction.assignment.length; i++){
				FormulaForReduction.assignment[i] = 2;	
			}
			FormulaForReduction.endAt = endAt;
			FormulaForReduction.uorp = uorp;
			long startTime = System.currentTimeMillis();	

			// call the solveDPLL method
			int[] solution = solveDPLL(coresUsed, FormulaForReduction);

			// print results
			if (solution != null){
				endTime = System.currentTimeMillis();
				System.out.println("Time Taken : "+(endTime - startTime)+"ms");
				System.out.println("A possible assignment for the variables has been found: ");
				System.out.println(Arrays.toString(solution));
				System.out.println("DPLL Called for "+glob+" many times");	
			}
			else{
				endTime = System.currentTimeMillis();
				System.out.println("Time Taken : "+(endTime - startTime)+"ms");
				System.out.println("The formula is a contradiction");
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

  
  
	// Parses clauses on in Data File and puts them in the formula
	// Throws exceptions if the file can't be read
	public void parseClauses(List<Clause> F, String FileName){
		try{
			BufferedReader br = new BufferedReader(new FileReader("/home/stu5/s11/sk3870/DPLLParallel/"+FileName));
			String clause;
			while ((clause = br.readLine()) != null) {
				clause += ">";
				Clause parsed = new Clause();
				String[] literals = clause.split(">"); 
				for (int j = 0; j < literals.length; j++){
					parsed.addLiteral(Integer.parseInt(literals[j]));
				}
				F.add(parsed); 
			}
			br.close();
		}
		catch(FileNotFoundException exception){
			exception.printStackTrace();
		}
		catch(IOException exception){
			exception.printStackTrace();
		}
	}

	// error help message
	public static void helpMessage(){
		System.out.println("Usage: $ java pj2 DPLL <Data FileName> <Count of Cores Used>");
	}

  	// solveDPLL method
	public int[] solveDPLL (int cores, Formula FormulaArg){
		if (solveDPLLFinal(FormulaArg , cores)){
			return FormulaArg.assignment;
		}
		return null;
	}

	// The core DPLL method
	// return True if formula gives a valid assignation, False if formula is a contradiction	
	public boolean solveDPLLFinal(Formula FormulaArg, int cores){
		int oldFormulaSize,newFormulaSize;
		glob++;
		do{
			threadsUsed=cores*1;

			// unit propagation methods parallel for	
			parallelFor(0,FormulaForReduction.F.size()-1).schedule(fixed).exec(new Loop(){
				Formula thrFormulaForReduction;
				public void start(){
					chunk=Math.ceil(FormulaForReduction.F.size()/threadsUsed);		
					thrFormulaForReduction=threadLocal(FormulaForReduction);	      			
			      	endAt=(int)(((rank()+1)*chunk)-1);
					thrFormulaForReduction.endAt=endAt;
			    }
				public void run(int i){
					UnitPropagate(thrFormulaForReduction,i,rank());
				}				
			});		
			FormulaForReduction.uorp=1;

			// loops to find negative litral of unit literals found in unit propagation
			// then remove those litrals from the clauses
			for(int k=1;k<FormulaForReduction.assignment.length;k++){
				if(FormulaForReduction.assignment[k-1]==1){
					for(int l=0;l<FormulaForReduction.F.size();l++){	
						Clause	c = FormulaForReduction.F.get(l);
						if(c.hasLiteral(-k)){
		 	        	   	FormulaForReduction.F.remove(l);
		        	   		c.rmvLiteral(-k);
	    		       		FormulaForReduction.F.add(l, c);
						}
					}
				}
			}
			oldFormulaSize=FormulaForReduction.F.size();

			// pure literal assignments parallel for
			parallelFor(1,FormulaForReduction.assignment.length).schedule(fixed).exec(new Loop(){
				Formula thrFormulaForReduction;
				public void start(){
					thrFormulaForReduction=threadLocal(FormulaForReduction);
					thrFormulaForReduction.endAt=FormulaForReduction.F.size();
				}
				public void run(int i){
					PureLiteralAssign(thrFormulaForReduction,i);
				}				
			});


			newFormulaSize=FormulaForReduction.F.size();
			FormulaForReduction.uorp=0;
		}while(newFormulaSize!=oldFormulaSize);

		// if formula is empty, then all clauses are satisfied
		if (FormulaForReduction.F.size()==0 ){
			return true;
		}
		// if there is an empty clause, then formula cannot be satisfied
		if (anEmptyClause(FormulaForReduction.F)){
			return false;
		} 

		int nextLiteral = ChooseLiteral(FormulaForReduction.F); 
		
		// Creates two new formulas adding to F literal and !literal respectively
		List<Clause> F1 = new LinkedList<Clause>(FormulaForReduction.F);
		List<Clause> F2 = new LinkedList<Clause>(FormulaForReduction.F);
		Clause new1 = new Clause();
		new1.addLiteral(nextLiteral);
		Clause new2 = new Clause();
		new2.addLiteral(-nextLiteral);    
		Formula FormulaForReductionOpp = new Formula(FormulaForReduction.F,FormulaForReduction.endAt,FormulaForReduction.assignment,FormulaForReduction.uorp);
		FormulaForReduction.F.add(new1);
		FormulaForReductionOpp.F.add(new2);    
		// call core DPLL again wth 2 new formulae
		return solveDPLLFinal(FormulaForReduction, cores) || solveDPLLFinal( FormulaForReductionOpp, cores);
	}

  

   	// Checks if the formula contains an empty clause
	public boolean anEmptyClause(List<Clause> F){
		ListIterator<Clause> it = F.listIterator(); 
		while (it.hasNext()){
			Clause c = (Clause)it.next();
			if (c.clauseIsEmpty()){
				return true;
			}
		}
		return false;
	}

  
   	// Randomly returns the first literal it can get
  	public int ChooseLiteral(List<Clause> F){
	  Clause first = F.get(0);
	  return first.getLiteral();
  	}
  
  	// method to perform unit propagation
  	public void UnitPropagate(Formula thrFormulaUnitProp, int i,int rank){    
  		if(i<thrFormulaUnitProp.F.size()){
		      	Clause cu = thrFormulaUnitProp.F.get(i);
		      	// if clause is unit clause
		      	if (cu.clauseunity()){
		 		int propagate = cu.getLiteral();        
		    	 	// assign true if literal > 0
		    	    if (propagate > 0){
		 		        thrFormulaUnitProp.assignment[propagate-1] = 1;
		        	}
		        	// assign false if literal < 0
		        	else{
		 	        	thrFormulaUnitProp.assignment[-propagate-1] = 0;
		    	    	}
		        	for (int j = 0; j < thrFormulaUnitProp.F.size(); j++){
		 	        	Clause c1 = thrFormulaUnitProp.F.get(j);
		    	      	if (c1.hasLiteral(propagate)){
		 		       		thrFormulaUnitProp.F.remove(j);
		        	    	j--; 
		          		}
		        	}
	      		}
      		}
  	}

  	// method to perform pure literal assignment
  	public static void PureLiteralAssign(Formula thrFormulaPureLit, int i){
    		int pos = 0;
    		int neg = 0;    
		int n = 0;
		ListIterator<Clause> it = thrFormulaPureLit.F.listIterator();
	      	while (it.hasNext()){
	 		Clause c = (Clause)it.next();
	        	if (c.hasLiteral(i)){
	 			pos++; 
	 			n++;
	        	}
	        	else if (c.hasLiteral(-i)){
			        neg++; 
	       			n++;
	       		}
	   	}      
	   	// if literal found in positive polarity
     		if (n!=0 && pos!=0 &&pos == n){
	 		thrFormulaPureLit.assignment[i-1] = 1; 
		       	Clause new1 = new Clause();
	    	   	new1.addLiteral(i);
	        	thrFormulaPureLit.F.add(new1);        
		}
	   	// if literal found in negative polarity
		else if (n!=0 && neg!=0 && neg == n){
			thrFormulaPureLit.assignment[i-1] = 0;        
	    	  	Clause new1 = new Clause();
	       		new1.addLiteral(-i);
	        	thrFormulaPureLit.F.add(new1);
	        }
	}
}
