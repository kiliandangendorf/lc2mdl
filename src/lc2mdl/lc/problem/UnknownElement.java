package lc2mdl.lc.problem;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import lc2mdl.mdl.quiz.QuestionStack;

public class UnknownElement extends ProblemElement{

	public UnknownElement(Problem problem,Node node){
		super(problem,node);
	}

	@Override
	public void consumeNode(){
		Element e = (Element)node;
		String name = e.getTagName();
		log.finer("consume unknown element: "+name);
	}

	@Override
	public void addToMdlQuestion(QuestionStack question){
	}

}
