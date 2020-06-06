package lc2mdl.lc.problem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
	
	private int boolNo=0;
	private HashMap<String,String> csDict;


	public PerlControlStructuresReplacer(PerlScript perlScript,String script){
		this.perlScript=perlScript;
		this.script=script;
	}

	public String getReplacedScript(){
		allowMultilineBlocks=true;

		//nice: irritating braces in strings aren't possible anymore;) (done in saveStrings)
		
		//temporary dictionary to prevent replacing same CS in different contexts
		//Contains critical CS this program writes ITSELF. These will be temporarily named as values to not confuse itself. 
		//In the end there will be clean maxima CS ;)
		csDict=new HashMap<>();
		csDict.put("if","lc2mdl_IF");
		csDict.put("else","lc2mdl_ELSE");
		csDict.put("while","lc2mdl_WHILE");
		csDict.put("unless","lc2mdl_UNLESS");
		csDict.put("do","lc2mdl_DO");
		
		
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
		
		
		

		//FOR
		//FOREACH

		//OPERATORS
		
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


		
		for(String cs:csDict.keySet()){
			script=script.replace(csDict.get(cs),cs);
		}
		
		return script;
	}
	private void replaceWhileLoops(){
		// while (...) {...}
		// until (...) {...}
		for(String loopType:Arrays.asList("while","until")){
			//FIND \b if {0,}(
			String whileBeginPat="(?<=\\b)"+Pattern.quote(loopType)+" {0,}\\(";

			int stmtStart,stmtEnd;
			int curIndex=0;

			findingWhileStatemnets:
			do{
				int[] stmtMatch=ConvertAndFormatMethods.getRegexStartAndEndIndexInText(whileBeginPat,script,curIndex);
				stmtStart=stmtMatch[0];
				// Break if no WHILE or UNTIL was found
				if(stmtStart==-1) break;

				//find WHILE resp. UNTIL condition (...)
				int openCondBrace=stmtMatch[1];
				Condition whileCondition=new Condition(openCondBrace);

				//now find block {...}
				Block whileBlock=new Block(whileCondition.closeBrace,loopType,true);
				if(!whileBlock.valid){
					curIndex=stmtStart+1;
					continue findingWhileStatemnets;
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

				stmtEnd=whileBlock.closeBrace;

				//replace old statement
				script=ConvertAndFormatMethods.replaceSubsequenceInText(script,stmtStart,stmtEnd,maximaStmtText);

				log.warning("---found control structure \""+loopType+"\", try to replace and reorder, please check result");

				// start over again from last match (prevents loop on IF replaced by IF)
				curIndex=stmtStart+maximaStmtText.length();
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
			String doLoopBeginPat="(?<=\\b)do {0,}\\{";
			
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
			String loopConditionPattern="(?<=\\b)(while|until) {0,}\\(";
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
			
			Condition loopCondition=new Condition(openCondBrace);
			
			//build new maxima statement string
			//do {BLOCK} while (CONDITION) => bool x: true; while (x) {BLOCK; x:CONDITION}
		    String boolName="lc2mdl_do_bool"+perlScript.problem.getIndex(perlScript)+"_"+boolNo++;
		    perlScript.problem.addVar(boolName);
		    
		    String maximaStmtText=" "+boolName+" : true"+";"+System.lineSeparator();
			switch(conditionType){
				case "while":
					maximaStmtText+=" "+csDict.get("while")+" ("+boolName+") ";
					break;
				case "until":
					maximaStmtText+=" "+csDict.get("while")+" (not "+boolName+") ";
					break;
			}
			String doBlockModified=doBlock.toString().substring(0,doBlock.toString().length()-1);
			doBlockModified+="; "+boolName+" : "+loopCondition+" }";
			maximaStmtText+=doBlockModified+" ";

			doEnd=loopCondition.closeBrace;
			
			script=ConvertAndFormatMethods.replaceSubsequenceInText(script,doStart,doEnd,maximaStmtText);
	
			addConvertWarningToScript("---found \"do\" statement with closing \""+conditionType+"\", try to replace and reorder with helper var \""+boolName+"\" -- please check condition.  ");
	
			// start over again from last match (prevents loop on IF replaced by IF)
			curIndex=doStart+maximaStmtText.length();
			
		}while(doStart!=-1);

		
	}

	private void replaceConditions(){
		//good news: Perl needs to have braces around (condition) and {block} (syntax error without)

		for(String conditionType:Arrays.asList("if","unless")){
			//FIND \b if {0,}(
			String ifBeginPat="(?<=\\b)"+Pattern.quote(conditionType)+" {0,}\\(";

			int stmtStart,stmtEnd;
			int curIndex=0;

			findingIfStatemnets:
			do{
				int[] stmtMatch=ConvertAndFormatMethods.getRegexStartAndEndIndexInText(ifBeginPat,script,curIndex);
				stmtStart=stmtMatch[0];
				// Break if no IF or UNLESS was found
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
					int[] optionalElsifMatch=ConvertAndFormatMethods.getRegexStartAndEndIndexInText("(?<=\\b)elsif {0,}\\(",script,lastBlockEnd);
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
				int[] optionalElseMatch=ConvertAndFormatMethods.getRegexStartAndEndIndexInText("(?<=\\b)else {0,}(?=\\{)",script,lastBlockEnd);
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

				stmtEnd=lastBlockEnd;

				//replace old statement
				script=ConvertAndFormatMethods.replaceSubsequenceInText(script,stmtStart,stmtEnd,maximaStmtText);

				log.warning("---found control structure \""+conditionType+"\", try to replace and reorder, please check result");

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
			//trival case if "{" is on startIndex
			if(startIndex<openBlockParen){
				//if anything else between startIndex (eg ")") and expected "{" than free space 
				String fromCondToBlock=script.substring(startIndex+1,openBlockParen);
				if(!fromCondToBlock.trim().isEmpty()){
					//NO BLOCK WAS FOUND AS EXPECTED
					addConvertWarningToScript("---found unexpected CS between \""+cs+"\" and mandatory block begin (will not work on)");
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
	
	private String makeBlockOneLine(String text){
		return ConvertAndFormatMethods.removeCR(text);
	}

	private void addConvertWarningToScript(String warning){
		perlScript.addConvertWarning(warning);
	}
}
