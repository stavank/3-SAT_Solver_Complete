// necessary imports for the program
import java.util.*;

// Clause class
// the objects store each clause as a hashset
public class Clause{
	public Set<Integer> m_literal;

	// empty contructor
	public Clause(){
		m_literal = new HashSet<Integer>();
	}

	// check if the clause is empty 
	public boolean clauseIsEmpty(){
		return (m_literal.size()==0);
	}

	// check if clause is unit clause
	public boolean clauseunity(){
		return (m_literal.size() == 1);
	}

	// get literal from a clause
	public int getLiteral() throws IndexOutOfBoundsException{
		if (clauseIsEmpty())
		{
			throw new IndexOutOfBoundsException("Empty clause");
		}
		Iterator iterator = m_literal.iterator();	
		return (int)iterator.next();
	}

	// add literal to a clause
	public void addLiteral(int i){
		m_literal.add((int)i);
	}

	// remove literal from a clause
	public void rmvLiteral(int i) {
		m_literal.remove((Integer)i);
	}

	// check if clause has a literal
	public boolean hasLiteral(int i){
		Iterator iterator = m_literal.iterator();		
		while(iterator.hasNext()){	
			if (iterator.next() == i){
				return true;
			}
		}
		return false;
	}	

	// convert hashset to a object array
	public Object[] clauseToString(){
		Object[] value = m_literal.toArray();
		return  value;
	}
}
