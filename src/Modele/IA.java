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

import Global.Configuration;
import Structures.Sequence;

import java.util.logging.Logger;

public abstract class IA {
	private Jeu jeu;
	Niveau niveau;
	Niveau nivOriginal;

	public static IA nouvelle(Jeu j) {
		IA resultat = null;
		// Méthode de fabrication pour l'IA, qui crée le bon objet selon la config
		String type = Configuration.lisChaine("IA");
		switch (type) {
			case "Aleatoire":
				resultat = new IAAleatoire();
				break;
			case "Teleportations":
				resultat = new IATeleportations();
				break;
			case "ParcoursFixe":
				resultat = new IAParcoursFixe();
				break;
			case "IAResolveur":
				resultat = new IAResolveur();
				break;
			default:
				Configuration.erreur("IA de type " + type + " non supportée");
		}
		if (resultat != null) {
			resultat.jeu = j;
		}
		return resultat;
	}

	public final Sequence<Coup> elaboreCoups() {
		niveau = jeu.niveau().clone();
		nivOriginal = jeu.niveau();
		return joue();
	}

	Sequence<Coup> joue() {
		return null;
	}
}
