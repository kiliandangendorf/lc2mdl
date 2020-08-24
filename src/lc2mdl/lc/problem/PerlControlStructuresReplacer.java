package lc2mdl.lc.problem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lc2mdl.Prefs;
import lc2mdl.util.ConvertAndFormatMethods;

public class PerlControlStructuresReplacer{
	public static Logger log=Logger.getLogger(PerlControlStructuresReplacer.class.getName());

	private class CondBlockPair{
		public String cond;
		public String block;

		public CondBlockPair(String cond,String block){
			this.cond=cond;
			this.block=block;
		}
	}
	private class Condition{
		public int openBrace;
		public int closeBrace;
		private String conditionStringWithBraces;
		public boolean valid;
		public Condition(int openBrace,String csName){
			conditionStringWithBraces=null;
			valid=false;
			this.openBrace=openBrace;
			
			closeBrace=findClosingConditionIndex(openBrace, csName);
			
			if(closeBrace>=0){			
				valid=true;
				//substring incl. closeBrace
				conditionStringWithBraces=script.substring(this.openBrace,closeBrace+1);
			}
		}
		public String toString(){return conditionStringWithBraces;}
	}
	private class Block{
		public int openBrace;
		public int closeBrace;
		private String blockStringWithBraces;
		public boolean valid;
		public Block(int startIndex,String csName,boolean mandatory){
			blockStringWithBraces=null;
			valid=false;
			
			int[] blockIndices=findIndicesOfFollowingBlockParentheses(startIndex,csName,mandatory);

			this.openBrace=blockIndices[0];
			this.closeBrace=blockIndices[1];
			
			if(openBrace!=-1){
				valid=true;
				//substring incl. closeBrace
				blockStringWithBraces=script.substring(openBrace,closeBrace+1);
				//cleanup in blocks will be done later...
			}
		}
		public String toString(){return blockStringWithBraces;}
	}

	private PerlScript perlScript;
	private String script;
	
	private int boolNo=0;
	private int varNo=0;
	private HashMap<String,String> csDict;


	public PerlControlStructuresReplacer(PerlScript perlScript,String script){
		this.perlScript=perlScript;
		this.script=script;
	}

	public String getReplacedScript(){
		//nice: irritating braces in strings or comments aren't possible anymore;) (done in saveStrings)
		
		//temporary dictionary to prevent replacing same CS in different contexts
		//Contains critical CS this program writes ITSELF. These will be temporarily named as values to not confuse itself. 
		//In the end there will be clean maxima CS ;)
		csDict=new HashMap<>();
		csDict.put("if","lc2mdl_IF");
		csDict.put("else","lc2mdl_ELSE");
		csDict.put("while","lc2mdl_WHILE");
		csDict.put("unless","lc2mdl_UNLESS");
		csDict.put("do","lc2mdl_DO");
		csDict.put("for","lc2mdl_FOR");
		csDict.put("next","lc2mdl_NEXT");
		csDict.put("in","lc2mdl_IN");
		
		
		// CONDITIONS
		// if (...) {...} elsif (...) {...} else {...} 
		// unless (...) {...} elsif (...) {...} else {...} 
		replaceConditions();		
		
				
		// LOOPS
		// do {...} while (...)
		// do {...} until (...)
		replaceDoLoops();
		
		// while (...) {...}
		// until (...) {...}
		replaceWhileLoops();

		// for (...; ...; ...) {...}
		// for (ARRAY) {...}
		replaceForLoops();
		
		// foreach (ARRAY) {...}
		replaceForEachLoops();
		

		// OPERATORS
		// e.g. $a++ --> (a:a+1) 
		// e.g. 1..9 --> makelist(i,i,1,9)
		replaceOperators();

		
		//Still TODO
		// - multiline statements currently only affects blocks. New statements are build in oneline
		// - do statement:  do '/foo/stat.pl';
		// - if and until as CS AFTER one SINGLE statement
		//		@see https://www.perltutorial.org/perl-if/
		//		ex: my $a = 1; print("Welcome to Perl if tutorial\n") if($a == 1);
		//		statement unless(condition);
		//		idea: Search backwards up to ";"
		// - Ternary Operator (condition)?{true}:{false} -->Check syntax
		// - Loop control Statements: next & last
		//		last≈break-->return? http://maxima.sourceforge.net/docs/manual/maxima_37.html
		// - Arrays (Lists) in Maxima start at 1 (not at 0 as it is in Perl)


		
		//TODO: Look for missed CS before re-replacing
		//if, for, foreach, etc...
		
		for(String cs:csDict.keySet()){
			script=script.replace(csDict.get(cs),cs);
		}
		
		return script;
	}

