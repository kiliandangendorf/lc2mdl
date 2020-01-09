package lc2mdl.lc.problem;

import lc2mdl.mdl.quiz.Question;
import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Node;

public class Outtext extends ProblemElement{
	
	private String text;

	public Outtext(Problem problem, Node node){
		super(problem, node);
	}

	@Override
	public void consumeNode() {
		log.finer("outtext:");
		text=node.getTextContent();
		//got everything;)
		node.setTextContent(null);

		text=transformTextElement(text);
	}

	@Override
	public void addToMdlQuestionStack(QuestionStack question) {
		addToMdlQuestion(question);
	}

	public void addToMdlQuestion(Question question) {
		//Add content to questiontext
		question.addToQuestionText(text);
	}
	public String getText() {
		return text;
	}
}
