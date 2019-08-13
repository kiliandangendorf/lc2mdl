package lc2mdl.lc.problem;

import org.w3c.dom.Node;

import lc2mdl.mdl.quiz.QuestionStack;

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
	public void addToMdlQuestion(QuestionStack question) {
		//Add content to questiontext
		question.addToQuestionText(text);
	}

}
