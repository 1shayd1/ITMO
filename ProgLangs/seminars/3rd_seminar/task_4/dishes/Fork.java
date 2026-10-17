package dishes;

public class Fork{
    protected Material material;

    public Fork(Material material){
        this.material = material;
    }

    @Override
    public String toString() {
        return material + " fork";
    }
}