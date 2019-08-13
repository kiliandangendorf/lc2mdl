package lc2mdl.lc;

import java.util.logging.Logger;

import lc2mdl.Prefs;
import lc2mdl.lc.problem.ProblemElement;
import lc2mdl.lc.problem.Problem;

public class ProblemConsumer{
	public static Logger log = Logger.getLogger(ProblemConsumer.class.getName());
	
	/**
	 * Running through all ProblemElements in given problem an calling method consumeNode().
	 */
	public void consumingDom(Problem p){
		log.fine(Prefs.CLI_LINE_SEP);
		log.fine("Starting comsuning DOM.");
		
		for(ProblemElement e:p.getElements()){
			e.consumeNode();
		}

		log.fine("Done comsuning DOM.");
	}
}
