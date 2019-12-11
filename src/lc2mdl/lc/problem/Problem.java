package lc2mdl.lc.problem;

import lc2mdl.lc.problem.response.Response;

import java.util.ArrayList;
import java.util.HashMap;

public class Problem {
	
	private String problemName;
	
	private ArrayList<ProblemElement> elements;
	
	private ArrayList<String> vars;

	// key: filename, value: full path of the images
	private HashMap<String,String> images;
	// tags: got from the relative path of the problem
	private ArrayList<String> tags = new ArrayList<>();
	// relative path of the problem, use it as category
	private String category="";
	private int numberOfHints=0;


	public Problem(String problemName, HashMap<String,String> images){
		this.problemName=problemName;
		elements=new ArrayList<>();
		vars=new ArrayList<>();
		vars.add("pi");
		this.images=images;
	}

	public Problem(String problemName, HashMap<String,String> images, String pathString){
		this.problemName=problemName;
		elements=new ArrayList<>();
		vars=new ArrayList<>();
		vars.add("pi");
		this.images=images;
		category = pathString.substring(0,pathString.lastIndexOf("/"));
		getTagsFromPath(pathString);
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
	 * @return 0 in case of not in list
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

		/**
	 * Returns index of given ProblemElement in List elements ONLY from class extending Response
	 * @return -1 in case of not in list
	 */
	public Response getCurrentResponse(ProblemElement element){
		Response curResponse = null;
		for(int i=0;i<elements.size();i++){
			if(elements.get(i)==element)break;
			//If same class then increment index
			if(Response.class.isAssignableFrom(elements.get(i).getClass())){
				curResponse = (Response) elements.get(i);
			}
		}
		return curResponse;
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
	
	private void getTagsFromPath(String path){
		String[] split = path.substring(1).split("/");
		for (int i=0; i<split.length-1; i++){
			this.tags.add(split[i]);
		}
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

	public ArrayList<String> getTags() {
		return tags;
	}

	public String getCategory() {
		return category;
	}

	public int getNumberOfHints() { return numberOfHints; }

	public void setNumberOfHints(int numberOfHints) { 	this.numberOfHints = numberOfHints; }

}
