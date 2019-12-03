package lc2mdl.lc.problem.response;

import lc2mdl.lc.problem.Problem;
import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Node;

public class OptionResponse extends ChoiceResponse {

    public OptionResponse(Problem problem, Node node) {
        super(problem, node);
    }


    @Override
    public void consumeNode() {

    }

    @Override
    public void addToMdlQuestion(QuestionStack question) {


    }
}
