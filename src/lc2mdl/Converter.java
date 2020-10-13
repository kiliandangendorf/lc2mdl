package lc2mdl;

import lc2mdl.lc.ProblemConsumer;
import lc2mdl.lc.ProblemReader;
import lc2mdl.lc.ProblemSimplifier;
import lc2mdl.lc.problem.Problem;
import lc2mdl.mdl.QuestionGenerator;
import lc2mdl.mdl.quiz.Quiz;
import lc2mdl.mdl.quiz.QuizElement;
import lc2mdl.util.LogFormatterKD;
import lc2mdl.xml.PreParser;
import lc2mdl.xml.XMLParser;
import lc2mdl.xml.XMLWriter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.*;

public class Converter{
	public static Logger log=Logger.getLogger(Converter.class.getName());

	private final boolean verbosity;
	private final boolean removeTmpFiles;
	private final boolean removeLogFiles;

	public Converter(boolean verbosity,boolean removeTmpFiles,boolean removeLogFiles){
		this.verbosity=verbosity;
		this.removeTmpFiles=removeTmpFiles;
		this.removeLogFiles=removeLogFiles;
	}

	/**
	 * Converts LON-CAPA inputfile (.problem) into Moodle-STACK outputfile (.xml)
	 * @param inputfile
	 * @param outputfile
	 * @return 0 in error case 
	 * <br>1 if convertion was successful 
	 * <br>2 if convertion was successful AND no unknown tags occured 
	 */

	public int convertFile(final File inputfile, final File outputfile) {
	   	File inputfolder=inputfile.getParentFile();
	   	return convertFile(inputfolder,inputfile,outputfile);
	}

