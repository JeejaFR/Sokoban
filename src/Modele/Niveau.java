package Modele;
import Global.Configuration;

public class Niveau extends Historique<Coup> implements Cloneable {
	static final int VIDE = 0;
	static final int MUR = 1;
	static final int POUSSEUR = 2;
	static final int CAISSE = 4;
	static final int BUT = 8;
	public static final int CAISSE_BLOQUEE = 16;
	public static final int CAISSE_BLOQUEE_TEMP = 32;
	static final int CROIX = 128;
	int l, c;
	int[][] cases;
	int[][] cases_originales;
	String nom;
	int pousseurL, pousseurC;
	int nbButs;
	int nbCaissesSurBut;
	int nbPas, nbPoussees;

	Niveau() {
		cases = new int[1][1];
		l = c = 1;
		pousseurL = pousseurC = -1;
	}

	int ajuste(int cap, int objectif) {
		while (cap <= objectif) {
			cap = cap * 2;
		}
		return cap;
	}

	void redimensionne(int nouvL, int nouvC) {
		int capL = ajuste(cases.length, nouvL);
		int capC = ajuste(cases[0].length, nouvC);
		if ((capL > cases.length) || (capC > cases[0].length)) {
			int[][] nouvelles = new int[capL][capC];
			for (int i = 0; i < cases.length; i++)
				for (int j = 0; j < cases[0].length; j++)
					nouvelles[i][j] = cases[i][j];
			cases = nouvelles;
		}
		if (nouvL >= l)
			l = nouvL + 1;
		if (nouvC >= c)
			c = nouvC + 1;
	}

	int[][] getCases() {
		return cases;
	}

	public int getNbCaissesSurBut() {
		return nbCaissesSurBut;
	}

	public int getNbButs() {
		return nbButs;
	}

	void fixeNom(String s) {
		nom = s;
	}

	void videCase(int i, int j) {
		redimensionne(i, j);
		cases[i][j] = VIDE;
	}

	void supprime(int contenu, int i, int j) {
		if (aBut(i, j)) {
			if (aCaisse(i, j) && ((contenu & CAISSE | contenu & BUT) != 0))
				nbCaissesSurBut--;
			if ((contenu & BUT) != 0)
				nbButs--;
		}
		if (aPousseur(i, j) && ((contenu & POUSSEUR) != 0))
			pousseurL = pousseurC = -1;
		cases[i][j] &= ~contenu;
	}

	void ajoute(int contenu, int i, int j) {
		redimensionne(i, j);
		int resultat = cases[i][j] | contenu;
		if ((resultat & BUT) != 0) {
			if (((resultat & CAISSE) != 0) && (!aCaisse(i, j) || !aBut(i, j)))
				nbCaissesSurBut++;
			if (!aBut(i, j))
				nbButs++;
		}
		if (((resultat & POUSSEUR) != 0) && !aPousseur(i, j)) {
			if (pousseurL != -1)
				throw new IllegalStateException("Plusieurs pousseurs sur le terrain !");
			pousseurL = i;
			pousseurC = j;
		}
		cases[i][j] = resultat;
	}

	int contenu(int i, int j) {
		return cases[i][j] & (POUSSEUR | CAISSE );// | CAISSE_BLOQUEE_TEMP | CAISSE_BLOQUEE);  // A MODIFIER ICI SI ON VEUT POUVOIR POUSSER LES CAISSES BLOQUEES
	}

	int decompteMouvement(Mouvement m) {
		if (m != null)
			return m.decompte();
		else
			return 0;
	}

	void decomptes(Coup cp) {
		nbPas += decompteMouvement(cp.pousseur());
		nbPoussees += decompteMouvement(cp.caisse());
	}

	@Override
	public void faire(Coup cp) {
		cp.fixeNiveau(this);
		decomptes(cp);
		super.faire(cp);
		if(cp.caisse!=null){
			actualiseToutesCaisses();
		}
	}

	@Override
	public Coup annuler() {
		Coup cp = super.annuler();
		decomptes(cp);
		if(cp.caisse!=null){
			actualiseToutesCaisses();
		}
		return cp;
	}

