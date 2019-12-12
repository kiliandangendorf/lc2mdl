package lc2mdl.lc.problem.response.hints;

import lc2mdl.lc.problem.Problem;
import lc2mdl.lc.problem.response.CustomResponse;
import lc2mdl.mdl.quiz.NodeMdl;
import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Node;

public class CustomHint extends CustomResponse implements ConditionalHint {
    public CustomHint(Problem problem, Node node) {
        super(problem, node);
    }

    @Override
    public void consumeNode() {
    }

    @Override
    public void addToMdlQuestion(QuestionStack question) {
    }

    @Override
    public void addHintNodeToMdlQuestion(QuestionStack question, NodeMdl parentNode) {

    }
}
