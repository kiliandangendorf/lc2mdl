package lc2mdl.lc.problem;

import java.util.ArrayList;

import lc2mdl.lc.problem.response.Response;

public class Problem {
	
	private String problemName;
	
	private ArrayList<ProblemElement> elements;
	
	private ArrayList<String> vars;
	
	public Problem(String problemName){
		this.problemName=problemName;
		elements=new ArrayList<>();
		vars=new ArrayList<>();
		vars.add("pi");
	}
	
	/**
	 * Returns index of given ProblemElement in List elements
	 */
	public int getIndex(ProblemElement element){
		return elements.indexOf(element);
	}
	
	/**
	 * Returns index of given ProblemElement in List elements ONLY from same concrete class
	 * @return -1 in case of not in list
	 */
	public int getIndexFromSameClassOnly(ProblemElement element){
		int index=-1;
		for(int i=0;i<elements.size();i++){
			if(elements.get(i)==element)break;
			//If same class then increment index
			if(elements.get(i).getClass().equals(element.getClass()))index++;
		}
		return index+1;
	}
	
	/**
	 * Returns index of given ProblemElement in List elements ONLY from class extending Response
	 * @return -1 in case of not in list
	 */
	public int getIndexFromResponse(ProblemElement element){
		int index=-1;
		for(int i=0;i<elements.size();i++){
			if(elements.get(i)==element)break;
			//If same class then increment index
			if(Response.class.isAssignableFrom(elements.get(i).getClass()))index++;
		}
		return index+1;
	}
	
	public void addElement(ProblemElement element) {
		this.elements.add(element);
	}
	
	/**
	 * Adds given var to list of vars, if not exist.
	 * Return true only if was not in list before 
	 */
	public boolean addVar(String var){
		if(!vars.contains(var)){
			vars.add(var);
			return true;
		}
		return false;
	}
	
	
	//================================================================================
    // Getter and Setter
    //================================================================================			
	public ArrayList<ProblemElement> getElements() {
		return elements;
	}
//	public void setElements(ArrayList<AbstractProblemElement> elements) {
//		this.elements = elements;
//	}
	public ArrayList<String> getPerlVars(){
		return vars;
	}
	public String getProblemName(){
		return problemName;
	}
}
