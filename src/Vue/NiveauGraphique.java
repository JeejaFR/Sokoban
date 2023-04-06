package Vue;

import Global.Configuration;
import Modele.Jeu;
import Modele.Niveau;
import Patterns.Observateur;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.InputStream;

public class NiveauGraphique extends JComponent implements Observateur {
	Image pousseur, mur, sol, caisse, but, caisseSurBut,caisse_bloquee,caisse_bloquee_temp;
	Jeu j;
	int largeurCase;
	int hauteurCase;
	// Décalage des éléments (pour pouvoir les animer)
	Vecteur [][] decalages;
	// Images du pousseur (pour l'animation)
	Image [][] pousseurs;
	int direction, etape;

	NiveauGraphique(Jeu jeu) {
		j = jeu;
		j.ajouteObservateur(this);
		pousseur = lisImage("Pousseur");
		mur = lisImage("Mur");
		sol = lisImage("Sol");
		caisse = lisImage("Caisse");
		but = lisImage("But");
		caisseSurBut = lisImage("Caisse_sur_but");
		caisse_bloquee = lisImage("Caisse_bloquee");
		caisse_bloquee_temp = lisImage("Caisse_bloquee_temp");

		pousseurs = new Image[4][4];
		for (int d = 0; d < pousseurs.length; d++)
			for (int i = 0; i < pousseurs[d].length; i++)
				pousseurs[d][i] = lisImage("Pousseur_" + d + "_" + i);
		etape = 0;
		direction = 2;
		metAJourPousseur();
	}

	private Image lisImage(String nom) {
		InputStream in = Configuration.ouvre("Images/" + nom + ".png");
		try {
			// Chargement d'une image utilisable dans Swing
			return ImageIO.read(in);
		} catch (Exception e) {
			Configuration.erreur("Impossible de charger l'image " + nom);
		}
		return null;
	}

	private void tracer(Graphics2D g, Image i, int x, int y, int l, int h) {
		g.drawImage(i, x, y, l, h, null);
	}

	public void paintComponent(Graphics g) {
		Graphics2D drawable = (Graphics2D) g;
		Niveau n = j.niveau();

		int largeur = getSize().width;
		int hauteur = getSize().height;
		largeurCase = largeur / n.colonnes();
		hauteurCase = hauteur / n.lignes();
		// On prend des cases carrées
		largeurCase = Math.min(largeurCase, hauteurCase);
		hauteurCase = largeurCase;

		// Le vecteur de décalages doit être conforme au niveau
		if ((decalages == null)
				|| (decalages.length != n.lignes())
				|| (decalages[0].length != n.colonnes()))
			decalages = new Vecteur[n.lignes()][n.colonnes()];

		// Tracé du niveau
		// En deux étapes à cause des décalages possibles
		for (int ligne = 0; ligne < n.lignes(); ligne++)
			for (int colonne = 0; colonne < n.colonnes(); colonne++) {
				int x = colonne * largeurCase;
				int y = ligne * hauteurCase;
				int marque = n.marque(ligne, colonne);
				// Tracé du sol
				if (n.aBut(ligne, colonne))
					tracer(drawable, but, x, y, largeurCase, hauteurCase);
				else
					tracer(drawable, sol, x, y, largeurCase, hauteurCase);
				if (marque > 0)
					tracerCroix(drawable, marque, x, y, largeurCase, hauteurCase);
			}
		for (int ligne = 0; ligne < n.lignes(); ligne++)
			for (int colonne = 0; colonne < n.colonnes(); colonne++) {
				int x = colonne * largeurCase;
				int y = ligne * hauteurCase;
				if(ligne>1 && colonne>1)
					drawable.drawString((ligne-2)+","+(colonne-2), x-7-largeurCase/2, y+4-hauteurCase/2);
				// Décalage éventuel
				Vecteur decal = decalages[ligne][colonne];
				if (decal != null) {
					x += decal.x * largeurCase;
					y += decal.y * hauteurCase;
				}
				// Tracé des objets
				if (n.aMur(ligne, colonne)){
					tracer(drawable, mur, x, y, largeurCase, hauteurCase);
				}else if (n.aPousseur(ligne, colonne)){
					tracer(drawable, pousseur, x, y, largeurCase, hauteurCase);
				}else if (n.aCaisse(ligne, colonne)){
					int marque = n.marque(ligne, colonne);
					if (n.aBut(ligne, colonne))
						tracer(drawable, caisseSurBut, x, y, largeurCase, hauteurCase);
					else{
						if(n.aCaisseBloquee(ligne, colonne)){
							tracer(drawable,caisse_bloquee, x, y, largeurCase, hauteurCase);
						}
						else{
							if(n.aCaisseBloqueeTemp(ligne, colonne))
								tracer(drawable,caisse_bloquee_temp, x, y, largeurCase, hauteurCase);
							else
								tracer(drawable, caisse, x, y, largeurCase, hauteurCase);
						}
					}
					if (marque > 0)
						tracerCroix(drawable, marque, x, y, largeurCase, hauteurCase);
				}else if (n.aCroix(ligne, colonne)) {
					int marque = n.marque(ligne, colonne);
					tracerCroix(drawable, marque, x, y, largeurCase, hauteurCase);
				}
			}
	}

	public void tracerCroix(Graphics2D drawable, int marque, int x, int y, int l, int h) {
		int s = l/10;
		drawable.setColor(new Color(marque));
		drawable.setStroke(new BasicStroke(s));
		drawable.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		drawable.drawLine(x+s, y+s, x+l-s, y+h-s);
		drawable.drawLine(x+h-s, y+s, x+s, y+h-s);
	}

	int hauteurCase() {
		return hauteurCase;
	}

	int largeurCase() {
		return largeurCase;
	}

	@Override
	public void miseAJour() {
		repaint();
	}

	public void decale(int l, int c, double dl, double dc) {
		if ((dl != 0) || (dc != 0)) {
			Vecteur v = decalages[l][c];
			if (v == null) {
				v = new Vecteur();
				decalages[l][c] = v;
			}
			v.x = dc;
			v.y = dl;
		} else {
			decalages[l][c] = null;
		}
		miseAJour();
	}

	// Animation du pousseur
	void metAJourPousseur() {
		pousseur = pousseurs[direction][etape];
	}

	public void metAJourDirection(int dL, int dC) {
		switch (dL + 2 * dC) {
			case -2:
				direction = 1;
				break;
			case -1:
				direction = 0;
				break;
			case 0:
				// Rien, pas de mouvement, direction inchangée
				break;
			case 1:
				direction = 2;
				break;
			case 2:
				direction = 3;
				break;
			default:
				Configuration.erreur("Bug interne, direction invalide");
		}
		metAJourPousseur();
	}

	public void changeEtape() {
		etape = (etape + 1) % pousseurs[direction].length;
		metAJourPousseur();
		miseAJour();
	}
}