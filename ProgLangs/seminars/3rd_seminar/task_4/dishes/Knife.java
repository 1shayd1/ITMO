package dishes;

public class Knife{
    protected Material material;

    public Knife(Material material){
        this.material = material;
    }

    @Override
    public String toString() {
        return material + " knife";
    }
}