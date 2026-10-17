package interfaces;

import exceptions.GiftOverflow;
import gifts.Gift;

public interface Reciever {
    Gift recieveGift(Gift gift) throws GiftOverflow;
}
