package Modele;
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

public class Mouvement {
	int [] ligne, colonne;
	int direction;
	public static final int AVANT = 1;
	public static final int ARRIERE = 0;

	Mouvement(int dL, int dC, int vL, int vC) {
		ligne = new int[2];
		colonne = new int[2];
		ligne[0] = dL;
		colonne[0] = dC;
		ligne[1] = vL;
		colonne[1] = vC;
		direction = AVANT;
	}

	public int depuisL() {
		return ligne[1-direction];
	}

	public int depuisC() {
		return colonne[1-direction];
	}

	public int versL() {
		return ligne[direction];
	}

	public int versC() {
		return colonne[direction];
	}

	public void fixeDirection(int dir) {
		direction = dir;
	}

	int decompte() {
		return direction*2 - 1;
	}

	@Override
	public String toString() {
		return "(" + ligne[0] + ", " + colonne[0] + ") " +
				((direction > 0) ? "->":"<-") +
				" (" + ligne[1] + ", " + colonne[1] + ")";
	}
}
