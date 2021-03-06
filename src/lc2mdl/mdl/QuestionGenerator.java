package lc2mdl.mdl;

import lc2mdl.Prefs;
import lc2mdl.lc.problem.Problem;
import lc2mdl.lc.problem.ProblemElement;
import lc2mdl.lc.problem.response.EssayResponse;
import lc2mdl.mdl.quiz.*;
import lc2mdl.multilanguage.Messages;

import java.util.ArrayList;
import java.util.logging.Logger;

public class QuestionGenerator{
	public static Logger log = Logger.getLogger(QuestionGenerator.class.getName());

	public ArrayList<QuizElement> generatingQuestions(Problem p){
		log.fine(Prefs.CLI_LINE_SEP);
		log.fine("Starting generating Question.");

		ArrayList<QuizElement> qlist = new ArrayList<QuizElement>();

		QuestionCategory category = generatingQuestionCategory(p);
		qlist.add(category);

		ArrayList<String> types = p.getQuestiontypes();
		Boolean isList = (types.size()>1);

		if (isList) {
			// split question
			for (String t : types) {
				switch (t) {
					case "stack":
						QuestionStack qs = generatingQuestionStack(p);
						qs.setName(qs.getName() + t);
						qlist.add(qs);
						break;
					case "essay":
						QuestionEssay qe = generatingAdditionalQuestionEssay(p);
						qe.setName(qe.getName() + t);
						qlist.add(qe);
						break;
					default:
						log.warning("unknown question type " + t);
						break;
				}
			}
		}else{
			if (!types.isEmpty()) {
				String t = types.get(0);
				// only one type
				switch (t) {
					case "stack":
						QuestionStack qs = generatingQuestionStack(p);
						qlist.add(qs);
						break;
					case "essay":
						QuestionEssay qe = generatingQuestionEssay(p);
						qlist.add(qe);
						break;
					default:
						log.warning("unknown question type " + t);
						break;
				}
			}
		}

		return qlist;
	}
	
	/**
	 * Generates questionstack object and fills it with all converted elements of given Problem.
	 */
	public QuestionStack generatingQuestionStack(Problem p){
		log.fine(Prefs.CLI_LINE_SEP);
		log.fine("Starting generating Stack Question.");
		
		QuestionStack question=new QuestionStack();
		
		question.setName(p.getProblemName());
		question.setTags(p.getTags());
		
		question.setPrtcorrect("<span style=\"font-size: 1.5em; color:green;\"><i class=\"fa fa-check\"></i></span> "+Messages.getString("QuestionStack.prtcorrect", p));//Correct answer, well done.
		question.setPrtpartiallycorrect("<span style=\"font-size: 1.5em; color:orange;\"><i class=\"fa fa-adjust\"></i></span> "+Messages.getString("QuestionStack.prtpartiallycorrect", p));//Your answer is partially correct.
		question.setPrtincorrect("<span style=\"font-size: 1.5em; color:red;\"><i class=\"fa fa-times\"></i></span> "+Messages.getString("QuestionStack.prtincorrect", p));//Incorrect answer.
		question.setPrtexample(Messages.getString("QuestionStack.prtexample", p));


		log.finer("add converted elements to question");
		for(ProblemElement e:p.getElements()){
			e.addToMdlQuestionStack(question);
		}
		
		log.finer("correct prt- and node-values");
		question.correctPrtValuesAndLinks();

		// for problems directly from the author - no meta basedon tag
		String qnote = question.getQuestionnote();
		if (!qnote.contains("LON-CAPA")) {
			String basedOn = p.getCategoryPrefix() + p.getCategory() + "/" + p.getProblemName() + ".problem";
			basedOn = "This question is based on LON-CAPA problem " + basedOn;
			qnote +=  ", " + System.lineSeparator()  + basedOn;
			question.setQuestionnote(qnote);
		}
		question.appendVarsToQuestionnote();

		log.fine("Done generating Question.");
		return question;
	}

	/**
	 * Generates questionessay object and fills it with all converted elements of given Problem.
	 */
	public QuestionEssay generatingQuestionEssay(Problem p){
		log.fine(Prefs.CLI_LINE_SEP);
		log.fine("Starting generating Essay Question.");

		QuestionEssay question=new QuestionEssay();

		question.setName(p.getProblemName());
		question.setTags(p.getTags());
		log.finer("add converted elements to question");
		for(ProblemElement e:p.getElements()){
			if (e.getQuestionType().equals("essay")){
				EssayResponse er = (EssayResponse)e;
				er.addToMdlQuestionEssay(question);
			}else {
				e.addToMdlQuestion(question);
			}
		}

		log.fine("Done generating Essay Question.");
		return question;
	}

	public QuestionEssay generatingAdditionalQuestionEssay(Problem p){
		log.fine(Prefs.CLI_LINE_SEP);
		log.fine("Starting generating additional Essay Question.");

		QuestionEssay question=new QuestionEssay();

		question.setName(p.getProblemName());
		question.setTags(p.getTags());
		for(ProblemElement e:p.getElements()){
			if (e.getQuestionType().equals("essay")){
				EssayResponse er = (EssayResponse)e;
				if (er.isFile()){
					question.addToQuestionText(Messages.getString("EssayResponse.essayTextFileEssay", p));
				}else{
					question.addToQuestionText(Messages.getString("EssayResponse.essayTextFieldEssay", p));
				}
				er.addToMdlQuestionEssay(question);

			}

		}

		log.fine("Done generating additional Essay Question.");
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
		//category.setInfo("Category set by lc2mdl.");
		return category;
	}

}
