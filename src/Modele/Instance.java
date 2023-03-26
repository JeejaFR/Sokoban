package Modele;

public class Instance {

    Position posPousseur;
    byte[][] caisses;

    public Instance(Position posPousseur, byte[][] caisses){
        this.posPousseur = posPousseur;
        this.caisses = new byte[caisses.length][caisses[0].length];
        for(int i = 0; i < caisses.length; i++){
            for(int j = 0; j < caisses[0].length; j++){
                this.caisses[i][j] = caisses[i][j];
            }
        }
    }

    public Position getPosPousseur() {
        return posPousseur;
    }

    public byte[][] getCaisses() {
        return caisses;
    }

    public boolean estInstance(Instance in) {
        if (!posPousseur.equals(in.getPosPousseur())) {
            return false;
        }
        for (int i = 0; i < caisses.length; i++) {
            for (int j = 0; j < caisses[0].length; j++) {
                if (caisses[i][j] != in.getCaisses()[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
