package Modele;

import Structures.SequenceListe;

public class ArbreChemins {
    private Instance courant;
    private ArbreChemins pere;
    private SequenceListe<Position> chemin;

    public ArbreChemins(Instance courant, SequenceListe<Position> chemin, ArbreChemins pere){
        this.courant = courant;
        this.chemin = chemin;
        this.pere = pere;
    }

    public Instance getCourant(){
        return courant;
    }

    public SequenceListe<Position> getChemin(){
        return chemin;
    }

    public ArbreChemins getPere(){
        return pere;
    }
}
