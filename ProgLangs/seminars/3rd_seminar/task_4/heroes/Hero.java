package heroes;

import gifts.Gift;
import java.util.ArrayList;

public abstract class Hero{

    private String name;
    private Emotion emotion;
    protected ArrayList<Gift> christineGifts = new ArrayList<>();


    public Hero(String name, Emotion emotion){
        this.name = name;
        this.emotion = emotion;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void setEmotion(Emotion emotion){
        this.emotion = emotion;
    }

    public Emotion getEmotion(){
        return emotion;
    }

    @Override
    public String toString() {
        return "Hero" + name + " feels " + emotion;
    }
}
