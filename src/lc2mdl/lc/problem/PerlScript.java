package lc2mdl.lc.problem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import lc2mdl.mdl.quiz.QuestionStack;

public class PerlScript extends ProblemElement{

	private String script;
	
	private int arrayNo=0;
	
	@Override
	public void consumeNode() {
		log.finer("perl-script:");
		this.script=node.getTextContent();
		//got everything;)
		node.setTextContent(null);
		
		Element e=(Element)node;
		remvoveAttributeIfExist(e,"type");		
		remvoveAttributeIfExist(e,"space");
		
		convertPerlScriptToMaxima();
	}

	@Override
	public void addToMdlQuestion(QuestionStack question) {
		question.addToQuestionVariables(script);
	}

	public PerlScript(Problem problem, Node node) {
		super(problem, node);
	}

	
	private void convertPerlScriptToMaxima(){
		log.finer("-convert perl-script.");

		//FUNCTIONS
		replaceFunctions();
		
		//VARIABLES
		findAndReplaceVariables();
				
		//ARRAYS
		findAndReplaceArraysAssignments();
		
		//COMMENTS
		replaceComments();

		//SYNTAX (equal sign, semicolon)
		//need to be placed after arrays and functions!
		replaceSyntax();

		//CONTROL-STRUCTURES
		searchForControlStructures();

		//SPECIAL CHARS
		searchForSpecialChars();
		
	}
	

	private void searchForSpecialChars(){
		ArrayList<String> specialChars=new ArrayList<>(Arrays.asList("\\$","@","&","{","}"));
		int[] count=new int[specialChars.size()];
		Arrays.fill(count,0);
		for(String sc:specialChars){
			count[specialChars.indexOf(sc)]=script.length()-script.replace(sc,"").length();
		}
		if(Arrays.stream(count).sum()>0){
			log.warning("--found special chars. $:"+count[0]+", @: "+count[1]+", &: "+count[2]+", {: "+count[3]+", }: "+count[4]);
		}

		//FUNCTIONS
		Pattern pattern;
		Matcher matcher;

		String allFunctions="&\\b(?![0-9]+)\\w+\\b {0,}\\(";	//any words (alphanumeric) not beginning with number, followed by ( //\b(?![0-9]+)\w+\b {0,}\([\s\S]*?\) optional with closing bracket
		pattern=Pattern.compile(allFunctions);
		matcher=pattern.matcher(script);
		while(matcher.find()){
			String func=matcher.group();
			log.warning("--unknown function: \""+func+"\"");
		}	
	}

	private void searchForControlStructures(){
		ArrayList<String> controlStructures=new ArrayList<>(Arrays.asList("if","for","foreach","continue","while", "until","next","last","redo","unless","else","elseif","do","goto"));
		for(String cs:controlStructures){
			String csPat="\\W"+cs+"\\W";
			Matcher matcher=Pattern.compile(csPat).matcher(script);
			while(matcher.find()){
				log.warning("--found control structure: "+cs);
			}			
		}
	}

	private void replaceComments(){
		String commentPat="#[^\n\r]*";
		Matcher matcher=Pattern.compile(commentPat).matcher(script);
		while(matcher.find()){
			String comment=matcher.group();
			//remove # at the beginning
			while(comment.charAt(0)=='#' && comment.length()>1){
				comment=comment.substring(1);
			}
			//remove # within
			comment=comment.replaceAll("#","[HASH-SIGN]");
			comment=comment.replaceAll("\\$","[DOLLAR-SIGN]");
			comment=comment.replaceAll("/\\*","[OPEN-COMMAND]");
			comment=comment.replaceAll("\\*/","[CLOSING-COMMAND]");
			//put into c-style comments /* ... */
			comment="/*"+comment+"*/";
			script=script.replaceFirst(commentPat,comment);
		}		
	}

	private void replaceSyntax(){
		HashMap<String,String> syntaxReplacements=new HashMap<>();
		
		//single equal sign (left no AND right no equal sign)
		//and not equal signs in quotes: (?<!=)=(?!=)(?=([^"]*"[^"]*")*[^"]*;)
		syntaxReplacements.put("(?<!=)=(?!=)(?=([^\"]*\"[^\"]*\")*[^\"]*;)", ": ");
		log.fine("--replace all \"=\" with \": \"");

		//newline or return at the end of line
		syntaxReplacements.put(";[\\r\\n]*", System.lineSeparator());
		log.fine("--remove multiple empty lines");
		replaceKeysByValues(syntaxReplacements,true);
	}

