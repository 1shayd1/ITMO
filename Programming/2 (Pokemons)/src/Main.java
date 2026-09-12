package ProgaLabs.Lab2;

import ProgaLabs.Lab2.Pokemons.*;
import ru.ifmo.se.pokemon.*;

public class Main {
    public static void main(String[] args) {

        Battle b = new Battle();
        Togedemaru togedemaru = new Togedemaru("Gucci Mane", 1);
        Cacnea cacnea = new Cacnea("Eminem", 1);
        Cacturne cacturne = new Cacturne("A$AP Rocky", 1);
        Flabebe flabebe = new Flabebe("Snoop Dogg", 1);
        Floette floette = new Floette("2-Pac", 1);
        Florges florges = new Florges("Jay-Z", 1);

        b.addAlly(togedemaru);
        b.addAlly(cacnea);
        b.addAlly(cacturne);

        b.addFoe(flabebe);
        b.addFoe(floette);
        b.addFoe(florges);

        b.go();
    }
}