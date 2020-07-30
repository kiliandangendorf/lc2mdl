package lc2mdl.lc.problem.display;

import lc2mdl.lc.problem.Gnuplot;
import lc2mdl.lc.problem.Image;
import lc2mdl.lc.problem.Problem;
import lc2mdl.lc.problem.ProblemElement;
import lc2mdl.mdl.quiz.Question;
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
    public void addToMdlQuestionStack(QuestionStack question) {
        addToMdlQuestion(question);
    }

     public void addToMdlQuestion(Question question) {
        question.addComment(comment);
        String oldFeedback = question.getGeneralfeedback();
        if(!oldFeedback.equals(""))feedbackText = oldFeedback + System.lineSeparator() + feedbackText;
        question.setGeneralfeedback(feedbackText);
    }


    @Override
    public void consumeNode() {
        log.finer("-found feedback after solved/postanswerdate");

        Element e=(Element)node;

        if(e.hasAttributes())log.warning("-still unknown attributes in solved/postanswerdate.");

        NodeList elements = e.getChildNodes();
        for (int i=0; i<elements.getLength(); i++){
            //make sure, if I can cast to Element
			if(!(elements.item(i).getNodeType()==Node.ELEMENT_NODE))continue;

			Element element=(Element)elements.item(i);
			switch(element.getTagName()) {
                case "outtext":
                    log.finer("found feedback outtext");
                    String out = element.getTextContent();
                    feedbackText += System.lineSeparator() + out;
                    element.setTextContent(null);
                    break;
                case "gnuplot":
                    log.finer("found feedback gnuplot");
                    Gnuplot gnuplot = new Gnuplot(problem,element);
                    gnuplot.consumeNode();
                    feedbackText += System.lineSeparator() + gnuplot.getPlotString();
                    break;
                case "img":
                    log.finer("found feedback image");
                    Image image = new Image(problem,element);
                    image.consumeNode();
                    feedbackText += System.lineSeparator() + image.getImgString();
                    break;
                default:
                    log.warning("found unknown feedback tag " + element.getTagName());
                    break;
            }
        }

        feedbackText = transformTextElement(feedbackText);

    }
}