	@Override
	public Coup refaire() {
		Coup cp = super.refaire();
		decomptes(cp);
		if(cp.caisse!=null){
			actualiseToutesCaisses();
		}
		return cp;
	}

	public Coup elaboreCoup(int dLig, int dCol) {
		int destL = pousseurL + dLig;
		int destC = pousseurC + dCol;
		Coup resultat = new Coup();

		if (aCaisse(destL, destC)) {
			int dCaisL = destL + dLig;
			int dCaisC = destC + dCol;

			if (estOccupable(dCaisL, dCaisC)) {
				resultat.deplacementCaisse(destL, destC, dCaisL, dCaisC);
			} else {
				return null;
			}
		}
		if (!aMur(destL, destC)) {
			resultat.deplacementPousseur(pousseurL, pousseurC, destL, destC);
			return resultat;
		}
		return null;
	}

	Coup deplace(int i, int j) {
		Coup cp = elaboreCoup(i, j);
		if (cp != null)
			faire(cp);
		return cp;
	}

	void ajouteMur(int i, int j) {
		ajoute(MUR, i, j);
	}

	void ajoutePousseur(int i, int j) {
		ajoute(POUSSEUR, i, j);
	}

	void ajouteCaisse(int i, int j) {
		ajoute(CAISSE, i, j);
	}

	void ajouteBut(int i, int j) {
		ajoute(BUT, i, j);
	}

	public int lignes() {
		return l;
	}

	public int colonnes() {
		return c;
	}

	public String nom() {
		return nom;
	}

	boolean estVide(int l, int c) {
		return cases[l][c] == VIDE;
	}

	public boolean aMur(int l, int c) {
		return (cases[l][c] & MUR) != 0;
	}

	public boolean aBut(int l, int c) {
		return (cases[l][c] & BUT) != 0;
	}

	public boolean aPousseur(int l, int c) {
		return (cases[l][c] & POUSSEUR) != 0;
	}

	public boolean aCaisse(int l, int c){
		return (cases[l][c] & CAISSE) != 0  || (cases[l][c] & CAISSE_BLOQUEE) != 0 || (cases[l][c] & CAISSE_BLOQUEE_TEMP) != 0;
	}

	public boolean aCaisseBloqueeTemp(int l, int c){
		return (cases[l][c] & CAISSE_BLOQUEE_TEMP) != 0;
	}

	public boolean estOccupable(int l, int c) {
		return (cases[l][c] & (MUR | CAISSE | POUSSEUR | CAISSE_BLOQUEE_TEMP | CAISSE_BLOQUEE)) == 0;
	}

	public boolean estTermine() {
		return nbCaissesSurBut == nbButs;
	}

	public int lignePousseur() {
		return pousseurL;
	}

	public int colonnePousseur() {
		return pousseurC;
	}

	// Par convention, la méthode clone de java requiert :
	// - que la classe clonée implémente Cloneable
	// - que le resultat soit construit avec la méthode clone de la classe parente (pour qu'un clonage
	//   profond fonctionne sur toute l'ascendence de l'objet)
	// Le nouvel objet sera de la même classe que l'objet cible du clonage (creation spéciale dans Object)
	@Override
	public Niveau clone() {
		try {
			Niveau resultat = (Niveau) super.clone();
			// Le clone de base est un clonage à plat pour le reste il faut
			// cloner à la main : cela concerne les cases
			resultat.cases = new int[cases.length][];
			for (int i=0; i< cases.length; i++)
				resultat.cases[i] = cases[i].clone();
			return resultat;
		} catch (CloneNotSupportedException e) {
			Configuration.erreur("Bug interne, niveau non clonable");
		}
		return null;
	}

	public int marque(int i, int j) {
		return (cases[i][j] >> 8) & 0xFFFFFF;
	}

	public void fixerMarque(int m, int i, int j) {
		cases[i][j] = (cases[i][j] & 0xFF) | (m << 8);
	}

	public int nbPas() {
		return nbPas;
	}

