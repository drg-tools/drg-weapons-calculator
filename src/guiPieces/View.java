package guiPieces;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
// import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.sf.image4j.codec.ico.ICODecoder;

import modelPieces.Weapon;

public class View extends JFrame implements Observer {
	
	private JMenuBar menuBar;
	private JMenu bestCombinationsMenu;
	private JMenuItem bcmIdealBurst, bcmIdealSustained, bcmSustainedWeakpoint, bcmSustainedWeakpointAccuracy, bcmIdealAdditional, bcmMaxDmg, 
					bcmMaxNumTargets, bcmDuration, bcmTTK, bcmOverkill, bcmAccuracy, bcmUtility;
	private JMenu exportMenu;
	private JMenuItem exportCurrent, exportAll;
	private JMenu miscMenu;	
	private JMenuItem miscWeaponTabScreenshot, miscExportCombination, miscLoadCombination, miscSuggestion, miscFAQ, miscGlossary;
	
	private Weapon[] drillerWeapons;
	private Weapon[] engineerWeapons;
	private Weapon[] gunnerWeapons;
	private Weapon[] scoutWeapons;
	
	// private JLayeredPane foregroundSwitcher;
	
	private JTabbedPane classTabs;
	private JTabbedPane drillerTabs;
	private JTabbedPane engineerTabs;
	private JTabbedPane gunnerTabs;
	private JTabbedPane scoutTabs;
	
	private JPanel FAQ, glossary;
	
