package Modele;

import Structures.SequenceListe;
import java.util.HashMap;

public class PoidsChemins {
    private int poids;
    private SequenceListe<Position> chemins;

    public PoidsChemins(int poids, SequenceListe<Position> chemins) {
        this.poids = poids;
        this.chemins = chemins;
    }

    public PoidsChemins(SequenceListe<Position> cheminPrecedent, PoidsChemins PoidsCheminActuel) {
        this.poids = PoidsCheminActuel.getPoids();
        this.chemins = new SequenceListe<>();
        SequenceListe<Position> ancienChemin = new SequenceListe<>();
        ancienChemin = cheminPrecedent;
        while (!ancienChemin.estVide()) {
            this.chemins.insereQueue(ancienChemin.extraitTete());
        }
        SequenceListe<Position> cheminActuel = PoidsCheminActuel.getChemins();
        while(cheminActuel != null && !cheminActuel.estVide()) {
            this.chemins.insereQueue(cheminActuel.extraitTete());
        }
    }

    public int getPoids() {
        return poids;
    }

    public SequenceListe<Position> getChemins() {
        return chemins;
    }
}