	public int nbPoussees() {
		return nbPoussees;
	}

	public void setOriginal() {
		cases_originales = new int[l][c];
		for(int i=0; i<l; i++)
			for(int j=0; j<c; j++)
				cases_originales[i][j] = cases[i][j];
	}
	public void reinitialiseNiveau() {
		nbButs = 0;
		nbCaissesSurBut = 0;
		nbPas = 0;
		nbPoussees = 0;
		for(int i=0; i<l; i++) {
			for (int j = 0; j < c; j++) {
				cases[i][j] = cases_originales[i][j];
				if (aPousseur(i, j)) {
					pousseurL = i;
					pousseurC = j;
				}
				if(aBut(i, j)) {
					nbButs++;
				}
				if(aCaisse(i, j) && aBut(i, j)) {
					nbCaissesSurBut++;
				}
			}
		}
	}

	public void afficheNiveau(){
		for(int i=0; i<l; i++) {
			for (int j = 0; j < c; j++) {
				if(aMur(i, j)) {
					System.out.print("#");
				}
				else if(aPousseur(i, j) && aBut(i, j)) {
					System.out.print("+");
				}
				else if(aPousseur(i, j)) {
					System.out.print("@");
				}
				else if(aCaisse(i, j) && aBut(i, j)) {
					System.out.print("*");
				}
				else if(aCaisse(i, j)) {
					System.out.print("$");
				}
				else if(aBut(i, j)) {
					System.out.print(".");
				}
				else {
					System.out.print(" ");
				}
			}
			System.out.println();
		}
	}

	public void setCroix(int l, int c){
		cases[l][c] |= CROIX;
	}

	public boolean aCroix(int l, int c){
		return (cases[l][c] & CROIX) != 0;
	}


	// DETECTION CASE BLOQUEE

	public boolean aCaisseBloquee(int l, int c){
		return (cases[l][c] & CAISSE_BLOQUEE) != 0;
	}
	boolean estCaseLibre(int l, int c){
		return (!aMur(l,c) && !aCaisse(l,c)) || aPousseur(l,c);
	}
	boolean aMurAutour(int l,int c){
		return aMur(l+1,c)||aMur(l-1,c)||aMur(l,c+1)||aMur(l,c-1);
	}
	boolean aBloqueeAutour(int l, int c){
		return aCaisseBloquee(l+1,c)||aCaisseBloquee(l-1,c)||aCaisseBloquee(l,c+1)||aCaisseBloquee(l,c-1);
	}
	boolean aAccesBut(int l, int c){
		int i=0;
		boolean gauche=false,droite=false,bas=false,haut=false;
		while(!aMur(l,c+i) && droite==false && !aCaisseBloquee(l,c+i)){ // à droite
			if(aBut(l,c+i) && cases[l][c-1]!=MUR && cases[l][c-1]!=CAISSE_BLOQUEE) droite = true;
			i++;
		}
		i=0;
		while(!aMur(l,c-i) && gauche==false && !aCaisseBloquee(l,c-i)){ // à gauche
			if(aBut(l,c-i) && cases[l][c+1]!=MUR && cases[l][c+1]!=CAISSE_BLOQUEE) gauche = true;
			i++;
		}
		i=0;
		while(!aMur(l-i,c) && haut==false && !aCaisseBloquee(l-i,c)){ // en haut
			if(aBut(l-i,c) && cases[l+1][c]!=MUR && cases[l+1][c]!=CAISSE_BLOQUEE) haut = true;
			i++;
		}
		i=0;
		while(!aMur(l+i,c) && droite==false && !aCaisseBloquee(l+i,c)){ // en bas
			if(aBut(l+i,c) && cases[l-1][c]!=MUR && cases[l+i][c]!=CAISSE_BLOQUEE) bas = true;
			i++;
		}
		i=0;
		return gauche||droite||bas||haut;
	}

