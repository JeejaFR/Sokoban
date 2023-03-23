package Modele;

import Global.Configuration;
import Structures.Sequence;
import Structures.SequenceListe;
import Structures.FAPListe;
import java.util.*;

import static Modele.Niveau.*;

class IAResolveur extends IA {
    private HashMap<String, byte[][]> instancesPossibles;
    private int[][] carte;
    private byte[][] caisses;
    private PositionPoids posPousseur;
    private int l, c;
    private int nb_buts, nb_caisses, nb_caisses_sur_but;
    private int profondeur = 0, nb_instances = 0, nb_instances_pareilles = 0;
    // Couleurs au format RGB (rouge, vert, bleu, un octet par couleur)
    final static int VERT = 0x00CC00;
    final static int MARRON = 0xBB7755;
    private static final int INFINI = Integer.MAX_VALUE;

    public IAResolveur() {

    }
    public void niveauToCarte(Niveau n){
        int[][] cases = n.getCases();
        l = n.lignes()-2;
        c = n.colonnes()-2;
        carte = new int[l][c];
        caisses = new byte[l][c];
        //supprime la 1ere ligne, la dernière ligne, la 1ere colonne, la dernière colonne de cases
        for(int i = 1; i < l+1; i++) {
            for (int j = 1; j < c + 1; j++) {
                if ((cases[i][j] & MUR) != 0 || (cases[i][j] & VIDE) != 0) {
                    carte[i - 1][j - 1] = cases[i][j];
                }else if((cases[i][j] & BUT) != 0){
                    carte[i - 1][j - 1] = cases[i][j];
                    this.nb_buts++;
                } else if ((cases[i][j] & CAISSE) != 0) {
                    carte[i - 1][j - 1] = VIDE;
                    this.nb_caisses++;
                    this.caisses[i - 1][j - 1] = CAISSE;
                } else if ((cases[i][j] & POUSSEUR) != 0) {
                    carte[i - 1][j - 1] = VIDE;
                    posPousseur = new PositionPoids(i - 1, j - 1, 0);
                }
            }
        }
    }

    @Override
    public Sequence<Coup> joue() {
        Sequence<Coup> resultat = Configuration.nouvelleSequence();
        Coup coup = null;
        boolean mur = true;
        int dL = 0, dC = 0;
        int nouveauL = 0;
        int nouveauC = 0;
        instancesPossibles = new HashMap<>();
        nb_instances = 0;
        profondeur = 0;
        nb_instances_pareilles = 0;
        nb_buts = 0;
        nb_caisses = 0;
        niveauToCarte(niveau);

        PoidsChemins poidsChemins = calcul_chemin(posPousseur, caisses, 0);
        SequenceListe<Position> chemins = poidsChemins.getChemins();

        if(chemins != null && !chemins.estVide()){
            if(!chemins.estVide()){//on supprime la première case qui contient la position actuelle du pousseur
                chemins.extraitTete();
            }
            while(!chemins.estVide()){
                Position pos = chemins.extraitTete();
                //System.out.println(pos.affiche());
                int dl = pos.getL() - posPousseur.getL();
                int dc = pos.getC() - posPousseur.getC();
                coup = niveau.deplace(dl, dc);
                resultat.insereQueue(coup);
                posPousseur = new PositionPoids(pos.getL(), pos.getC(), 0);
                //si on met une pause ici, le pousseur reste figé et bouge uniquement à la toute fin.
            }
        }
        return resultat;
    }

