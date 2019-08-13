package lc2mdl.mdl;

import java.util.logging.Logger;

import lc2mdl.Prefs;
import lc2mdl.lc.problem.ProblemElement;
import lc2mdl.lc.problem.Problem;
import lc2mdl.mdl.quiz.QuestionStack;

public class QuestionGenerator{
	public static Logger log = Logger.getLogger(QuestionGenerator.class.getName());
	
	/**
	 * Generates question object and fills it with all converted elements of given Problem. 
	 */
	public QuestionStack generatingQuestionStack(Problem p){
		log.fine(Prefs.CLI_LINE_SEP);
		log.fine("Starting generating Question.");
		
		QuestionStack question=new QuestionStack();
		
		question.setName(p.getProblemName());
		log.finer("add converted elements to question");
		for(ProblemElement e:p.getElements()){
			e.addToMdlQuestion(question);	
		}
		
		log.finer("correct node-values");
		question.correctPrtValuesAndLinks();

		log.fine("Done generating Question.");
		return question;
	}

}