	boolean aCaisseNonBloquante(int l,int c){
		return (cases[l][c]==CAISSE || cases[l][c]==12 || cases[l][c]==CAISSE_BLOQUEE_TEMP);
	}
	boolean bloqueeVerticalDroit(int l,int c, int nord){
		if(aAccesBut(l,c)) return false;
		int i=0;
		if(nord==1){
			while(!aMur(l,c+i) && !aCaisseBloquee(l,c+i)){
				if((estCaseLibre(l-1,c+i)|| aBut(l,c+i)||aCaisseNonBloquante(l-1,c+i))&& cases[l][c-1]!=MUR && cases[l+1][c+i]!=MUR && cases[l+1][c+i]!=CAISSE_BLOQUEE){
					return false;
				}
				i++;
			}
			return true;
		}else{
			while(!aMur(l,c+i) && !aCaisseBloquee(l,c+i)){
				if((estCaseLibre(l+1,c+i)|| aBut(l,c+i)||aCaisseNonBloquante(l+1,c+i))&& cases[l][c-1]!=MUR && cases[l-1][c+i]!=MUR && cases[l-1][c+i]!=CAISSE_BLOQUEE){
					return false;
				}
				i++;
			}
			return true;
		}
	}
	boolean bloqueeVerticalGauche(int l,int c, int nord){
		int i=0;
		if(aAccesBut(l,c)) return false;
		if(nord==1){
			while(!aMur(l,c-i) && !aCaisseBloquee(l,c-i)){
				if((estCaseLibre(l-1,c-i)|| aBut(l,c-i)||aCaisseNonBloquante(l-1,c-i))&& cases[l][c+1]!=MUR && cases[l+1][c-i]!=MUR && cases[l+1][c-i]!=CAISSE_BLOQUEE){
					return false;
				}
				i++;
			}
			return true;
		}else{
			while(!aMur(l,c-i) && !aCaisseBloquee(l,c-i)){
				if((estCaseLibre(l+1,c-i) || aBut(l,c-i)||aCaisseNonBloquante(l+1,c-i))&& cases[l][c+1]!=MUR && cases[l-1][c-i]!=MUR && cases[l-1][c-i]!=CAISSE_BLOQUEE) return false;
				i++;
			}
			return true;
		}
	}
	boolean bloqueeHorizontalHaut(int l, int c, int ouest){
		int i=0;
		if(aAccesBut(l,c)) return false;
		if(ouest==1){
			while(!aMur(l-i,c) && !aCaisseBloquee(l-i,c)){
				if((estCaseLibre(l-i,c-1)||aBut(l-i,c)||aCaisseNonBloquante(l-i,c-1))&& cases[l+1][c]!=MUR && cases[l-i][c+1]!=MUR && cases[l-i][c+1]!=CAISSE_BLOQUEE) return false;
				i++;
			}
			return true;
		}else{
			while(!aMur(l-i,c) && !aCaisseBloquee(l-i,c)){
				if((estCaseLibre(l-i,c+1) || aBut(l-i,c)||aCaisseNonBloquante(l-i,c+1))&& cases[l+1][c]!=MUR && cases[l-i][c-1]!=MUR && cases[l-i][c-1]!=CAISSE_BLOQUEE) return false;
				i++;
			}
			return true;
		}
	}
	boolean bloqueeHorizontalBas(int l, int c, int ouest){
		int i=0;
		if(aAccesBut(l,c)) return false;
		if(ouest==1){
			while(!aMur(l+i,c) && !aCaisseBloquee(l+i,c)){
				if((estCaseLibre(l+i,c-1) || aBut(l+i,c)||aCaisseNonBloquante(l+i,c-1))&& cases[l-1][c]!=MUR && cases[l+i][c+1]!=MUR && cases[l+i][c+1]!=CAISSE_BLOQUEE) return false;
				i++;
			}
			return true;
		}else{
			while(!aMur(l+i,c) && !aCaisseBloquee(l+i,c)){
				if((estCaseLibre(l+i,c+1) || aBut(l+i,c)||aCaisseNonBloquante(l+i,c+1))&& cases[l-1][c]!=MUR && cases[l+i][c-1]!=MUR && cases[l+i][c-1]!=CAISSE_BLOQUEE) return false;
				i++;
			}
			return true;
		}
	}

