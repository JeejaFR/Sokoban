package Modele;

import Global.Configuration;
import Structures.Sequence;
import Structures.SequenceListe;
import Structures.FAPListe;
import java.util.*;

import static Modele.Niveau.*;

class IAResolveur extends IA {
    private HashMap<String, byte[][]> instances;
    private int[][] carte;
    private byte[][] caisses;
    private Position posPousseur;
    private int l, c;
    private int nb_buts, nb_caisses;
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
                if (((cases[i][j] & MUR) != 0 || (cases[i][j] & VIDE) != 0)) {
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
                    posPousseur = new Position(i - 1, j - 1);
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
        instances = new HashMap<>();
        nb_instances = 0;
        nb_instances_pareilles = 0;
        nb_buts = 0;
        nb_caisses = 0;
        niveauToCarte(niveau);
        if(nb_buts!= nb_caisses){
            Configuration.erreur("Niveau impossible à résoudre : le nombre de caisses est différent du nombre de buts.");
            return null;
        }
        System.out.println("PosPousseur: " + posPousseur.affiche());
        ArrayList<ArbreChemins> chemins = calcul_chemin(posPousseur, caisses);
        System.out.println("chemins.size() : "+chemins.size());

        for(int i=0; i<chemins.size(); i++){
            SequenceListe<Position> chemin = chemins.get(i).getChemin();
            while(!chemin.estVide()){
                Position pos = chemin.extraitTete();
                System.out.println("pos: " + pos.affiche());
                /*
                int dl = pos.getL() - posPousseur.getL();
                int dc = pos.getC() - posPousseur.getC();
                coup = niveau.deplace(dl, dc);
                resultat.insereQueue(coup);
                posPousseur = pos;
                 */
            }
        }
        return resultat;
    }

    public ArrayList<ArbreChemins> calcul_chemin(Position posPousseur, byte[][] caisses){
        Position posCourante = null;
        ArrayList<ArbreChemins> chemin = new ArrayList<ArbreChemins>();
        SequenceListe<ArrayList<Position>> caissesDepl = new SequenceListe<ArrayList<Position>>();
        ArrayList<Position> caisseDeplCourante = new ArrayList<Position>();
        SequenceListe<Position> cheminCourant = new SequenceListe<Position>();
        LinkedList<ArbreChemins> queue = new LinkedList <ArbreChemins>();
        ArbreChemins arbreCourant = null;
        Instance instanceCourante = null;

        ajouterInstance(posPousseur, caisses, instances);
        Instance instanceDepart = new Instance(posPousseur, caisses);

        ArbreChemins cheminsTete = new ArbreChemins(instanceDepart, null, null);

        queue.add(cheminsTete);

        while(!queue.isEmpty()){
            cheminsTete = queue.poll();//ArbreChemins

            //récupère l'instance courante qui contient la position du pousseur et les caisses
            posPousseur = cheminsTete.getCourant().getPosPousseur();
            caisses = cheminsTete.getCourant().getCaisses();

            //récupère les chemins possibles pour le pousseur depuis l'instance courante
            CheminInstance cheminsPousseurCaisse = Dijkstra(posPousseur, caisses);

            //pour chaque chemin possible du pousseur à une caisse
            for(int i = 0; i < cheminsPousseurCaisse.getChemins().size(); i++){
                cheminCourant = cheminsPousseurCaisse.getChemins().get(i);//on récupère le chemin courant SequenceListe<Position>
                posPousseur = cheminCourant.extraitQueue();//dernière position du chemin courant
                caissesDepl = caissesDeplacables(posPousseur, caisses);//SequenceListe<ArrayList<Position>>

                while(!caissesDepl.estVide()){
                    caisseDeplCourante = caissesDepl.extraitTete();
                    byte[][] caissesNew = pousserCaisse(caisseDeplCourante, caisses);
                    Position posPousseurNew = caisseDeplCourante.get(0);//position de la caisse avant qu'elle soit poussée
                    if(!estInstance(posPousseurNew, caissesNew, instances)){
                        int nb_caisses_sur_but = nbCaissesSurBut(caissesNew);

                        if(nb_caisses_sur_but == nb_caisses){
                            System.out.println("=========================== Toutes les caisses sont sur les buts ===========================");
                            instanceCourante = new Instance(posPousseurNew, caissesNew);//instance de victoire
                            while(!instanceCourante.estInstance(instanceDepart)){
                                arbreCourant = new ArbreChemins(instanceCourante, cheminCourant, cheminsTete);
                                chemin.add(arbreCourant);
                                instanceCourante = arbreCourant.getPere().getCourant();
                                cheminCourant = arbreCourant.getPere().getChemin();
                            }
                            chemin.add(arbreCourant);
                            //inverse le chemin
                            ArrayList<ArbreChemins> cheminInverse = new ArrayList<ArbreChemins>();
                            for(int j = chemin.size()-1; j >= 0; j--){
                                cheminInverse.add(chemin.get(j));
                            }
                            return cheminInverse;
                        }else{
                            ajouterInstance(posPousseurNew, caissesNew, instances);
                            instanceCourante = new Instance(posPousseurNew, caissesNew);
                            queue.add(new ArbreChemins(instanceCourante, cheminCourant, cheminsTete));
                        }
                    }
                }//pas de solution pour ce chemin
            }
            //System.out.println("nb instances: " + nb_instances);
        }
        return chemin;
    }