    public PoidsChemins calcul_chemin(PositionPoids posPousseur, byte[][] caisses, int nb_caisses_sur_but){
        /*System.out.println("//////////// en bas ////////////////");
        afficheCaisses(caisses);
        System.out.println("//////////// en haut ////////////////");*/
        profondeur++;
        //System.out.println("Profondeur : " + profondeur);
        //System.out.println("Nb instances : " + nb_instances);
        //System.out.println("Nb instances pareilles : " + nb_instances_pareilles);
        PositionPoids posPousseurPossible;
        Position posCourante = null;
        SequenceListe<Position> chemin = new SequenceListe<Position>();
        SequenceListe<SequenceListe<Position>> chemins = Dijkstra(posPousseur, caisses);
        //System.out.println("Chemins possibles : " + chemins.getTaille());
        int k = 1;
        while(!chemins.estVide()){
            SequenceListe<Position> cheminCourant = chemins.extraitTete();//chemin k
            //System.out.println("Chemin numéro "+k+" : " + cheminCourant.getTaille());
            while(!cheminCourant.estVide()){
                posCourante = cheminCourant.extraitTete();//dernière position du chemin k
                chemin.insereQueue(posCourante);
            }
            //nivOriginal.setCroix(posCourante.getL()+1, posCourante.getC()+1);
            //System.out.println("Chemin "+k+" Position courante : " + posCourante.affiche());
            k++;
            posPousseurPossible = new PositionPoids(posCourante.getL(), posCourante.getC(), 0);
            //niveau.setCroix(posPousseurPossible.getL()+1, posPousseurPossible.getC()+1);
            SequenceListe<ArrayList<Position>> caissesDep = caissesDeplacables(posPousseurPossible, caisses);
            if(!caissesDep.estVide()){
                ArrayList<Position> caisseCouranteDep = caissesDep.extraitTete();
                if(nb_caisses_sur_but==2){
                    //System.out.println("2 buts atteints");
                    //afficheCaisses(caisses);
                }
                byte[][] caissesNew = pousserCaisse(caisseCouranteDep, caisses);
                PositionPoids posPousseurNew = new PositionPoids(caisseCouranteDep.get(0).getL(), caisseCouranteDep.get(0).getC(), 0);
                if(!estInstance(posPousseurNew.getPos(), caissesNew, instancesPossibles)){
                    nb_caisses_sur_but = nbCaisseSurBut(caisses);
                    if(!estBut(caisseCouranteDep.get(0)) && estBut(caisseCouranteDep.get(1))){
                        nb_caisses_sur_but++;
                        System.out.println("La caisse "+caisseCouranteDep.get(0).affiche()+" est maintenant sur le but en "+caisseCouranteDep.get(1).affiche());
                        System.out.println("Buts atteints : "+nb_caisses_sur_but);
                        System.out.println("position joueur : "+posCourante.l + " c: "+posPousseur.c);
                        afficheCaisses(caissesNew);
                        System.out.println("--------------------------------------------");
                    }else{
                        if(estBut(caisseCouranteDep.get(0)) && !estBut(caisseCouranteDep.get(1))){
                            nb_caisses_sur_but--;
                        }
                    }
                    ajouterInstance(posPousseurNew.getPos(), caissesNew, instancesPossibles);
                    if(nb_caisses_sur_but == nb_caisses){
                        chemin.insereQueue(posPousseurNew.getPos());
                        System.out.println("=========================== Toutes les caisses sont sur les buts ===========================");
                        afficheCaisses(caissesNew);
                        System.exit(0);
                        return new PoidsChemins(0, chemin);
                    }else{
                        //System.out.println("Toutes les caisses ne sont pas sur les buts");
                        PoidsChemins suivant = new PoidsChemins(chemin, calcul_chemin(posPousseurNew, caissesNew, nb_caisses_sur_but));
                        if(suivant.getPoids()==0){
                            return suivant;
                        }
                    }
                }else{
                    nb_instances_pareilles++;
                    //System.out.println("Instance déjà rencontrée");
                    //System.exit(0);
                }
            }
        }

        //System.out.println("Pas de chemin possible");
        return new PoidsChemins(1, null);
    }

    public boolean estBut(Position p){
        return (carte[p.l][p.c] & BUT) != 0;
    }

    public int nbCaisseSurBut(byte[][] caisses){
        int nbCaisseBut = 0;
        for(int i=0;i<caisses.length;i++){
            for(int j=0;j<caisses[0].length;j++){
                if(caisses[i][j] == CAISSE && carte[i][j]==BUT){
                    nbCaisseBut++;
                }
            }
        }
        return nbCaisseBut;
    }

