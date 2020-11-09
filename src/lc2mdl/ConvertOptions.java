package lc2mdl;

import java.util.Locale;

import lc2mdl.multilanguage.Messages;
import lc2mdl.util.CmdReader;

public class ConvertOptions{
	
	//all defaults are false
	
	// Prefer checkbox, if only two options (optionresponse)
	private static boolean preferCheckbox=false;
	
	
	//take "de" if nothing is set
	private static String defaultLang = "de";

	
	public static void fillWithCmdOptions(CmdReader cmd){
		preferCheckbox=cmd.optionIsSet("-p");
		
		chooseDefaultLanguge(cmd);
	}
	private static void chooseDefaultLanguge(CmdReader cmd){
		//TODO: more languages and a nicer condition ;)
		boolean de=cmd.optionIsSet("--de");
		boolean en=cmd.optionIsSet("--en");
		if(en && !de)defaultLang="en";
		
		//be sure it's lower-case
		Locale lang=new Locale(defaultLang);
		Messages.setLanguage(lang);
	}

	public static boolean isPreferCheckbox(){
		return preferCheckbox;
	}
	public static String getDefaultLang(){
		return defaultLang;
	}

}