    public boolean estBut(Position p){
        return (carte[p.l][p.c] & BUT) != 0;
    }

    public int nbCaissesSurBut(byte[][] caisses){
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

    public SequenceListe<ArrayList<Position>> caissesDeplacables(Position p, byte[][] caisses){
        //renvoie la liste des caisses déplaçables ainsi que leur future position une fois déplacées
        SequenceListe<ArrayList<Position>> caissesDep = new SequenceListe<>();
        ArrayList<Position> caisseDeplacee = new ArrayList<>();
        Position pCaisse;
        pCaisse = getPosCaisse(p.l+1, p.c, caisses);//si la caisse est en-dessous du pousseur
        if(pCaisse != null){
            if(!estCaseHorsMap(pCaisse.l+1, pCaisse.c) && estCaseLibre(pCaisse.l+1, pCaisse.c, caisses) && !estCaseBloquante_V2(pCaisse.l,pCaisse.c,pCaisse.l+1, pCaisse.c, supprimeCaisse(pCaisse, caisses))){
                caisseDeplacee.add(pCaisse);
                caisseDeplacee.add(new Position(pCaisse.l+1, pCaisse.c));
                caissesDep.insereQueue(caisseDeplacee);
            }
        }
        pCaisse = null;
        pCaisse = getPosCaisse(p.l-1, p.c, caisses);//si la caisse est au-dessus du pousseur
        if(pCaisse != null){
            if(!estCaseHorsMap(pCaisse.l-1, pCaisse.c) && estCaseLibre(pCaisse.l-1, pCaisse.c, caisses) && !estCaseBloquante_V2(pCaisse.l,pCaisse.c,pCaisse.l-1, pCaisse.c, supprimeCaisse(pCaisse, caisses))){
                caisseDeplacee.clear();
                caisseDeplacee.add(pCaisse);
                caisseDeplacee.add(new Position(pCaisse.l-1, pCaisse.c));
                caissesDep.insereQueue(caisseDeplacee);
            }
        }
        pCaisse = null;
        pCaisse = getPosCaisse(p.l, p.c+1, caisses);//si la caisse est à droite du pousseur
        if(pCaisse != null){
            if(!estCaseHorsMap(pCaisse.l, pCaisse.c+1) && estCaseLibre(pCaisse.l, pCaisse.c+1, caisses) && !estCaseBloquante_V2(pCaisse.l,pCaisse.c,pCaisse.l, pCaisse.c+1, supprimeCaisse(pCaisse, caisses))){
                caisseDeplacee.clear();
                caisseDeplacee.add(pCaisse);
                caisseDeplacee.add(new Position(pCaisse.l, pCaisse.c+1));
                caissesDep.insereQueue(caisseDeplacee);
            }
        }
        pCaisse = null;
        pCaisse = getPosCaisse(p.l, p.c-1, caisses);//si la caisse est à gauche du pousseur
        if(pCaisse != null){
            if(!estCaseHorsMap(pCaisse.l, pCaisse.c-1) && estCaseLibre(pCaisse.l, pCaisse.c-1, caisses) && !estCaseBloquante_V2(pCaisse.l,pCaisse.c,pCaisse.l, pCaisse.c-1, supprimeCaisse(pCaisse, caisses))){
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
        if(!estCaseHorsMap(p.l-1, p.c) && distance[p.l-1][p.c] == distSuivante) {
            posNord = new PositionPoids(p.l-1, p.c, distance[p.l-1][p.c]);
            casesAdjacentes.add(posNord);
        }
        if(!estCaseHorsMap(p.l+1, p.c) && distance[p.l+1][p.c] == distSuivante) {
            posSud = new PositionPoids(p.l+1, p.c, distance[p.l+1][p.c]);
            casesAdjacentes.add(posSud);
        }
        if(!estCaseHorsMap(p.l, p.c+1) && distance[p.l][p.c+1] == distSuivante) {
            posEst = new PositionPoids(p.l, p.c+1, distance[p.l][p.c+1]);
            casesAdjacentes.add(posEst);
        }
        if(!estCaseHorsMap(p.l, p.c-1) && distance[p.l][p.c-1] == distSuivante) {
            posOuest = new PositionPoids(p.l, p.c-1, distance[p.l][p.c-1]);
            casesAdjacentes.add(posOuest);
        }
        if(casesAdjacentes.size() > 1) {
            Collections.shuffle(casesAdjacentes);
        }
        if(casesAdjacentes.size() == 0) {
            return null;
        }else{//renvoie l'une des cases au hasard
            return casesAdjacentes.get(0);
        }
    }

    public CheminInstance Dijkstra(Position pos, byte[][] caisses){
        PositionPoids pousseur = new PositionPoids(pos.getL(), pos.getC(), 0);
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
        CheminInstance sequenceChemins = new CheminInstance();
        SequenceListe<Position> chemin = new SequenceListe<>();
        PositionPoids caseSuivante;
        Position tete = null;
        while(!caissesAccessibles.estVide()){
            tete = caissesAccessibles.extraitTete();
            chemin.insereTete(new Position(tete.getL(), tete.getC()));//la case à côté de la caisse est ajoutée au chemin courant
            caseSuivante = parcourtDistances(tete, distance);
            if(caseSuivante == null){
                //System.out.println("Aucune case accessible");
                //System.out.println("Position courante : " + tete.getL() + " " + tete.getC());
                //afficheDistances(distance);
                //System.exit(0);
                sequenceChemins.ajoutChemin(chemin);
                sequenceChemins.ajoutCaisses(caisses);
                return sequenceChemins;
            }
            chemin.insereTete(new Position(caseSuivante.getL(), caseSuivante.getC()));
            while(caseSuivante.poids != 0){
                caseSuivante = parcourtDistances(new Position(caseSuivante.getL(), caseSuivante.getC()), distance);
                chemin.insereTete(new Position(caseSuivante.getL(), caseSuivante.getC()));
            }
            sequenceChemins.ajoutChemin(chemin);//"this.cheminPousseurCaisses" is null
            sequenceChemins.ajoutCaisses(caisses);
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
            if(!estCaseHorsMap(pCaisse.l+1, pCaisse.c) && estCaseLibre(pCaisse.l+1, pCaisse.c, caisses) && !estCaseBloquante_V2(pCaisse.l,pCaisse.c,pCaisse.l+1, pCaisse.c, supprimeCaisse(pCaisse, caisses))){
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
            if(!estCaseHorsMap(pCaisse.l-1, pCaisse.c) && estCaseLibre(pCaisse.l-1, pCaisse.c, caisses) && !estCaseBloquante_V2(pCaisse.l,pCaisse.c,pCaisse.l-1, pCaisse.c, supprimeCaisse(pCaisse, caisses))){
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
            if(!estCaseHorsMap(pCaisse.l, pCaisse.c+1) && estCaseLibre(pCaisse.l, pCaisse.c+1, caisses) && !estCaseBloquante_V2(pCaisse.l,pCaisse.c,pCaisse.l, pCaisse.c+1, supprimeCaisse(pCaisse, caisses))){
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
            if(!estCaseHorsMap(pCaisse.l, pCaisse.c-1) && estCaseLibre(pCaisse.l, pCaisse.c-1, caisses) && !estCaseBloquante_V2(pCaisse.l,pCaisse.c,pCaisse.l, pCaisse.c-1, supprimeCaisse(pCaisse, caisses))){
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
        if(!aMur(l,c) && getPosCaisse(l,c,caisses)==null){
            return true;
        }else{
            return false;
        }
    }

    boolean aMur(int l, int c) {
        if(estCaseHorsMap(l,c)) return true;
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
        System.out.println("--------------------------------------");
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

    //////////////////////////////////////////////////////////////////////
    /////////////////////////// CASE BLOQUANTE ///////////////////////////
    //////////////////////////////////////////////////////////////////////

    public boolean aBut(int l, int c) {
        return (carte[l][c] & BUT) != 0;
    }

    public boolean aCaisseBloquee(int l, int c,byte[][] caisses){
        return (caisses[l][c] & CAISSE_BLOQUEE) != 0;
    }

    boolean aMurAutour(int l,int c){
        return aMur(l+1,c)||aMur(l-1,c)||aMur(l,c+1)||aMur(l,c-1);
    }
    boolean aBloqueeAutour(int l, int c){
        return aCaisseBloquee(l+1,c,caisses)||aCaisseBloquee(l-1,c,caisses)||aCaisseBloquee(l,c+1,caisses)||aCaisseBloquee(l,c-1,caisses);
    }
    boolean aAccesBut(int l, int c,byte[][] caisses){
        int i=0;
        boolean gauche=false,droite=false,bas=false,haut=false;
        while(!aMur(l,c+i) && droite==false && !aCaisseBloquee(l,c+i,caisses)) { // à droite
            if (!estCaseHorsMap(l, c - 1)) { // pas sur
                if (aBut(l, c + i) && carte[l][c - 1] != MUR) droite = true;
            }
            i++;
        }
        i=0;
        while(!aMur(l,c-i) && gauche==false && !aCaisseBloquee(l,c-i,caisses)){ // à gauche
            if(!estCaseHorsMap(l,c+1)){ // pas sur
                if(aBut(l,c-i) && carte[l][c+1]!=MUR) gauche = true;
            }
            i++;
        }
        i=0;
        while(!aMur(l-i,c) && haut==false && !aCaisseBloquee(l-i,c,caisses)){ // en haut
            if(!estCaseHorsMap(l+1,c)){ // pas sur
                if(aBut(l-i,c) && carte[l+1][c]!=MUR) haut = true;
            }
            i++;
        }
        i=0;
        while(!aMur(l+i,c) && droite==false && !aCaisseBloquee(l+i,c,caisses)){ // en bas
            if(!estCaseHorsMap(l-1,c)){// pas sur
                if(aBut(l+i,c) && carte[l-1][c]!=MUR) bas = true;
            }
            i++;
        }
        i=0;
        return gauche||droite||bas||haut;
    }
    boolean bloqueeVerticalDroit(int l,int c, int nord, byte[][] caisses){
        if(aAccesBut(l,c,caisses)) return false;
        if(aMur(l,c)) return true;
        int i=0;
        if(nord==1){
            while(!aMur(l,c+i) && !aCaisseBloquee(l,c+i,caisses)){
                if(estCaseHorsMap(l,c-1) || estCaseHorsMap(l+1,c+i)) return true;
                if(estCaseHorsMap(l-1,c+i)){
                    if(aBut(l,c+i) && carte[l][c-1]!=MUR && carte[l+1][c+i]!=MUR) return false;
                }else{
                    if((estCaseLibre(l-1,c+i,caisses)|| aBut(l,c+i)||caisses[l-1][c+i]==CAISSE)&& carte[l][c-1]!=MUR && carte[l+1][c+i]!=MUR) return false;
                }
                i++;
            }
            return true;
        }else{
            while(!aMur(l,c+i) && !aCaisseBloquee(l,c+i,caisses)){
                if(estCaseHorsMap(l,c-1) || estCaseHorsMap(l-1,c+i)) return true;
                if(estCaseHorsMap(l+1,c+i)){
                    if(aBut(l,c+i) && carte[l][c-1]!=MUR && carte[l-1][c+i]!=MUR) return false;
                }
                else{
                    if((estCaseLibre(l+1,c+i,caisses)|| aBut(l,c+i)||caisses[l+1][c+i]==CAISSE)&& carte[l][c-1]!=MUR && carte[l-1][c+i]!=MUR) return false;
                }
                i++;
            }
            return true;
        }
    }
    boolean bloqueeVerticalGauche(int l,int c, int nord,byte[][] caisses){
        int i=0;
        if(aAccesBut(l,c,caisses)) return false;
        if(aMur(l,c)) return true;
        if(nord==1){
            while(!aMur(l,c-i) && !aCaisseBloquee(l,c-i,caisses)){
                if(estCaseHorsMap(l,c+1) || estCaseHorsMap(l+1,c-i)) return true;
                if(estCaseHorsMap(l-1,c-i)){
                    if(aBut(l,c-i) && carte[l][c+1]!=MUR && carte[l+1][c-i]!=MUR) return false;
                }else{
                    if((estCaseLibre(l-1,c-i,caisses)|| aBut(l,c-i)||caisses[l-1][c-i]==CAISSE)&& carte[l][c+1]!=MUR && carte[l+1][c-i]!=MUR) return false;
                }
                i++;
            }
            return true;
        }else{
            while(!aMur(l,c-i) && !aCaisseBloquee(l,c-i,caisses)){
                if(estCaseHorsMap(l,c+1) || estCaseHorsMap(l-1,c-i)) return true;
                if(estCaseHorsMap(l+1,c-i)){
                    if(aBut(l,c-i) && carte[l][c+1]!=MUR && carte[l-1][c-i]!=MUR) return false;
                }else{
                    if((estCaseLibre(l+1,c-i,caisses) || aBut(l,c-i)||caisses[l+1][c-i]==CAISSE)&& carte[l][c+1]!=MUR && carte[l-1][c-i]!=MUR) return false;
                }
                i++;
            }
            return true;
        }
    }
    boolean bloqueeHorizontalHaut(int l, int c, int ouest,byte[][] caisses){
        int i=0;
        if(aAccesBut(l,c,caisses)) return false;
        if(aMur(l,c)) return true;
        if(ouest==1){
            while(!aMur(l-i,c) && !aCaisseBloquee(l-i,c,caisses)){
                if(estCaseHorsMap(l+1,c) || estCaseHorsMap(l-i,c+1)) return true;
                if(estCaseHorsMap(l-i,c-1)){
                    if(aBut(l-i,c) && carte[l+1][c]!=MUR && carte[l-i][c+1]!=MUR) return false;
                }else{
                    if((estCaseLibre(l-i,c-1,caisses)||aBut(l-i,c)||caisses[l-i][c-1]==CAISSE) && carte[l+1][c]!=MUR && carte[l-i][c+1]!=MUR) return false;
                }
                i++;
            }
            return true;
        }else{
            while(!aMur(l-i,c) && !aCaisseBloquee(l-i,c,caisses)){
                if(estCaseHorsMap(l+1,c) || estCaseHorsMap(l-i,c-1)) return true;
                if(estCaseHorsMap(l-i,c+1)){
                    if(aBut(l-i,c) && carte[l+1][c]!=MUR && carte[l-i][c-1]!=MUR) return false;
                }else{
                    if((estCaseLibre(l-i,c+1,caisses) || aBut(l-i,c)||caisses[l-i][c+1]==CAISSE)&& carte[l+1][c]!=MUR && carte[l-i][c-1]!=MUR) return false;
                }
                i++;
            }
            return true;
        }
    }
    boolean bloqueeHorizontalBas(int l, int c, int ouest,byte[][] caisses){
        int i=0;
        if(aAccesBut(l,c,caisses)) return false;
        if(aMur(l,c)) return true;
        if(ouest==1){
            while(!aMur(l+i,c) && !aCaisseBloquee(l+i,c,caisses)){
                if(estCaseHorsMap(l-1,c) || estCaseHorsMap(l+i,c+1)) return true;
                if(estCaseHorsMap(l+i,c-1)){
                    if(aBut(l+i,c) && carte[l-1][c]!=MUR && carte[l+i][c+1]!=MUR) return false;
                }
                else{
                    if((estCaseLibre(l+i,c-1,caisses) || aBut(l+i,c)||caisses[l+i][c-1]==CAISSE)&& carte[l-1][c]!=MUR && carte[l+i][c+1]!=MUR) return false;
                }

                i++;
            }
            return true;
        }else{
            while(!aMur(l+i,c) && !aCaisseBloquee(l+i,c,caisses)){
                if(estCaseHorsMap(l-1,c) || estCaseHorsMap(l+i,c-1)) return true;
                if(estCaseHorsMap(l+i,c+1)){
                    if(aBut(l+i,c) && carte[l-1][c]!=MUR && carte[l+i][c-1]!=MUR) return false;
                }else{
                    if((estCaseLibre(l+i,c+1,caisses) || aBut(l+i,c)||caisses[l+i][c+1]==CAISSE)&& carte[l-1][c]!=MUR && carte[l+i][c-1]!=MUR) return false;
                }
                i++;
            }
            return true;
        }
    }
    boolean gestionPlusieurTemp(int l, int c,byte[][] caisses){
        if(estCaisseBloqueeTemp(l+1,c,caisses)&&!aMurAutour(l+1,c)) return false;
        if(estCaisseBloqueeTemp(l-1,c,caisses)&&!aMurAutour(l-1,c)) return false;
        if(estCaisseBloqueeTemp(l,c+1,caisses)&&!aMurAutour(l,c+1)) return false;
        if(estCaisseBloqueeTemp(l,c-1,caisses)&&!aMurAutour(l,c-1)) return false;

        return estCaisseBloqueeTemp(l,c+1,caisses) || estCaisseBloqueeTemp(l,c-1,caisses) || estCaisseBloqueeTemp(l+1,c,caisses) || estCaisseBloqueeTemp(l-1,c,caisses); // || cases[l][c+1]==CAISSE_BLOQUEE || cases[l][c-1]==CAISSE_BLOQUEE || cases[l+1][c]==CAISSE_BLOQUEE || cases[l-1][c]==CAISSE_BLOQUEE;
    }

    boolean estCaisseBloquee(int l, int c,byte[][] caisses){
        if(aMur(l,c) || estCaseLibre(l,c,caisses)) return false;
        if(!aBut(l,c)) {
            // CAS CAISSE TEMPORAIRE
            if(estCaisseBloqueeTemp(l,c,caisses)){
                if(!aMurAutour(l,c)) return false;//&&!aBloqueeAutour(l,c)) return false;
                return gestionPlusieurTemp(l,c,caisses);
            }

            // CAS CONDUITS
            if(aMur(l-1,c)&&aMur(l+1,c)) return (bloqueeVerticalDroit(l,c,0,caisses)||bloqueeVerticalDroit(l,c,1,caisses)) && (bloqueeVerticalGauche(l,c,0,caisses)||bloqueeVerticalGauche(l,c,1,caisses)); //conduit horizontale
            if(aMur(l,c-1)&&aMur(l,c+1)) return (bloqueeHorizontalBas(l,c,0,caisses)||bloqueeHorizontalBas(l,c,1,caisses)) && (bloqueeHorizontalHaut(l,c,0,caisses)||bloqueeHorizontalHaut(l,c,1,caisses)); //conduit verical

            // CAS GENERALS
            if(aMur(l-1,c)) return bloqueeVerticalDroit(l,c,1,caisses)&&bloqueeVerticalGauche(l,c,1,caisses); // mur dessus de la caisse
            if(aMur(l+1,c)) return bloqueeVerticalDroit(l,c,0,caisses)&&bloqueeVerticalGauche(l,c,0,caisses); // mur dessous de la caisse
            if(aMur(l,c-1)) return bloqueeHorizontalHaut(l,c,1,caisses)&&bloqueeHorizontalBas(l,c,1,caisses); // mur a gauche de la caisse
            if(aMur(l,c+1)) return bloqueeHorizontalHaut(l,c,0,caisses)&&bloqueeHorizontalBas(l,c,0,caisses); // mur a droite de la caisse
        }
        return false;
    }
    boolean estCaisseBloqueeTemp(int l,int c,byte[][] caisses){
        if(estCaseHorsMap(l,c)) return false;
        if(caisses[l][c]!=CAISSE && caisses[l][c]!=CAISSE_BLOQUEE_TEMP && carte[l][c]!=MUR && caisses[l][c]!=VIDE) return false;
        if(aMur(l,c)) return false;
        if(estCaseLibre(l,c,caisses))return false;
        if(!aBut(l,c)){
            if(!estCaseLibre(l-1,c,caisses) && (!estCaseLibre(l,c-1,caisses) || !estCaseLibre(l,c+1,caisses))){
                if(aMur(l-1,c) && (aMur(l,c-1)||(aMur(l,c+1)))) return false;
                return true;
            }
            if(!estCaseLibre(l+1,c,caisses) && (!estCaseLibre(l,c-1,caisses) || !estCaseLibre(l,c+1,caisses))){
                if(aMur(l+1,c) && (aMur(l,c-1)||(aMur(l,c+1)))) return false;
                return true;
            }
        }
        return false;
    }

    boolean estCaseBloquante_V2(int l_initial, int c_initial, int l, int c,byte[][] caisses){
        if(aMur(l,c)) return true;

        byte[][] saveCaisses = caisses;
        //System.out.println("l: "+l+" c: "+c+" cases[l][c]: "+ cases[l][c]);
        saveCaisses[l][c] = CAISSE;
        saveCaisses[l_initial][c_initial] = VIDE;
        actualiseToutesCaisses(saveCaisses);

        if(estCaisseBloquee(l,c,saveCaisses)){
            return true;
        }
        return false;
    }
    public boolean aCaisse(int l, int c,byte[][] caisses){
        return (caisses[l][c] & CAISSE) != 0  || (caisses[l][c] & CAISSE_BLOQUEE_TEMP) != 0; // || (cases[l][c] & CAISSE_BLOQUEE) != 0;
    }
    void actualiseUneCaisse(int l, int c,byte[][] caisses){
        if (aCaisse(l,c,caisses)){
            //if(cases[l][c]==CAISSE_BLOQUEE) return;
            if(aBut(l,c)){
                return;
            }
            if (estCaisseBloquee(l,c,caisses)){
                caisses[l][c] = CAISSE_BLOQUEE; //FAUDRA TERMINER L'INSTANCE ! A FAIRE
                return;
            }
            if (estCaisseBloqueeTemp(l,c,caisses)) {
                caisses[l][c] = CAISSE_BLOQUEE_TEMP;
                return;
            }
            caisses[l][c] = CAISSE;
        }
    }

    public void actualiseToutesCaisses(byte[][] caisses){
        for(int l=0;l<caisses.length;l++){
            for(int c=0;c<caisses[0].length;c++){
                actualiseUneCaisse(l,c,caisses);
            }
        }
    }
    void actualiseCaisses(int l, int c,byte[][] caisses){
        actualiseUneCaisse(l+1,c,caisses);
        actualiseUneCaisse(l-1,c,caisses);
        actualiseUneCaisse(l,c+1,caisses);
        actualiseUneCaisse(l,c-1,caisses);
    }






}