    public byte[][] pousserCaisse(ArrayList<Position> caisse, byte[][] caisses){
        byte[][] caissesNew = copieByte(caisses);
        caissesNew[caisse.get(0).getL()][caisse.get(0).getC()] = VIDE;//on supprime la caisse de sa position actuelle
        caissesNew[caisse.get(1).getL()][caisse.get(1).getC()] = CAISSE;//on ajoute la caisse à sa nouvelle position
        return caissesNew;
    }

    public SequenceListe<ArrayList<Position>> caissesDeplacables(PositionPoids p, byte[][] caisses){
        //renvoie la liste des caisses déplaçables ainsi que leur future position une fois déplacées
        SequenceListe<ArrayList<Position>> caissesDep = new SequenceListe<>();
        ArrayList<Position> caisseDeplacee = new ArrayList<>();
        Position pCaisse;
        pCaisse = getPosCaisse(p.l+1, p.c, caisses);//si la caisse est en-dessous du pousseur
        if(pCaisse != null){
            if(!estCaseHorsMap(pCaisse.l+1, pCaisse.c) && estCaseLibre(pCaisse.l+1, pCaisse.c, caisses) && !estCaseBloquante(pCaisse.l+1, pCaisse.c, supprimeCaisse(pCaisse, caisses))){
                caisseDeplacee.add(pCaisse);
                caisseDeplacee.add(new Position(pCaisse.l+1, pCaisse.c));
                caissesDep.insereQueue(caisseDeplacee);
            }
        }
        pCaisse = null;
        pCaisse = getPosCaisse(p.l-1, p.c, caisses);//si la caisse est au-dessus du pousseur
        if(pCaisse != null){
            if(!estCaseHorsMap(pCaisse.l-1, pCaisse.c) && estCaseLibre(pCaisse.l-1, pCaisse.c, caisses) && !estCaseBloquante(pCaisse.l-1, pCaisse.c, supprimeCaisse(pCaisse, caisses))){
                caisseDeplacee.clear();
                caisseDeplacee.add(pCaisse);
                caisseDeplacee.add(new Position(pCaisse.l-1, pCaisse.c));
                caissesDep.insereQueue(caisseDeplacee);
            }
        }
        pCaisse = null;
        pCaisse = getPosCaisse(p.l, p.c+1, caisses);//si la caisse est à droite du pousseur
        if(pCaisse != null){
            if(!estCaseHorsMap(pCaisse.l, pCaisse.c+1) && estCaseLibre(pCaisse.l, pCaisse.c+1, caisses) && !estCaseBloquante(pCaisse.l, pCaisse.c+1, supprimeCaisse(pCaisse, caisses))){
                caisseDeplacee.clear();
                caisseDeplacee.add(pCaisse);
                caisseDeplacee.add(new Position(pCaisse.l, pCaisse.c+1));
                caissesDep.insereQueue(caisseDeplacee);
            }
        }
        pCaisse = null;
        pCaisse = getPosCaisse(p.l, p.c-1, caisses);//si la caisse est à gauche du pousseur
        if(pCaisse != null){
            if(!estCaseHorsMap(pCaisse.l, pCaisse.c-1) && estCaseLibre(pCaisse.l, pCaisse.c-1, caisses) && !estCaseBloquante(pCaisse.l, pCaisse.c-1, supprimeCaisse(pCaisse, caisses))){
                caisseDeplacee.clear();
                caisseDeplacee.add(pCaisse);
                caisseDeplacee.add(new Position(pCaisse.l, pCaisse.c-1));
                caissesDep.insereQueue(caisseDeplacee);
            }
        }
        return caissesDep;
    }