	private void replaceOperators(){
				
		String operatorPat;
		StringBuffer sb;
		Matcher matcher;

		// range operator 
		// 1..9 --> makelist(i,i,1,9)
		log.finer("--replace range operators \"x..y\" with \"(makelist(i,i,x,y)\"");
		operatorPat="(?<=\\b)[\\w]+\\.\\.[\\w]+(?=\\b)";
		sb=new StringBuffer();
		matcher=Pattern.compile(operatorPat).matcher(script);
		while(matcher.find()){
			String list=matcher.group();
			String[] listBounds=list.split("\\.\\.");
			if(listBounds.length!=2){
				addConvertWarningToScript("---error in match for range operator in \""+list+"\" (will not work on)");
			}else{
				int from, to;
				// test if numeric (maxima makelist does not support on strings)
				try{
					from=Integer.parseInt(listBounds[0]);
					to=Integer.parseInt(listBounds[1]);
					
					// in maxima this "i" is in function scope and won't affect other "i"'s -->no own variable is needed
					// 1..9 --> makelist(i,i,1,9) (where makelist (expr, i, i_0, i_1) )
					String replacement="makelist(i,i,"+from+","+to+")";
					
					matcher.appendReplacement(sb,replacement);
				}catch(NumberFormatException e){
					addConvertWarningToScript("---found range operator \""+list+"\" with not-numeric range-operator (will not work on. Only numeric range operators are supported yet)");
				}
			}
		}
		matcher.appendTail(sb);
		script=sb.toString();		
		
		// ++ -> +1
		// $a++ => (a:a+1) 
		log.finer("--replace all \"x++\" with \"(x: x+1)\"");		
		operatorPat="(?<=\\b)[\\w]+ {0,}\\+\\+(?!\\+)";
		sb=new StringBuffer();
		matcher=Pattern.compile(operatorPat).matcher(script);
		while(matcher.find()){
			String var=matcher.group();
			// remove ++ in the end
			var=var.substring(0,var.length()-2);
			//at least one char must be in front of "++" (otherwise there would be no match)
			String replacement=var+": "+var+" + 1";
			replacement="("+replacement+")";
			matcher.appendReplacement(sb,replacement);
		}
		matcher.appendTail(sb);
		script=sb.toString();

		// -- -> -1		
		// $a-- => (a:a-1) 
		log.finer("--replace all \"x--\" with \"(x: x-1)\"");
		operatorPat="(?<=\\b)[\\w]+ {0,}\\-\\-(?!\\-)";
		sb=new StringBuffer();
		matcher=Pattern.compile(operatorPat).matcher(script);
		while(matcher.find()){
			String var=matcher.group();
			// remove -- in the end
			var=var.substring(0,var.length()-2);
			//at least one char must be in front of "--" (otherwise there would be no match)
			String replacement=var+": "+var+" - 1";
			replacement="("+replacement+")";
			matcher.appendReplacement(sb,replacement);
		}
		matcher.appendTail(sb);
		script=sb.toString();
		
		// $# -> length()		
		// $#a => (length(a)-1) 
		log.finer("--replace all array-lengths \"$#x\" with \"(length(x)-1)\"");
		//these Dollars wasn't recognized before because of the following "#"
		operatorPat="(?<=\\W)\\$\\#[\\w]+(?=\\b)";
		sb=new StringBuffer();
		matcher=Pattern.compile(operatorPat).matcher(script);
		while(matcher.find()){
			String var=matcher.group();
			// remove "$#" in the beginning
			var=var.substring(2);
			//var must be at least one character (otherwise there would be no match)
			String replacement="length("+var+")-1";
			replacement="("+replacement+")";
			matcher.appendReplacement(sb,replacement);
		}
		matcher.appendTail(sb);
		script=sb.toString();

		
		//x%y -> mod(x,y) 
		log.finer("--replace all \"x%y\" with \" mod(x, y) \"");
		script=script.replaceAll("([\\w]+) {0,}\\% {0,}([\\w]+)","mod($1, $2)");

		//x**y -> x^y 
		log.finer("--replace all \"x**y\" with \" x ^ y \"");
		script=script.replaceAll("([\\w]+) {0,}\\*\\* {0,}([\\w]+)","$1 ^ $2");

		
		//BEFORE equals signs ("=") are replaced
		
		//x+=y -> x: x+y 
		log.finer("--replace all \"x+=y\" with \" x: x + y \"");
		script=script.replaceAll("([\\w]+) {0,}\\+= {0,}([\\w]+)","$1: $1 + $2");
		
		//x-=y -> x: x-y 
		log.finer("--replace all \"x-=y\" with \" x: x - y \"");
		script=script.replaceAll("([\\w]+) {0,}\\-= {0,}([\\w]+)","$1: $1 - $2");
		
		//x*=y -> x: x*y 
		log.finer("--replace all \"x*=y\" with \" x: x * y \"");
		script=script.replaceAll("([\\w]+) {0,}\\*= {0,}([\\w]+)","$1: $1 * $2");
		
		//x/=y -> x: x/y 
		log.finer("--replace all \"x/=y\" with \" x: x / y \"");
		script=script.replaceAll("([\\w]+) {0,}\\/= {0,}([\\w]+)","$1: $1 / $2");
		
		//x%=y -> x: mod(x,y) 
		log.finer("--replace all \"x%=y\" with \" x: mod(x, y) \"");
		script=script.replaceAll("([\\w]+) {0,}\\%= {0,}([\\w]+)","$1: mod($1, $2)");
		
		//x**=y -> x: x^y 
		log.finer("--replace all \"x**=y\" with \" x: x^y \"");
		script=script.replaceAll("([\\w]+) {0,}\\*\\*= {0,}([\\w]+)","$1: $1 ^ $2");

		//TODO: Consider calculation rules! E.g. "a*=b+c" should be "a:a*(b+c)" but actually it is "a:a*b+c" :/
		
		
		//Strings are still replaced by "lc2mdltext#" so we can handle it as variable
		//x.=y -> x: sconcat(x, y) 
		log.finer("--replace all \"x.=y\" with \" x: sconcat(x, y) \"");
		script=script.replaceAll("([\\w]+) {0,}\\.= {0,}([\\w]+)","$1: sconcat($1, $2)");
		//TODO "." operator
		
		// = -> :
		log.finer("--replace all \"=\" with \": \"");
		// single equal sign (left no AND right no equal sign, left no !,<,>)
		// since savestrings() not needed anymore to look for " (2020-04-23)
		// and not equal signs in quotes: (?<!=)=(?!=)(?=([^"]*"[^"]*")*[^"]*;)
		// script=script.replaceAll("(?<![!=<>]) {0,}=(?!=) {0,}(?=([^\"]*\"[^\"]*\")*[^\"]*;)",": ");
		script=script.replaceAll("(?<![=<>])=(?![=<>])",": ");

		// ** -> ^
		// replace double ** (only if double occurs, not more or less)
		// look ahead (?<![\\*])
		// look behind (?!\\*)
		log.finer("--replace all \"**\" with \"^\"");
		script=script.replaceAll("(?<![\\*])\\*\\*(?!\\*)","^");

		//== -> =
		log.finer("--replace all \"==\" with \"=\"");
		script=script.replaceAll("(?<![=])==(?!=)","=");

		//&& -> and
		log.finer("--replace all \"&&\" with \" and \"");
		script=script.replaceAll("(?<![&]) {0,}&& {0,}(?!&)"," and ");

		//|| -> or
		log.finer("--replace all \"||\" with \" or \"");
		script=script.replaceAll("(?<![\\|]) {0,}\\|\\| {0,}(?!\\|)"," or ");		
		
		// TODO: More Operators 

	}

