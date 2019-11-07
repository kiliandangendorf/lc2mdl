package lc2mdl.lc.problem.display;

import lc2mdl.lc.problem.Problem;
import lc2mdl.lc.problem.ProblemElement;
import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class DisplayFeedback extends ProblemElement {

    protected String comment;
    private String feedbackText;

    public DisplayFeedback(Problem problem, Node node){
        super(problem,node);
        feedbackText="";
    }

    @Override
    public void addToMdlQuestion(QuestionStack question) {
        question.addComment(comment);
        String oldFeedback = question.getGeneralfeedback();
        feedbackText = oldFeedback + System.lineSeparator() + feedbackText;
        question.setGeneralfeedback(feedbackText);
    }

    @Override
    public void consumeNode() {
        log.finer("-found feedback after solved/postanswerdate");

        Element e=(Element)node;

        if(e.hasAttributes())log.warning("-still unknown attributes in solved/postanswerdate.");

        NodeList outtexts=e.getElementsByTagName("outtext");
        for(int j=0;j<outtexts.getLength();j++){
            Element text=(Element)outtexts.item(j);
            String out=text.getTextContent();
            feedbackText += System.lineSeparator() + out;
            text.setTextContent(null);
        }

        String out = e.getTextContent();
        feedbackText += System.lineSeparator() + out;
        e.setTextContent(null);

        feedbackText = transformTextElement(feedbackText);

    }
}
