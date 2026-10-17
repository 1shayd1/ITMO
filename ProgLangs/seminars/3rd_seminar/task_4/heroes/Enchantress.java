package heroes;

import dishes.*;
import exceptions.DoNotLikeGift;
import exceptions.GiftOverflow;
import gifts.Gift;
import gifts.Toys;
import interfaces.*;

public class Enchantress extends Hero implements Invite, Makegift{

    public Enchantress(String name, Emotion emotion){
        super(name, emotion);
    }

    @Override
    public void beInvited() {
        System.out.print(getName()); Invite.super.beInvited();
    }

    public void getSetOfDishes(SetOfDishes setOfDishes){
        System.out.println(getName() + " got " + setOfDishes);
    }

    public void getDinner(DinnerDishes dinnerDishes){
        System.out.println(getName() + " got " + dinnerDishes);
    }

    @Override
    public String toString() {
        return "Enchantress " + getName() + " feels " + getEmotion();
    }

    @Override
    public Gift makeGift(Gift gift, Reciever hero) throws DoNotLikeGift, GiftOverflow{
        if (gift.toy() == Toys.TOY_CASTLE || gift.toy() == Toys.DRESS){
            throw new DoNotLikeGift();
        }
        return hero.recieveGift(gift);
    }

}
