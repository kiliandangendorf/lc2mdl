package lc2mdl.lc.problem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import lc2mdl.Prefs;
import lc2mdl.mdl.quiz.QuestionStack;
import lc2mdl.util.ConvertAndFormatMethods;
import lc2mdl.util.FileFinder;

public class PerlScript extends ProblemElement{
	/* HINT:
	 * Best way to use Matcher seems to be using "matcher.appendReplacement(...)" and "matcher.appendTail(...)" on a StringBuffer.
	 * This makes sure, to replace exactly this match (not all matching Strings).
	 * Problem here is, we need not only the match, but also some charSequences before and after.
	 * 
	 * For RegEx-Strings use look ahead and behind, eg. String csPat="(?<=[\\W])"+cs+"(?=\\W)";
	 * 
	 * Regarding replace-Functions:
	 * - no regex needed => use only replace()
	 * - if needed => use Matcher.quoteReplacement() in replacement-String (i.e. for backslashes and dollar-signs)
	 * 		Pattern: replaceAll(Pattern.quote(),Matcher.quoteReplacement());
	 * 		Example: replaceAll(Pattern.quote("/"), Matcher.quoteReplacement("\\"));
	 * @see https://stackoverflow.com/questions/12941266/replace-and-replaceall-in-java
	 * */

	private String script;
	private String scriptComment;
	private String convertWarning;
	private ArrayList<String> stringsInScript=new ArrayList<String>();
	private ArrayList<String> commentsInScript=new ArrayList<String>();

	private int arrayNo=0;
	private int stringNo=0;
	private int commentNo=0;

	@Override
	public void consumeNode(){
		log.finer("perl-script:");
		this.script=node.getTextContent();
		this.scriptComment="Original Perl Script"+System.lineSeparator()+this.script;
		this.convertWarning="/*  Start Script Part */";

		log.finer("--put original Perl script in comment.");
		// got everything;)
		node.setTextContent(null);

		Element e=(Element)node;
		removeAttributeIfExist(e,"type");
		removeAttributeIfExist(e,"space");

		convertPerlScriptToMaxima();
	}

	@Override
	public void addToMdlQuestionStack(QuestionStack question){
		question.addComment(scriptComment);
		question.addToQuestionVariables(script);
	}

	public PerlScript(Problem problem,Node node){
		super(problem,node);
		questionType="stack";
		problem.addQuestionType(questionType);
	}

	private void convertPerlScriptToMaxima(){
		log.finer("-convert perl-script.");

		// save Strings to prevent them from converting
		saveStrings();
		
		// save COMMENTS to prevent them from converting
		saveComments();

		// FUNCTIONS
		replaceFunctions();

		// VARIABLES
		findAndReplaceVariables();

		// ARRAYS
		findAndReplaceArraysAssignments();

		// UNKNOWN CONTROL-STRUCTURES
		searchForUnknownControlStructures();

		// CONTROL-STRUCTURES THAT CAN BE REPLACED
		replaceControlStructures();

		// SYNTAX (equal sign, semicolon)
		// need to be placed after arrays, functions and control structures !
		replaceBlocks();

		// = -> :, etc. (needs to be placed BEFORE replaceStrings() )
		replaceSyntax();

		// replace Comments
		replaceComments();

		// replace Strings
		replaceStrings();

		// SPECIAL CHARS
		searchForSpecialChars();

		script=convertWarning+System.lineSeparator()+script;
	}

	public void addConvertWarning(String warning){
		log.warning(warning);
		convertWarning=convertWarning+System.lineSeparator()+"/* "+warning+" */";
	}

	private void searchForSpecialChars(){
		log.finer("-- search for special characters");
		ArrayList<String> specialChars=new ArrayList<>(Arrays.asList("\\$","@","&","{","}"));
		int[] count=new int[specialChars.size()];
		Arrays.fill(count,0);
		for(String sc:specialChars){
			count[specialChars.indexOf(sc)]=script.length()-script.replace(sc,"").length();
		}
		if(Arrays.stream(count).sum()>0){
//			log.warning("--found special chars. $:"+count[0]+", @: "+count[1]+", &: "+count[2]+", {: "+count[3]+", }: "+count[4]);
			addConvertWarning("--found special chars. $: "+count[0]+", @: "+count[1]+", &: "+count[2]+", {: "+count[3]+", }: "+count[4]);
		}
	}

