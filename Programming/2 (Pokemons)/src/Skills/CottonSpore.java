package ProgaLabs.Lab2.Skills;

import ru.ifmo.se.pokemon.*;

public class CottonSpore extends StatusMove {
    public CottonSpore() {
        super(Type.GRASS, 0, 100);
    }

    protected void applyOppEffects(Pokemon pokemon) {
        pokemon.setMod(Stat.SPEED, -2);
    }

    protected String describe() {
        return "Применил Cotton Spore";
    }
}
