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
package Modele;

import Global.Configuration;
import Patterns.Commande;
import Structures.Sequence;

public class Historique<E extends Commande> {
	Sequence<E> passe, futur;

	public Historique() {
		reinitialise();
	}

	void reinitialise() {
		passe = Configuration.nouvelleSequence();
		futur = Configuration.nouvelleSequence();
	}

	public boolean peutAnnuler() {
		return !passe.estVide();
	}

	public boolean peutRefaire() {
		return !futur.estVide();
	}

	private E transfert(Sequence<E> source, Sequence<E> destination) {
		E resultat = source.extraitTete();
		destination.insereTete(resultat);
		return resultat;
	}

	public E annuler() {
		E comm = transfert(passe, futur);
		comm.desexecute();
		return comm;
	}

	public E refaire() {
		E comm = transfert(futur, passe);
		comm.execute();
		return comm;
	}

	public void faire(E nouveau) {
		nouveau.execute();
		passe.insereTete(nouveau);
		while (!futur.estVide()) {
			futur.extraitTete();
		}
	}

	@Override
	protected Historique clone() throws CloneNotSupportedException {
		// Pour simplifier, on décide de ne pas cloner l'historique :
		// notre seul cas d'utilisation est pour le niveau de l'IA qui
		// n'a pas besoin de l'historique utilisateur. Par contre il faut
		// que le clone ait son propre historique (initialement vide)
		Historique resultat = (Historique) super.clone();
		resultat.reinitialise();
		return resultat;
	}
}
