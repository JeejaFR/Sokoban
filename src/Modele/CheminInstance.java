package Modele;

import Structures.SequenceListe;

import java.util.ArrayList;

public class CheminInstance {

    private SequenceListe<SequenceListe<Position>> cheminPousseurCaisses;
    private byte[][] caisses;

    public CheminInstance(){
        cheminPousseurCaisses = new SequenceListe<SequenceListe<Position>>();
    }

    public void ajoutCheminQueue(SequenceListe<Position> chemin){
        cheminPousseurCaisses.insereQueue(chemin);
    }

    public void ajoutCheminTete(SequenceListe<Position> chemin){
        cheminPousseurCaisses.insereTete(chemin);
    }

    public void ajoutCaisses(byte[][] caisses){
        this.caisses = caisses;
    }

    public SequenceListe<SequenceListe<Position>> getChemins() {
        return cheminPousseurCaisses;
    }

    public byte[][] getCaisses() {
        return caisses;
    }

    public int getNbChemins() {
        return cheminPousseurCaisses.taille();
    }
}
