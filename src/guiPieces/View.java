package guiPieces;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;

import modelPieces.Weapon;

public class View extends JFrame implements Observer {
	
	private JMenuBar menuBar;
	private JMenu bestCombinationsMenu;
	private JMenuItem bcmBurst, bcmSustained, bcmAdditional, bcmMaxDmg, bcmMaxNumTargets, bcmDuration;
	private JMenu exportMenu;
	private JMenuItem exportCurrent, exportAll;
	private JMenu miscMenu;	
	private JMenuItem miscExport, miscLoad;
	
	private Weapon[] drillerWeapons;
	private Weapon[] engineerWeapons;
	private Weapon[] gunnerWeapons;
	private Weapon[] scoutWeapons;
	
	private JTabbedPane classTabs;
	private JTabbedPane drillerTabs;
	private JTabbedPane engineerTabs;
	private JTabbedPane gunnerTabs;
	private JTabbedPane scoutTabs;
	
	public View(Weapon[] dWeapons, Weapon[] eWeapons, Weapon[] gWeapons, Weapon[] sWeapons) {
		drillerWeapons = dWeapons;
		engineerWeapons = eWeapons;
		gunnerWeapons = gWeapons;
		scoutWeapons = sWeapons;
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("MeatShield's DRG DPS Calculator (DRG Update 28.5)");
		setPreferredSize(new Dimension(1620, 780));
		
		constructMenu();
		
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
		
		add(classTabs);
		setContentPane(classTabs);
		pack();
		setVisible(true);
	}

	private void constructMenu() {
		menuBar = new JMenuBar();
		
		bestCombinationsMenu = new JMenu("Best Combinations");
		bcmBurst = new JMenuItem("Best Ideal Burst DPS");
		bestCombinationsMenu.add(bcmBurst);
		bcmSustained = new JMenuItem("Best Ideal Sustained DPS");
		bestCombinationsMenu.add(bcmSustained);
		bcmAdditional = new JMenuItem("Best Additional Target DPS");
		bestCombinationsMenu.add(bcmAdditional);
		bcmMaxDmg = new JMenuItem("Most Multi-Target Damage");
		bestCombinationsMenu.add(bcmMaxDmg);
		bcmMaxNumTargets = new JMenuItem("Most Number of Targets Hit");
		bestCombinationsMenu.add(bcmMaxNumTargets);
		bcmDuration = new JMenuItem("Longest Firing Duration");
		bestCombinationsMenu.add(bcmDuration);
		menuBar.add(bestCombinationsMenu);
		
		exportMenu = new JMenu("Export Stats to CSV");
		exportCurrent = new JMenuItem("Export current weapon");
		exportMenu.add(exportCurrent);
		exportAll = new JMenuItem("Export all weapons");
		exportMenu.add(exportAll);
		menuBar.add(exportMenu);
		
		miscMenu = new JMenu("Misc. Actions");
		miscExport = new JMenuItem("Export current weapon combination");
		miscMenu.add(miscExport);
		miscLoad = new JMenuItem("Load combination for current weapon");
		miscMenu.add(miscLoad);
		menuBar.add(miscMenu);
		
		this.setJMenuBar(menuBar);
	}
	
	// Getters used by GuiController
	public JMenuItem getBcmBurst() {
		return bcmBurst;
	}
	public JMenuItem getBcmSustained() {
		return bcmSustained;
	}
	public JMenuItem getBcmAdditional() {
		return bcmAdditional;
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
	public JMenuItem getExportCurrent() {
		return exportCurrent;
	}
	public JMenuItem getExportAll() {
		return exportAll;
	}
	public JMenuItem getMiscExport() {
		return miscExport;
	}
	public JMenuItem getMiscLoad() {
		return miscLoad;
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
		bcmBurst.addActionListener(parent);
		bcmSustained.addActionListener(parent);
		bcmAdditional.addActionListener(parent);
		bcmMaxDmg.addActionListener(parent);
		bcmMaxNumTargets.addActionListener(parent);
		bcmDuration.addActionListener(parent);
		exportCurrent.addActionListener(parent);
		exportAll.addActionListener(parent);
		miscExport.addActionListener(parent);
		miscLoad.addActionListener(parent);
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
}
