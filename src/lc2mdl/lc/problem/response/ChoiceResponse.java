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

    public ChoiceResponse(Problem problem, Node node) {

        super(problem, node);
        answerdisplay+= (problem.getIndexFromSameClassOnly(this));
        responseprefix+= answerdisplay+"_";
        answer = responseprefix+"true";
    }

    protected void consumeFoils(Element e){

        boolean random = true;
        int currentfoil=1;
        int currentconcept=1;
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
                    Element elementfg = (Element)fgList.item(j);
                    if (elementfg.getTagName().equals("foil")){
                        Foil foil=new Foil(elementfg);
                        if (!foil.value.equals("unused")){
                            foil.addFoilVars(responseprefix,currentfoil);
                            currentfoil++;
                        }
                        nodesToRemove.add(elementfg);
                    }else {
                        if(elementfg.getTagName().equals("conceptgroup")){
                            NodeList cgList = elementfg.getChildNodes();
                            int currentcgfoil=1;
                            String prefix = responseprefix+"concept"+currentconcept+"_";
                            for (int k=0; k<cgList.getLength();k++) {
                                Element elementcg = (Element) cgList.item(k);
                                if (elementcg.getTagName().equals("foil")) {
                                    Foil foil = new Foil(elementcg);
                                    if (!foil.value.equals("unused")) {
                                        foil.addFoilVars(prefix, currentfoil);
                                        currentcgfoil++;
                                    }
                                    nodesToRemove.add(elementcg);
                                }
                                removeAttributeIfExist(elementfg,"concept");
                            }
                            if (currentcgfoil>1) {
                                additionalCASVars += System.lineSeparator()+prefix+": makelist("+prefix+"foil[k],k,1,"+(currentcgfoil-1)+")";
                                additionalCASVars += System.lineSeparator()+responseprefix+"foil["+i+"] : random_selection("+prefix+",1)";
                                currentconcept++;
                                currentfoil++;

                            }
                        }
                    }
                }
            }

        }

        additionalCASVars += System.lineSeparator()+answerdisplay+": makelist("+responseprefix+"foil[k],k,1,"+(currentfoil-1)+")";
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
                if (!checkBox) { value = "\""+value+"\""; }
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

}
