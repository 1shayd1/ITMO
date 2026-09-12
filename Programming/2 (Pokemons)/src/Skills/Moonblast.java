package ProgaLabs.Lab2.Skills;

import ru.ifmo.se.pokemon.*;

public class Moonblast extends SpecialMove {
    public Moonblast() {
        super(Type.FAIRY, 95, 100);
    }

    protected void applyOppDamage(Pokemon pokemon, double v) {
        pokemon.setMod(Stat.HP, (int) Math.round(v));
    }

    protected void applyOppEffects(Pokemon pokemon) {
        if (Math.random() <= 0.3) {
            pokemon.setMod(Stat.SPECIAL_ATTACK, -1);
        }
    }

    protected String describe() {
        return "Применил Moonblast";
    }
}