	public boolean estCaseDisponible(int l, int c) {
		return (cases[l][c] & (MUR | CAISSE | CAISSE_BLOQUEE_TEMP | CAISSE_BLOQUEE)) == 0;
	}
	boolean estBloqueeEnCarre(int l,int c){
		//caisse en haut à droite
		if(!estCaseDisponible(l+1,c)&&!estCaseDisponible(l+1,c-1)&&!estCaseDisponible(l,c-1)) return true;
		//caisse en bas à droite
		if(!estCaseDisponible(l-1,c)&&!estCaseDisponible(l-1,c-1)&&!estCaseDisponible(l,c-1)) return true;
		//caisse en haut à gauche
		if(!estCaseDisponible(l+1,c)&&!estCaseDisponible(l+1,c+1)&&!estCaseDisponible(l,c+1)) return true;
		//caisse en bas à gauche
		if(!estCaseDisponible(l-1,c)&&!estCaseDisponible(l-1,c+1)&&!estCaseDisponible(l,c+1)) return true;

		return false;
	}

	boolean pourra_bouger_vertical(int l,int c){
		return ((cases[l-1][c]==VIDE||cases[l-1][c]==CAISSE)&&(cases[l+1][c]!=CAISSE_BLOQUEE&&cases[l+1][c]!=MUR))||((cases[l+1][c]==VIDE||cases[l+1][c]==CAISSE)&&(cases[l-1][c]!=CAISSE_BLOQUEE&&cases[l-1][c]!=MUR));
	}
	boolean pourra_bouger_horizontal(int l,int c){
		return ((cases[l][c-1]==VIDE||cases[l][c-1]==CAISSE)&&(cases[l][c+1]!=CAISSE_BLOQUEE&&cases[l][c+1]!=MUR))||((cases[l][c+1]==VIDE||cases[l][c+1]==CAISSE)&&(cases[l][c-1]!=CAISSE_BLOQUEE&&cases[l][c-1]!=MUR));
	}
	boolean gestionPlusieurTemp(int l, int c){
		if(estCaisseBloqueeTemp(l+1,c)&&(!aMurAutour(l+1,c)||((cases[l+1][c+1]==VIDE || cases[l+1][c+1]==CAISSE)&&(cases[l+1][c-1]==VIDE || cases[l+1][c-1]==CAISSE))||pourra_bouger_horizontal(l,c))) return false;
		if(estCaisseBloqueeTemp(l-1,c)&&(!aMurAutour(l-1,c)||((cases[l-1][c+1]==VIDE || cases[l-1][c+1]==CAISSE)&&(cases[l-1][c-1]==VIDE || cases[l-1][c-1]==CAISSE))||pourra_bouger_horizontal(l,c))) return false;
		if(estCaisseBloqueeTemp(l,c+1)&&(!aMurAutour(l,c+1)||((cases[l-1][c+1]==VIDE || cases[l-1][c+1]==CAISSE)&&(cases[l+1][c+1]==VIDE || cases[l+1][c+1]==CAISSE))||pourra_bouger_vertical(l,c))) return false;
		if(estCaisseBloqueeTemp(l,c-1)&&(!aMurAutour(l,c-1)||((cases[l+1][c-1]==VIDE || cases[l+1][c-1]==CAISSE)&&(cases[l-1][c-1]==VIDE || cases[l-1][c-1]==CAISSE))||pourra_bouger_vertical(l,c))) return false;

		return estCaisseBloqueeTemp(l,c+1) || estCaisseBloqueeTemp(l,c-1) || estCaisseBloqueeTemp(l+1,c) || estCaisseBloqueeTemp(l-1,c) || cases[l][c+1]==CAISSE_BLOQUEE || cases[l][c-1]==CAISSE_BLOQUEE || cases[l+1][c]==CAISSE_BLOQUEE || cases[l-1][c]==CAISSE_BLOQUEE;
	}


