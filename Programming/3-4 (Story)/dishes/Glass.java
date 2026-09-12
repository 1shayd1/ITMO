package dishes;

public class Glass{
    protected Material material;

    public Glass(Material material){
        this.material = material;
    }

    @Override
    public String toString() {
        return material + " glass";
    }
}