	private void replaceForEachLoops(){
		// foreach [$var] (ARRAY) {...}
		String forBeginPat="(?<=\\b)"+Pattern.quote("foreach")+"\\s*\\(";

		int stmtStart;
		int curIndex=0;

		findingForEachStatements:
			do{
				int[] stmtMatch=ConvertAndFormatMethods.getRegexStartAndEndIndexInText(forBeginPat,script,curIndex);
				stmtStart=stmtMatch[0];
				// Break if no FOREACH was found
				if(stmtStart==-1) break;

				//find FOREACH condition (...)
				int openCondBrace=stmtMatch[1];
				Condition foreachCondition=new Condition(openCondBrace, "foreach");
				if(!foreachCondition.valid){
					curIndex=stmtStart+1;
					continue findingForEachStatements;
				}
				
				//now find block {...}
				Block foreachBlock=new Block(foreachCondition.closeBrace,"foreach",true);
				if(!foreachBlock.valid){
					curIndex=stmtStart+1;
					continue findingForEachStatements;
				}
				
				//replace this foreach-loop
				int nextIndex=replaceOneForEachLoopAndReturnNewIndex(stmtMatch,foreachCondition,foreachBlock,"foreach");
				if(nextIndex<0){
					//Error in replacing
					curIndex=stmtStart+1;
					continue findingForEachStatements;					
				}else{
					//everything's fine
					curIndex=stmtStart+1;//nextIndex;
				}
			}while(stmtStart!=-1);
	}
	
