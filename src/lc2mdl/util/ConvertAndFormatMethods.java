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


	public static int findMatchingParentheses(String text, int start){
		return (findMatchingParentheses(text, start, true));
	}
	public static int findMatchingParentheses(String text, int start, boolean round){

			int end=start;
			char charOpen;
			if (round) {
				charOpen='(';
			} else {
				charOpen='{';
			}
			char charClose;
			if (round) {
				charClose=')';
			} else {
				charClose='}';
			}

			int bracketCount=1;
			while (end < text.length()){
				if(text.charAt(end)==charOpen) {
					end++;
					break;
				}
				end++;
			}
			while(bracketCount>0){
				if(text.charAt(end)==charOpen) bracketCount++;
				if(text.charAt(end)==charClose) bracketCount--;
				end++;
				if (end > text.length()) break;
			}

			if(bracketCount!=0){
				return (-1);
			} else {
				return (end);
			}
	}

	public static String removeCR(String text){
		String newText = text.replaceAll("\\n","");
		newText = newText.replaceAll("\\r", "");
		return (newText);
	}
}
