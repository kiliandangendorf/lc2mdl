package lc2mdl.lc.problem;

import org.w3c.dom.Node;

import lc2mdl.mdl.quiz.QuestionStack;

public class UnknownElement extends ProblemElement{

	public UnknownElement(Problem problem,Node node){
		super(problem,node);
	}

	@Override
	public void consumeNode(){
		log.finer("unknown element:");
	}

	@Override
	public void addToMdlQuestion(QuestionStack question){
	}

}
