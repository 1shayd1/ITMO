package ProgaLabs.Lab2.Pokemons;

import ProgaLabs.Lab2.Skills.*;
import ru.ifmo.se.pokemon.*;

public class Cacturne extends Cacnea {
    public Cacturne(String name, int level){
        super(name, level);
        setType(Type.GRASS, Type.DARK);
        setStats(70, 115, 60,115,60,55);
        addMove(new FocusBlast());
    }
}