	private void replaceControlStructures(){
		PerlControlStructuresReplacer cSReplacer=new PerlControlStructuresReplacer(this,script);

		script=cSReplacer.getReplacedScript();
	}

	private void replaceBlocks(){
		// Find innermost block { ... }
		String blockPat="([\\r\\n]*\\{(?:\\{??[^\\{]*?\\}))+";
		Matcher matcher=Pattern.compile(blockPat).matcher(script);
		while(matcher.find()){
			log.finer("--replace block.");
			String block=matcher.group();
			String newBlock=replaceSyntaxInBlock(block);
			script=script.replace(block,newBlock);
			matcher=Pattern.compile(blockPat).matcher(script);
		}
	}

	private String replaceSyntaxInBlock(String blockStringWithBraces){
		HashMap<String,String> replacements=new LinkedHashMap<>();
		String newBlock=blockStringWithBraces;

		//changing braces 
		replacements.put("\\{","(");
		replacements.put("\\}",")");

		//replace semicolons by comma
		replacements.put("; {0,}",", ");
		//remove last comma in block
		replacements.put(",(\\s+)\\)","$1)");
		 
		if(!Prefs.ALLOW_MULTILINE_BLOCKS){
			//remove CR & LF
			replacements.put("[\\r\\n]+"," ");
			//remove everything after comma
			replacements.put(",\\s*",", ");
			//remove tabs
			replacements.put("\\t"," ");
			//cleanup multiple generated whitespace
			replacements.put(" {2,}"," ");
		}

		for(String key:replacements.keySet()){
			newBlock=newBlock.replaceAll(key,replacements.get(key));
		}
		return(newBlock);
	}

	private void searchForUnknownControlStructures(){
		ArrayList<String> controlStructures=new ArrayList<>(Arrays.asList("next","last","redo","goto","sub"));
		for(String cs:controlStructures){
			String csPat="\\W"+cs+"\\W";
			Matcher matcher=Pattern.compile(csPat).matcher(script);
			while(matcher.find()){
				addConvertWarning("---found unknown control structure: "+cs+" (will not work on)");
			}
		}
	}

	private void saveComments(){
		log.finer("--save comments before transforming");
		commentNo=0;

		// not "$#" what means length of an array
		String commentPat="(?<!\\$)#.*(?=[\\n\\r])";
		StringBuffer sb=new StringBuffer();
		Matcher matcher=Pattern.compile(commentPat).matcher(script);
		while(matcher.find()){
			String comment=matcher.group();
			// remove # at the beginning
			while(comment.charAt(0)=='#'&&comment.length()>1){
				comment=comment.substring(1);
			}
			//to prevent generating "**"
			if(comment.charAt(0)=='#'&&comment.length()==1) comment=" ";
			// remove # within
			comment=comment.replaceAll("#","[HASH-SIGN]");
			comment=comment.replaceAll("\\$","[DOLLAR-SIGN]");
			comment=comment.replaceAll("/\\*","[OPEN-COMMAND]");
			comment=comment.replaceAll("\\*/","[CLOSING-COMMAND]");
			// put into c-style comments /* ... */
			comment="/*"+comment+"*/";
			
			String replacement="lc2mdl_comment"+commentNo++;
			commentsInScript.add(comment);
			
			matcher.appendReplacement(sb,replacement);
		}
		matcher.appendTail(sb);
		script=sb.toString();
	}
	private void replaceComments(){
		log.finer("--replace comments again");

		String buf=script;
		//going inverse direction (as it was at replaceStrings) 
		for(int i=commentNo-1;i>=0;i--){
			String commentText=commentsInScript.get(i);
			//prevent to match lc2mdl_comment10 with lc2mdl_comment1
//			buf=buf.replaceAll("(?<=\\W)lc2mdl_comment"+i+"(?=\\W)",Matcher.quoteReplacement(commentText));
			buf=buf.replaceAll("(?<=\\b)lc2mdl_comment"+i+"(?=\\b)",Matcher.quoteReplacement(commentText));
		}
		script=buf;
	}

