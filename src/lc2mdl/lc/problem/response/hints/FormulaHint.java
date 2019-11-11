package lc2mdl.lc.problem.response.hints;


import lc2mdl.lc.problem.Problem;
import lc2mdl.lc.problem.response.FormulaResponse;
import lc2mdl.mdl.quiz.NodeMdl;
import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Node;

public class FormulaHint extends FormulaResponse implements ConditionalHint {

	private boolean link;

	public FormulaHint(Problem problem,Node node, boolean link){
		super(problem,node);
		this.link=link;
	}


    @Override
    public void consumeNode() {

    }

    @Override
    public void addHintNodeToMdlQuestion(QuestionStack question, NodeMdl parentNode) {

    }
}
