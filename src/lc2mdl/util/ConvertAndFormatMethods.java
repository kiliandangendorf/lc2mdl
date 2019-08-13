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
}
