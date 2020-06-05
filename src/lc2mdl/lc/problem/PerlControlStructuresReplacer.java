package lc2mdl.lc.problem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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
		public String conditionStringWithBraces;
		public Condition(int openBrace){
			conditionStringWithBraces=null;
			this.openBrace=openBrace;
			//function returns one char AFTER closing brace
			closeBrace=ConvertAndFormatMethods.findMatchingParentheses(script,openBrace)-1;
			
			if(closeBrace!=-1){			
				//substring incl. closeBrace
				conditionStringWithBraces=script.substring(this.openBrace,closeBrace+1);
			}
		}
		public String toString(){return conditionStringWithBraces;}
	}
	private class Block{
		public int openBrace;
		public int closeBrace;
		public String blockStringWithBraces;
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
				if(!allowMultilineBlocks) blockStringWithBraces=makeBlockOneLine(blockStringWithBraces);				
			}
		}
		public String toString(){return blockStringWithBraces;}
	}

	private PerlScript perlScript;
	private String script;
	private boolean allowMultilineBlocks;

	public PerlControlStructuresReplacer(PerlScript perlScript,String script){
		this.perlScript=perlScript;
		this.script=script;
	}

	public String getReplacedScript(){
		//TODO: MULTILINE BOOLEAN?
		allowMultilineBlocks=false;

		//nice: Braces in strings aren't possible anymore;) (done in saveStrings)
		//good news: Perl needs to have braces around (condition) and {block} (syntax error without)

		//CONDITIONS
		//if elsif else AND unless elsif else
		replaceConditions();
		//TODO: if auch NACH EINEM Stmt möglich
		//@see https://www.perltutorial.org/perl-if/
		//my $a = 1; print("Welcome to Perl if tutorial\n") if($a == 1);
		//Dasselbe für unless: statement unless(condition);
		//aber jeweils SINGLE-STATEMENT, kein Block ;)
		
		//TODO: Ternary Operator (condition)?{true}:{false} -->Check syntax
		
		//DO before WHILE
		// do{} while()
		// while(){}
		//TODO: gucken, dass das zweite NICHT erneut ersetzt wird
		//auch möglich:  do '/foo/stat.pl';
		
		//LOOPS
		//DO (WHILE | UNTIL)
		//WHILE
		//UNTIL
		//Loop control Statements: next & last
		//last≈break-->return? http://maxima.sourceforge.net/docs/manual/maxima_37.html
		
		//OPERATORS

		//FOR
		//FOREACH
		//NACH do und while
		
		return script;
	}

	private void doingStuff(String script){


		/*
		 * HINT: Best way to use Matcher seems to be using
		 * "matcher.appendReplacement(...)" and "matcher.appendTail(...)" on a
		 * StringBuffer. This makes sure, to replace exactly this match (not all
		 * matching Strings). Problem here is, we need not only the match, but
		 * also some charSequences before and after. TODO: Maybe we need a
		 * separate Matcher for each CS and use appendReplacement(...) in most
		 * possible cases... TODO: in this case, we should use regex look ahead
		 * and behind, eg. String csPat="(?<=[\\W])"+cs+"(?=\\W)";
		 */
		/*
		 * //Linked HashMap to keep order of insertion
		 * LinkedHashMap<String,String> controlStructureStringReplacements;
		 * 
		 * //Keep order in this List: for has do be done BEFORE foreach
		 * ArrayList<String> controlStructures=new ArrayList<>(
		 * Arrays.asList("do","unless","else","elsif","if","until","while","for"
		 * ,"foreach")); for(String cs:controlStructures){
		 * controlStructureStringReplacements=new LinkedHashMap<>();
		 * 
		 * String csPat="\\W"+cs+"\\W"; Matcher
		 * matcher=Pattern.compile(csPat).matcher(script); int startFind=0;
		 * while(matcher.find(startFind)){ // System.out.println(startFind+" ");
		 * log.warning("--found control structure: "
		 * +cs+", try to replace, please check result"); int
		 * csStart=matcher.start()+1; startFind=matcher.end()-1; int
		 * csEnd=matcher.end()-1; int parenStart=csEnd; int parenEnd=parenStart;
		 * //searchEnd only to find opening parenthesis int searchEnd=csEnd+20;
		 * if(searchEnd>script.length()) searchEnd=script.length();
		 * while(parenStart<searchEnd){ if(script.charAt(parenStart)=='('){
		 * break; } parenStart++; } if(parenStart<searchEnd){
		 * parenEnd=ConvertAndFormatMethods.findMatchingParentheses(script,
		 * parenStart); }else{ parenStart=csEnd; //else doesn't need parentheses
		 * if(!cs.equals("else"))addConvertWarning("--found "
		 * +cs+" without parentheses (will not work on)"); }
		 * 
		 * if(parenEnd>0){ switch(cs){ case "do": int doStart=csStart; int
		 * blockStart=matcher.end()-1; int
		 * blockEnd=ConvertAndFormatMethods.findMatchingParentheses(script,
		 * blockStart,false); if(blockEnd<0){
		 * log.warning("--unmatched block parantheses {..} "); break; } String
		 * blockString=script.substring(blockStart,blockEnd); String
		 * whilePat="((while)|(until))"; Matcher
		 * csMatcher=Pattern.compile(whilePat).matcher(script.substring(blockEnd
		 * )); if(csMatcher.find()){
		 * addConvertWarning("-- found do statement with closing "+csMatcher.
		 * group() +" -- revert statements -- please check condition.  "); int
		 * whileStart=csMatcher.start(); int whileEnd=csMatcher.end();
		 * whileEnd=ConvertAndFormatMethods.findMatchingParentheses(script.
		 * substring(blockEnd), whileEnd); int doEnd=blockEnd+whileEnd; String
		 * whileString=script.substring(blockEnd).substring(whileStart,whileEnd)
		 * ; String doString=script.substring(doStart,doEnd); String
		 * newString=" "+whileString+blockString;
		 * controlStructureStringReplacements.put(doString,newString); //
		 * script=script.replace(doString,newString);
		 * 
		 * }else{ addConvertWarning(
		 * "-- found do statement without closing while or until -- please check."
		 * ); } break;
		 * 
		 * 
		 * 
		 * case "until": parenEnd++; // nobreak, continue with the while case
		 * case "while": String oldWhile=script.substring(csStart,parenEnd);
		 * String newWhile=oldWhile+" do "; //replace "until" in string in case
		 * of "until"
		 * if(cs.equals("until"))newWhile=newWhile.replace("until","unless");
		 * controlStructureStringReplacements.put(oldWhile,newWhile); //
		 * script=script.replace(oldWhile,newWhile); startFind=csEnd; break;
		 * 
		 * case "for": if(parenStart<parenEnd){ String
		 * forString=script.substring(csStart,parenEnd); String
		 * parenString=script.substring(parenStart+1,parenEnd-1); String[]
		 * parenSplit=parenString.split(";"); // System.out.println(forString+"
		 * // "+parenString+" "+parenSplit.length); if(parenSplit.length>2){
		 * String start=parenSplit[0]; start=start.replaceFirst("=",":"); String
		 * cond=parenSplit[1]; String next=parenSplit[2]; String
		 * newForString="for "+start+" next "+next+" while( "+cond+" ) do";
		 * controlStructureStringReplacements.put(forString,newForString); //
		 * script=script.replace(forString,newForString);
		 * 
		 * }
		 * 
		 * } startFind=csEnd; break;
		 * 
		 * case "foreach": String forString=script.substring(csStart,parenEnd);
		 * String parenString=script.substring(parenStart,parenEnd); String
		 * varString="lcvariable"; if(csEnd<parenStart)
		 * varString=script.substring(csEnd+1,parenStart); String
		 * newForString="for "+varString+" in "+parenString+" do ";
		 * controlStructureStringReplacements.put(forString,newForString); //
		 * script=script.replace(forString,newForString); startFind=csEnd;
		 * break; default: break; } }
		 * 
		 * } replaceStringKeysByValues(controlStructureStringReplacements); }
		 * 
		 * 
		 * //Cleanup CS controlStructures.add("elseif"); for(String
		 * cs:controlStructures){ //remove CR StringBuffer replacement=new
		 * StringBuffer(); //if comment is closing or opening before "{" there
		 * will be no match String
		 * csPat="(?<=\\W)"+cs+"\\W"+"([^\\{](?!\\*\\/|\\/\\*))*\\{"; //earlier
		 * regex //String csPat=cs+"[^\\{]*\\{"; Matcher
		 * matcher=Pattern.compile(csPat).matcher(script);
		 * while(matcher.find()){ String
		 * csString=ConvertAndFormatMethods.removeCR(matcher.group());
		 * matcher.appendReplacement(replacement,Matcher.quoteReplacement(
		 * csString)); } matcher.appendTail(replacement);
		 * script=replacement.toString();
		 * 
		 * //remove Whitespace-char replacement=new StringBuffer();
		 * csPat="(?<=\\W) {0,}"+cs+" {0,}(?=\\W)";
		 * matcher=Pattern.compile(csPat).matcher(script);
		 * while(matcher.find()){
		 * matcher.appendReplacement(replacement,Matcher.quoteReplacement(" "
		 * +cs+" ")); } matcher.appendTail(replacement);
		 * script=replacement.toString(); }
		 */
	}

	private void replaceConditions(){
		//do "if" before "unless", because unless will be replaced by "if (not condition)"
		for(String conditionType:Arrays.asList("if","unless")){
			//FIND \b if {0,}(
			String ifBeginPat="(?<=\\b)"+Pattern.quote(conditionType)+" {0,}\\(";

			int stmtStart,stmtEnd;
			int curIndex=0;

			findingIfStatemnets:
			do{
				int[] stmtMatch=ConvertAndFormatMethods.getRegexStartAndEndIndexInText(ifBeginPat,script,curIndex);
				stmtStart=stmtMatch[0];
				// Break if no IF ot UNLESS was found
				if(stmtStart==-1) break;

				//find IF condition (...)
				int openCondBrace=stmtMatch[1];
				Condition ifCondition=new Condition(openCondBrace);

				//now find IF block {...}
				Block ifBlock=new Block(ifCondition.closeBrace,conditionType,true);
				if(!ifBlock.valid){
					curIndex=stmtStart+1;
					continue;
				}

				//look for ELSIF (cond) {block}
				ArrayList<CondBlockPair> elsifConditionAndBlocksList=new ArrayList<>();
				int lastBlockEnd=ifBlock.closeBrace;
				int optionalElsifStart;
				//n-times
				do{
					//look from lastBlockIndices[1] on for literal ELSIF
					int[] optionalElsifMatch=ConvertAndFormatMethods.getRegexStartAndEndIndexInText("elsif {0,}\\(",script,lastBlockEnd);
					optionalElsifStart=optionalElsifMatch[0];
					if(optionalElsifStart==-1) break;

					//there are elsif somewhere in script
					int openCondBraceElsif=optionalElsifMatch[1];
					String fromLastBlockToElsif=script.substring(lastBlockEnd+1,openCondBraceElsif);
					//break if it's not directly follwing
					if(!fromLastBlockToElsif.trim().equals("elsif")) break;

					//direct following elsif was found

					//find ELSIF condition (...)
					Condition elsifCondition=new Condition(openCondBraceElsif);

					//now find ELSIF block {...}
					Block elsifBlock=new Block(elsifCondition.closeBrace,"elsif",true);
					if(!elsifBlock.valid){
						curIndex=stmtStart+1;
						continue findingIfStatemnets;
					}

					elsifConditionAndBlocksList.add(new CondBlockPair(elsifCondition.toString(),elsifBlock.toString()));
					lastBlockEnd=elsifBlock.closeBrace;
				}while(optionalElsifStart!=-1);

				//look for ELSE {block}
				//one time
				//look from lastBlockIndices[1] on for literal ELSE (no condition here, so only look BEFORE "{")
				Block elseBlock=null;
				int[] optionalElseMatch=ConvertAndFormatMethods.getRegexStartAndEndIndexInText("else {0,}(?=\\{)",script,lastBlockEnd);
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
							continue findingIfStatemnets;
						}
						lastBlockEnd=elseBlock.closeBrace;
					}
				}

				//build new maxima statement string
				String maximaStmtText="";
				switch(conditionType){
					case "if":
						maximaStmtText=" if "+ifCondition+" then "+ifBlock+" ";
						break;
					case "unless":
						//substring(1) means without open brace
						maximaStmtText=" if (not "+ifCondition.toString().substring(1)+" then "+ifBlock+" ";
						break;
				}

				for(CondBlockPair pair:elsifConditionAndBlocksList){
					maximaStmtText+="elseif "+pair.cond+" then "+pair.block+" ";
				}
				if(elseBlock!=null){
					maximaStmtText+="else "+elseBlock+" ";
				}

				stmtEnd=lastBlockEnd;

				//replace old statement
				script=ConvertAndFormatMethods.replaceSubsequenceInText(script,stmtStart,stmtEnd,maximaStmtText);

				log.warning("--found control structure \""+conditionType+"\", try to replace and reorder, please check result");

				// start over again from last match (prevents loop on IF replaced by IF)
				curIndex=stmtStart+maximaStmtText.length();
			}while(stmtStart!=-1);
		}
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
			//if anything else between startIndex (eg ")") and expected "{" than free space 
			String fromCondToBlock=script.substring(startIndex+1,openBlockParen);
			if(!fromCondToBlock.trim().isEmpty()){
				//NO BLOCK WAS FOUND AS EXPECTED
				addConvertWarningToScript("---found unexpected CS between \""+cs+"\" and mandatory block begin (will not work on)");
				return indices;
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
	
	private String makeBlockOneLine(String text){
		return ConvertAndFormatMethods.removeCR(text);
	}

	private void addConvertWarningToScript(String warning){
		perlScript.addConvertWarning(warning);
	}
}
