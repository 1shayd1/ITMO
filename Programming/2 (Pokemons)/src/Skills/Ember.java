package ProgaLabs.Lab2.Skills;

import ru.ifmo.se.pokemon.*;

public class Ember extends SpecialMove {
    public Ember() {
        super(Type.FIRE, 40, 100);
    }

    protected void applyOppDamage(Pokemon pokemon, double v) {
        pokemon.setMod(Stat.HP, (int) Math.round(v));
    }

    protected void applyOppEffects(Pokemon pokemon) {
        if (Math.random() <= 0.1) {
            Effect.burn(pokemon);
        }
    }

    protected String describe() {
        return "Применил Ember";
    }
}
