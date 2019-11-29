package lc2mdl.mdl.quiz;

import java.util.ArrayList;

public abstract class Question extends QuizElement{
    protected ArrayList<String> tags = new ArrayList<>();
    protected String path;

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

}
