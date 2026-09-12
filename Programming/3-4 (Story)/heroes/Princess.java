package heroes;

import exceptions.GiftOverflow;
import gifts.Gift;
import interfaces.Reciever;

public class Princess extends Hero implements Reciever {

    public Princess(String name, Emotion emotion){
        super(name, emotion);
    }

    public void showGifts(){
        System.out.println(christineGifts);
    }

    @Override
    public String toString() {
        return "Princess " + getName() + " feels " + getEmotion();
    }

    @Override
    public Gift recieveGift(Gift gift) throws GiftOverflow {
        if (christineGifts.size() == 4){
            throw new GiftOverflow();
        } else{
            christineGifts.add(gift);
            System.out.println(getName() + " got " + gift);
        }
        return gift;
    }
}
