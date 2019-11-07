package lc2mdl.lc.problem.display;

import lc2mdl.lc.problem.Problem;
import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Node;

public class PostAnswerDate extends DisplayFeedback {

    public PostAnswerDate(Problem problem, Node node){
        super(problem,node);
        comment= "General Feedback is meant to be shown after the quiz is closed.";
    }

}
