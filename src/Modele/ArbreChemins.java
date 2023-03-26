package Modele;

public class ArbreChemins {
    private Instance courant;
    private ArbreChemins pere;

    public ArbreChemins(Instance courant, ArbreChemins pere){
        this.courant = courant;
        this.pere = pere;
    }

    public Instance getCourant(){
        return courant;
    }

    public ArbreChemins getPere(){
        return pere;
    }
}
