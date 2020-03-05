package lc2mdl.lc.problem;

import lc2mdl.mdl.quiz.QuestionStack;
import lc2mdl.util.ConvertAndFormatMethods;
import lc2mdl.util.FileFinder;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PerlScript extends ProblemElement{

	private String script;
	private String scriptComment;
	private String convertWarning;
	private ArrayList<String> stringsInScript=new ArrayList<String>();

	private int arrayNo=0;
	private int stringNo=0;

	// determines if e.g. random(...) will be converted, too, not only
	// &random(...)
	private final boolean SUPPORT_OLD_CAPA_FUNCTIONS_WITHOUT_AMPERSAND=true;

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

		// FUNCTIONS
		replaceFunctions();

		// VARIABLES
		findAndReplaceVariables();

		// ARRAYS
		findAndReplaceArraysAssignments();

		// COMMENTS
		replaceComments();

		// UNKNOWN CONTROL-STRUCTURES
		searchForControlStructures();

		// CONTROL-STRUCTURES THAT CAN BE REPLACED
		replaceControlStructures();

		// SYNTAX (equal sign, semicolon)
		// need to be placed after arrays, functions and control structures !
		replaceBlocks();

		replaceSyntax();

		// replace Strings
		replaceStrings();

		// SPECIAL CHARS
		searchForSpecialChars();

		script=convertWarning+System.lineSeparator()+script;
	}

	private void addConvertWarning(String warning){
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
			log.warning("--found special chars. $:"+count[0]+", @: "+count[1]+", &: "+count[2]+", {: "+count[3]+", }: "
					+count[4]);
		}
	}

	private void replaceControlStructures(){
		ArrayList<String> controlStructures=new ArrayList<>(
				Arrays.asList("do","unless","else","elsif","if","until","while","for","foreach"));
		for(String cs:controlStructures){
			String csPat="\\W"+cs+"\\W";
			Matcher matcher=Pattern.compile(csPat).matcher(script);
			int startFind=0;
			while(matcher.find(startFind)){
				// System.out.println(startFind+" ");
				log.warning("--found control structure: "+cs+", try to replace, please check result");
				int csStart=matcher.start()+1;
				startFind=matcher.end()-1;
				int csEnd=matcher.end()-1;
				int parenStart=csEnd;
				int parenEnd=parenStart;
				int searchEnd=csEnd+20;
				if(searchEnd>script.length()) searchEnd=script.length();
				while(parenStart<searchEnd){
					if(script.charAt(parenStart)=='('){
						break;
					}
					parenStart++;
				}
				if(parenStart<searchEnd){
					parenEnd=ConvertAndFormatMethods.findMatchingParentheses(script,parenStart);
				}else{
					parenStart=csEnd;
					addConvertWarning("--found "+cs+" without parentheses ");
				}

				if(parenEnd>0){
					switch(cs){
						case "do":
							int doStart=matcher.start();
							int blockStart=matcher.end()-1;
							int blockEnd=ConvertAndFormatMethods.findMatchingParentheses(script,blockStart,false);
							if(blockEnd<0){
								log.warning("--unmatched block parantheses {..} ");
								break;
							}
							String blockString=script.substring(blockStart,blockEnd);
							String whilePat="((while)|(until))";
							Matcher csMatcher=Pattern.compile(whilePat).matcher(script.substring(blockEnd));
							if(csMatcher.find()){
								addConvertWarning("-- found do statement with closing "+csMatcher.group()
										+" -- revert statements -- please check condition.  ");
								int whileStart=csMatcher.start();
								int whileEnd=csMatcher.end();
								whileEnd=ConvertAndFormatMethods.findMatchingParentheses(script.substring(blockEnd),
										whileEnd);
								int doEnd=blockEnd+whileEnd;
								String whileString=script.substring(blockEnd).substring(whileStart,whileEnd);
								String doString=script.substring(doStart,doEnd);
								String newString=" "+whileString+blockString;
								script=script.replace(doString,newString);

							}else{
								addConvertWarning(
										"-- found do statement without closing while or until -- please check.");
							}
							break;

						case "unless":{
							// log.warning("script.length= "+script.length()+"
							// csstart= "+csStart+" csend= "+csEnd+" parenStart=
							// "+parenStart+" parenend= "+parenEnd);
							String condSub=" ";
							String cond="unless";
							if(parenStart<parenEnd){
								condSub=script.substring(parenStart,parenEnd);
							}
							condSub=" if (not "+condSub+") ";
							cond=script.substring(csStart,parenEnd);
							
							//TODO correct?
//							script=script.replaceFirst(cond,condSub);
							script=script.replace(cond,condSub);
							startFind=parenEnd;
						}
							break;

						case "else":
							String csString=matcher.group();
							String csNewString="  "+ConvertAndFormatMethods.removeCR(csString)+" ";
							script=script.replace(csString,csNewString);
							startFind=csStart+csNewString.length()+1;
							break;

						case "elsif":
							csString=matcher.group();
							csNewString="  "+ConvertAndFormatMethods.removeCR(csString)+" ";
							csNewString=csNewString.replace("elsif","elseif");
							script=script.replace(csString,csNewString);
							parenEnd+=csNewString.length()-csString.length();
							// parenEnd += 4;
							// nobreak, continue with the if case
							matcher=Pattern.compile(csPat).matcher(script);

						case "if":

							if(parenStart<parenEnd){
								String oldIf=script.substring(csStart,parenEnd);
								String newIf=oldIf+" then ";
								script=script.replace(oldIf,newIf);
								startFind=csEnd;
							}else{
								startFind=csEnd;
								addConvertWarning("-- found if without parentheses !");
							}
							break;

						case "until":
							script.replace(cs,"unless");
							parenEnd++;
							// nobreak, continue with the while case

						case "while":
							String oldWhile=script.substring(csStart,parenEnd);
							String newWhile=oldWhile+" do ";
							script=script.replace(oldWhile,newWhile);
							startFind=csEnd;
							break;

						case "for":
							if(parenStart<parenEnd){
								String forString=script.substring(csStart,parenEnd);
								String parenString=script.substring(parenStart+1,parenEnd-1);
								String[] parenSplit=parenString.split(";");
								// System.out.println(forString+"
								// "+parenString+" "+parenSplit.length);
								if(parenSplit.length>2){
									String start=parenSplit[0];
									start=start.replaceFirst("=",":");
									String cond=parenSplit[1];
									String next=parenSplit[2];
									String newForString="for "+start+" next "+next+" while( "+cond+" ) do";
									script=script.replace(forString,newForString);

								}

							}
							startFind=csEnd;
							break;

						case "foreach":
							String forString=script.substring(csStart,parenEnd);
							String parenString=script.substring(parenStart,parenEnd);
							String varString="lcvariable";
							if(csEnd<parenStart) varString=script.substring(csEnd+1,parenStart);
							String newForString="for "+varString+" in "+parenString+" do ";
							script=script.replace(forString,newForString);
							startFind=csEnd;
							break;
						default:
							break;
					}
				}

			}

		}

		for(String cs:controlStructures){
			String csPat=cs+"[^\\{]*\\{";
			Matcher matcher=Pattern.compile(csPat).matcher(script);
			while(matcher.find()){
				String csString=matcher.group();
				// System.out.println(matcher.group());
				String csNewString=ConvertAndFormatMethods.removeCR(csString);
				script=script.replace(csString,csNewString);
			}
		}

	}

	private void replaceBlocks(){
		// Find innermost block { ... }
		String blockPat="([\\r\\n]*\\{(?:\\{??[^\\{]*?\\}))+";
		Matcher matcher=Pattern.compile(blockPat).matcher(script);
		if(matcher.find()){
			String parenPat="\"[^\"]*\\{[^\"]*\"";
			Matcher pMatcher=Pattern.compile(parenPat).matcher(script);
			if(pMatcher.find()){
				addConvertWarning("--replace parentheses { .. } to ( .. ) -- please check text variables ");
			}
		}
		while(matcher.find()){
			log.finer("--replace block.");
			String block=matcher.group();
			String newBlock=replaceSyntaxInBlock(block);
			script=script.replace(block,newBlock);
			matcher=Pattern.compile(blockPat).matcher(script);
		}

	}

	private String replaceSyntaxInBlock(String block){
		HashMap<String,String> replacements=new HashMap<>();
		String newBlock=block;

		replacements.put("(?<![!=<>])=(?!=)(?=([^\"]*\"[^\"]*\")*[^\"]*;)",": ");
		replacements.put("\\{","(");
		replacements.put("\\}",")");
		replacements.put("[\\r\\n]+"," ");
		replacements.put(";[\\r\\n]*",", ");
		replacements.put(";(?=([^\"]*\"[^\"]*\")*[^\"]*;)",", ");
		replacements.put(",\\s+\\)"," )");
		Pattern pattern;
		Matcher matcher;
		for(String key:replacements.keySet()){
			pattern=Pattern.compile(key);
			matcher=pattern.matcher(newBlock);
			while(matcher.find()){
				newBlock=newBlock.replaceFirst(key,replacements.get(key));
			}
		}
		return(newBlock);
	}

	private void searchForControlStructures(){
		ArrayList<String> controlStructures=new ArrayList<>(Arrays.asList("next","last","redo","goto","sub","unless"));
		for(String cs:controlStructures){
			String csPat="\\W"+cs+"\\W";
			Matcher matcher=Pattern.compile(csPat).matcher(script);
			while(matcher.find()){
				addConvertWarning("--found unknown control structure: "+cs);
			}
		}
	}

	private void replaceComments(){
		String commentPat="#[^\n\r]*";
		Matcher matcher=Pattern.compile(commentPat).matcher(script);
		while(matcher.find()){
			String comment=matcher.group();
			// remove # at the beginning
			while(comment.charAt(0)=='#'&&comment.length()>1){
				comment=comment.substring(1);
			}
			// remove # within
			comment=comment.replaceAll("#","[HASH-SIGN]");
			comment=comment.replaceAll("\\$","[DOLLAR-SIGN]");
			comment=comment.replaceAll("/\\*","[OPEN-COMMAND]");
			comment=comment.replaceAll("\\*/","[CLOSING-COMMAND]");
			// put into c-style comments /* ... */
			comment="/*"+comment+"*/";
			script=script.replaceFirst(commentPat,comment);
		}
	}

	private void replaceSyntax(){
		HashMap<String,String> syntaxReplacements=new HashMap<>();

		// single equal sign (left no AND right no equal sign, left no !,<,>)
		// and not equal signs in quotes: (?<!=)=(?!=)(?=([^"]*"[^"]*")*[^"]*;)
		syntaxReplacements.put("(?<![!=<>])=(?!=)(?=([^\"]*\"[^\"]*\")*[^\"]*;)",": ");
		log.finer("--replace all \"=\" with \": \"");

		// newline or return at the end of line
		syntaxReplacements.put(";[\\r\\n]*",";"+System.lineSeparator());
//		syntaxReplacements.put(";[\\r\\n]*",System.lineSeparator());
		log.finer("--remove multiple empty lines");
		replaceKeysByValues(syntaxReplacements,true);
	}

	private void replaceFunctions(){
		log.finer("--replace functions");
		HashMap<String,String> functionReplacements=new HashMap<>();

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

		if(SUPPORT_OLD_CAPA_FUNCTIONS_WITHOUT_AMPERSAND){
			HashMap<String,String> additionalFunctionsWithoutAmpersand=new HashMap<String,String>();
			for(String key:functionReplacements.keySet()){
				if(key.charAt(0)=='&'){
					String val=functionReplacements.get(key);
					// make ampersand (&) optional
					String newKey="&?"+key.substring(1);// ,key.length());
					additionalFunctionsWithoutAmpersand.put(newKey,val);
				}
			}
			functionReplacements.putAll(additionalFunctionsWithoutAmpersand);
		}
		replaceKeysByValues(functionReplacements);

		// DIFFERENT SYNTAX CONVERSION (different parameters)
		// regex was not possible for balanced parentheses

		String scriptOriginal=script;
		String functionBeginPat;
		// optional &-sign (ampersand)
		if(SUPPORT_OLD_CAPA_FUNCTIONS_WITHOUT_AMPERSAND) functionBeginPat="&?\\w+\\(";
		else functionBeginPat="&\\w+\\(";
		Matcher functionMatcher=Pattern.compile(functionBeginPat).matcher(scriptOriginal);
		while(functionMatcher.find()){
			String functionName=functionMatcher.group();
			int start=functionMatcher.start();
			//char-position in script AFTER opening bracket
			int pos=functionMatcher.end();

			ArrayList<String> params=new ArrayList<>();

			// Look for balanced parenthesis and get function-params
			int lastPos=pos;
			int bracketCount=1;
			int curlyBracketCount=0;
			int squareBracketCount=0;
			while(bracketCount>0){
				switch(scriptOriginal.charAt(pos)){
					case '(':
						bracketCount++;
						break;
					case ')':
						bracketCount--;
						if(bracketCount==0){
							String param=scriptOriginal.substring(lastPos,pos);
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
							if(curlyBracketCount==0 && squareBracketCount==0){
								String param=scriptOriginal.substring(lastPos,pos);
								params.add(param);
								lastPos=pos+1;
							}
						}
						break;

				}

				if(scriptOriginal.charAt(pos)==';') break;
				pos++;
			}
			if(bracketCount!=0){
				log.warning("--function with unbalanced parentheses");
				continue;
			}
			String completeFunction=scriptOriginal.substring(start,pos);// end exclusive

			switch(functionName){
				case "&choose(":
				case "choose(":
					// turn from $var=&choose($i, ... ) into array: [...];
					// $var=array[$i]

					// generate unique arrayName
					String arrayName="lcmdlarray"+problem.getIndex(this)+arrayNo++;
					problem.addVar(arrayName);

					StringBuilder arrayDefinition=new StringBuilder(arrayName+": [");
					for(int i=1;i<params.size();i++){
						if(i!=1) arrayDefinition.append(" ,");
						arrayDefinition.append(params.get(i));
					}
					arrayDefinition.append("];");

					// make assignment on array[i]
					String assignArrayValue=arrayName+"["+params.get(0)+"];";

					// whole statement
					String leftStatement=getFirstMatchAsString(script,"[^;\\n\\r]*(?=("+functionBeginPat+"))");
					String wholeStatement=leftStatement+completeFunction;

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
					String pow=params.get(0)+"^"+params.get(1);
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
					if(!functionReplacements.containsValue(functionName))
						log.warning("--unknown function: "+functionName);
			}
		}

		HashMap<String,String> paramReplacement=new HashMap<>();
		paramReplacement.put("parameter_setting(\"([a-zA-Z][a-zA-Z0-9_]*?)\")","lcparam_$1");
		replaceKeysByValues(paramReplacement);

		// script =
		// script.replaceAll("parameter_setting(\"([a-zA-Z][a-zA-Z0-9_]*?)\")","lcparam_$1");
	}

	private void findAndReplaceVariables(){
		ArrayList<String> vars=new ArrayList<>();

		// find all variables
		String varPat="\\$([a-zA-Z]+([a-zA-Z0-9])*)";
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
				log.finer("--replace \""+leftBrace+"\" with \"[\" and \")\" with \"]\"");
				// if normal array assignment, just replace round braces
				varAssignment=varAssignment.replaceFirst(leftBracePat,"[");
				varAssignment=varAssignment.replaceFirst(rightBracePat,"]");
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
		String buf=script;
		stringNo=0;
		//Strings in "..."
		// TODO there are still problems with this regex
		String patString="(\"\")|((?<!\\\\)\"(((\\\")|[^\"])*?)[^\\\\]\")";
//		String patString="(\"\")|([\"'])(?:(?=(\\\\?))\\2.)*?\\1";
		Pattern pat=Pattern.compile(patString);
		Matcher matcher=pat.matcher(buf);
		while(matcher.find()){
			String stringText=matcher.group();
			// log.finer("--- found "+stringText);
			String replacement="lc2mdltext"+stringNo;
			stringNo++;
			stringsInScript.add(stringText);
			buf=buf.replaceFirst(patString,replacement);
		}
		
		//Strings in '...'
		patString="'(([^'])*?)'";
		pat=Pattern.compile(patString);
		matcher=pat.matcher(buf);
		while(matcher.find()){
			String stringText=matcher.group();
			// log.finer("--- found "+stringText);
			String replacement="lc2mdltext"+stringNo;
			stringNo++;
			stringsInScript.add(stringText);
			buf=buf.replaceFirst(patString,replacement);
		}

		script=buf;
	}

	private void replaceStrings(){
		log.finer("--replace variables in strings ");
		String buf=script;
		for(int i=0;i<stringNo;i++){
			String stringText=stringsInScript.get(i);
			// remove " or '
			stringText=stringText.substring(1,stringText.length()-1);
			stringText=transformTextVariable(stringText);
			stringText=replaceImagePathBySVG(stringText);
			// log.finer("replace text" + stringText);
			buf=buf.replaceFirst("lc2mdltext"+i,stringText);
		}

		script=buf;
	}

	private String replaceImagePathBySVG(String pathString){
		String svgString=pathString;
		pathString=pathString.substring(1,pathString.length()-1); // remove " "
		if(pathString.endsWith(".png")||pathString.endsWith(".jpg")||pathString.endsWith("gif")
				||pathString.endsWith(".PNG")||pathString.endsWith(".JPG")||pathString.endsWith("GIF")){
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

	private void replaceKeysByValues(HashMap<String,String> replacements){
		replaceKeysByValues(replacements,false);
	}

	private void replaceKeysByValues(HashMap<String,String> replacements,boolean silent){
		Pattern pattern;
		Matcher matcher;
		for(String key:replacements.keySet()){
			pattern=Pattern.compile(key);
			matcher=pattern.matcher(script);
			while(matcher.find()){
				String s=matcher.group();
				if(!silent) log.finer("--replace \""+s+"\" with \""+replacements.get(key)+"\"");
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
