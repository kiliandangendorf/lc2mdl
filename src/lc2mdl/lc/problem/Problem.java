package lc2mdl.lc.problem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import lc2mdl.lc.problem.response.Response;
import lc2mdl.util.FileFinder;

public class Problem {
	public static Logger log = Logger.getLogger(ProblemElement.class.getName());
	
	private String problemName;
	
	private ArrayList<ProblemElement> elements;
	
	private ArrayList<String> vars;

	private ArrayList<String> questiontypes = new ArrayList<>();

	// key: filename, value: absolute path of the images
	private HashMap<String,String> images=new HashMap<String, String>();

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
		this(problemName, images);
		getCategoryAndTagsFromPath(pathString);
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

	public void addQuestionType(String type) {
		if (!questiontypes.contains(type)) {
			questiontypes.add(type);
		}
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
	
	private void getCategoryAndTagsFromPath(String path){

		category = "";
		String[] split = path.substring(1).split("/");
		int startindex = 0;
		if (path.startsWith("/res")) {
			startindex = 3;
		}
		for (int i=startindex; i<split.length-1; i++){
			category += "/"+split[i];
			this.tags.add(split[i]);
		}
	}

	/**
	 * Gives absolute path to image filename 
	 * @param filename: eg. "rechtwinkligesDreieck.png"
	 * @return absolute path to image file
	 */
	public String getAbsImagePathFromFilename(String filename){
		return images.get(filename);
	}
	/**
	 * Gives absolute path to LC-image-path
	 * @param lcPath: eg. "/res/fhwf/riegler/TWW-Vorkurs/images/rechtwinkligesDreieck.png"
	 * @return absolute path to image file
	 */
	public String getAbsImagePathFromLcPath(String lcPath){
		String filename = FileFinder.extractFileName(lcPath);
		return getAbsImagePathFromFilename(filename);
	}

	//================================================================================
    // Getter and Setter
    //================================================================================			
	public ArrayList<ProblemElement> getElements() {
		return elements;
	}

	public ArrayList<String> getPerlVars(){
		return vars;
	}
	public String getProblemName(){
		return problemName;
	}

	public void setProblemName(String problemName) {
		this.problemName = problemName;
	}

	public ArrayList<String> getTags() {
		return tags;
	}

	public ArrayList<String> getQuestiontypes() {
		return questiontypes;
	}

	public String getCategory() {
		return category;
	}

	public int getNumberOfHints() { return numberOfHints; }

	public void setNumberOfHints(int numberOfHints) { 	this.numberOfHints = numberOfHints; }

}
