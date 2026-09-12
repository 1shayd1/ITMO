package gifts;


public record Gift(Toys toy, Qualities quality){

    @Override
    public String toString() {
        return "Gift{" +
                "toy=" + toy +
                ", quality=" + quality +
                '}';
    }
}