	private int replaceOneForEachLoopAndReturnNewIndex(int[] stmtMatch, Condition foreachCondition, Block foreachBlock, String forName){
		int stmtStart=stmtMatch[0];
		int stmtEnd;
		//for|foreach [$var] (1..9) {BLOCK}
		//for|foreach [$var] (@array) {BLOCK}
		//if it is an array inside. Then its the SAME as a foreach loop
		//@see https://www.perltutorial.org/perl-for-loop/

	    String controlVarName;
		//find optional variable name between FOR and "(" (empty string, if there is no)
		controlVarName=script.substring(stmtStart+forName.length(), foreachCondition.openBrace).trim();
		if(controlVarName.length()==0){
			// generate unique control variable 
		    controlVarName="lc2mdl_for_cvar"+perlScript.problem.getIndex(perlScript)+"_"+varNo++;			
		}

	    // TODO case of nested loops following replacement should only affect this block layer
	    //		it's possible to have the following: foreach(@b){print "$_";foreach(@c){print "$_";}}
	    //		maybe search backwards? 
	    // replace $_ by controlVarName in block
		String foreachBlockWithVar=foreachBlock.toString().replaceAll("(?<=\\b)\\_(?=\\b)",Matcher.quoteReplacement(controlVarName));

		//Warn that there could be control variables in escaped strings...
		if(foreachBlockWithVar.contains("lc2mdltext")){
			addConvertWarningToScript("---found strings in \""+forName+"\" loop. Not sure, if they contain the control variable. Please look for \"_\" resp. (?<=\\b)\\_(?=\\b) in loop strings resp. sconcat() and replace manually by \"lc2mdl_for_cvarX_Y\"");
		}

		String conditionStringWithoutBraces=foreachCondition.toString().substring(1,foreachCondition.toString().length()-1);
		String optionalAssignments="";

		// check condition for "a..b" or "a","b","c" or array-variable
		if(conditionStringWithoutBraces.contains("..")){
			// (a..b)
			// similar to what is done in replaceOpertaors(). But here specific feedback
			String[] arrayBounds=conditionStringWithoutBraces.split("\\.\\.");
			if(arrayBounds.length!=2){
				addConvertWarningToScript("---found \""+forName+"\" with unexpected range-operator (will not work on)");
				return -1;
			}
			int from, to;
			// test if numeric (maxima does not support on strings)
			try{
				from=Integer.parseInt(arrayBounds[0]);
				to=Integer.parseInt(arrayBounds[1]);
			}catch(NumberFormatException e){
				addConvertWarningToScript("---found \""+forName+"\" with not-numeric range-operator (will not work on)");
			    return -1;  
			}
			
			// build new ConditionString
			// in maxima this "i" is in on scope and won't affect other "i"'s -->no own variable is needed
			// maxima example: for a in (makelist(i,i,1,9)) do (display(a));
			conditionStringWithoutBraces="makelist(i,i,"+from+","+to+")";
		}else if(conditionStringWithoutBraces.contains(",")){
			// ("a","b","c")
			String[] entries=conditionStringWithoutBraces.split(",");
		    String arrayName="lc2mdl_for_array"+perlScript.problem.getIndex(perlScript)+"_"+varNo++;			

			StringBuilder arrayDefinition=new StringBuilder(arrayName+": [");
			for(int i=1;i<entries.length;i++){
				if(i!=1) arrayDefinition.append(" ,");
				arrayDefinition.append(entries[i]);
			}
			arrayDefinition.append("];");
			arrayDefinition.append(System.lineSeparator());

			optionalAssignments+=arrayDefinition;
			// new conditionString is just the array-name
			conditionStringWithoutBraces=arrayName;
		}else{
			// (array)
			// Dollar- and At-Signs were removed before
			if(conditionStringWithoutBraces.matches("\\w+")){
				//seems to be an array-variable that was declared before...
				//everythings'fine go on
			}else{
				//if nothing above
				addConvertWarningToScript("---found \""+forName+"\" with one unexpected parameter, that is not an array (will not work on)");
				return -1;
			}			
		}
		
		//build new maxima statement string
		String maximaStmtText=optionalAssignments;
		//for VAR in ARRAY do {BLOCK}
		maximaStmtText+=" "+csDict.get("for")+" ";
		maximaStmtText+=controlVarName+" ";
		maximaStmtText+=csDict.get("in")+" "+"("+conditionStringWithoutBraces+")"+" ";
		maximaStmtText+=csDict.get("do")+" "+foreachBlockWithVar+" ";
		if(Prefs.ADD_SEMICOLON_AT_END_OF_STMT)maximaStmtText+=";";

		stmtEnd=foreachBlock.closeBrace;

		//replace old statement
		script=ConvertAndFormatMethods.replaceSubsequenceInText(script,stmtStart,stmtEnd,maximaStmtText);

		log.info("---found control structure \""+forName+"\", try to replace and reorder, please check result");

		// return end of new statement in script
		return stmtStart+maximaStmtText.length();
	}

