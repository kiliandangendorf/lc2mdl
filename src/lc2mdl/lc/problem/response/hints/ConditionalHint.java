package lc2mdl.lc.problem.response.hints;

import lc2mdl.mdl.quiz.NodeMdl;
import lc2mdl.mdl.quiz.QuestionStack;

public interface ConditionalHint{	
	
	public void consumeNode();
	
	public void addHintNodeToMdlQuestion(QuestionStack question, NodeMdl parentNode);
}
