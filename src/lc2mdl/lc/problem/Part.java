package lc2mdl.lc.problem;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import lc2mdl.mdl.quiz.Prt;
import lc2mdl.mdl.quiz.QuestionStack;

public class Part extends ProblemElement{
	
	public Part(Problem problem, Node node){
		super(problem, node);
	}

	@Override
	public void consumeNode() {
		log.finer("part:");
		Element element=(Element)node;
		log.finer("-removed id");
		element.removeAttribute("id");
	}

	@Override
	public void addToMdlQuestion(QuestionStack question) {
		//Only make new prt in question
		question.addPrt(new Prt());
	}

}
