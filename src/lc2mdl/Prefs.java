package lc2mdl;

public class Prefs {

	//**********************
	// Perl converting prefs
	//**********************
	
	// determines if e.g. random(...) will be converted, too, not only &random(...)
	public static final boolean SUPPORT_OLD_CAPA_FUNCTIONS_WITHOUT_AMPERSAND=true;

	// add semicolon at end of new added statements
	public static final boolean ADD_SEMICOLON_AT_END_OF_STMT=true;

	//Formatting / Syntax
	public static final boolean ALLOW_MULTILINE_BLOCKS=true;
	public static final boolean ALLOW_MULTILINE_MAXIMA_STRINGS=false;	

	
	//**********************
	// misc
	//**********************
	
	public static final String DEFAULT_LANG = "de";

	// Prefer checkbox, if only two options (optionresponse)
	public static final Boolean PREFER_CHECKBOX = false;

	
	//**********************
	// Operating constants
	//**********************
	
	public static final String LC_XML_SCHEMA = "xsd/old_loncapa.xsd";

	public static final String MDL_XML_SCHEMA = "xsd/quiz_kilian.xsd";
	
	public static final String XML_SUFFIX=".lc2mdl.xml.tmp";
	
	public static final String LOG_SUFFIX=".lc2mdl.log";
	
	public static final String WHAT_DO_I_DO="This program tries to convert LON-CAPA problem-files into Moodle-STACK xml-files.";
	
	public static final String CLI_LINE_SEP=System.lineSeparator()+"###########################";

	public static final String STILL_TODO="ANYWAYS: check generated Moodle-Stack xml file!"+System.lineSeparator()
		+ "- check questionsvariables for correct working maxima."+System.lineSeparator()
		+ "- check questiontext for any 'not-HTML tags'.";

}