	public int convertFile(final File inputfolder, final File inputfile, final File outputfile){
		final File logfile;
		File xmlfile=null;
		int convertedSuccessful=0;

		// LOGGER CONFIG
		logfile=new File(outputfile.getAbsolutePath()+Prefs.LOG_SUFFIX);
		configLogger(logfile.getAbsolutePath());

		// get the relative path to the problem
		String pathString = inputfile.getAbsolutePath();
		String folderString = inputfolder.getAbsolutePath();
		pathString = pathString.replace(folderString,"");


		// LET'S START
		log.fine(Prefs.CLI_LINE_SEP);
		log.info("START: "+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		log.finer("inputfile: "+inputfile.getName());
		log.finer("outputfile: "+outputfile.getName());
		log.finer("logfile: "+logfile.getName());

		
		// PROBLEMS NAME
		String problemName=inputfile.getName().substring(0,inputfile.getName().lastIndexOf('.'));

		try{
			// PREPARSE
			PreParser pp=new PreParser();

			HashMap<String,String> libfiles = new HashMap<String, String>();
			HashMap<String,String> imagefiles = new HashMap<String, String>();

			//creates XML file
			String xmlfileName=outputfile.getAbsolutePath()+Prefs.XML_SUFFIX;
			pp.lookForIncludes(inputfolder,inputfile,libfiles,imagefiles);
			xmlfile=pp.preParse(inputfile, xmlfileName);

			// PARSE TO DOM
			XMLParser xp=new XMLParser();
			Document dom=xp.parseXML2DOM(xmlfile);

			// SIMPLIFY
			ProblemSimplifier ps=new ProblemSimplifier();
			//but preserve HTML-tags
			ps.simplify(dom, true);

			// same for all libraries
			String libpath= outputfile.getParent()+"/";
			HashMap<String,Document> libDoms = new HashMap<String, Document>();
			for (String key : libfiles.keySet()){
				String xmlLibName = libpath + key+ Prefs.XML_SUFFIX;
				File xmlLibFile = pp.preParse(new File(libfiles.get(key)), xmlLibName);
				Document domLib = xp.parseXML2DOM(xmlLibFile);
				ps.simplify(domLib, true);
				libDoms.put(key,domLib);
			}

			// PRINT
			// DOMPrinter dpr=new DOMPrinter();
			// dpr.printDoc(dom);
	
			
			// READING PROBLEM ELEMENTS
			ProblemReader pr=new ProblemReader();
			Problem p=pr.readingDom(dom, problemName, libDoms, imagefiles,pathString);
	

			// CONSUMING PROBLEM ELEMENTS
			ProblemConsumer pc=new ProblemConsumer();
			pc.consumingDom(p);
			
			
			// GENERATE QUESTION DOM
			QuestionGenerator qg=new QuestionGenerator();
			ArrayList<QuizElement> qlist = qg.generatingQuestions(p);

					
			// WRITE QUIZ-XML FILE
			Quiz quiz=new Quiz();
			quiz.addAllQuizelements(qlist);
			XMLWriter xw=new XMLWriter();
			xw.writeQuiz2XML(quiz,outputfile);
			
			
			// SIMPLIFY AGAIN (remove "consumed" parts of DOM)
			ps.simplify(dom);		
			//WRITE LEFT ELEMENTS IN XML-TMP-FILE
			if(dom.getDocumentElement()!=null){
				XMLWriter.writeDom2XML(dom,xmlfile);
			}
	
			
			// DONE
			log.fine(Prefs.CLI_LINE_SEP);
			log.info("DONE: "+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			log.fine("Summary:");
			if(dom.getDocumentElement()==null){
				log.fine("- Great: DOM was 100% consumed. No unknown problem-tags occured.");
				if(xmlfile!=null)if(xmlfile.exists() && !xmlfile.isDirectory())xmlfile.delete();
				log.finer("- deleted tmp file (because it's empty anyway).");
				convertedSuccessful=2;
			}else{
				if(!removeTmpFiles){
					log.fine("- still unknown problem-tags. Check tmp file for all left unknown tags. ("+xmlfileName+")");					
				}
				convertedSuccessful=1;
			}
			if(!removeLogFiles){
				log.fine("- check logfile for all warnings. (cat "+logfile.getAbsolutePath()+" | grep WARNING)");					
			}
			log.fine(Prefs.STILL_TODO);

		}catch(Exception e){
			log.severe("error while converting: ");
			log.severe(e.getMessage());
			log.severe("Exception : "+ ExceptionUtils.getStackTrace(e));
			//TODO: here the StackTrace is shown as long as this program is under construction. 
			// e.printStackTrace();

			log.fine(Prefs.CLI_LINE_SEP);
			log.info("ABORTED: "+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

			log.info("Check log-file for any occured warning. ("+logfile.getAbsolutePath()+")");
			if(outputfile.exists() && !outputfile.isDirectory()){
				log.info("You should delete outputfile to be sure, not to work on damaged quiz-file.");
			}
			
		}finally{
						
			//REMOVE TMP/LOG FILES 
			if(removeTmpFiles){
				log.finer("CLEANUP: delete tmp file.");
				if(xmlfile!=null)if(xmlfile.exists() && !xmlfile.isDirectory())xmlfile.delete();
			}
			if(removeLogFiles){
				log.finer("CLEANUP: delete log file.");
				if(logfile.exists() && !logfile.isDirectory())logfile.delete();
			}
		}		
		
		return convertedSuccessful;
	}

	private void configLogger(String logFileName){
		LogManager.getLogManager().reset(); // remove all default logger
		LogManager.getLogManager().getLogger("").setLevel(Level.ALL);
		
		ConsoleHandler ch=new ConsoleHandler();
		ch.setFormatter(new LogFormatterKD());
		ch.setLevel(Level.FINE);
		LogManager.getLogManager().getLogger("").addHandler(ch);
		
		FileHandler fh;
		try{
			fh=new FileHandler(logFileName,false);
			fh.setFormatter(new LogFormatterKD());
			fh.setLevel(Level.ALL);
			LogManager.getLogManager().getLogger("").addHandler(fh);
		}catch(SecurityException|IOException e){
			log.severe("couldn't create or open file: "+logFileName+"("+e.getMessage()+")");
		}
		
		if(verbosity)ch.setLevel(Level.ALL);
	}
}