	private void replaceForLoops(){
		// for (...; ...; ...) {...}
		// for (ARRAY) {...}
		String forBeginPat="(?<=\\b)"+Pattern.quote("for")+"\\s*\\(";
		
		//Vars are already replaced, so there is no $ in front
		String forWithVarBeginPat="(?<=\\b)"+Pattern.quote("for")+"(\\s*\\(| {1,}((\\$)?[\\w]+\\s*)?\\()";
		forBeginPat=forWithVarBeginPat;
		
		int stmtStart,stmtEnd;
		int curIndex=0;

		findingForStatements:
		do{
			int[] stmtMatch=ConvertAndFormatMethods.getRegexStartAndEndIndexInText(forBeginPat,script,curIndex);
			stmtStart=stmtMatch[0];
			// Break if no FOR was found
			if(stmtStart==-1) break;

			//find FOR condition (...)
			int openCondBrace=stmtMatch[1];
			Condition wholeForCondition=new Condition(openCondBrace, "for");
			if(!wholeForCondition.valid){
				curIndex=stmtStart+1;
				continue findingForStatements;
			}
						
			//now find block {...}
			Block forBlock=new Block(wholeForCondition.closeBrace,"for",true);
			if(!forBlock.valid){
				curIndex=stmtStart+1;
				continue findingForStatements;
			}

			//splitting up and reorder condition
			String[] forParams=wholeForCondition.toString().substring(1,wholeForCondition.toString().length()-1).split(";");
			if(forParams.length==1){
				//for (1..9) {BLOCK}
				//for (@array) {BLOCK}
				
				//Do same as foreach ;)
				int nextIndex=replaceOneForEachLoopAndReturnNewIndex(stmtMatch,wholeForCondition,forBlock,"for");

				if(nextIndex<0){
					//Error in replacing
					curIndex=stmtStart+1;
					continue findingForStatements;					
				}else{
					//everything's fine
					curIndex=stmtStart+1;//nextIndex;
				}
			}else if(forParams.length==3){
				//for (INIT ; CONDITION ; COMMAND) {BLOCK}
				String init=forParams[0].trim();
				String condition=forParams[1].trim();
				String command=forParams[2].trim();
		
				//build new maxima statement string
				//for INIT next COMMAND while (CONDITION) do {BLOCK}
				String maximaStmtText=" "+csDict.get("for")+" ";
				maximaStmtText+="("+init+") "+csDict.get("next")+" ("+command+") "+csDict.get("while")+" ("+condition+") ";
				//TODO: is NEXT still correct, if I replace ++ Operator?
				
				maximaStmtText+=csDict.get("do")+" "+forBlock+" ";
				if(Prefs.ADD_SEMICOLON_AT_END_OF_STMT)maximaStmtText+=";";
	
				stmtEnd=forBlock.closeBrace;
	
				//replace old statement
				script=ConvertAndFormatMethods.replaceSubsequenceInText(script,stmtStart,stmtEnd,maximaStmtText);
	
				log.info("---found control structure \""+"for"+"\", try to replace and reorder, please check result");
	
				// start over again from last match (prevents loop on replacement)
				curIndex=stmtStart+1;
			}else{
				//for with 0, 2 or more than 3 Parts in (...)
				addConvertWarningToScript("---found \""+"for"+"\" with unexpected parameters (will not work on)");

				curIndex=stmtStart+1;
				continue findingForStatements;
			}
		}while(stmtStart!=-1);
	}
	
