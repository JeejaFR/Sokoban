package Modele;
import Global.Configuration;

public class Niveau extends Historique<Coup> implements Cloneable {
	static final int VIDE = 0;
	static final int MUR = 1;
	static final int POUSSEUR = 2;
	static final int CAISSE = 4;
	static final int BUT = 8;
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
		return cases[i][j] & (POUSSEUR | CAISSE);
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
	}

	@Override
	public Coup annuler() {
		Coup cp = super.annuler();
		decomptes(cp);
		return cp;
	}

	@Override
	public Coup refaire() {
		Coup cp = super.refaire();
		decomptes(cp);
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

	String nom() {
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

	public boolean aCaisse(int l, int c) {
		return (cases[l][c] & CAISSE) != 0;
	}

	public boolean estOccupable(int l, int c) {
		return (cases[l][c] & (MUR | CAISSE | POUSSEUR)) == 0;
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
}
