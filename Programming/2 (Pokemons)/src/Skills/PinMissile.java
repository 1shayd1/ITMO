package ProgaLabs.Lab2.Skills;

import ru.ifmo.se.pokemon.*;

public class PinMissile extends PhysicalMove {
    public PinMissile() {
        super(Type.BUG, 25, 95);
    }

    protected void applyOppDamage(Pokemon def, double damage) {
        double rand = Math.random();
        if (rand < 0.375) {
            def.setMod(Stat.HP, (int) damage * 2);
        } else if (rand < 0.75) {
            def.setMod(Stat.HP, (int) damage * 3);
        } else if (rand < 0.875) {
            def.setMod(Stat.HP, (int) damage * 4);
        } else {
            def.setMod(Stat.HP, (int) damage * 5);
        }
    }

    protected String describe() {
        return "Применил Pin Missile";
    }
}
