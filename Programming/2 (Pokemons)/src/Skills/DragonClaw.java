package ProgaLabs.Lab2.Skills;

import ru.ifmo.se.pokemon.*;

public class DragonClaw extends PhysicalMove {
    public DragonClaw() {
        super(Type.DRAGON, 80, 100);
    }

    protected void applyOppDamage(Pokemon pokemon, double v) {
        pokemon.setMod(Stat.HP, (int) Math.round(v));
    }

    protected String describe() {
        return "Применил Dragon Claw";
    }
}