    public PositionPoids parcourtDistances(Position p, int[][] distance){
        int distSuivante = distance[p.l][p.c]-1;
        //System.out.println("Distance suivante : " + distSuivante);
        ArrayList<PositionPoids> casesAdjacentes = new ArrayList<>();
        PositionPoids posNord, posSud, posEst, posOuest;
        if(!estCaseHorsMap(p.l-1, p.c)) {
            posNord = new PositionPoids(p.l-1, p.c, distance[p.l-1][p.c]);
            if(posNord.poids == distSuivante){
                casesAdjacentes.add(posNord);
            }
        }
        if(!estCaseHorsMap(p.l+1, p.c)) {
            posSud = new PositionPoids(p.l+1, p.c, distance[p.l+1][p.c]);
            if(posSud.poids == distSuivante){
                casesAdjacentes.add(posSud);
            }
        }
        if(!estCaseHorsMap(p.l, p.c+1)) {
            posEst = new PositionPoids(p.l, p.c+1, distance[p.l][p.c+1]);
            if(posEst.poids == distSuivante){
                casesAdjacentes.add(posEst);
            }
        }
        if(!estCaseHorsMap(p.l, p.c-1)) {
            posOuest = new PositionPoids(p.l, p.c-1, distance[p.l][p.c-1]);
            if(posOuest.poids == distSuivante){
                casesAdjacentes.add(posOuest);
            }
        }
        if(casesAdjacentes.size() > 1) {
            Collections.shuffle(casesAdjacentes);
        }
        if(casesAdjacentes.size() == 0) {
            return null;
        }else{
            return casesAdjacentes.get(0);
        }
    }

    public SequenceListe<SequenceListe<Position>> Dijkstra(PositionPoids pousseur, byte[][] caisses){
        SequenceListe<Position> caissesAccessibles = new SequenceListe<>();
        int[][] distance = new int[l][c];
        for(int i = 0; i < distance.length; i++){
            for(int j = 0; j < distance[0].length; j++){
                distance[i][j] = INFINI;
            }
        }
        distance[pousseur.getL()][pousseur.getC()] = 0;
        boolean[][] visite = new boolean[l][c];
        for(int i = 0; i < visite.length; i++){
            for(int j = 0; j < visite[0].length; j++){
                visite[i][j] = false;
            }
        }
        visite[pousseur.getL()][pousseur.getC()] = true;
        FAPListe<PositionPoids> queue = new FAPListe<>();
        queue.insere(pousseur);

        while(!queue.estVide()){
            //extrait le sommet de distance minimale
            PositionPoids p = queue.extrait();
            if(estAdjacentCaisse(p, caisses)){
                //System.out.println("Position du pousseur : " + p.getL() + " " + p.getC());
                //System.out.println("Pousseur adjacent à au moins une caisse");
                caissesAccessibles.insereTete(new Position(p.getL(), p.getC()));
            }
            SequenceListe<PositionPoids> casesAccessibles = casesAccessibles(p, caisses);//renvoie les cases accessibles à côté du pousseur
            while(!casesAccessibles.estVide()){
                PositionPoids q = casesAccessibles.extraitTete();
                if(!visite[q.getL()][q.getC()]){//si la case accessible n'a pas été visitée
                    visite[q.getL()][q.getC()] = true;
                    distance[q.getL()][q.getC()] = distanceMin(distance,q.getL(),q.getC()) + 1;
                    queue.insere(q);
                }
            }
        }
        SequenceListe<SequenceListe<Position>> sequenceChemins = new SequenceListe<>();
        SequenceListe<Position> chemin = new SequenceListe<>();
        PositionPoids caseSuivante;
        Position tete = null;
        while(!caissesAccessibles.estVide()){
            tete = caissesAccessibles.extraitTete();
            chemin.insereTete(new Position(tete.getL(), tete.getC()));//la case à côté de la caisse est ajoutée au chemin courant
            caseSuivante = parcourtDistances(tete, distance);
            if(caseSuivante == null){
                sequenceChemins.insereTete(chemin);
                return sequenceChemins;
            }
            chemin.insereTete(new Position(caseSuivante.getL(), caseSuivante.getC()));
            while(caseSuivante.poids != 0){
                caseSuivante = parcourtDistances(new Position(caseSuivante.getL(), caseSuivante.getC()), distance);
                chemin.insereTete(new Position(caseSuivante.getL(), caseSuivante.getC()));
            }
            sequenceChemins.insereTete(chemin);
            chemin = new SequenceListe<>();
        }
        return sequenceChemins;
    }