	private void replaceWhileLoops(){
		// while (...) {...}
		// until (...) {...}
		for(String loopType:Arrays.asList("while","until")){
			//FIND \b if {0,}(
			String whileBeginPat="(?<=\\b)"+Pattern.quote(loopType)+"\\s*\\(";

			int stmtStart,stmtEnd;
			int curIndex=0;

			findingWhileStatements:
			do{
				int[] stmtMatch=ConvertAndFormatMethods.getRegexStartAndEndIndexInText(whileBeginPat,script,curIndex);
				stmtStart=stmtMatch[0];
				// Break if no WHILE or UNTIL was found
				if(stmtStart==-1) break;

				//find WHILE resp. UNTIL condition (...)
				int openCondBrace=stmtMatch[1];
				Condition whileCondition=new Condition(openCondBrace, loopType);
				if(!whileCondition.valid){
					curIndex=stmtStart+1;
					continue findingWhileStatements;
				}

				//now find block {...}
				Block whileBlock=new Block(whileCondition.closeBrace,loopType,true);
				if(!whileBlock.valid){
					curIndex=stmtStart+1;
					continue findingWhileStatements;
				}

				//build new maxima statement string
				String maximaStmtText="";
				switch(loopType){
					case "while":
						maximaStmtText=" "+csDict.get("while")+" "+whileCondition+" "+csDict.get("do")+" "+whileBlock+" ";
						break;
					case "until":
						maximaStmtText=" "+csDict.get("unless")+" "+whileCondition+" "+csDict.get("do")+" "+whileBlock+" ";
						break;
				}
				if(Prefs.ADD_SEMICOLON_AT_END_OF_STMT)maximaStmtText+=";";

				stmtEnd=whileBlock.closeBrace;

				//replace old statement
				script=ConvertAndFormatMethods.replaceSubsequenceInText(script,stmtStart,stmtEnd,maximaStmtText);

				log.info("---found control structure \""+loopType+"\", try to replace and reorder, please check result");

				// start over again from last match (prevents loop on replacement)
				curIndex=stmtStart+1;
			}while(stmtStart!=-1);
		}
		
	}
	private void replaceDoLoops(){
		//do {...} while (...)
		//do {...} until (...)

		int doStart,doEnd;
		int curIndex=0;
		
		findingDoLoops:
		do{
	
			Block doBlock=null;
			String doLoopBeginPat="(?<=\\b)do\\s*\\{";
			
			int[] doMatch=ConvertAndFormatMethods.getRegexStartAndEndIndexInText(doLoopBeginPat,script,curIndex);
			doStart=doMatch[0];
			//break if no DO was found
			if(doStart==-1)break;
			
			//find DO block {...}
			int doBlockOpenBrace=doMatch[1];
			doBlock=new Block(doBlockOpenBrace,"do",true);
			if(!doBlock.valid){
				curIndex=doStart+1;
				continue findingDoLoops;
			}
			
			//now find DO condition (while|until) (...)			
			String loopConditionPattern="(?<=\\b)(while|until)\\s*\\(";
			int[] loopConditionMatch=ConvertAndFormatMethods.getRegexStartAndEndIndexInText(loopConditionPattern,script,doBlock.closeBrace);
			int loopConditionStart=loopConditionMatch[0];
			
			if(loopConditionStart==-1){
				addConvertWarningToScript("---found \""+"do"+"\" without any condition (will not work on)");
				curIndex=doStart+1;
				continue findingDoLoops;
			}
			//there are loopConditions somewhere in script
			int openCondBrace=loopConditionMatch[1];
			
			String fromBlockToCondition=script.substring(doBlock.closeBrace+1,openCondBrace);
			String conditionType=null;
			switch(fromBlockToCondition.trim()){
				case "while":
				case "until":
					//set name
					conditionType=fromBlockToCondition.trim();
				break;
				default:
					addConvertWarningToScript("---found \"do\" statement without closing while or until (will not work on)");
					curIndex=doStart+1;
					continue findingDoLoops;
			}
			
			Condition loopCondition=new Condition(openCondBrace, "do");
			if(!loopCondition.valid){
				curIndex=doStart+1;
				continue findingDoLoops;	
			}
			
			
			//build new maxima statement string
			//do {BLOCK} while (CONDITION) => bool x: true; while (x) do {BLOCK; x:CONDITION}
		    String boolName="lc2mdl_do_bool"+perlScript.problem.getIndex(perlScript)+"_"+boolNo++;
		    perlScript.problem.addVar(boolName);
		    
		    String maximaStmtText="/* replaced perl-"+csDict.get("do")+"-loop here using \""+boolName+"\" as helper: */"+System.lineSeparator();
		    maximaStmtText+=" "+boolName+" : true"+";"+System.lineSeparator();
			switch(conditionType){
				case "while":
					maximaStmtText+=" "+csDict.get("while")+" ("+boolName+") ";
					break;
				case "until":
					maximaStmtText+=" "+csDict.get("while")+" (not "+boolName+") ";
					break;
			}
			maximaStmtText+=csDict.get("do")+" ";
			
			String doBlockModified=doBlock.toString().substring(0,doBlock.toString().length()-1);
			doBlockModified+=boolName+" : "+loopCondition+" }";
			maximaStmtText+=doBlockModified+" ";
			if(Prefs.ADD_SEMICOLON_AT_END_OF_STMT)maximaStmtText+=";";

			doEnd=loopCondition.closeBrace;
			
			script=ConvertAndFormatMethods.replaceSubsequenceInText(script,doStart,doEnd,maximaStmtText);
	
			addConvertWarningToScript("---found \"do\" statement with closing \""+conditionType+"\", try to replace and reorder with helper var \""+boolName+"\" -- please check condition.  ");
	
			// start over again from last match (prevents loop on replacement)
			curIndex=doStart+1;
			
		}while(doStart!=-1);

		
	}

