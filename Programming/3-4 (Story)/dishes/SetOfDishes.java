package dishes;

public record SetOfDishes(Plate plate, Fork fork, Spoon spoon, Knife knife, Glass glass) {

    @Override
    public String toString() {
        return "SetOfDishes{" +
                "plate=" + plate +
                ", fork=" + fork +
                ", spoon=" + spoon +
                ", knife=" + knife +
                ", glass=" + glass +
                '}';
    }
}
