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
import Patterns.Observateur;

import javax.swing.*;
import java.awt.*;

// L'interface runnable déclare une méthode run
public class InterfaceGraphique implements Runnable, InterfaceUtilisateur, Observateur {
	Jeu j;
	CollecteurEvenements control;
	JFrame frame;
	boolean maximized;
	NiveauGraphique niv;
	JLabel nbPas, nbPoussees;
	JToggleButton IA, animation;

	InterfaceGraphique(Jeu jeu, CollecteurEvenements c) {
		j = jeu;
		control = c;
	}

	public static void demarrer(Jeu j, CollecteurEvenements c) {
		InterfaceGraphique vue = new InterfaceGraphique(j, c);
		c.ajouteInterfaceUtilisateur(vue);
		SwingUtilities.invokeLater(vue);
	}

	public void toggleFullscreen() {
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = env.getDefaultScreenDevice();
		if (maximized) {
			device.setFullScreenWindow(null);
			maximized = false;
		} else {
			device.setFullScreenWindow(frame);
			maximized = true;
		}
	}

	private JLabel creerLabel(String text) {
		JLabel label = new JLabel(text);
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		return label;
	}

	private JToggleButton creerToggleButton(String text) {
		JToggleButton bouton = new JToggleButton(text);
		bouton.setAlignmentX(Component.CENTER_ALIGNMENT);
		bouton.setFocusable(false);
		return bouton;
	}

	private JButton creerButton(String text) {
		JButton bouton = new JButton(text);
		bouton.setAlignmentX(Component.CENTER_ALIGNMENT);
		bouton.setFocusable(false);
		return bouton;
	}

	private Box creerBoiteHorizontale() {
		Box boite = Box.createHorizontalBox();
		boite.setAlignmentX(Component.CENTER_ALIGNMENT);
		return boite;
	}

	public void run() {
		// Eléments de l'interface
		frame = new JFrame("Ma fenetre a moi");
		niv = new NiveauGraphique(j);

		// Texte et contrôles à droite de la fenêtre
		Box boiteTexte = Box.createVerticalBox();

		// Titre
		boiteTexte.add(creerLabel("Sokoban"));
		boiteTexte.add(creerLabel("Caisses restantes : " + (j.niveau().getNbButs()-j.niveau().getNbCaissesSurBut())));
		// Remplissage de l'espace entre le titre et les boutons
		boiteTexte.add(Box.createGlue());

		// Infos sur le jeu
		nbPas = creerLabel("Pas : 0");
		boiteTexte.add(nbPas);
		nbPoussees = creerLabel("Poussées : 0");
		boiteTexte.add(nbPoussees);

		// Contrôles comportementaux
		IA = creerToggleButton("IA");
		boiteTexte.add(IA);
		animation = creerToggleButton("Animation");
		boiteTexte.add(animation);
		JButton prochain = creerButton("Prochain");
		boiteTexte.add(prochain);

		// Annuler / refaire
		BoutonAnnuler annuler = new BoutonAnnuler(j);
		annuler.setFocusable(false);
		BoutonRefaire refaire = new BoutonRefaire(j);
		refaire.setFocusable(false);
		Box annulerRefaire = creerBoiteHorizontale();
		annulerRefaire.add(annuler);
		annulerRefaire.add(refaire);
		boiteTexte.add(annulerRefaire);

		// Remplissage de l'espace entre le titre et les boutons
		boiteTexte.add(Box.createGlue());

		boiteTexte.add(creerLabel("Copyright G. Huard, 2018"));

		// Retransmission des évènements au contrôleur
		niv.addMouseListener(new AdaptateurSouris(niv, control));
		frame.addKeyListener(new AdaptateurClavier(control));
		Timer chrono = new Timer(16, new AdaptateurTemps(control));
		IA.addActionListener(new AdaptateurIA(control));
		animation.addActionListener(new AdaptateurAnimations(control));
		prochain.addActionListener(new AdaptateurProchain(control));
		annuler.addActionListener(new AdaptateurAnnuler(control));
		refaire.addActionListener(new AdaptateurRefaire(control));

		// Mise en place de l'interface
		frame.add(boiteTexte, BorderLayout.EAST);
		frame.add(niv);
		j.ajouteObservateur(this);
		chrono.start();

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1280, 720);
		//centrer la fenetre
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	@Override
	public void changeEtatIA(boolean b) {
		IA.setSelected(b);
	}

	@Override
	public void changeEtatAnimations(boolean b) {
		animation.setSelected(b);
	}

	@Override
	public void decale(int l, int c, double dl, double dc) {
		niv.decale(l, c, dl, dc);
	}

	@Override
	public void metAJourDirection(int dL, int dC) {
		niv.metAJourDirection(dL, dC);
	}

	@Override
	public void changeEtape() {
		niv.changeEtape();
	}

	@Override
	public void miseAJour() {
		nbPas.setText("Pas : " + j.niveau().nbPas());
		nbPoussees.setText("Poussées : " + j.niveau().nbPoussees());
	}
}