	private void replaceSyntax(){

		log.finer("--remove multiple empty lines");
		// newline or return at the end of line
		script=script.replaceAll(";[\\r\\n]{2,}",";"+System.lineSeparator()+System.lineSeparator());

//		log.finer("--remove multiple spaces");
//		script=script.replaceAll(" {2,}"," ");

		log.finer("--remove multiple semicola");
		script=script.replaceAll("(; {0,}){2,}","; ");

		log.finer("--remove multiple commata");
		script=script.replaceAll("(, {0,}){2,}",", ");
		
		log.finer("--remove whitespaces at line beginning");
		script=script.replaceAll("(?<=[\\r\\n]) (?=\\S)","");

		// Operator-Replacement moved to class PerlControlStructuresReplacer
	}

	private void replaceFunctions(){
		log.finer("--replace functions");
		HashMap<String,String> functionReplacements=new LinkedHashMap<>();

		// ONE BY ONE CONVERSION (same parameters)

		// basic mathematical functions
		functionReplacements.put("&log\\(","log("); // natural
		// log10 only with Maxima pkg possible
		functionReplacements.put("&exp\\(","exp(");
		// pow see below
		functionReplacements.put("&sqrt\\(","sqrt(");
		functionReplacements.put("&abs\\(","abs(");
		functionReplacements.put("&sgn\\(","signum(");
		functionReplacements.put("&min\\(","min(");
		functionReplacements.put("&max\\(","max(");
		functionReplacements.put("&ceil\\(","ceiling(");
		functionReplacements.put("&floor\\(","floor(");
		functionReplacements.put("&round\\(","round(");
		functionReplacements.put("&factorial\\(","factorial(");

		// trigonometric functions
		functionReplacements.put("&sin\\(","sin(");
		functionReplacements.put("&cos\\(","cos(");
		functionReplacements.put("&tan\\(","tan(");
		functionReplacements.put("&asin\\(","asin(");
		functionReplacements.put("&acos\\(","acos(");
		functionReplacements.put("&atan\\(","atan(");
		functionReplacements.put("&atan2\\(","atan2("); // both with 2 parameters

		// hyperbolic functions
		functionReplacements.put("&sinh\\(","sinh(");
		functionReplacements.put("&cosh\\(","cosh(");
		functionReplacements.put("&tanh\\(","tanh(");
		functionReplacements.put("&asinh\\(","asinh(");
		functionReplacements.put("&acosh\\(","acosh(");
		functionReplacements.put("&atanh\\(","atanh(");

		// string functions
		functionReplacements.put("&to_string\\(","sconcat(");
		functionReplacements.put("&sub_string\\(","substring("); // both with 2 or three parameters

		if(Prefs.SUPPORT_OLD_CAPA_FUNCTIONS_WITHOUT_AMPERSAND){
			HashMap<String,String> additionalFunctionsWithoutAmpersand=new LinkedHashMap<String,String>();
			for(String key:functionReplacements.keySet()){
				if(key.charAt(0)=='&'){
					String val=functionReplacements.get(key);
					// make ampersand (&) optional
					String newKey="\\&?"+key.substring(1);// ,key.length());
					additionalFunctionsWithoutAmpersand.put(newKey,val);
				}
			}
			functionReplacements.putAll(additionalFunctionsWithoutAmpersand);
		}
		replaceRegExKeysByValues(functionReplacements);

		// DIFFERENT SYNTAX CONVERSION (different parameters)
		// regex was not possible for balanced parentheses
		String functionBeginPat;
		// optional &-sign (ampersand)
		if(Prefs.SUPPORT_OLD_CAPA_FUNCTIONS_WITHOUT_AMPERSAND){
			//only if there is a word boundary in front of function name
			functionBeginPat="\\&?(?:\\b)\\w+\\(";
		}
		else{
			functionBeginPat="\\&\\w+\\(";
		}
		Matcher functionMatcher=Pattern.compile(functionBeginPat).matcher(script);
		int lastIndex=0;
		while(functionMatcher.find(lastIndex)){
			String functionName=functionMatcher.group();
			int start=functionMatcher.start();
			//for not matching same function multiple times
			lastIndex=start+1;
			//char-position in script BEHIND opening bracket
			int pos=functionMatcher.end();
			
			//skip if name matches known CS
			ArrayList<String> misunderstandableCS=new ArrayList<>(Arrays.asList("if","elsif","unless","while","until","for","foreach"));
			if(misunderstandableCS.contains(functionName.substring(0,functionName.length()-1)))continue;

			ArrayList<String> params=new ArrayList<>();

			// Look for balanced parenthesis and get function-params
			int lastPos=pos;
			int bracketCount=1;
			int curlyBracketCount=0;
			int squareBracketCount=0;
			while(bracketCount>0){
				switch(script.charAt(pos)){
					case '(':
						bracketCount++;
						break;
					case ')':
						bracketCount--;
						if(bracketCount==0){
							String param=script.substring(lastPos,pos);
							params.add(param);
						}
						break;
					case '{':
						curlyBracketCount++;
						break;
					case '[':
						squareBracketCount++;
						break;
					case '}':
						curlyBracketCount--;
						break;
					case ']':
						squareBracketCount--;
						break;

					case ',':
						//if param of THIS function
						if(bracketCount==1){
							//in case param is an array
							if(curlyBracketCount==0&&squareBracketCount==0){
								String param=script.substring(lastPos,pos);
								params.add(param);
								lastPos=pos+1;
							}
						}
						break;
					case ';':
						log.warning("--found unexpected ';' in function parameters in "+functionName);
						break;
				}
				pos++;
				//if pos out of String bound
				//TODO: (maybe look back if char before was an escape-symbol?)
				if(pos>=script.length()){
					log.warning("--can't find end of function within script-String (most likely because of escape-symbols) in function "+functionName);
					break;
				}
			}
			if(bracketCount!=0){
				log.warning("--function with unbalanced parentheses");
				continue;
			}
			String completeFunction=script.substring(start,pos);// end exclusive

			switch(functionName){
				case "&choose(":
				case "choose(":
					// turn from $var=&choose($i, ... ) into array: [...];
					// $var=array[$i]

					// generate unique arrayName
					String arrayName="lcmdlarray"+problem.getIndex(this)+"_"+arrayNo++;
					problem.addVar(arrayName);

					StringBuilder arrayDefinition=new StringBuilder(arrayName+": [");
					for(int i=1;i<params.size();i++){
						if(i!=1) arrayDefinition.append(" ,");
						arrayDefinition.append(params.get(i));
					}
					arrayDefinition.append("]; ");

					// make assignment on array[i]
					String assignArrayValue=arrayName+"["+params.get(0)+"]";//;";

					//get left side of statement followed by EXACTLY that match
					String leftStatement=getFirstMatchAsString(script,"[^;\\n\\r]*(?=("+Pattern.quote(completeFunction)+"))");

					String wholeStatement=leftStatement+completeFunction;
					if(leftStatement==null||leftStatement.equals("")){
						wholeStatement=completeFunction;
						log.info("--found not assigned choose-function: \""+wholeStatement+"\"");
					}

					// define array before statement
					String newStatement=arrayDefinition.toString()+leftStatement+assignArrayValue;

					log.finer("--replace \""+wholeStatement+"\" with \""+newStatement+"\"");
					script=script.replace(wholeStatement,newStatement);
					break;

				case "&cas(":
				case "cas(":
					String cas=params.get(0);
					String maxima=params.get(1);
					if(cas.equals("\"maxima\"")||cas.equals("'maxima'")){
						log.finer("--replaced \""+completeFunction+"\" with \""+maxima+"\"");
						script=script.replace(completeFunction,params.get(1));
					}else{
						log.warning("--found &cas for unknown cas: "+cas);
					}
					break;

				case "&pow(":
				case "pow(":
					String pow="("+params.get(0)+")"+"^"+"("+params.get(1)+")";
					log.finer("--replace \""+completeFunction+"\" with \""+pow+"\"");
					script=script.replace(completeFunction,pow);
					break;

				case "&random_permutation(":
				case "random_permutation(":
					String permutation="random_permutation(";
					//only second parameter is list, seed will be ignored
					if(params.size()>1) permutation+=params.get(1)+")";
					else permutation+=params.get(0)+")";
					log.finer("--replace \""+completeFunction+"\" with \""+permutation+"\"");
					if(params.size()>1) log.finer("---seed "+params.get(0)+" was ignored");
					script=script.replace(completeFunction,permutation);
					break;

				case "&random(":
				case "random(":
					String lower=params.get(0);
					String upper=params.get(1);
					String step=(params.size()<3)?"1":params.get(2);
					String random="rand_with_step("+lower+", "+upper+", "+step+")";
					log.finer("--replace \""+completeFunction+"\" with \""+random+"\"");
					script=script.replace(completeFunction,random);					
					break;

				default:
					if(!functionReplacements.containsValue(functionName)) log.warning("--unknown function: "+functionName);
			}
			
			//refresh matcher on modified script
			functionMatcher=Pattern.compile(functionBeginPat).matcher(script);
		}

		HashMap<String,String> paramReplacement=new LinkedHashMap<>();
		paramReplacement.put("parameter_setting(\"([a-zA-Z][a-zA-Z0-9_]*?)\")","lcparam_$1");
		replaceRegExKeysByValues(paramReplacement);
	}