	public View(Weapon[] dWeapons, Weapon[] eWeapons, Weapon[] gWeapons, Weapon[] sWeapons) {
		drillerWeapons = dWeapons;
		engineerWeapons = eWeapons;
		gunnerWeapons = gWeapons;
		scoutWeapons = sWeapons;
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("MeatShield's DRG DPS Calculator (DRG Update 28.8)");
		setPreferredSize(new Dimension(1620, 780));
		
		// Add the icon
		try {
			// Image sourced from http://www.zazzle.com/meat+shield+stickers
			List<BufferedImage> image = ICODecoder.read(new File("images/meatShield_composite.ico"));
			setIconImages(image);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		constructMenu();
		
		// foregroundSwitcher = new JLayeredPane();
		
		constructWeaponTabs();
		constructFAQ();
		constructGlossary();
		
		// foregroundSwitcher.moveToFront(classTabs);
		
		// add(foregroundSwitcher);
		// setContentPane(foregroundSwitcher);
		setContentPane(classTabs);
		pack();
		setVisible(true);
	}

	private void constructMenu() {
		menuBar = new JMenuBar();
		
		bestCombinationsMenu = new JMenu("Best Combinations");
		bcmIdealBurst = new JMenuItem("Best Ideal Burst DPS");
		bestCombinationsMenu.add(bcmIdealBurst);
		bcmIdealSustained = new JMenuItem("Best Ideal Sustained DPS");
		bestCombinationsMenu.add(bcmIdealSustained);
		bcmSustainedWeakpoint = new JMenuItem("Best Sustained + Weakpoint DPS");
		bestCombinationsMenu.add(bcmSustainedWeakpoint);
		bcmSustainedWeakpointAccuracy = new JMenuItem("Best Sustained + Weakpoint + Accuracy DPS");
		bcmSustainedWeakpointAccuracy.setEnabled(false);  // TODO: Re-enable this once Accuracy is implemented.
		bestCombinationsMenu.add(bcmSustainedWeakpointAccuracy);
		bcmIdealAdditional = new JMenuItem("Best Additional Target DPS");
		bestCombinationsMenu.add(bcmIdealAdditional);
		bcmMaxDmg = new JMenuItem("Most Multi-Target Damage");
		bestCombinationsMenu.add(bcmMaxDmg);
		bcmMaxNumTargets = new JMenuItem("Most Number of Targets Hit");
		bestCombinationsMenu.add(bcmMaxNumTargets);
		bcmDuration = new JMenuItem("Longest Firing Duration");
		bestCombinationsMenu.add(bcmDuration);
		bcmTTK = new JMenuItem("Fastest Avg Time To Kill");
		bestCombinationsMenu.add(bcmTTK);
		bcmOverkill = new JMenuItem("Lowest Avg Overkill");
		bestCombinationsMenu.add(bcmOverkill);
		bcmAccuracy = new JMenuItem("Highest Accuracy");
		bcmAccuracy.setEnabled(false);  // TODO: Re-enable this once Accuracy is implemented.
		bestCombinationsMenu.add(bcmAccuracy);
		bcmUtility = new JMenuItem("Most Utility");
		bestCombinationsMenu.add(bcmUtility);
		menuBar.add(bestCombinationsMenu);
		
		exportMenu = new JMenu("Export Stats to CSV");
		exportCurrent = new JMenuItem("Export current weapon");
		exportMenu.add(exportCurrent);
		exportAll = new JMenuItem("Export all weapons");
		exportMenu.add(exportAll);
		menuBar.add(exportMenu);
		
		miscMenu = new JMenu("Misc. Actions");
		miscWeaponTabScreenshot = new JMenuItem("Save screenshot of current build");
		miscMenu.add(miscWeaponTabScreenshot);
		miscExportCombination = new JMenuItem("Export current weapon combination");
		miscMenu.add(miscExportCombination);
		miscLoadCombination = new JMenuItem("Load combination for current weapon");
		miscMenu.add(miscLoadCombination);
		miscSuggestion = new JMenuItem("Suggest a change for this program");
		miscMenu.add(miscSuggestion);
		miscFAQ = new JMenuItem("Frequently Asked Questions");
		miscFAQ.setEnabled(false);  // TODO: Re-enable this once the FAQ is working 
		miscMenu.add(miscFAQ);
		miscGlossary = new JMenuItem("Glossary of Terms");
		miscGlossary.setEnabled(false);  // TODO: Re-enable this once the Glossary is working
		miscMenu.add(miscGlossary);
		menuBar.add(miscMenu);
		
		setJMenuBar(menuBar);
	}
	
	private void constructWeaponTabs() {
		classTabs = new JTabbedPane();
		
		// Driller
		drillerTabs = new JTabbedPane();
		for (int i = 0; i < drillerWeapons.length; i++) {
			drillerWeapons[i].addObserver(this);
			drillerTabs.addTab(drillerWeapons[i].getFullName(), new WeaponTab(drillerWeapons[i]));
		}
		classTabs.addTab("Driller", drillerTabs);
		
		// Engineer
		engineerTabs = new JTabbedPane();
		for (int i = 0; i < engineerWeapons.length; i++) {
			engineerWeapons[i].addObserver(this);
			engineerTabs.addTab(engineerWeapons[i].getFullName(), new WeaponTab(engineerWeapons[i]));
		}
		classTabs.addTab("Engineer", engineerTabs);
		
		// Gunner
		gunnerTabs = new JTabbedPane();
		for (int i = 0; i < gunnerWeapons.length; i++) {
			gunnerWeapons[i].addObserver(this);
			gunnerTabs.addTab(gunnerWeapons[i].getFullName(), new WeaponTab(gunnerWeapons[i]));
		}
		classTabs.addTab("Gunner", gunnerTabs);
		
		// Scout
		scoutTabs = new JTabbedPane();
		for (int i = 0; i < scoutWeapons.length; i++) {
			scoutWeapons[i].addObserver(this);
			scoutTabs.addTab(scoutWeapons[i].getFullName(), new WeaponTab(scoutWeapons[i]));
		}
		classTabs.addTab("Scout", scoutTabs);
		
		// foregroundSwitcher.add(classTabs);
		add(classTabs);
	}
	
	// TODO: fill out these JPanels with scrollable content
	private void constructFAQ() {
		FAQ = new JPanel();
		
		//foregroundSwitcher.add(FAQ);
	}
	
	private void constructGlossary() {
		glossary = new JPanel();
		
		//foregroundSwitcher.add(glossary);
	}
	
	// Getters used by GuiController
	public JMenuItem getBcmIdealBurst() {
		return bcmIdealBurst;
	}
	public JMenuItem getBcmIdealSustained() {
		return bcmIdealSustained;
	}
	public JMenuItem getBcmSustainedWeakpoint() {
		return bcmSustainedWeakpoint;
	}
	public JMenuItem getBcmSustainedWeakpointAccuracy() {
		return bcmSustainedWeakpointAccuracy;
	}
	public JMenuItem getBcmIdealAdditional() {
		return bcmIdealAdditional;
	}
	public JMenuItem getBcmMaxDmg() {
		return bcmMaxDmg;
	}
	public JMenuItem getBcmMaxNumTargets() {
		return bcmMaxNumTargets;
	}
	public JMenuItem getBcmDuration() {
		return bcmDuration;
	}
	public JMenuItem getBcmTTK() {
		return bcmTTK;
	}
	public JMenuItem getBcmOverkill() {
		return bcmOverkill;
	}
	public JMenuItem getBcmAccuracy() {
		return bcmAccuracy;
	}
	public JMenuItem getBcmUtility() {
		return bcmUtility;
	}
	
	public JMenuItem getExportCurrent() {
		return exportCurrent;
	}
	public JMenuItem getExportAll() {
		return exportAll;
	}
	
	public JMenuItem getMiscScreenshot() {
		return miscWeaponTabScreenshot;
	}
	public JMenuItem getMiscExport() {
		return miscExportCombination;
	}
	public JMenuItem getMiscLoad() {
		return miscLoadCombination;
	}
	public JMenuItem getMiscSuggestion() {
		return miscSuggestion;
	}
	public JMenuItem getMiscFAQ() {
		return miscFAQ;
	}
	public JMenuItem getMiscGlossary() {
		return miscGlossary;
	}

	public int getCurrentClassIndex() {
		return classTabs.getSelectedIndex();
	}
	
	public int getCurrentWeaponIndex() {
		int classIndex = classTabs.getSelectedIndex();
		if (classIndex == 0) {
			return drillerTabs.getSelectedIndex();
		}
		else if (classIndex == 1) {
			return engineerTabs.getSelectedIndex();
		}
		else if (classIndex == 2) {
			return gunnerTabs.getSelectedIndex();
		}
		else if (classIndex == 3) {
			return scoutTabs.getSelectedIndex();
		}
		else {	
			return -1;
		}
	}
	
	// This method gets called by GuiController; I use it to add it as an ActionListener to all buttons and menu items in the GUI
	public void activateButtonsAndMenus(ActionListener parent) {
		bcmIdealBurst.addActionListener(parent);
		bcmIdealSustained.addActionListener(parent);
		bcmSustainedWeakpoint.addActionListener(parent);
		bcmSustainedWeakpointAccuracy.addActionListener(parent);
		bcmIdealAdditional.addActionListener(parent);
		bcmMaxDmg.addActionListener(parent);
		bcmMaxNumTargets.addActionListener(parent);
		bcmDuration.addActionListener(parent);
		bcmTTK.addActionListener(parent);
		bcmOverkill.addActionListener(parent);
		bcmAccuracy.addActionListener(parent);
		bcmUtility.addActionListener(parent);
		
		exportCurrent.addActionListener(parent);
		exportAll.addActionListener(parent);
		
		miscWeaponTabScreenshot.addActionListener(parent);
		miscExportCombination.addActionListener(parent);
		miscLoadCombination.addActionListener(parent);
		miscSuggestion.addActionListener(parent);
		miscFAQ.addActionListener(parent);
		miscGlossary.addActionListener(parent);
	}
	
	@Override
	public void update(Observable o, Object arg) {
		// TODO: this if chain and for loop trick only works as long as only driller Weapons are in the drillerWeapons array ( + etc.) and there are no duplicate instantiations of one Weapon.
		// Realistically, it should be improved to do object ID matching to items in each of the arrays.
		
		// In theory, these if and for statements should work together to only update the one WeaponTab that got updated by a button click, instead of rebuilding every tab on every button click.
		String packageName = o.getClass().getPackageName();
		String weaponName = "";
		if (o instanceof Weapon) {
			weaponName = ((Weapon) o).getFullName();
		}
		
		if (packageName == "drillerWeapons") {
			for (int i = 0; i < drillerWeapons.length; i++) {
				if (drillerWeapons[i].getFullName() == weaponName) {
					drillerTabs.setComponentAt(i, new WeaponTab(drillerWeapons[i]));
					break;
				}
			}
		}
		else if (packageName == "engineerWeapons") {
			for (int i = 0; i < engineerWeapons.length; i++) {
				if (engineerWeapons[i].getFullName() == weaponName) {
					engineerTabs.setComponentAt(i, new WeaponTab(engineerWeapons[i]));
					break;
				}
			}
		}
		else if (packageName == "gunnerWeapons") {
			for (int i = 0; i < gunnerWeapons.length; i++) {
				if (gunnerWeapons[i].getFullName() == weaponName) {
					gunnerTabs.setComponentAt(i, new WeaponTab(gunnerWeapons[i]));
					break;
				}
			}
		}
		else if (packageName == "scoutWeapons") {
			for (int i = 0; i < scoutWeapons.length; i++) {
				if (scoutWeapons[i].getFullName() == weaponName) {
					scoutTabs.setComponentAt(i, new WeaponTab(scoutWeapons[i]));
					break;
				}
			}
		}

		repaint();
	}
	
	public BufferedImage getScreenshot() {
		// Sourced from https://stackoverflow.com/a/44019372
		BufferedImage img = new BufferedImage(classTabs.getWidth(), classTabs.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = img.createGraphics();
		classTabs.printAll(g2d);
		g2d.dispose();
		return img;
	}
	
	public void bringFAQtoForeground() {
		
	}
	
	public void bringGlossaryToForeground() {
		
	}
}
