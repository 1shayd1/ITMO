package ProgaLabs.Lab2.Pokemons;

import ProgaLabs.Lab2.Skills.*;
import ru.ifmo.se.pokemon.*;

public class Togedemaru extends Pokemon {
    public Togedemaru(String name, int level){
        super(name, level);
        setType(Type.ELECTRIC, Type.STEEL);
        setStats(65, 98, 63,40,73,96);
        setMove(new Tackle(), new DragonClaw(), new Ember(), new ChargeBeam());
    }
}