	private void replaceFunctions(){
		HashMap<String,String> functionReplacements=new HashMap<>();

		//ONE BY ONE CONVERSION (same parameters)
		
		//basic mathematical functions
		functionReplacements.put("&log\\(", "log("); //natural
		//log10 only with Maxima pkg possible
		functionReplacements.put("&exp\\(", "exp(");
		//pow see below
		functionReplacements.put("&sqrt\\(", "sqrt(");
		functionReplacements.put("&abs\\(", "abs(");
		functionReplacements.put("&sgn\\(", "signum(");
		functionReplacements.put("&min\\(", "min(");
		functionReplacements.put("&max\\(", "max(");
		functionReplacements.put("&ceil\\(", "ceiling(");
		functionReplacements.put("&floor\\(", "floor(");
		functionReplacements.put("&round\\(", "round(");
		functionReplacements.put("&factorial\\(", "factorial(");
		
		//trigonometric functions
		functionReplacements.put("&sin\\(", "sin(");
		functionReplacements.put("&cos\\(", "cos(");
		functionReplacements.put("&tan\\(", "tan(");
		functionReplacements.put("&asin\\(", "asin(");
		functionReplacements.put("&acos\\(", "acos(");
		functionReplacements.put("&atan\\(", "atan(");
		functionReplacements.put("&atan2\\(", "atan2("); //both with 2 parameters
		//hyperbolic functions
		functionReplacements.put("&sinh\\(", "sinh(");
		functionReplacements.put("&cosh\\(", "cosh(");
		functionReplacements.put("&tanh\\(", "tanh(");
		functionReplacements.put("&asinh\\(", "asinh(");
		functionReplacements.put("&acosh\\(", "acosh(");
		functionReplacements.put("&atanh\\(", "atanh(");
		
		//string functions
		functionReplacements.put("&to_string\\(", "sconcat(");
		functionReplacements.put("&sub_string\\(", "substring("); //both with 2 or three parameters		
		functionReplacements.put("&random_permutation\\(", "random_permutation("); //both with 2 parameters
		
				
		replaceKeysByValues(functionReplacements);
		
		
		//DIFFERENT SYNTAX CONVERSION (different parameters)
		//regex was not possible for balanced parenthesis
		
		String scriptOroginal=script;		
		String functionBeginPat="&\\w+\\(";
		Matcher functionMatcher=Pattern.compile(functionBeginPat).matcher(scriptOroginal);
		while(functionMatcher.find()){
			String functionName=functionMatcher.group();
			int start=functionMatcher.start();
			int end=functionMatcher.end();
			
			ArrayList<String> params=new ArrayList<>();
			
			//Look for balanced parenthesis and get function-params
			int lastSep=end;
			int bracketCount=1;
			while(bracketCount>0){
				if(scriptOroginal.charAt(end)=='(')bracketCount++;
				if(scriptOroginal.charAt(end)==')'){
					bracketCount--;
					if(bracketCount==0){
						String param=scriptOroginal.substring(lastSep,end);
						params.add(param);
					}
				}
				if(scriptOroginal.charAt(end)==','){
					if(bracketCount==1){
						String param=scriptOroginal.substring(lastSep,end);
						params.add(param);
						lastSep=end+1;
					}
				}
				if(scriptOroginal.charAt(end)==';')break;
				end++;
			}
			if(bracketCount!=0){
				log.warning("--function with unbalanced parenthesis");
				continue;
			}
			String completeFunction=scriptOroginal.substring(start,end);//end exclusive
			
			
			switch(functionName){
				case "&choose(":case "choose(":
					//turn from $var=&choose($i, ... ) into array: [...]; $var=array[$i]
					
					//generate unique arrayName
					String arrayName="lc2mdlarray"+problem.getIndex(this)+arrayNo++;
					problem.addVar(arrayName);
					
					StringBuilder arrayDefinition=new StringBuilder(arrayName+": [");
					for(int i=1;i<params.size();i++){
						if(i!=1)arrayDefinition.append(" ,");
						arrayDefinition.append(params.get(i));
					}
					arrayDefinition.append("];");
					
					//make assignment on array[i]
					String assignArrayValue=arrayName+"["+params.get(0)+"];";
					
					//whole statement
					String leftStatement=getFirstMatchAsString(script,"[^;\n\r]*(?="+functionBeginPat+")");
					String wholeStatement=leftStatement+completeFunction;
					
					//define array before statement
					String newStatement=arrayDefinition.toString()+leftStatement+assignArrayValue;

					log.finer("--replace \""+wholeStatement+"\" with \""+newStatement+"\"");
					script=script.replace(wholeStatement,newStatement);
					break;
					
					
				case "&cas(":case "cas(":
					String cas=params.get(0);
					String maxima=params.get(1);
					if(cas.equals("\"maxima\"") || cas.equals("'maxima'")){
						log.finer("--replaced \""+completeFunction+"\" with \""+maxima+"\"");
						script=script.replace(completeFunction,params.get(1));
					}else{
						log.warning("--found &cas for unknown cas: "+cas);
					}
					break;
					
					
				case "&pow(":case "pow(":
					String pow=params.get(0)+"^"+params.get(1);
					log.finer("--replace \""+completeFunction+"\" with \""+pow+"\"");
					script=script.replace(completeFunction,pow);
					break;
					
					
				case "&random_permutation(":case "random_permutation(":
					String permutation="random_permutation("+params.get(1)+")";
					log.finer("--replace \""+completeFunction+"\" with \""+permutation+"\"");
					script=script.replace(completeFunction,permutation);					
					break;
					
					
				case "&random(":case "random":
					String lower=params.get(0);
					String upper=params.get(1);
					String step=(params.size()<3)?"1":params.get(2);
					String random="rand_with_step("+lower+", "+upper+", "+step+")";
					log.finer("--replace \""+completeFunction+"\" with \""+random+"\"");
					script=script.replace(completeFunction,random);					
					break;
					
					
				default:
					log.warning("--unknown function: "+functionName);
			}
		}
	}
	
