package lc2mdl.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

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

		while((bracketCount>0)&&(end<text.length())){
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
		String newText = text.replaceAll("\\n","");
		newText = newText.replaceAll("\\r", "");
		return (newText);
	}
}
