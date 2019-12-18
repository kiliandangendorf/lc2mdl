package lc2mdl.mdl;

import lc2mdl.Prefs;
import lc2mdl.lc.problem.Problem;
import lc2mdl.lc.problem.ProblemElement;
import lc2mdl.mdl.quiz.Question;
import lc2mdl.mdl.quiz.QuestionCategory;
import lc2mdl.mdl.quiz.QuestionEssay;
import lc2mdl.mdl.quiz.QuestionStack;

import java.util.ArrayList;
import java.util.logging.Logger;

public class QuestionGenerator{
	public static Logger log = Logger.getLogger(QuestionGenerator.class.getName());

	public ArrayList<Question> generatingQuestions(Problem p){
		log.fine(Prefs.CLI_LINE_SEP);
		log.fine("Starting generating Question.");

		ArrayList<Question> qlist = new ArrayList<>();

		ArrayList<String> types = p.getQuestiontypes();
		Boolean isList = (types.size()>1);

		for (String t : types){
			switch (t) {
				case "stack":
					QuestionStack qs = generatingQuestionStack(p);
					if (isList) {
						qs.setName(qs.getName()+t);
					}
					qlist.add(qs);
					break;
				case "essay":
					QuestionEssay qe = generatingQuestionEssay(p);
					if (isList) {
						qe.setName(qe.getName()+t);
					}
					qlist.add(qe);
					break;
				default:
					log.warning("unknown question type "+t);
					break;
			}
		}

		return qlist;
	}
	
	/**
	 * Generates questionstack object and fills it with all converted elements of given Problem.
	 */
	public QuestionStack generatingQuestionStack(Problem p){
		log.fine(Prefs.CLI_LINE_SEP);
		log.fine("Starting generating Question.");
		
		QuestionStack question=new QuestionStack();
		
		question.setName(p.getProblemName());
		question.setTags(p.getTags());
		log.finer("add converted elements to question");
		for(ProblemElement e:p.getElements()){
			e.addToMdlQuestion(question);	
		}
		
		log.finer("correct node-values");
		question.correctPrtValuesAndLinks();

		log.fine("Done generating Question.");
		return question;
	}

	/**
	 * Generates questionessay object and fills it with all converted elements of given Problem.
	 */
	public QuestionEssay generatingQuestionEssay(Problem p){
		log.fine(Prefs.CLI_LINE_SEP);
		log.fine("Starting generating Question.");

		QuestionEssay question=new QuestionEssay();

		question.setName(p.getProblemName());
		question.setTags(p.getTags());
		log.finer("add converted elements to question");
		for(ProblemElement e:p.getElements()){
			// e.addToMdlQuestion(question);
		}

		log.fine("Done generating Question.");
		return question;
	}
	/**
	 * Generates a questioncategry object and fills it with the path to the problem
	 */
	public QuestionCategory generatingQuestionCategory(Problem p){
		log.fine(Prefs.CLI_LINE_SEP);
		log.fine("Starting generating Category.");

		QuestionCategory category = new QuestionCategory();

		category.setCategoryWithPrefix(p.getCategory());
		category.setInfo("Category set by lc2mdl.");
		return category;
	}

}
