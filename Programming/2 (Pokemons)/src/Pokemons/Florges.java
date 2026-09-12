package ProgaLabs.Lab2.Pokemons;

import ProgaLabs.Lab2.Skills.*;
import ru.ifmo.se.pokemon.*;

public class Florges extends Floette {
    public Florges(String name, int level){
        super(name, level);
        setType(Type.FAIRY);
        setStats(78, 65, 68,112,154,75);
        addMove(new Swagger());
    }
}