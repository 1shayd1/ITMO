package exceptions;

public class GiftOverflow extends Exception {

    @Override
    public String getMessage() {
        return "There are already enough gifts!";
    }
}
