package ProgaLabs.Lab2.Pokemons;

import ProgaLabs.Lab2.Skills.*;
import ru.ifmo.se.pokemon.*;

public class Cacnea extends Pokemon {
    public Cacnea(String name, int level){
        super(name, level);
        setType(Type.GRASS);
        setStats(50, 85, 40,85,40,35);
        setMove(new PinMissile(), new EnergyBall(), new CottonSpore());
    }
}