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

import Controleur.ControleurMediateur;
import Global.Configuration;
import Modele.IA;
import Modele.Jeu;
import Modele.LecteurNiveaux;
import Modele.Position;
import Structures.SequenceListe;
import Vue.CollecteurEvenements;
import Vue.InterfaceGraphique;
import Vue.InterfaceTextuelle;
import java.io.InputStream;
import java.util.ArrayList;

public class Sokoban {
	final static String typeInterface = Configuration.lisChaine("Interface");

	public static void main(String[] args) {


		InputStream in;
		in = Configuration.ouvre("Niveaux/niveaux.txt");

		ArrayList<SequenceListe<Position>> cheminFinal = new ArrayList<SequenceListe<Position>>();
		SequenceListe<Position> chemin = new SequenceListe<Position>();
		/*
		//démare le calcul du temps (61 ms en moyenne)
		long startTime = System.currentTimeMillis();
		for(int k=0; k<10000; k++) {
			for (int i = 0; i < 100; i++) {
				chemin = new SequenceListe<Position>();
				Position p = new Position((int) (Math.random() * 20), (int) (Math.random() * 20));
				chemin.insereTete(p);
			}
			cheminFinal.add(chemin);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Temps d'exécution : " + (endTime - startTime) + " ms");
		// on inverse cheminFinal
		startTime = System.currentTimeMillis();
		ArrayList<SequenceListe<Position>> cheminInverse = new ArrayList<SequenceListe<Position>>();
		for(int i=cheminFinal.size()-1; i>=0; i--){
			cheminInverse.add(cheminFinal.get(i));
		}
		endTime = System.currentTimeMillis();
		System.out.println("Temps d'exécution inversion : " + (endTime - startTime) + " ms");
		//affiche la taille de la liste
		System.out.println("Taille de la liste : " + cheminInverse.size());
		*/

		LecteurNiveaux l = new LecteurNiveaux(in);
		Jeu j = new Jeu(l);
		CollecteurEvenements control = new ControleurMediateur(j);
		switch (typeInterface) {
			case "Graphique":
				InterfaceGraphique.demarrer(j, control);
				break;
			case "Textuelle":
				InterfaceTextuelle.demarrer(j, control);
				break;
			default:
				Configuration.erreur("Interface inconnue");
		}
	}
}
