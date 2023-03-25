package Modele;

import Structures.SequenceListe;

import java.util.ArrayList;

public class CheminInstance {

    private ArrayList<SequenceListe<Position>> cheminPousseurCaisses;
    private byte[][] caisses;

    public CheminInstance(){}

    public void ajoutChemin(SequenceListe<Position> chemin){
        cheminPousseurCaisses.add(chemin);
    }

    public void ajoutCaisses(byte[][] caisses){
        this.caisses = caisses;
    }

    public ArrayList<SequenceListe<Position>> getChemins() {
        return cheminPousseurCaisses;
    }

    public byte[][] getCaisses() {
        return caisses;
    }

    public int getNbChemins() {
        return cheminPousseurCaisses.size();
    }
}
