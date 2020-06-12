package lc2mdl.lc.problem;

import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Node;

public class BlockEnd extends ProblemElement {

    private String additionalCASVariables = "";
    private String addToText = "";

    public BlockEnd(Problem problem, Node node){
        super(problem,node);
        this.problem = problem;
    }

    @Override
    public void consumeNode() {
        additionalCASVariables = ");"+System.lineSeparator()+"/* Block End */"+System.lineSeparator()+System.lineSeparator();
        addToText = "[[/ if ]]";
    }

    @Override
    public void addToMdlQuestionStack(QuestionStack question) {
        question.addToQuestionVariables(additionalCASVariables);
        question.addToQuestionText(addToText);
    }
}
