package lc2mdl.lc.problem.display;

import org.w3c.dom.Node;

import lc2mdl.lc.problem.Problem;

public class Solved extends DisplayFeedback {

    public Solved(Problem problem, Node node) {
        super(problem, node);
        comment = "General feedback is meant to be shown after the problem is solved.";
    }

}
