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
}
