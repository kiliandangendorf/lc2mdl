package lc2mdl.xml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import lc2mdl.Prefs;

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

		
		xmlReplacements.put("<answer {0,}type=\"loncapa/perl\" {0,}>", "<answer type=\"loncapa/perl\"><![CDATA[");
		xmlReplacements.put("<answer {0,}>", "<answer><![CDATA[");
//		xmlReplacements.put("<answer(>| [^>]*>)", "<answer><![CDATA["); would remove attributes
		xmlReplacements.put("</answer {0,}>", "]]></answer>");
		
		
		//HTML References
		//replace NO-BRAKE-SPACE escape by itself
//		xmlReplacements.put("&nbsp;", " ");
//		xmlReplacements.put("&infin;", "∞");
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
