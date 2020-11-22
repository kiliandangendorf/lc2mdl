package lc2mdl;

import java.util.Locale;

import lc2mdl.multilanguage.Messages;
import lc2mdl.util.CmdReader;

public class ConvertOptions{
	
	// all defaults are false
	
	// Prefer checkbox, if only two options (optionresponse)
	private static boolean preferCheckbox=false;
	
	// use Moodle's multilang-Plugin in multilangugae text-output 
	private static boolean multilang=false;
	
	// take "de" if nothing is set
	private static String defaultLang = "de";

	
	public static void fillWithCmdOptions(CmdReader cmd){
		preferCheckbox=cmd.optionIsSet("-p");
		multilang=cmd.optionIsSet("-m");
		
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
	public static boolean isMultilang(){
		return multilang;
	}
	public static String getDefaultLang(){
		return defaultLang;
	}

}
