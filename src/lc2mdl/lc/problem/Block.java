package lc2mdl.lc.problem;

import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Block extends ProblemElement {
    private String additionalCASVariables = "";
    private int index=0;
    private String addToText = "";
    private String blockWarning = " ATTENTION !!! Conditional block ";

    public Block(Problem problem, Node node) {
        super(problem, node);
        index = problem.getIndexFromSameClassOnly(this)+1;
    }

    @Override
    public void consumeNode() {
        log.finer("--found conditional block");

        Element e = (Element)node;

        if (e.hasAttribute("condition")) {
            String condition = e.getAttribute("condition");
            if (!condition.equals("")){
                addToText = System.lineSeparator()+blockWarning+"block_condition"+index+System.lineSeparator();

                condition = transformBlockCondition(condition);
                additionalCASVariables = System.lineSeparator()+" "+System.lineSeparator()+"/* "+blockWarning+" */";
                additionalCASVariables += System.lineSeparator()+"block_condition"+index+" : "+condition;

            }
        }else{
            log.warning("--found block without condition");
        }
        removeAttributeIfExist(e,"condition");

    }

    @Override
    public void addToMdlQuestion(QuestionStack question) {

        question.addToQuestionText(addToText);
        question.addToQuestionVariables(additionalCASVariables);
    }

    private String transformBlockCondition(String condition){
        condition = condition.replaceAll("\\$","");
        condition = condition.replaceAll(" LE "," <= ");
        condition = condition.replaceAll(" GE "," >= ");
        condition = condition.replaceAll(" LT "," < ");
        condition = condition.replaceAll(" GT "," > ");
        condition = condition.replaceAll("\\|\\|"," or ");
         // && replaced by "and" already in PreParser
        condition = condition.replaceAll("!\\(","not (");

        return condition;
    }
}
