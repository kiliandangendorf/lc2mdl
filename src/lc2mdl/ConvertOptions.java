package lc2mdl;

import lc2mdl.util.CmdReader;

public class ConvertOptions{
	
	//all defaults are false
	
	// Prefer checkbox, if only two options (optionresponse)
	private static boolean preferCheckbox=false;
	
	public static void fillWithCmdOptions(CmdReader cmd){
		preferCheckbox=cmd.optionIsSet("-p");
	}

	public static boolean isPreferCheckbox(){
		return preferCheckbox;
	}


}
