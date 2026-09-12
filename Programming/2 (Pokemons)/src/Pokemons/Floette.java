package ProgaLabs.Lab2.Pokemons;

import ProgaLabs.Lab2.Skills.*;
import ru.ifmo.se.pokemon.*;

public class Floette extends Flabebe {
    public Floette(String name, int level){
        super(name, level);
        setType(Type.FAIRY);
        setStats(54, 45, 47,75,98,52);
        addMove(new Tackle());
    }
}