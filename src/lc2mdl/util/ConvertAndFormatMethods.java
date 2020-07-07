package lc2mdl.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConvertAndFormatMethods{
	
	/**
	 * Converts double into String with decimal point (".") as decimal seperator and 7 digits after decimal point. 
	 */
	public static String double2StackString(Double d){
		DecimalFormat df = new DecimalFormat("#0.0000000");
	    DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
	    dfs.setDecimalSeparator('.');
	    df.setDecimalFormatSymbols(dfs);
		return df.format(d);
	}


	/**
	 *
	 * @param text   String with round parentheses
	 * @param start  start position
	 * @return       int end position
	 */
	public static int findMatchingParentheses(String text, int start){
		return (findMatchingParentheses(text, start, true));
	}

	/**
	 *
	 * @param text   String with round or curly parentheses
	 * @param start  start position
	 * @param round  true if round, false if curly
	 *
	 * @return       int end position (or -1 if nothing found)
	 */
	public static int findMatchingParentheses(String text,int start,boolean round){

		int end=start;
		char charOpen,charClose;
		if(round){
			charOpen='(';
			charClose=')';
		}else{
			charOpen='{';
			charClose='}';
		}

		int bracketCount=1;
		while(end<text.length()){
			if(text.charAt(end)==charOpen){
				end++;
				break;
			}
			end++;
		}

		while((bracketCount>0)&&(end<text.length()-1)){
			if(text.charAt(end)==charOpen) bracketCount++;
			if(text.charAt(end)==charClose) bracketCount--;
			end++;
		}

		if(bracketCount!=0){
			return(-1);
		}else{
			return(end);
		}
	}

	/**
	 *  Removes all CR / new lines in
	 * @param text
	 * @return
	 */
	public static String removeCR(String text){
		String newText = text.replaceAll("\\n"," ");
		newText = newText.replaceAll("\\r", " ");
		return (newText);
	}
	
	/**
	 * Returns the start index of a regex match in a given text from a given startIndex
	 * @param regex to match in text
	 * @param text to be searched
	 * @param startIndex starting search at that index
	 * @return first index of match, -1 if there's no match  
	 */
	public static int getRegexStartIndexInText(String regex, String text, int startIndex){
	    Matcher matcher = Pattern.compile(regex).matcher(text);
	    if(matcher.find(startIndex)){
	        return matcher.start();
	    }
	    return -1;
	}
	/**
	 * Returns the start index of a regex match in a given text (starting at char 0)
	 * @param regex to match in text
	 * @param text to be searched
	 * @return first index of match, -1 if there's no match  
	 */
	public static int getRegexStartIndexInText(String regex, String text){
	    return getRegexStartIndexInText(regex, text, 0);
	}
	/**
	 * Returns the start and end index of a regex match in a given text from a given startIndex <br/>
	 * Example:<br/>
	 * 		regex="oob", text="foobar"<br/>
	 * 		return is [1,3]
	 * @param regex to match in text
	 * @param text to be searched
	 * @return first start and end index of match, [-1,-1] if there's no match  
	 */
	public static int[] getRegexStartAndEndIndexInText(String regex, String text, int startIndex){
	    Matcher matcher = Pattern.compile(regex).matcher(text);
	    if(matcher.find(startIndex)){
	        return new int[] {matcher.start(), matcher.end()-1};
	    }
	    return new int[]{-1,-1};
	}
	
	/**
	 * Replaces sequence between from and to in the text by replacement
	 * @param text where replacement should be done
	 * @param from index of first char that should be replaced (incl.)
	 * @param to index of last char that should be replaced (incl.)
	 * @param replacement string
	 * @return modified text
	 */
	public static String replaceSubsequenceInText(String text, int from, int to, String replacement){
		//String.replace was not save, because it would replace any occurance of the replacement string 
		// (eg. elsif(condition) matches if(condition)
		return text.substring(0,from)+replacement+text.substring(to+1,text.length());

	}

}