	private void findAndReplaceVariables(){
		ArrayList<String> vars=new ArrayList<>();
		
		//find all variables
		String varPat="\\$([a-zA-Z]+([a-zA-Z0-9])*)";
		Pattern pattern=Pattern.compile(varPat);
		Matcher matcher=pattern.matcher(script);	
		while(matcher.find()){
			String s=matcher.group();
			s=s.substring(1);//remove first char $
			if(!vars.contains(s))vars.add(s);
			problem.addVar(s);
			script=script.replaceFirst(varPat, s);
		}
		if(vars.size()>0)log.finer("--found variables: "+vars);
	}

	private void findAndReplaceArraysAssignments(){		
		ArrayList<String> arrays=new ArrayList<>();
		
		//looking for whole Assignment
		String varAssignmentPat="@\\w+ {0,}= {0,}&?\\w*\\([\\s\\S]*?\\) {0,};";
		Pattern pattern=Pattern.compile(varAssignmentPat);
		Matcher matcherAssignment=pattern.matcher(script);	
		while(matcherAssignment.find()){
			String varAssignment=matcherAssignment.group();
			
			//check if its array assignment or function
			String leftBracePat="(?<=) {0,}&?\\w*\\((?=[\\s\\S]*?\\) {0,};)";//funtcion incl.
			String rightBracePat="\\)(?= {0,};)";//only in no function
			Matcher matchBraces=Pattern.compile(leftBracePat).matcher(varAssignment);
			String leftBrace="";
			String functionName="";
			boolean isFunction=false;
			while(matchBraces.find()){
				leftBrace=matchBraces.group();
				if(leftBrace.matches(" {0,}&?\\w+\\(")){
					functionName=getFirstMatchAsString(leftBrace,"&?\\w+\\(");
					isFunction=true;
				}
				break;
			}
			if(isFunction){
				log.warning("--found unknown array function: "+functionName);
			}
			if(!isFunction){
				log.finer("--replace \""+leftBrace+"\" with \"[\" and \")\" with \"]\"");
				//if normal array assignment, just replace round braces
				varAssignment=varAssignment.replaceFirst(leftBracePat,"[");
				varAssignment=varAssignment.replaceFirst(rightBracePat,"]");
			}
			
			//extract array-name and put to problemVars
			String arrayPat="@\\w+(?= {0,}=)";
			Pattern pat=Pattern.compile(arrayPat);
			Matcher matcherArray=pat.matcher(varAssignment);
			while(matcherArray.find()){
				String array=matcherArray.group();
				array=array.substring(1);//remove first char @
				if(!arrays.contains(array))arrays.add(array);
				problem.addVar(array);
				varAssignment=varAssignment.replaceFirst(arrayPat,array);
				break;
			}
			
			//replace converted array assignment
			script=script.replaceFirst(varAssignmentPat, varAssignment);
		}
		if(arrays.size()>0){
			//now replace all arrays in script
			for(String array:arrays){
				script=script.replace("@"+array,array);
			}
			log.finer("--found arrays: "+arrays);
		}
	}

	private void replaceKeysByValues(HashMap<String,String> replacements){
		replaceKeysByValues(replacements,false);
	}
	private void replaceKeysByValues(HashMap<String,String> replacements, boolean silent){
		Pattern pattern;
		Matcher matcher;
		for(String key:replacements.keySet()){
			pattern=Pattern.compile(key);
			matcher=pattern.matcher(script);
			while(matcher.find()){
				String s=matcher.group();
				if(!silent)log.finer("--replace \""+s+"\" with \""+replacements.get(key)+"\"");
				script=script.replaceFirst(key,replacements.get(key));
			}
		}	
	}
	
}