	private void replaceConditions(){
		//good news: Perl needs to have braces around (condition) and {block} (syntax error without)

		for(String conditionType:Arrays.asList("if","unless")){
			//FIND \b if {0,}(
			String ifBeginPat="(?<=\\b)"+Pattern.quote(conditionType)+"\\s*\\(";

			int stmtStart,stmtEnd;
			int curIndex=0;

			findingIfStatements:
			do{
				int[] stmtMatch=ConvertAndFormatMethods.getRegexStartAndEndIndexInText(ifBeginPat,script,curIndex);
				stmtStart=stmtMatch[0];
				// Break if no IF or UNLESS was found
				if(stmtStart==-1) break;

				//find IF condition (...)
				int openCondBrace=stmtMatch[1];
				Condition ifCondition=new Condition(openCondBrace, conditionType);
				if(!ifCondition.valid){
					curIndex=stmtStart+1;
					continue findingIfStatements;
				}

				//now find IF block {...}
				Block ifBlock=new Block(ifCondition.closeBrace,conditionType,true);
				if(!ifBlock.valid){
					curIndex=stmtStart+1;
					continue findingIfStatements;
				}

				//look for ELSIF (cond) {block}
				ArrayList<CondBlockPair> elsifConditionAndBlocksList=new ArrayList<>();
				int lastBlockEnd=ifBlock.closeBrace;
				int optionalElsifStart;
				//n-times
				do{
					//look from lastBlockIndices[1] on for literal ELSIF
					int[] optionalElsifMatch=ConvertAndFormatMethods.getRegexStartAndEndIndexInText("(?<=\\b)elsif\\s*\\(",script,lastBlockEnd);
					optionalElsifStart=optionalElsifMatch[0];
					if(optionalElsifStart==-1) break;

					//there are elsif somewhere in script
					int openCondBraceElsif=optionalElsifMatch[1];
					String fromLastBlockToElsif=script.substring(lastBlockEnd+1,openCondBraceElsif);
					//break if it's not directly follwing
					if(!fromLastBlockToElsif.trim().equals("elsif")) break;

					//direct following elsif was found

					//find ELSIF condition (...)
					Condition elsifCondition=new Condition(openCondBraceElsif, "elsif");
					if(!elsifCondition.valid){
						curIndex=stmtStart+1;
						continue findingIfStatements;						
					}

					//now find ELSIF block {...}
					Block elsifBlock=new Block(elsifCondition.closeBrace,"elsif",true);
					if(!elsifBlock.valid){
						curIndex=stmtStart+1;
						continue findingIfStatements;
					}

					elsifConditionAndBlocksList.add(new CondBlockPair(elsifCondition.toString(),elsifBlock.toString()));
					lastBlockEnd=elsifBlock.closeBrace;
				}while(optionalElsifStart!=-1);

				//look for ELSE {block}
				//one time
				//look from lastBlockIndices[1] on for literal ELSE (no condition here, so only look BEFORE "{")
				Block elseBlock=null;
				int[] optionalElseMatch=ConvertAndFormatMethods.getRegexStartAndEndIndexInText("(?<=\\b)else\\s*(?=\\{)",script,lastBlockEnd);
				int optionalElseStart=optionalElseMatch[0];

				if(optionalElseStart!=-1){
					int beforeElseBlock=optionalElseMatch[1];
					//there are else somewhere in script
					String fromLastBlockToElse=script.substring(lastBlockEnd+1,beforeElseBlock+1);
					if(fromLastBlockToElse.trim().equals("else")){
						//direct following else was found

						//no condition ;) 

						//now find ELSE block {...}
						elseBlock=new Block(beforeElseBlock,"else",true);
						if(!elseBlock.valid){
							curIndex=stmtStart+1;
							continue findingIfStatements;
						}
						lastBlockEnd=elseBlock.closeBrace;
					}
				}

				//build new maxima statement string
				String maximaStmtText="";
				switch(conditionType){
					case "if":
						maximaStmtText=" "+csDict.get("if")+" "+ifCondition+" then "+ifBlock+" ";
						break;
					case "unless":
						//substring(1) means without open brace
						maximaStmtText=" "+csDict.get("if")+" (not "+ifCondition.toString().substring(1)+" then "+ifBlock+" ";
						break;
				}

				for(CondBlockPair pair:elsifConditionAndBlocksList){
					maximaStmtText+="elseif "+pair.cond+" then "+pair.block+" ";
				}
				if(elseBlock!=null){
					maximaStmtText+="else "+elseBlock+" ";
				}
				if(Prefs.ADD_SEMICOLON_AT_END_OF_STMT)maximaStmtText+=";";

				stmtEnd=lastBlockEnd;

				//replace old statement
				script=ConvertAndFormatMethods.replaceSubsequenceInText(script,stmtStart,stmtEnd,maximaStmtText);

				log.info("---found control structure \""+conditionType+"\", try to replace and reorder, please check result");

				// start over again from last match (prevents loop on replacement)
				curIndex=stmtStart+1;
			}while(stmtStart!=-1);
		}
	}
	
