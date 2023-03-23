package Vue;
/*
 * Sokoban - Encore une nouvelle version (à but pédagogique) du célèbre jeu
 * Copyright (C) 2018 Guillaume Huard
 *
 * Ce programme est libre, vous pouvez le redistribuer et/ou le
 * modifier selon les termes de la Licence Publique Générale GNU publiée par la
 * Free Software Foundation (version 2 ou bien toute autre version ultérieure
 * choisie par vous).
 *
 * Ce programme est distribué car potentiellement utile, mais SANS
 * AUCUNE GARANTIE, ni explicite ni implicite, y compris les garanties de
 * commercialisation ou d'adaptation dans un but spécifique. Reportez-vous à la
 * Licence Publique Générale GNU pour plus de détails.
 *
 * Vous devez avoir reçu une copie de la Licence Publique Générale
 * GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307,
 * États-Unis.
 *
 * Contact:
 *          Guillaume.Huard@imag.fr
 *          Laboratoire LIG
 *          700 avenue centrale
 *          Domaine universitaire
 *          38401 Saint Martin d'Hères
 */

import Modele.Jeu;
import Modele.RedacteurNiveau;
import Patterns.Observateur;

import java.util.Scanner;

// Interface textuelle permettant de mettre en évidence la modularité de la vue
public class InterfaceTextuelle implements InterfaceUtilisateur, Observateur {
	Jeu j;
	CollecteurEvenements control;
	RedacteurNiveau affichage;

	InterfaceTextuelle(Jeu jeu, CollecteurEvenements c) {
		j = jeu;
		control = c;
		j.ajouteObservateur(this);
		affichage = new RedacteurNiveau(System.out);
	}

	public static void demarrer(Jeu j, CollecteurEvenements c) {
		InterfaceTextuelle vue = new InterfaceTextuelle(j, c);
		c.ajouteInterfaceUtilisateur(vue);
		vue.miseAJour();
		Scanner s = new Scanner(System.in);
		while (true) {
			System.out.print("Commande > ");
			c.toucheClavier(s.next());
		}
	}

	public void toggleFullscreen() {
		System.out.println("Pas de plein écran en mode textuel");
	}

	// On ignore simplement les animations et la direction du pousseur en mode textuel
	@Override
	public void changeEtape() { }
	@Override
	public void metAJourDirection(int dL, int dC) { }
	@Override
	public void decale(int versL, int versC, double dL, double dC) { }
	@Override
	public void changeEtatIA(boolean b) { }
	@Override
	public void changeEtatAnimations(boolean b) { }

	@Override
	public void miseAJour() {
		affichage.ecrisNiveau(j.niveau());
	}
}
