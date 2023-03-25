package Modele;

public class ArbreChemins {
    private CheminInstance courant;
    private ArbreChemins pere;

    public ArbreChemins(CheminInstance courant, ArbreChemins pere){
        this.courant = courant;
        this.pere = pere;
    }

    public CheminInstance getCourant(){
        return courant;
    }

    public ArbreChemins getPere(){
        return pere;
    }
}
