package dishes;

public class Plate{
    protected Material material;
    protected PreciousStones stones;

    public Plate(Material material, PreciousStones stones){
        this.material = material;
        this.stones = stones;
    }

    @Override
    public String toString() {
        return material + " spoon with " + stones + " on it";
    }
}
