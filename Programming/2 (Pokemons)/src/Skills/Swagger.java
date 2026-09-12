package ProgaLabs.Lab2.Skills;

import ru.ifmo.se.pokemon.*;

public class Swagger extends StatusMove {
    public Swagger() {
        super(Type.NORMAL, 0, 85);
    }

    protected void applyOppEffects(Pokemon pokemon) {
        if (pokemon.getStat(Stat.ATTACK) == 6) {
            pokemon.setMod(Stat.SPECIAL_ATTACK, +2);
        } else if (pokemon.getStat(Stat.ATTACK) == 5) {
            pokemon.setMod(Stat.ATTACK, +1);
        } else {
            pokemon.setMod(Stat.ATTACK, +2);
        }
        pokemon.confuse();
    }

    protected String describe() {
        return "Применил Swagger";
    }
}
