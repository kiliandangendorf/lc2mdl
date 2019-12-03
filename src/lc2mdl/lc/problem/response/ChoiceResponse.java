package lc2mdl.lc.problem.response;

import lc2mdl.lc.problem.Problem;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public abstract class ChoiceResponse extends Response {

    protected ArrayList<Foil> foilList = new ArrayList<Foil>();


    public ChoiceResponse(Problem problem, Node node) {
        super(problem, node);
    }

    private void consumeFoils(Element e){

        boolean random = true;
        int currentfoil=0;
        int currentconcept=0;

        if (e.hasAttribute("randomize")){
            if (e.getAttribute("randomize").equals("no")){
                random = false;
            }
            e.removeAttribute("randomize");
        }

        NodeList nodeList = e.getChildNodes();
        for (int i=0; i<nodeList.getLength(); i++){
            Element element=(Element)nodeList.item(i);
            if (element.getTagName().equals("foilgroup")){
                NodeList fgList = element.getChildNodes();
                for (int j=0; j<fgList.getLength(); j++){
                    Element elementfg = (Element)fgList.item(j);
                    if (elementfg.getTagName().equals("foil")){
                        Foil foil=new Foil(elementfg);
                        if (!foil.value.equals("unused")){
                            foilList.add(foil);
                            currentfoil++;
                        }
                    }else if(elementfg.getTagName().equals("conceptgroup")){
                        NodeList cgList = elementfg.getChildNodes();
                        int cgStart=currentfoil;
                        for (int k=0; k<cgList.getLength();k++){

                        }
                    }
                }
            }

        }

    }

    protected void BuildFoilVars(){
        int i=0;
        for (Foil foil : foilList){
            if (!foil.value.equals("unused")) {
                additionalCASVars += System.lineSeparator()+"foilvalue["+i+"] : "+foil.value;
                additionalCASVars += System.lineSeparator()+"foilname["+i+"] : "+foil.name;
                additionalCASVars += System.lineSeparator()+"foildescription["+i+"] : "+foil.decription;
                additionalCASVars += System.lineSeparator()+"foil["+i+"] : "+"["+"foilname["+i+"],foilvalue["+i+"],foildescription["+i+"]]";
            }
            i++;
        }
    }

    protected class Foil{
        private String value="";
        private String name="";
        private String decription="";

        public Foil(Element e){
            if(e.hasAttribute("value")){
                value = e.getAttribute("value");
                e.removeAttribute("value");
            }else{
                log.warning("foil without value");
            }
            if (e.hasAttribute("name")){
                name = e.getAttribute("name");
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
                    log.warning("found foil content of type"+el.getTagName());
                }
            }

        }
    }

}