    public boolean estAdjacentCaisse(PositionPoids p, byte[][] caisses){
        Position pCaisse;
        pCaisse = getPosCaisse(p.l+1, p.c, caisses);//si la caisse est en-dessous du pousseur
        if(pCaisse != null){
            //System.out.println("La caisse "+pCaisse.affiche()+" est en-dessous du pousseur qui est en "+p.affiche());
            //System.out.println("estCaseLibre : "+(!estCaseHorsMap(pCaisse.l+1, pCaisse.c)&&estCaseLibre(pCaisse.l+1, pCaisse.c, caisses)));
            //System.out.println("estCaseBloquante : "+estCaseBloquante(pCaisse.l+1, pCaisse.c, caisses));
            //System.out.println("estCaseHorsMap : "+estCaseHorsMap(pCaisse.l+1, pCaisse.c));
            if(!estCaseHorsMap(pCaisse.l+1, pCaisse.c) && estCaseLibre(pCaisse.l+1, pCaisse.c, caisses) && !estCaseBloquante(pCaisse.l+1, pCaisse.c, supprimeCaisse(pCaisse, caisses))){
                return true;
            }
        }
        pCaisse = null;
        pCaisse = getPosCaisse(p.l-1, p.c, caisses);//si la caisse est au-dessus du pousseur
        if(pCaisse != null){
            //System.out.println("La caisse "+pCaisse.affiche()+" est au-dessus du pousseur qui est en "+p.affiche());
            //System.out.println("estCaseLibre : "+(!estCaseHorsMap(pCaisse.l-1, pCaisse.c)&&estCaseLibre(pCaisse.l-1, pCaisse.c, caisses)));
            //System.out.println("estCaseBloquante : "+estCaseBloquante(pCaisse.l-1, pCaisse.c, caisses));
            //System.out.println("estCaseHorsMap : "+estCaseHorsMap(pCaisse.l-1, pCaisse.c));
            if(!estCaseHorsMap(pCaisse.l-1, pCaisse.c) && estCaseLibre(pCaisse.l-1, pCaisse.c, caisses) && !estCaseBloquante(pCaisse.l-1, pCaisse.c, supprimeCaisse(pCaisse, caisses))){
                return true;
            }
        }
        pCaisse = null;
        pCaisse = getPosCaisse(p.l, p.c+1, caisses);//si la caisse est à droite du pousseur
        if(pCaisse != null){
            //System.out.println("La caisse "+pCaisse.affiche()+" est à droite du pousseur qui est en "+p.affiche());
            //System.out.println("estCaseLibre : "+(!estCaseHorsMap(pCaisse.l, pCaisse.c+1)&&estCaseLibre(pCaisse.l, pCaisse.c+1, caisses)));
            //System.out.println("estCaseBloquante : "+estCaseBloquante(pCaisse.l, pCaisse.c+1, caisses));
            //System.out.println("estCaseHorsMap : "+estCaseHorsMap(pCaisse.l, pCaisse.c+1));
            if(!estCaseHorsMap(pCaisse.l, pCaisse.c+1) && estCaseLibre(pCaisse.l, pCaisse.c+1, caisses) && !estCaseBloquante(pCaisse.l, pCaisse.c+1, supprimeCaisse(pCaisse, caisses))){
                return true;
            }
        }
        pCaisse = null;
        pCaisse = getPosCaisse(p.l, p.c-1, caisses);//si la caisse est à gauche du pousseur
        if(pCaisse != null){
            //System.out.println("La caisse "+pCaisse.affiche()+" est à gauche du pousseur qui est en "+p.affiche());
            //System.out.println("estCaseLibre : "+(!estCaseHorsMap(pCaisse.l, pCaisse.c-1)&&estCaseLibre(pCaisse.l, pCaisse.c-1, caisses)));
            //System.out.println("estCaseBloquante : "+estCaseBloquante(pCaisse.l, pCaisse.c-1, caisses));
            //System.out.println("estCaseHorsMap : "+estCaseHorsMap(pCaisse.l, pCaisse.c-1));
            if(!estCaseHorsMap(pCaisse.l, pCaisse.c-1) && estCaseLibre(pCaisse.l, pCaisse.c-1, caisses) && !estCaseBloquante(pCaisse.l, pCaisse.c-1, supprimeCaisse(pCaisse, caisses))){
                return true;
            }
        }
        return false;
    }

