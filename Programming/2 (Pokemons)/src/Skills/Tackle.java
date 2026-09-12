package ProgaLabs.Lab2.Skills;

import ru.ifmo.se.pokemon.*;

public class Tackle extends PhysicalMove {
    public Tackle() {
        super(Type.NORMAL, 45, 100);
    }

    protected void applyOppDamage(Pokemon pokemon, double v) {
        pokemon.setMod(Stat.HP, (int) Math.round(v));
    }

    protected String describe() {
        return "Применил Tackle";
    }

}
