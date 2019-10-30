package lc2mdl.xml;

import lc2mdl.Prefs;
import lc2mdl.util.FileFinder;

import java.io.*;
import java.util.HashMap;
import java.util.logging.Logger;

public class PreParser {	
	private static final Logger log = Logger.getLogger(PreParser.class.getName());
	private int curLine=0;

	/**
	 * Replacing outtext-tags and creating CDATA-Tags in given String s.
	 */
	private String replaceLCByXML(String s){
		HashMap<String, String> xmlReplacements=new HashMap<>();

		//Using CDATA-Tags no need to remove special-characters as & - &amp; etc.
		
		xmlReplacements.put("<startouttext {0,}/>", "<outtext><![CDATA[");
		xmlReplacements.put("<endouttext {0,}/>", "]]></outtext>");

		xmlReplacements.put("<script {0,}type=\"loncapa/perl\" {0,}>", "<script type=\"loncapa/perl\"><![CDATA[");
		xmlReplacements.put("</script {0,}>", "]]></script>");

		xmlReplacements.put("<answer {0,}name=\"(.*)\" {0,}type=\"(.*)\" {0,}>", "<answer name=\"$1\" type=\"$2\"><![CDATA[");
		xmlReplacements.put("<answer {0,}type=\"loncapa/perl\" {0,}>", "<answer type=\"loncapa/perl\"><![CDATA[");
		xmlReplacements.put("<answer {0,}>", "<answer><![CDATA[");

//		xmlReplacements.put("<answer(>| [^>]*>)", "<answer><![CDATA["); would remove attributes
		xmlReplacements.put("</answer {0,}>", "]]></answer>");
		xmlReplacements.put("importmode=\"\"","");
		
		//HTML References
		//replace NO-BRAKE-SPACE escape by itself
        // HTML stuff
		xmlReplacements.put("(<td>\\s*)&nbsp;(\\s*</td>)","$1<![CDATA[ &nbsp; ]]>$2");
		xmlReplacements.put("(<TD>\\s*)&nbsp;(\\s*</TD>)","$1<![CDATA[ &nbsp; ]]>$2");
		xmlReplacements.put("&nbsp;"," ");
		xmlReplacements.put("&uuml;","ü");
		xmlReplacements.put("&auml;","ü");
		xmlReplacements.put("&ouml;","ü");
		xmlReplacements.put("&euro;","€");
		xmlReplacements.put("&le;","\\\\(\\le\\\\)");
		xmlReplacements.put("&ge;","\\\\(\\ge\\\\)");
 		xmlReplacements.put("&lt;","\\\\(\\lt\\\\)");
		xmlReplacements.put("&gt;","\\\\(\\gt\\\\)");
       	xmlReplacements.put("&infin;", "\\\\( \\infty \\\\)");
       	xmlReplacements.put("&epsilon;","\\\\( \\varepsion \\\\)");
       	xmlReplacements.put("<sub>","\\\\(_");
       	xmlReplacements.put("</sub>","\\\\)");
       	xmlReplacements.put("&ne;","\\\\(\\neq\\\\)");

		// LON-CAPA inbuilt functions
		xmlReplacements.put("&check_status","check_status");
		xmlReplacements.put("&EXT","EXT");

		// make it an correct attribute

		xmlReplacements.put("condition=\"&abs", "condition=\"abs");
		xmlReplacements.put("condition=\"([^<]*)<([^<]*)\"", "condition=\"$1 LT $2\"");
		xmlReplacements.put("condition=\"([^>]*)>([^>]*)\"", "condition=\"$1 GT $2\"");
		xmlReplacements.put("condition=\"([^<]*)<([^<]*)<([^<]*)\"", "condition=\"$1 LT $2 LT $3\"");
		xmlReplacements.put("condition=\"([^>]*)>([^>]*)>([^>]*)\"", "condition=\"$1 GT $2 GT $3\"");
		xmlReplacements.put("condition=\"([^<]*)<([^<]*)<([^<]*)<([^<]*)\"", "condition=\"$1 LT $2 LT $3 LT $4\"");
		xmlReplacements.put("condition=\"([^>]*)>([^>]*)>([^>]*)>([^>]*)\"", "condition=\"$1 GT $2 GT $3 GT $4\"");
		xmlReplacements.put("condition=\"([^&]*)&&([^&]*)\"", "condition=\"$1 AND $2\"");
		xmlReplacements.put("condition=\"([^&]*)&&([^&]*)&&([^&]*)\"", "condition=\"$1 AND $2 AND $3\"");

        xmlReplacements.put("options=\"([^<]*)<([^<]*)\"", "options=\"$1 \\\\( \\lt $2\"");
        xmlReplacements.put("options=\"([^>]*)>([^>]*)\"", "options=\"$1 \\\\( \\gt $2\"");

//		occurs error
//	    <foilgroup options="('singul&auml;r','regul&auml;r')" texoptions="">


		String buf;
		for(HashMap.Entry<String, String> item : xmlReplacements.entrySet()) {
			buf=s.replaceAll(item.getKey(), item.getValue());
			if(!buf.equals(s))log.finer("line "+curLine+": replaced "+item.getKey()+" with "+item.getValue());
			s=buf;
	    }	
		return s;
	}

