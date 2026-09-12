package dishes;

public class Spoon{
    protected Material material;

    public Spoon(Material material){
        this.material = material;
    }

    @Override
    public String toString() {
        return material + " spoon";
    }
}