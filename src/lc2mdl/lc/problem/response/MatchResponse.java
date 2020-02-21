package lc2mdl.lc.problem.response;

import lc2mdl.lc.problem.Problem;
import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MatchResponse extends OptionResponse {

    public MatchResponse(Problem problem, Node node) {
        super(problem, node);
        isCheckBox = false;
        isMatch = true;
    }

    @Override
    public void consumeNode() {

        log.finer("matchresponse:");

        Element e=(Element)node;

        NodeList nodeList = e.getChildNodes();
        for (int i=0; i<nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            if (element.getTagName().equals("foilgroup")) {
                consumeItemGroup(element);
            }
        }

        // Foils
        consumeFoils(e);

        setAddCASVarsForDropDown();

        consumeIdAndName(e);

        if(e.hasAttributes())log.warning("-still unknown attributes in response.");

        //RESPONSEPARAM
        consumeResponseParameter(e);

        //TEXTLINE size
        consumeTextline(e);

        //HINTGROUP
        consumeHintgroups(e);

        //Additional Text
        consumeText(e);

    }

    @Override
    public void addToMdlQuestionStack(QuestionStack question) {
        super.addToMdlQuestionStack(question);
    }

    private void consumeItemGroup(Element e){
        NodeList nodeList = e.getChildNodes();
        for (int i=0; i<nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            if (element.getTagName().equals("itemgroup")) {
                removeAttributeIfExist(element,"direction");
                removeAttributeIfExist(element,"location");
                removeAttributeIfExist(element,"randomize");

                NodeList nodeList1 = element.getChildNodes();
                char currentChar = 'A';
                additionalText += System.lineSeparator()+"<br/>";
                for (int j=0; j<nodeList1.getLength(); j++){
                    Element itemElement = (Element) nodeList1.item(j);
                    if (itemElement.getTagName().equals("item")){
                        if(itemElement.hasAttribute("name")){
                            String name=itemElement.getAttribute("name");
                            itemElement.removeAttribute("name");
                            removeAttributeIfExist(itemElement,"location");
                            matchOptions.put(name,""+currentChar);
                            options.add(""+currentChar);
                            Element out = (Element)itemElement.getFirstChild();
                            if (out.getTagName().equals("outtext")){
                                String contString = out.getTextContent();
                                out.setTextContent(null);
                                additionalText += System.lineSeparator()+currentChar+" "+contString+"<br/>";
                                currentChar++;
                            }
                        }
                    }
                }
                additionalText += System.lineSeparator()+"<br/>";

            }
        }


    }
}
