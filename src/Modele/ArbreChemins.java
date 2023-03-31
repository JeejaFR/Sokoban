package Modele;

import Structures.SequenceListe;

public class ArbreChemins implements Comparable<ArbreChemins>{
    private Instance courant;
    private ArbreChemins pere;
    private SequenceListe<Position> chemin;
    private int poids;

    public ArbreChemins(Instance courant, SequenceListe<Position> chemin, ArbreChemins pere, int p){
        this.courant = courant;
        this.chemin = chemin;
        this.pere = pere;
        this.poids = p;
    }

    public int getPoids(){
        return poids;
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

    @Override
    public int compareTo(ArbreChemins o) {
        return Integer.compare(o.getPoids(), this.getPoids());
    }
}
