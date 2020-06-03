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

		return script;
	}

	private void doingStuff(String script){

//		case "unless":{
//			// log.warning("script.length= "+script.length()+"
//			// csstart= "+csStart+" csend= "+csEnd+" parenStart=
//			// "+parenStart+" parenend= "+parenEnd);
//			String condSub=" ";
//			String cond="unless";
//			if(parenStart<parenEnd){
//				condSub=script.substring(parenStart,parenEnd);
//			}
//			condSub=" if (not "+condSub+") ";
//			cond=script.substring(csStart,parenEnd);
//
//			controlStructureStringReplacements.put(cond,condSub);
//			startFind=parenEnd;
//			addConvertWarning("--replaced \"unless\" partially. Please correct order of replaced \"unless\"-statement in context \""+condSub+"\"");
//			}
//			break;
//
//		case "else":
//			String csString=matcher.group();
//			String csNewString="  "+ConvertAndFormatMethods.removeCR(csString)+" ";
//			controlStructureStringReplacements.put(csString,csNewString);
//			startFind=csStart+csNewString.length()+1;
//			break;
//			
//		case "elsif":
//		case "if":
//
//			if(parenStart<parenEnd){
//				//use first \W, too for making sure "if ..." doesn't match "eslif ..."
//				String oldIf=script.substring(csStart-1,parenEnd);
//				String newIf=oldIf+" then ";
//
//				//handle elsif as special case of if
//				if(cs.equals("elsif")){
//					csString=matcher.group();
//					newIf=newIf.replaceFirst(Pattern.quote("elsif"),"elseif");
//					//there are no changes in script, so parenEnd doesn't need to change
//					//parenEnd+=csNewString.length()-csString.length();
//					// parenEnd += 4;
//					//matcher=Pattern.compile(csPat).matcher(script);
//				}
//				
//				controlStructureStringReplacements.put(oldIf,newIf);
//				startFind=csEnd;
//			}else{
//				startFind=csEnd;
//			}
//			break;
		//OPERATORS

		//LOOPS
		//WHILE
		//UNTIL
		//FOR
		//FOREACH
		//DO (WHILE | UNTIL)

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

			findingIfStatemnets:do{
				int[] stmtMatch=ConvertAndFormatMethods.getRegexStartAndEndIndexInText(ifBeginPat,script,curIndex);
				stmtStart=stmtMatch[0];
				// Break if no IF was found
				if(stmtStart==-1) break;

				//find IF condition (...)
				int openCondBrace=stmtMatch[1];
				int closeCondBrace=ConvertAndFormatMethods.findMatchingParentheses(script,openCondBrace)-1;
				String condition=script.substring(openCondBrace,closeCondBrace+1);

				//now find IF block {...}
				int[] ifBlockIndices=findIndicesOfFollowingBlockParentheses(closeCondBrace,conditionType,true);
				if(ifBlockIndices[0]<0){
					curIndex=stmtStart+1;
					continue;
				}
				String ifBlock=script.substring(ifBlockIndices[0],ifBlockIndices[1]+1);
				if(!allowMultilineBlocks) ifBlock=makeBlockOneLine(ifBlock);

				//look for ELSIF (cond) {block}
				ArrayList<CondBlockPair> elsifConditionAndBlocksList=new ArrayList<>();
				int[] lastBlockIndices=ifBlockIndices;
				int[] optionalElsifMatch;
				//n-times
				do{
					//look from lastBlockIndices[1] on for literal ELSIF
					optionalElsifMatch=ConvertAndFormatMethods.getRegexStartAndEndIndexInText("elsif {0,}\\(",script,lastBlockIndices[1]);
					if(optionalElsifMatch[0]==-1) break;

					//there are elsif somewhere in script
					String fromLastBlockToElsif=script.substring(lastBlockIndices[1]+1,optionalElsifMatch[1]);
					if(!fromLastBlockToElsif.trim().equals("elsif")) break;

					//direct following elsif was found

					//find ELSIF condition (...)
					int openCondBraceElsif=optionalElsifMatch[1];
					int closeCondBraceElsif=ConvertAndFormatMethods.findMatchingParentheses(script,openCondBraceElsif)-1;
					String conditionElsif=script.substring(openCondBraceElsif,closeCondBraceElsif+1);

					//now find ELSIF block {...}
					int[] elsifBlockIndices=findIndicesOfFollowingBlockParentheses(closeCondBraceElsif,"elsif",true);
					if(elsifBlockIndices[0]<0){
						curIndex=stmtStart+1;
						continue findingIfStatemnets;
					}
					String elsifBlock=script.substring(elsifBlockIndices[0],elsifBlockIndices[1]+1);
					if(!allowMultilineBlocks) elsifBlock=makeBlockOneLine(elsifBlock);

					elsifConditionAndBlocksList.add(new CondBlockPair(conditionElsif,elsifBlock));
					lastBlockIndices=elsifBlockIndices;
					System.out.println(lastBlockIndices[0]+" "+lastBlockIndices[1]);

				}while(optionalElsifMatch[0]!=-1);

				//look for ELSE {block}
				//one time
				//look from lastBlockIndices[1] on for literal ELSE (no condition here, so only look before "{")
				String elseBlock=null;
				int[] optionalElseMatch=ConvertAndFormatMethods.getRegexStartAndEndIndexInText("else {0,}(?=\\{)",script,lastBlockIndices[1]);
				System.out.println(optionalElseMatch[0]+"  "+optionalElseMatch[1]);
				if(optionalElseMatch[0]!=-1){
					//there are else somewhere in script
					String fromLastBlockToElse=script.substring(lastBlockIndices[1]+1,optionalElseMatch[1]+1);
					if(fromLastBlockToElse.trim().equals("else")){
						//direct following else was found

						//no condition ;) 

						//now find ELSE block {...}
						int[] elseBlockIndices=findIndicesOfFollowingBlockParentheses(optionalElseMatch[1],"else",true);
						if(elseBlockIndices[0]<0){
							curIndex=stmtStart+1;
							continue findingIfStatemnets;
						}
						elseBlock=script.substring(elseBlockIndices[0],elseBlockIndices[1]+1);
						if(!allowMultilineBlocks) elseBlock=makeBlockOneLine(elseBlock);

						lastBlockIndices=elseBlockIndices;
					}
				}

				//build new maxima statement string
				String maximaStmtText="";
				switch(conditionType){
					case "if":
						maximaStmtText=" if "+condition+" then "+ifBlock+" ";
						break;
					case "unless":
						maximaStmtText=" if (not "+condition.substring(1)+" then "+ifBlock+" ";
						break;
				}

				for(CondBlockPair pair:elsifConditionAndBlocksList){
					maximaStmtText+="elseif "+pair.cond+" then "+pair.block+" ";
				}
				if(elseBlock!=null){
					maximaStmtText+="else "+elseBlock+" ";
				}

				stmtEnd=lastBlockIndices[1];

				//replace old statement
				script=ConvertAndFormatMethods.replaceSubsequenceInText(script,stmtStart,stmtEnd,maximaStmtText);

//			String perlStmtText=script.substring(stmtStart,stmtEnd+1);
//			log.finest("---replaced "+perlStmtText+" by "+maximaStmtText);
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
			addConvertWarning("---found \""+cs+"\" without any block \"{\" (will not work on)");
			return indices;
		}

		if(mandatory){
			//if anything else between startIndex (eg ")") and expected "{" than free space 
			String fromCondToBlock=script.substring(startIndex+1,openBlockParen);
			if(!fromCondToBlock.trim().isEmpty()){
				//NO BLOCK WAS FOUND AS EXPECTED
				addConvertWarning("---found unexpected CS between \""+cs+"\" and mandatory block begin (will not work on)");
				return indices;
			}
		}

		int closeBlockParen=ConvertAndFormatMethods.findMatchingParentheses(script,startIndex,false)-1;
		if(closeBlockParen<0){
			//NO BLOCK END WAS FOUND
			addConvertWarning("---found \""+cs+"\" without block end \"}\" (will not work on)");
			return indices;
		}
		return new int[]{openBlockParen,closeBlockParen};
	}
	
	private String makeBlockOneLine(String text){
		return ConvertAndFormatMethods.removeCR(text);
	}

	private void addConvertWarning(String warning){
		perlScript.addConvertWarning(warning);
	}
}