	boolean estCaisseBloquee(int l, int c){
		if(estBloqueeEnCarre(l,c)) return true;
		if(aMur(l,c) || estCaseLibre(l,c)) return false;
		if(!aBut(l,c)) {
			// CAS CAISSE TEMPORAIRE
			if(estCaisseBloqueeTemp(l,c)){
				if(!aMurAutour(l,c)&&!aBloqueeAutour(l,c)) return false;
				return gestionPlusieurTemp(l,c);
			}

			// CAS CONDUITS
			if(aMur(l-1,c)&&aMur(l+1,c)) return (bloqueeVerticalDroit(l,c,0)||bloqueeVerticalDroit(l,c,1)) && (bloqueeVerticalGauche(l,c,0)||bloqueeVerticalGauche(l,c,1)); //conduit horizontale
			if(aMur(l,c-1)&&aMur(l,c+1)) return (bloqueeHorizontalBas(l,c,0)||bloqueeHorizontalBas(l,c,1)) && (bloqueeHorizontalHaut(l,c,0)||bloqueeHorizontalHaut(l,c,1)); //conduit verical

			// CAS GENERALS
			if(aMur(l-1,c)) return bloqueeVerticalDroit(l,c,1)&&bloqueeVerticalGauche(l,c,1); // mur dessus de la caisse
			if(aMur(l+1,c)) return bloqueeVerticalDroit(l,c,0)&&bloqueeVerticalGauche(l,c,0); // mur dessous de la caisse
			if(aMur(l,c-1)) return bloqueeHorizontalHaut(l,c,1)&&bloqueeHorizontalBas(l,c,1); // mur a gauche de la caisse
			if(aMur(l,c+1)) return bloqueeHorizontalHaut(l,c,0)&&bloqueeHorizontalBas(l,c,0); // mur a droite de la caisse
		}
		return false;
	}
	boolean estCaisseBloqueeTemp(int l,int c){
		if(cases[l][c]!=CAISSE && cases[l][c]!=CAISSE_BLOQUEE_TEMP && cases[l][c]!=MUR && cases[l][c]!=VIDE) return false;
		if(aMur(l,c)) return false;
		if(estCaseLibre(l,c))return false;
		if(!aBut(l,c)){
			if(!estCaseLibre(l-1,c) && (!estCaseLibre(l,c-1) || !estCaseLibre(l,c+1))){
				if(aMur(l-1,c) && (aMur(l,c-1)||(aMur(l,c+1)))) return false;
				return true;
			}
			if(!estCaseLibre(l+1,c) && (!estCaseLibre(l,c-1) || !estCaseLibre(l,c+1))){
				if(aMur(l+1,c) && (aMur(l,c-1)||(aMur(l,c+1)))) return false;
				return true;
			}
		}
		return false;
	}

	boolean estCaseBloquante(int l, int c){
		if(aMur(l,c)) return true;
		int[][] save_carte = cases;
		int save_case = cases[l][c];
		System.out.println("l: "+l+" c: "+c+" cases[l][c]: "+ cases[l][c]);
		cases[l][c] = CAISSE;
		if(estCaisseBloquee(l,c)){
			cases[l][c] = save_case;
			return true;
		}
		cases[l][c] = save_case;
		return false;
	}
	void actualiseUneCaisse(int l, int c){
		if (aCaisse(l,c)){
			if(cases[l][c]==CAISSE_BLOQUEE) return;
			if(aBut(l,c)){
				return;
			}
			if (estCaisseBloquee(l,c)){
				cases[l][c] = CAISSE_BLOQUEE;
				return;
			}
			if (estCaisseBloqueeTemp(l,c)) {
				cases[l][c] = CAISSE_BLOQUEE_TEMP;
				return;
			}
			cases[l][c] = CAISSE;
		}
	}

	public void actualiseToutesCaisses(){
		for(int l=0;l<lignes();l++){
			for(int c=0;c<colonnes();c++){
				actualiseUneCaisse(l,c);
			}
		}
	}
	void actualiseCaisses(int l, int c){
		actualiseUneCaisse(l+1,c);
		actualiseUneCaisse(l-1,c);
		actualiseUneCaisse(l,c+1);
		actualiseUneCaisse(l,c-1);
	}

}
