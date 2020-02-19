package lc2mdl.lc.problem.response;

import lc2mdl.lc.problem.Problem;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class ChoiceResponse extends Response {

    protected String responseprefix="";
    protected String answer="";
    protected String answerdisplay="choice";
    protected int numberOfFoils=1;
    protected boolean isCheckBox =false;
    protected String checkBoxValue ="";
    protected String addToFeedbackVariables="";
    protected ArrayList<InputFoil> foilsList = new ArrayList<InputFoil>();
    protected boolean random = true;
    protected boolean isMatch = false;
    protected HashMap<String,String> matchOptions = new HashMap<String,String>();

    public ChoiceResponse(Problem problem, Node node) {

        super(problem, node);
        answerdisplay+= (problem.getIndexFromResponse(this))+1;
        responseprefix+= answerdisplay;
        answer = responseprefix+"_true";

        questionType = "stack";
        problem.addQuestionType(questionType);

    }

    protected void consumeFoils(Element e){


        int currentfoil=1;
        int currentconcept=0;
        int max=20;

        if (e.hasAttribute("randomize")){
            if (e.getAttribute("randomize").equals("no")){
                random = false;
            }
            e.removeAttribute("randomize");
        }
        removeAttributeIfExist(e,"direction");

        if (e.hasAttribute("max")){
            if (!e.getAttribute("max").equals("")){
                max = Integer.parseInt(e.getAttribute("max"));
            }
            e.removeAttribute("max");
        }

        removeAttributeIfExist(e,"TeXlayout");

        additionalCASVars += System.lineSeparator()+System.lineSeparator()+"/* choice response  */";
        ArrayList<Node> nodesToRemove=new ArrayList<>();
        NodeList nodeList = e.getChildNodes();
        for (int i=0; i<nodeList.getLength(); i++){
            Element element=(Element)nodeList.item(i);
            if (element.getTagName().equals("foilgroup")){
                removeAttributeIfExist(element,"texoptions");
                removeAttributeIfExist(element,"noprompt");
                NodeList fgList = element.getChildNodes();
                additionalCASVars += System.lineSeparator()+responseprefix+": [];";
                additionalCASVars += System.lineSeparator()+responseprefix+"_concepts : [];";
                additionalCASVars += System.lineSeparator()+responseprefix+"_conceptfoils : [];";
                for (int j=0; j<fgList.getLength(); j++){
                    additionalCASVars += System.lineSeparator();
                    Element elementfg = (Element)fgList.item(j);
                    // normal foil
                    if (elementfg.getTagName().equals("foil")){
                        Foil foil=new Foil(elementfg);
                        if (!foil.value.equals("unused")){
                            foil.addFoilVars(responseprefix,currentfoil);
                            foilsList.add(new InputFoil(currentfoil,foil.name,""));
                            currentfoil++;
                        }
                        nodesToRemove.add(elementfg);
                    }else {
                        // concept group
                        if(elementfg.getTagName().equals("conceptgroup")){
                            currentconcept++;
                            String conceptString = "";
                            if (elementfg.hasAttribute("concept")){
                                conceptString = elementfg.getAttribute("concept");
                                // note concept
                                additionalCASVars += System.lineSeparator()+"/* conceptgroup "+conceptString+" */";
                                additionalCASVars += System.lineSeparator()+responseprefix+"_concepts : endcons(";
                                additionalCASVars += "\""+conceptString+"\","+responseprefix+"_concepts);";
                                elementfg.removeAttribute("concept");
                            }
                            NodeList cgList = elementfg.getChildNodes();
                            int currentcgfoil=1;
                            String conceptArray = responseprefix+"_concept"+currentconcept;
                            String prefix = conceptArray;
                            additionalCASVars += System.lineSeparator()+conceptArray+": []";
                            String conceptfoils = System.lineSeparator()+responseprefix+"_conceptfoils : endcons ( [";
                            for (int k=0; k<cgList.getLength();k++) {
                                Element elementcg = (Element) cgList.item(k);
                                // concept foils
                                if (elementcg.getTagName().equals("foil")) {
                                    Foil foil = new Foil(elementcg);
                                    if (!foil.value.equals("unused")) {
                                        foil.addFoilVars(prefix, currentcgfoil);
                                        if (currentcgfoil>1) {
                                            conceptfoils += ",";
                                        }
                                        conceptfoils += foil.name;
                                        currentcgfoil++;
                                    }
                                    nodesToRemove.add(elementcg);
                                }
                             }
                            conceptfoils += "],"+responseprefix+"_conceptfoils); ";
                            additionalCASVars += conceptfoils;
                            if (currentcgfoil>1) {
                                additionalCASVars += System.lineSeparator()+responseprefix+" : endcons( rand("+conceptArray+"),"+responseprefix+");";

                                foilsList.add(new InputFoil(currentfoil,"",conceptString));
                                currentfoil++;

                            }
                        }
                    }
                }
            }

        }

        additionalCASVars += System.lineSeparator();
        removeNodesFromDOM(nodesToRemove);
        numberOfFoils = currentfoil-1;

        if (random && isCheckBox){
            additionalCASVars += System.lineSeparator()+answerdisplay+" : random_permutation("+answerdisplay+");";
            if (max<numberOfFoils){
                numberOfFoils = max;
                additionalCASVars += System.lineSeparator()+"choice : rand_selection("+answerdisplay+","+max+");";
                additionalCASVars += System.lineSeparator()+answerdisplay+"_truechoice : mcq_correct(choice);";
                additionalCASVars += System.lineSeparator()+"while emptyp("+answerdisplay+"_truechoice) do (choice : rand_selection("+answerdisplay+","+max+"), "+answerdisplay+"_truechoice : mcq_correct(choice) );";
                additionalCASVars += System.lineSeparator()+answerdisplay+" : choice;";
            }
        }

        if (random && !isCheckBox){
            log.warning("foils were randomized, they are not in the Moodle Stack question");
        }

    }


    protected class Foil{
        private String value="";
        private String name="";
        private String description ="";

        public Foil(Element e){
            if(e.hasAttribute("value")){
                value = e.getAttribute("value");
                if (!isCheckBox) {
                    if (isMatch){
                        value = "\"" + matchOptions.get(value) + "\"";
                    }else {
                        if (value.startsWith("$")) {
                            value = value.replaceAll("\\$", "");
                        } else {
                            value = "\"" + value + "\"";
                        }
                    }
                }else {
                    if (!checkBoxValue.equals("")){
                        if (value.startsWith("$")){
                            value = value.replaceAll("\\$", "");
                            value = "is("+value+" = "+"\""+checkBoxValue+"\")";
                        }else {
                            if (value.equals(checkBoxValue)) {
                                value = "true";
                            } else {
                                value = "false";
                            }
                        }
                    }

                }
                e.removeAttribute("value");
            }else{
                log.warning("foil without value");
            }
            if (e.hasAttribute("name")){
                name = "\""+e.getAttribute("name")+"\"";
                e.removeAttribute("name");
            }else{
                log.warning("foil without name");
            }

            NodeList nlist=e.getChildNodes();
            description = "";
            for (int i=0; i<nlist.getLength();i++){
                Element el = (Element)nlist.item(i);
                if (el.getTagName().equals("outtext")){
                    description += el.getTextContent();
                }else{
                    log.warning("found foil content of type "+el.getTagName());
                }
            }
            description = transformFoil(description);

        }

        public void addFoilVars(String prefix, int i){
            additionalCASVars += System.lineSeparator()+prefix+"_foilvalue : "+value+";";
            additionalCASVars += System.lineSeparator()+prefix+"_foilname : "+name+";";
            additionalCASVars += System.lineSeparator()+prefix+"_foildescription : "+ description+";";
            additionalCASVars += System.lineSeparator()+prefix+"_foil : "+"["+prefix+"_foilname,"+prefix+"_foilvalue,"+prefix+"_foildescription]"+";";
            additionalCASVars += System.lineSeparator()+prefix+" : endcons("+prefix+"_foil,"+prefix+")"+";";
        }

        private String transformFoil(String foilString){
            foilString = foilString.replaceAll("\\\\([a-zA-Z:;, {}])","\\\\\\\\$1");
            foilString = transformTextVariable(foilString);
            return foilString;
        }
     }

    protected class InputFoil{
        private String foilName="";
        private String conceptName="";
        private String inName ="";

        public InputFoil(int i, String foilname, String conceptname){
            inName = inputName+"_"+i;
            foilName = foilname;
            conceptName = conceptname;
        }

        public String getFoilName() { return foilName; }

        public String getConceptName() { return conceptName; }

        public String getInName() { return inName;  }
    }

    public boolean isCheckBox() { return isCheckBox;    }

    public String getCheckBoxValue() { return checkBoxValue;    }

    public ArrayList<InputFoil> getFoilsList() { return foilsList; }

}