	public void lookForIncludes(File inputfolder, File infile, HashMap<String,String> libs, HashMap<String,String> images) throws Exception {
		log.fine(Prefs.CLI_LINE_SEP);
		log.fine("Starting XML Preparsing -- Look for Imports and Allows.");
		int line=0;
		int countlibs=0;
		int countims=0;
		FileReader fr=null;
		BufferedReader br=null;
		try {
			fr = new FileReader(infile);
			br = new BufferedReader(fr);

			String s;
			while ((s = br.readLine()) != null) {
				line++;

				if (s.contains("<import") || s.contains("<allow")) {

					if (s.contains("<allow")) {
						int pathStart = s.indexOf("src=\"") + 4;
						int pathEnd = s.indexOf("\"", pathStart+1);
						String path = "";
						if (pathStart < pathEnd) {
							path = s.substring(pathStart, pathEnd);
						} else {
							throw (new Exception(" allow tag -- no content"));
						}
						log.fine("-- found allow tag with path " + path);
						try {
							String name = FileFinder.findFilesRecursively(inputfolder, path, images);
							log.fine("-- found image " + name + " in path " + images.get(name));
							countims++;
						}catch (FileNotFoundException e){
							log.warning(e.getMessage());
						}
					}
					if (s.contains("<import")) {
						int pathStart = s.indexOf(">") + 1;
						int pathEnd = s.indexOf("<", pathStart);
						String path = "";
						if (pathStart < pathEnd) {
							path = s.substring(pathStart, pathEnd);
						} else {
							throw (new Exception(" import tag -- no content"));
						}

						log.fine("--found import tag with path " + path);
						try {
							String name = FileFinder.findFilesRecursively(inputfolder, path, libs);
							log.fine("-- found library " + name + " in path " + libs.get(name));
							log.fine("-- look for includes in library "+ name);
							lookForIncludes(inputfolder, new File(libs.get(name)),libs,images);
							countlibs++;
						}catch (FileNotFoundException e){
							log.warning(e.getMessage());
						}
					}
				}
			}

		}catch (FileNotFoundException e){
			log.warning(e.getMessage());
		}catch (Exception e)
		{
			log.severe(e.getMessage());
			throw e;
		}
		log.fine("-- "+countlibs+" libraries and "+countims+" images found.");
	}

	public File preParse(final File infile) throws Exception{
		return preParse(infile, infile.getAbsolutePath()+Prefs.XML_SUFFIX);
	}
	
	/**
	 * Reading XML File line by line and replaces sequences. Target: well-formed XML
	 * @param infile File
	 * @param xmlfileName XML File will be created
	 * @return XML File named by xmlfileName
	 * @throws IOException 
	 */
	public File preParse(final File infile, String xmlfileName) throws Exception{
		log.fine(Prefs.CLI_LINE_SEP);
		log.fine("Starting XML Replacement (Preparsing).");
		
		File xmlfile=new File(xmlfileName);
		try {
			xmlfile.createNewFile();//creates if not exists
		} catch (IOException e) {
			log.severe("couldn't create or open file: "+xmlfile.getName()+"("+e.getMessage()+")");
			throw e;
		}
		
		FileReader fr=null;
		BufferedReader br=null;
		
		FileWriter fw=null;
		BufferedWriter bw=null;
		
		try {
			fr = new FileReader(infile);
			br=new BufferedReader(fr);
			
			fw=new FileWriter(xmlfile);
			bw=new BufferedWriter(fw);
						
			String s;
			while((s=br.readLine())!=null){
				curLine++;
				s=replaceLCByXML(s);
				bw.write(s);
				bw.newLine();
			}		
		} catch (FileNotFoundException e) {//fileInput
			log.severe("file not found: "+infile.getName()+"("+e.getMessage()+")");
			throw e;
		} catch (IOException e) {//fileOutput
			log.severe("not able to write file: "+xmlfile.getName()+"("+e.getMessage()+")");
			throw e;
		} finally {
			try{
				if(br!=null)br.close();
				if(fr!=null)fr.close();
				
				if(bw!=null)bw.close();
				if(fw!=null)fw.close();
			}catch (IOException e) {
				log.severe("unable to close FileBuffers"+"("+e.getMessage()+")");
            }
		}
		
		log.fine("Done XML Replacement (Preparsing).");
		return xmlfile;	
	}
}