    private byte[][] supprimeCaisse(Position pCaisse, byte[][] caisses) {
        byte [][] caisses2 = copieByte(caisses);
        caisses2[pCaisse.l][pCaisse.c] = VIDE;
        return caisses2;
    }

    public int distanceMin(int distance[][], int l, int c){
        int posNord, posSud, posEst, posOuest;
        if(l+1 > distance.length-1){
            posSud = INFINI;
            posNord = distance[l-1][c];
        }else{
            posSud = distance[l + 1][c];
            if (l - 1 < 0){
                posNord = INFINI;
            }else{
                posNord = distance[l - 1][c];
            }
        }
        if(c+1 > distance[0].length-1){
            posEst = INFINI;
            int dist =  distance[0].length-1;
            posOuest = distance[l][c-1];
        }else{
            posEst = distance[l][c+1];
            if(c-1 < 0){
                posOuest = INFINI;
            }else{
                posOuest = distance[l][c-1];
            }
        }
        return Math.min(Math.min(posNord, posSud), Math.min(posEst, posOuest));
    }

    public SequenceListe<PositionPoids> casesAccessibles(PositionPoids posCourante, byte[][] caisses){
        SequenceListe<PositionPoids> cases = new SequenceListe<>();
        if(posCourante.l+1 <= l-1 && estCaseLibre(posCourante.l+1,posCourante.c, caisses)){
            cases.insereTete(new PositionPoids(posCourante.l+1,posCourante.c, posCourante.poids+1));
        }
        if(posCourante.l-1 >= 0 && estCaseLibre(posCourante.l-1,posCourante.c, caisses)){
            cases.insereTete(new PositionPoids(posCourante.l-1,posCourante.c, posCourante.poids+1));
        }
        if(posCourante.c+1 <= c-1 && estCaseLibre(posCourante.l,posCourante.c+1, caisses)){
            cases.insereTete(new PositionPoids(posCourante.l,posCourante.c+1, posCourante.poids+1));
        }
        if(posCourante.c-1 >= 0 && estCaseLibre(posCourante.l,posCourante.c-1, caisses)){
            cases.insereTete(new PositionPoids(posCourante.l,posCourante.c-1, posCourante.poids+1));
        }
        cases.insereQueue(posCourante);//la case courante est accessible puisque le pousseur est déjà dessus
        return cases;
    }

    boolean estCaseHorsMap(int l, int c){
        return (l < 0 || l > this.l-1 || c < 0 || c > this.c-1);
    }

    boolean estCaseBloquante(int l, int c, byte[][] caisses){
        if(!estCaseHorsMap(l-1,c)&&!estCaseHorsMap(l,c-1)&&!estCaseHorsMap(l,c+1)&&!estCaseHorsMap(l+1,c)) {
            if((!estCaseLibre(l - 1, c, caisses) && (!estCaseLibre(l, c - 1, caisses) || !estCaseLibre(l, c + 1, caisses))) ||
                    (!estCaseLibre(l + 1, c, caisses) && (!estCaseLibre(l, c - 1, caisses) || !estCaseLibre(l, c + 1, caisses)))){
            }
        }
        return false;
    }

    public Position posDerriere(Position pousseur,Position caisse){
        int diff_l = caisse.l-pousseur.l;
        int diff_c = caisse.c-pousseur.c;
        return new Position(pousseur.l+diff_l*2, pousseur.c+diff_c*2);
    }

