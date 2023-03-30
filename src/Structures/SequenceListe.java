package Structures;
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

import Modele.Position;

public class SequenceListe<E> implements Sequence<E>, Comparable<SequenceListe<Position>> {
	Maillon<E> tete, queue;
	private int taille = 0;

	@Override
	public void insereQueue(E element) {
		Maillon<E> m = new Maillon<>(element, null);
		if (tete == null) {
			tete = queue = m;
		} else {
			queue.suivant = m;
			queue = queue.suivant;
		}
		taille++;
	}

	public void incrementeTaille(){
		taille++;
	}

	@Override
	public void insereTete(E element) {
		Maillon<E> m = new Maillon<>(element, tete);
		if (tete == null) {
			tete = queue = m;
		} else {
			tete = m;
		}
		taille++;
	}

	@Override
	public E extraitTete() {
		E resultat;
		// Exception si tete == null (sequence vide)
		resultat = tete.element;
		tete = tete.suivant;
		if (tete == null) {
			queue = null;
		}
		taille--;
		return resultat;
	}

	public E extraitQueue(){
		if(queue == null){
			return null;
		}else if(tete == queue){// il y a un seul élément dans la liste
			E element = queue.element;
			tete = queue = null;
			taille--;
			return element;
		}else{
			// il y a plusieurs éléments dans la liste
			Maillon<E> precedent = tete;
			while(precedent.suivant != queue){
				precedent = precedent.suivant;
			}
			E element = queue.element;
			queue = precedent;
			queue.suivant = null;
			taille--;
			return element;
		}
	}

	public void melangeAleatoire(){//mélange la liste aléatoirement
		SequenceListe<E> listeMelangee = new SequenceListe<>();
		//on ajoute des éléments de manière aléatoire dans la nouvelle liste
		while(!this.estVide()){
			int alea = (int)(Math.random() * 2);
			if(alea == 0){
				listeMelangee.insereTete(this.extraitTete());
			}else{
				listeMelangee.insereQueue(this.extraitTete());
			}
		}
		//on remet les éléments dans la liste d'origine
		while(!listeMelangee.estVide()){
			this.insereQueue(listeMelangee.extraitTete());
		}
	}

	public E getTete(){
		return tete.element;
	}

	public E getQueue(){
		return queue.element;
	}

	@Override
	public boolean estVide() {
		return tete == null;
	}

	public int taille() {
		return taille;
	}

	@Override
	public String toString() {
		String resultat = "SequenceListe [ ";
		boolean premier = true;
		Maillon<E> m = tete;
		while (m != null) {
			if (!premier)
				resultat += ", ";
			resultat += m.element;
			m = m.suivant;
			premier = false;
		}
		resultat += " ]";
		return resultat;
	}

	@Override
	public Iterateur<E> iterateur() {
		return new IterateurSequenceListe<>(this);
	}

	@Override
	public int compareTo(SequenceListe<Position> o) {
		int size1 = taille();
		int size2 = o.taille();
		if(size1 == size2) {//Si les deux listes ont la même taille, renvoie 0 pour indiquer que leur poids est identique.
			return 0;
		}else{//La liste ayant la taille la plus élevée est considérée comme supérieure à l'autre liste
			return Integer.compare(size2, size1);
		}
	}
}
