package lc2mdl;

public class Prefs {

	public static final String LC_XML_SCHEMA = "xsd/old_loncapa.xsd";

	public static final String MDL_XML_SCHEMA = "xsd/quiz_kilian.xsd";
	public static final String DEFAULT_LANG = "de";
	
	public static final String XML_SUFFIX=".lc2mdl.xml.tmp";
	
	public static final String LOG_SUFFIX=".lc2mdl.log";
	
	public static final String WHAT_DO_I_DO="This program tries to convert LON-CAPA problem-files into Moodle-STACK xml-files.";
	
	public static final String CLI_LINE_SEP=System.lineSeparator()+"###########################";

	public static final String STILL_TODO="ANYWAYS: check generated Moodle-Stack xml file!"+System.lineSeparator()
		+ "- check questionsvariables for correct working maxima."+System.lineSeparator()
		+ "- check questiontext for any 'not-HTML tags'.";

	public static final String BIN_DIR="/usr/bin/";
	 // directory for convert (ImageMagick)

	// text for option responses with checkboxes
	public static final String OPTION_TEXT="<br/><br/>Die vorhandenen Optionen sind: ";
	public static final String CHECKBOX_TEXT="Bitte kreuzen Sie alle Aussagen an, auf die die Option ";
	public static final String CHECKBOX_TEXT_END=" zutrifft!";
	// Prefer checkbox, if only two options
	public static final Boolean PREFER_CHECKBOX = false;

	// text for essay response within a stack question
	public static final String ESSAY_TEXT_FIELD_STACK="Bitte beantworten Sie die Frage im Textfeld der nachfolgenden Frage!";
	public static final String ESSAY_TEXT_FILE_STACK="Bitte beantworten Sie die Frage durch Hochladen der passenden Datei in der nachfolgenden Frage!";
	public static final String ESSAY_TEXT_FIELD_ESSAY="Bitte beantworten Sie die vorhergehende Frage in diesem Textfeld!";
	public static final String ESSAY_TEXT_FILE_ESSAY="Bitte beantworten Sie die vorhergehende Frage durch Hochladen der passenden Datei!";
	public static final String ESSAY_FILE_EXT="Zum Hochladen bitte nur Dateien des folgenden Typs verwenden: ";


	//**********************
	// Perl converting prefs
	//**********************
	
	// determines if e.g. random(...) will be converted, too, not only &random(...)
	public static final boolean SUPPORT_OLD_CAPA_FUNCTIONS_WITHOUT_AMPERSAND=true;

	public static final boolean ALLOW_MULTILINE_BLOCKS=true;
	// add semicolon at end of new added statements
	public static final boolean ADD_SEMICOLON_AT_END_OF_STMT=true;
	
	public static final boolean ALLOW_MULTILINE_MAXIMA_STRINGS=true;	

}