	private int findClosingConditionIndex(int openBrace,String cs){
	
		//function returns one char AFTER closing brace
		int closeBrace=ConvertAndFormatMethods.findMatchingParentheses(script,openBrace)-1;
		
		if(closeBrace<0){
			//NO CONDITION END WAS FOUND
			addConvertWarningToScript("---found \""+cs+"\" without condition end \")\" (will not work on)");
			return -1;
		}
		return closeBrace;
	}

	/**
	 * @param startIndex
	 *            from this index, search will be start
	 * @param cs
	 *            name of current CS for Logging an Warning
	 * @param mandatory
	 *            if true: no non-whitespace-char is allowed between
	 *            startIndex+1 and Block
	 * @return [-1,-1] in failure
	 */
	private int[] findIndicesOfFollowingBlockParentheses(int startIndex,String cs,boolean mandatory){
		int[] indices=new int[]{-1,-1};

		int openBlockParen=script.indexOf("{",startIndex);

		if(openBlockParen<0){
			//NO BLOCK BEGIN WAS FOUND AT ALL
			addConvertWarningToScript("---found \""+cs+"\" without any block \"{\" (will not work on)");
			return indices;
		}

		if(mandatory){
			//trivial case if "{" is on startIndex
			if(startIndex<openBlockParen){
				//if anything else between startIndex (eg ")") and expected "{" than free space 
				String fromCondToBlock=script.substring(startIndex+1,openBlockParen);
				if(!fromCondToBlock.trim().isEmpty()){
					//NO BLOCK WAS FOUND AS EXPECTED
					addConvertWarningToScript("---found unexpected CS "+" between \""+cs+"\" and mandatory block begin (will not work on)");
					return indices;
				}
			}
		}

		int closeBlockParen=ConvertAndFormatMethods.findMatchingParentheses(script,startIndex,false)-1;
		if(closeBlockParen<0){
			//NO BLOCK END WAS FOUND
			addConvertWarningToScript("---found \""+cs+"\" without block end \"}\" (will not work on)");
			return indices;
		}
		return new int[]{openBlockParen,closeBlockParen};
	}
	
	private void addConvertWarningToScript(String warning){
		perlScript.addConvertWarning(warning);
	}
}