    public boolean estButDerriere(Position pousseur,Position caisse){
        return estBut(posDerriere(pousseur,caisse));
    }

    boolean estCaseLibre(int l, int c, byte[][] caisses){
        //afficheCaisse(caisses);
        if(!aMur(l,c) && getPosCaisse(l,c,caisses)==null){
            return true;
        }else{
            return false;
        }
    }

    boolean aMur(int l, int c) {
        return (carte[l][c] & MUR) != 0;
    }

    public byte[][] copieByte( byte[][] caisses){
        byte[][] caisses2 = new byte[caisses.length][caisses[0].length];
        for(int i=0;i<caisses2.length;i++){
            for(int j=0;j<caisses2[0].length;j++){
                caisses2[i][j] = caisses[i][j];
            }
        }
        return caisses2;
    }

    public void ajouterInstance(Position p, byte[][] caisses, HashMap<String, byte[][]> instances){
        nb_instances++;
        byte[][] instanceCopie = copieByte(caisses);
        instanceCopie[p.getL()][p.getC()] = POUSSEUR ;
        String posPousseur = p.getL() + "," + p.getC();
        int clePousseur = posPousseur.hashCode();
        int cleInstance = Arrays.deepHashCode(instanceCopie);
        int cle = clePousseur+cleInstance;
        //System.out.println("cle = " + cle);
        //afficheCaisses(instanceCopie);
        instances.put(String.valueOf(cle), instanceCopie);
    }

    public boolean estInstance(Position p, byte[][] caisses, HashMap<String, byte[][]> instances) {
        byte[][] instanceCopie = copieByte(caisses);
        instanceCopie[p.getL()][p.getC()] = POUSSEUR;
        String posPousseur = p.getL() + "," + p.getC();
        int clePousseur = posPousseur.hashCode();
        int cleInstance = Arrays.deepHashCode(instanceCopie);
        int cle = clePousseur+cleInstance;
        if(!instances.containsKey(String.valueOf(cle))){
            return false;
        }else{
            byte[][] instanceTrouvee = instances.get(String.valueOf(cle));
            for(int i = 0; i < instanceCopie.length; i++){
                for(int j = 0; j < instanceCopie[0].length; j++){
                    if(instanceCopie[i][j] != instanceTrouvee[i][j]){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    void afficheCaisses(byte[][] caisses){
        for(int i=0; i<caisses.length; i++){
            for(int j=0; j<caisses[0].length; j++){
                System.out.print(caisses[i][j] + " ");
            }
            System.out.println();
        }
    }

    public Position getPosCaisse(int l, int c, byte[][] caisses){
        if(estCaseHorsMap(l,c)) return null;
        if(caisses[l][c]==CAISSE){
            return new Position(l, c);
        }
        return null;
    }

    public void afficheDistances(int[][] distance){
        for(int i = 0; i < distance.length; i++){
            for(int j = 0; j < distance[0].length; j++){
                //si distance i j est égal à infini
                if(distance[i][j] == INFINI) {
                    System.out.print("+00 ");
                }else{
                    if(distance[i][j]<10) {
                        System.out.print(" " + distance[i][j] + "  ");
                    }else{
                        System.out.print(distance[i][j] + "  ");
                    }
                }
            }
            System.out.println();
        }
    }

    public void afficheCarte(int[][] carte){
        for (int i = 0; i < carte.length; i++) {
            for (int j = 0; j < carte[i].length; j++) {
                switch (carte[i][j]) {
                    case MUR:
                        System.out.print("#");
                        break;
                    case POUSSEUR:
                        System.out.print("@");
                        break;
                    case CAISSE:
                        System.out.print("$");
                        break;
                    case BUT:
                        System.out.print(".");
                        break;
                    case VIDE:
                        System.out.print(" ");
                        break;
                    default:
                        System.out.println("Erreur de lecture de la carte ligne " + i + " colonne " + j);
                        System.exit(1);
                }
            }
            System.out.println();
        }
    }
}
