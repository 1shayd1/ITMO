package ProgaLabs.Lab2.Skills;

import ru.ifmo.se.pokemon.*;

public class ChargeBeam extends SpecialMove{
    public ChargeBeam(){
        super(Type.ELECTRIC,50,90);
    }

    protected void applyOppDamage(Pokemon pokemon, double v) {
        pokemon.setMod(Stat.HP, (int) Math.round(v));
    }

    protected void applySelfEffects(Pokemon pokemon) {
        if (Math.random() <= 0.7){
            pokemon.setMod(Stat.SPECIAL_ATTACK, +1);
        }
    }

    protected String describe(){
        return "Применил Charge Beam";
    }
}

