package Controleur;
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

import Global.Configuration;
import Modele.*;
import Structures.Iterateur;
import Structures.Sequence;
import Vue.CollecteurEvenements;
import Vue.InterfaceUtilisateur;

public class ControleurMediateur implements CollecteurEvenements {
	Jeu jeu;
	InterfaceUtilisateur vue;
	Sequence<Animation> animations;
	double vitesseAnimations;
	int lenteurPas;
	Animation mouvement;
	boolean animationsSupportees, animationsActives;
	int lenteurJeuAutomatique;
	IA joueurAutomatique;
	boolean IAActive;
	AnimationJeuAutomatique animationIA;

	public ControleurMediateur(Jeu j) {
		jeu = j;

		animations = Configuration.nouvelleSequence();
		vitesseAnimations = Configuration.lisDouble("VitesseAnimations");
		lenteurPas = Configuration.lisInt("LenteurPas");
		animations.insereTete(new AnimationPousseur(lenteurPas, this));
		mouvement = null;
		// Tant qu'on ne reçoit pas d'évènement temporel, on n'est pas sur que les
		// animations soient supportées (ex. interface textuelle)
		animationsSupportees = false;
		animationsActives = false;
	}

	@Override
	public void clicSouris(int l, int c) {
		int dL = l - jeu.lignePousseur();
		int dC = c - jeu.colonnePousseur();
		int sum = dC + dL;
		sum = sum * sum;
		if ((dC * dL == 0) && (sum == 1))
			deplace(dL, dC);
	}

	void repercute(Coup cp, int direction) {
		vue.metAJourDirection(cp.dirPousseurL(), cp.dirPousseurC());
		if (animationsActives) {
			mouvement = new AnimationCoup(cp, vitesseAnimations, this);
			animations.insereQueue(mouvement);
		} else
			testFin();
	}

	void joue(Coup cp) {
		if (cp != null) {
			jeu.joue(cp);
			repercute(cp, Mouvement.AVANT);
		} else {
			Configuration.alerte("Coup null fourni, probablement un bug dans l'IA");
		}
	}

	void annule() {
		if ((mouvement == null) && jeu.peutAnnuler()) {
			Coup cp = jeu.annuler();
			repercute(cp, Mouvement.ARRIERE);
		}
	}

	void refait() {
		if ((mouvement == null) && jeu.peutRefaire()) {
			Coup cp = jeu.refaire();
			repercute(cp, Mouvement.AVANT);
		}

	}

	void deplace(int dL, int dC) {
		if (mouvement == null) {
			Coup cp = jeu.elaboreCoup(dL, dC);
			if (cp != null)
				joue(cp);
		}
	}

	private void testFin() {
		if (jeu.niveauTermine()) {
			jeu.prochainNiveau();
			if (jeu.jeuTermine())
				System.exit(0);
		}
	}


	@Override
	public void toucheClavier(String touche) {
		switch (touche) {
			case "Left":
				deplace(0, -1);
				break;
			case "Right":
				deplace(0, 1);
				break;
			case "Up":
				deplace(-1, 0);
				break;
			case "Down":
				deplace(1, 0);
				break;
			case "Undo":
				annule();
				break;
			case "Redo":
				refait();
				break;
			case "Quit":
				System.exit(0);
				break;
			case "Pause":
				basculeAnimations();
				break;
			case "IA":
				basculeIA();
				break;
			case "Next":
				prochain();
				break;
			case "Full":
				vue.toggleFullscreen();
				break;
			default:
				System.out.println("Touche inconnue : " + touche);
		}
	}

	public void prochain() {
		jeu.prochainNiveau();
		testFin();
	}

	public void ajouteInterfaceUtilisateur(InterfaceUtilisateur v) {
		vue = v;
	}

	@Override
	public void tictac() {
		// On sait qu'on supporte les animations si on reçoit des évènements temporels
		if (!animationsSupportees) {
			animationsSupportees = true;
			animationsActives = Configuration.lisBoolean("Animations");
			vue.changeEtatAnimations(animationsActives);
		}
		// On traite l'IA séparément pour pouvoir l'activer même si les animations
		// "esthétiques" sont désactivées
		if (IAActive && (mouvement == null)) {
			animationIA.tictac();
		}
		if (animationsActives) {
			Iterateur<Animation> it = animations.iterateur();
			while (it.aProchain()) {
				Animation a = it.prochain();
				a.tictac();
				if (a.estTerminee()) {
					if (a == mouvement) {
						testFin();
						mouvement = null;
					}
					it.supprime();
				}
			}
		}
	}

	public void changeEtape() {
		vue.changeEtape();
	}

	public void decale(int versL, int versC, double dL, double dC) {
		vue.decale(versL, versC, dL, dC);
	}

	public void basculeAnimations() {
		if (animationsSupportees && (mouvement == null))
			animationsActives = !animationsActives;
			vue.changeEtatAnimations(animationsActives);
	}

	public void basculeIA() {
		if (animationsSupportees) {
			if (joueurAutomatique == null) {
				joueurAutomatique = IA.nouvelle(jeu);
				if (joueurAutomatique != null) {
					lenteurJeuAutomatique = Configuration.lisInt("LenteurJeuAutomatique");
					animationIA = new AnimationJeuAutomatique(lenteurJeuAutomatique, joueurAutomatique, this);
				}
			}
			if (joueurAutomatique != null)
				IAActive = !IAActive;
				vue.changeEtatIA(IAActive);
		}
	}
}
