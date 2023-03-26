package Modele;

import Structures.SequenceListe;

import java.util.ArrayList;

public class CheminInstance {

    private ArrayList<SequenceListe<Position>> cheminPousseurCaisses;
    private byte[][] caisses;

    public CheminInstance(){
        cheminPousseurCaisses = new ArrayList<SequenceListe<Position>>();
    }

    public void ajoutChemin(SequenceListe<Position> chemin){
        cheminPousseurCaisses.add(chemin);
    }

    public void ajoutCaisses(byte[][] caisses){
        this.caisses = caisses;
    }

    public ArrayList<SequenceListe<Position>> getChemins() {
        return cheminPousseurCaisses;
    }

    public void afficheChemins(){
        for(int i = 0; i < cheminPousseurCaisses.size(); i++){
            System.out.println("Chemin "+i);
            cheminPousseurCaisses.get(i).toString();
        }
    }

    public byte[][] getCaisses() {
        return caisses;
    }

    public int getNbChemins() {
        return cheminPousseurCaisses.size();
    }
}
