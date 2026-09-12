package ProgaLabs.Lab2.Pokemons;

import ProgaLabs.Lab2.Skills.*;
import ru.ifmo.se.pokemon.*;

public class Flabebe extends Pokemon {
    public Flabebe(String name, int level){
        super(name, level);
        setType(Type.FAIRY);
        setStats(44, 38, 39,61,79,42);
        setMove(new Moonblast(), new Psychic());
    }
}