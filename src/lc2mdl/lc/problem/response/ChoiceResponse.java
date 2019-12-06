package lc2mdl.lc.problem.response;

import lc2mdl.lc.problem.Problem;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public abstract class ChoiceResponse extends Response {

    protected String responseprefix="";
    protected String answer="";
    protected String answerdisplay="choice";
    protected int numberOfFoils=1;
    protected boolean checkBox=false;
    protected String checkBoxValue ="";

    public ChoiceResponse(Problem problem, Node node) {

        super(problem, node);
        answerdisplay+= (problem.getIndexFromResponse(this))+1;
        responseprefix+= answerdisplay+"_";
        answer = responseprefix+"true";
    }

    protected void consumeFoils(Element e){

        boolean random = true;
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

        ArrayList<Node> nodesToRemove=new ArrayList<>();
        NodeList nodeList = e.getChildNodes();
        for (int i=0; i<nodeList.getLength(); i++){
            Element element=(Element)nodeList.item(i);
            if (element.getTagName().equals("foilgroup")){
                removeAttributeIfExist(element,"texoptions");
                removeAttributeIfExist(element,"noprompt");
                NodeList fgList = element.getChildNodes();
                for (int j=0; j<fgList.getLength(); j++){
                    additionalCASVars += System.lineSeparator();
                    Element elementfg = (Element)fgList.item(j);
                    // normal foil
                    if (elementfg.getTagName().equals("foil")){
                        Foil foil=new Foil(elementfg);
                        if (!foil.value.equals("unused")){
                            foil.addFoilVars(responseprefix,currentfoil);
                            currentfoil++;
                        }
                        nodesToRemove.add(elementfg);
                    }else {
                        // concept group
                        if(elementfg.getTagName().equals("conceptgroup")){
                            currentconcept++;
                            if (elementfg.hasAttribute("concept")){
                                String conceptString = elementfg.getAttribute("concept");
                                // note concept
                                additionalCASVars += System.lineSeparator()+responseprefix+"concept["+currentconcept+"] : ";
                                additionalCASVars += "\""+conceptString+"\"";
                                elementfg.removeAttribute("concept");
                            }
                            NodeList cgList = elementfg.getChildNodes();
                            int currentcgfoil=1;
                            String conceptArray = responseprefix+"concept"+currentconcept;
                            String prefix = conceptArray+"_";
                            String conceptfoils = System.lineSeparator()+responseprefix+"conceptfoils["+currentconcept+"] : [";
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
                            conceptfoils += "]";
                            additionalCASVars += conceptfoils;
                            if (currentcgfoil>1) {
                                additionalCASVars += System.lineSeparator()+conceptArray+": makelist("+prefix+"foil[k],k,1,"+(currentcgfoil-1)+")";
                                additionalCASVars += System.lineSeparator()+responseprefix+"foil["+currentfoil+"] : random_selection("+conceptArray+",1)";
                                currentfoil++;

                            }
                        }
                    }
                }
            }

        }

        if (currentconcept>0) {
            additionalCASVars += System.lineSeparator();
            additionalCASVars += System.lineSeparator() + responseprefix + "concepts : makelist(" + responseprefix + "concept[k],k,1," + currentconcept + ")";
            additionalCASVars += System.lineSeparator() + responseprefix + "conceptfoils : makelist(" + responseprefix + "conceptfoils[k],k,1," + currentconcept + ")";
        }

        additionalCASVars += System.lineSeparator();
        additionalCASVars += System.lineSeparator()+answerdisplay+" : makelist("+responseprefix+"foil[k],k,1,"+(currentfoil-1)+")";
        removeNodesFromDOM(nodesToRemove);

        if (random){
            additionalCASVars += System.lineSeparator()+answerdisplay+" : random_permutation("+answerdisplay+")";
        }

        numberOfFoils = currentfoil-1;
        if (max<numberOfFoils){
            numberOfFoils = max;
            additionalCASVars += System.lineSeparator()+answerdisplay+" : random_selection("+answerdisplay+","+max+")";
        }



    }


    protected class Foil{
        private String value="";
        private String name="";
        private String decription="";

        public Foil(Element e){
            if(e.hasAttribute("value")){
                value = e.getAttribute("value");
                if (!checkBox) {
                    value = "\""+value+"\"";
                }else {
                    if (!checkBoxValue.equals("")){
                        if (value.equals(checkBoxValue)){
                            value = "true";
                        }else{
                            value = "false";
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
            for (int i=0; i<nlist.getLength();i++){
                Element el = (Element)nlist.item(i);
                if (el.getTagName().equals("outtext")){
                    decription = el.getTextContent();
                    decription = "\""+transformTextElement(decription)+"\"";
                }else{
                    log.warning("found foil content of type "+el.getTagName());
                }
            }

        }

        public void addFoilVars(String prefix, int i){
            additionalCASVars += System.lineSeparator()+prefix+"foilvalue["+i+"] : "+value;
            additionalCASVars += System.lineSeparator()+prefix+"foilname["+i+"] : "+name;
            additionalCASVars += System.lineSeparator()+prefix+"foildescription["+i+"] : "+decription;
            additionalCASVars += System.lineSeparator()+prefix+"foil["+i+"] : "+"["+prefix+"foilname["+i+"],"+prefix+"foilvalue["+i+"],"+prefix+"foildescription["+i+"]]";
            if (value.equals("true")){
                additionalCASVars  += System.lineSeparator()+prefix+"true : "+name;
            }
        }
    }

    public String getResponseprefix() {
        return responseprefix;
    }
}
