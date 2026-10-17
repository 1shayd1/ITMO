package heroes;

public class Witch extends Hero {

    public Witch(String name, Emotion emotion){
        super(name, emotion);
    }

    @Override
    public String toString() {
        return "Witch " + getName() + " feels " + getEmotion();
    }
}
