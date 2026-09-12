package interfaces;

import exceptions.DoNotLikeGift;
import exceptions.GiftOverflow;
import gifts.Gift;

public interface Makegift{
     Gift makeGift(Gift gift, Reciever hero) throws DoNotLikeGift, GiftOverflow;
}
