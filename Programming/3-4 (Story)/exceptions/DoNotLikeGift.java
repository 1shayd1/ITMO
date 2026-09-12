package exceptions;

public class DoNotLikeGift extends RuntimeException{

    @Override
    public String getMessage() {
        return "Princess doesn't like this gift(((";
    }
}