	private void findAndReplaceVariables(){
		ArrayList<String> vars=new ArrayList<>();

		// find all variables
		// rules for var-names: https://webassign.net/manual/instructor_guide/t_i_setting_perl_variables.htm 
		String varPat="\\$([a-zA-Z\\_]+([a-zA-Z0-9\\_])*)";
		Pattern pattern=Pattern.compile(varPat);
		Matcher matcher=pattern.matcher(script);
		while(matcher.find()){
			String s=matcher.group();
			s=s.substring(1);// remove first char $
			if(!vars.contains(s)) vars.add(s);
			problem.addVar(s);
			script=script.replaceFirst(varPat,s);
		}
		if(vars.size()>0) log.finer("--found variables: "+vars);
	}

	private void findAndReplaceArraysAssignments(){
		ArrayList<String> arrays=new ArrayList<>();

		// looking for whole Assignment
		String varAssignmentPat="@\\w+ {0,}= {0,}&?\\w*\\([\\s\\S]*?\\) {0,};";
		Pattern pattern=Pattern.compile(varAssignmentPat);
		Matcher matcherAssignment=pattern.matcher(script);
		while(matcherAssignment.find()){
			String varAssignment=matcherAssignment.group();
			String varAssignmentOriginal=varAssignment;
			// check if its array assignment or function
			String leftBracePat="(?<=) {0,}&?\\w*\\((?=[\\s\\S]*?\\) {0,};)";// funtcion incl.
			String rightBracePat="\\)(?= {0,};)";// only in no function
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
				//if contains range operator (e.g. "1..9"), do not replace braces (see PerlControlStructuresReplacer replaceOperators
				if(!varAssignment.contains("..")){
					log.finer("--replace \""+leftBrace+"\" with \"[\" and \")\" with \"]\"");
					// if normal array assignment, just replace round braces
					varAssignment=varAssignment.replaceFirst(leftBracePat,"[");
					varAssignment=varAssignment.replaceFirst(rightBracePat,"]");
				}
			}

			// extract array-name and put to problemVars
			String arrayPat="@\\w+(?= {0,}=)";
			Pattern pat=Pattern.compile(arrayPat);
			Matcher matcherArray=pat.matcher(varAssignment);
			String array="no array";
			while(matcherArray.find()){
				array=matcherArray.group();
				array=array.substring(1);// remove first char @
				if(!arrays.contains(array)) arrays.add(array);
				problem.addVar(array);
				varAssignment=varAssignment.replaceFirst(arrayPat,array);
				break;
			}

			// replace converted array assignment
			script=script.replace(varAssignmentOriginal,varAssignment);
		}
		if(arrays.size()>0){
			// now replace all arrays in script
			for(String array:arrays){
				script=script.replace("@"+array,array);
			}
			log.finer("--found arrays: "+arrays);
		}
	}

	private void saveStrings(){
		log.finer("--save strings before transforming");
		stringNo=0;

		// Loop for double and single quotation marks
		for(char quote:Arrays.asList('\"','\'')){
			char escape='\\';

			int stringStart,stringEnd;
			int lastStart=0;

			findString:do{
				stringStart=script.indexOf(quote,lastStart);

				// Break if there aren't any quotes
				if(stringStart==-1) break;

				// Continue, if...
				if(stringStart>0){
					//...if escaped quotation mark
					if(script.charAt(stringStart-1)==escape){
						
						//only if it's not an escaped backslash, e.g in 
						if(!(stringStart>1 && script.charAt(stringStart-1)==escape)){
							// start over again with next quotes
							lastStart=stringStart+1;
							continue;
						}
					}
					
					//...if it's meant to be an apostrophe 
					if(script.substring(stringStart-1,stringStart+1).matches("\\w\\'")){
						// start over again with next quotes
						lastStart=stringStart+1;
						continue;
					}
				}

				stringEnd=stringStart;
				while(true){
					stringEnd++;
					if(stringEnd>=script.length()){
						log.warning("---found no end for string beginning with "+script.substring(stringStart,(stringStart+10<script.length()?stringStart+10:script.length()-1))
								+" ...\". Following strings won't be found.");
						// start over again with next quotes
						lastStart=stringStart+1;
						continue findString;
					}

					// Find end if quotation mark is not escaped
					if(script.charAt(stringEnd)==quote){
						if(script.charAt(stringEnd-1)!=escape){
							//end found
							break;
						}
						if(script.charAt(stringEnd-1)==escape){
							if(stringEnd>stringStart+1){
								if(script.charAt(stringEnd-2)==escape){
									//end found because of escaped backslash
									break;
								}
							}
						}
					}
				}

				String stringText=script.substring(stringStart,stringEnd+1);
				String replacement="lc2mdltext"+stringNo;

				//We don't need, if we use Matcher.quoteReplacement() 
				//preserve backslashes (cause we need it later for replaceAll)
				//stringsInScript.add(stringText.replace("\\","\\\\"));
				stringsInScript.add(stringText);

//				script=script.replace(stringText,replacement);
				//Only replace exactly this substring, not any matching string (bad example "," as string or as separator "..","..")
				script=ConvertAndFormatMethods.replaceSubsequenceInText(script,stringStart,stringEnd,replacement);

				log.finest("---replaced "+stringText+" by "+replacement);

				// start over again from last quote (prevent loop on escaped marks)
				lastStart=stringStart+1;

				stringNo++;
			}while(stringStart!=-1);
		}

		//TODO: Cannot handle nested strings of type: 'text "in" quotes' yet
	}

	private void replaceStrings(){
		log.finer("--replace variables in strings ");
		String buf=script;
		//going inverse direction to replace nested string-replacements
		for(int i=stringNo-1;i>=0;i--){
			String stringText=stringsInScript.get(i);
			// remove " or '
			stringText=stringText.substring(1,stringText.length()-1);
			stringText=transformTextVariable(stringText);
			stringText=replaceImagePathBySVG(stringText);
			// log.finer("replace text" + stringText);

			//prevent to match lc2mdltext10 with lc2mdltext1
//			buf=buf.replaceFirst("(?<=\\W)lc2mdltext"+i+"(?=\\W)",stringText);
			buf=buf.replaceAll("(?<=\\W)lc2mdltext"+i+"(?=\\W)",Matcher.quoteReplacement(stringText));
		}

		script=buf;
	}

	private String replaceImagePathBySVG(String pathString){
		String svgString=pathString;
		pathString=pathString.substring(1,pathString.length()-1); // remove " "
		if(pathString.endsWith(".png")||pathString.endsWith(".jpg")||pathString.endsWith("gif")||pathString.endsWith(".PNG")||pathString.endsWith(".JPG")
				||pathString.endsWith("GIF")){
			svgString=FileFinder.extractFileName(pathString);
			log.finer("pathname = "+pathString+" , filename = "+svgString);
			svgString=problem.getImagesSVG().get(svgString);
			if(svgString==null){
				log.warning("did not find svg for image "+pathString);
				svgString=pathString;
			}
		}
		return svgString;
	}

	private void replaceStringKeysByValues(HashMap<String,String> replacements){
		replaceStringKeysByValues(replacements,false);
	}

	private void replaceStringKeysByValues(HashMap<String,String> replacements,boolean silent){
		for(String key:replacements.keySet()){
			//no log here; it was done before
			script=script.replace(key,replacements.get(key));
		}
	}

	private void replaceRegExKeysByValues(HashMap<String,String> replacements){
		replaceRegExKeysByValues(replacements,false);
	}

	private void replaceRegExKeysByValues(HashMap<String,String> replacements,boolean silent){
		Pattern pattern;
		Matcher matcher;
		for(String key:replacements.keySet()){
			pattern=Pattern.compile(key);
			matcher=pattern.matcher(script);
			while(matcher.find()){
				String s=matcher.group();
				if(!silent) log.finer("--replace \""+s+"\" with \""+replacements.get(key)+"\"");
				//no Matcher.quoteReplacement( here, beacuse we need regex-groups
				script=script.replaceFirst(key,replacements.get(key));
			}
		}
	}

	public String getScript(){
		return script;
	}

	public String getScriptComment(){
		return scriptComment;
	}
}
