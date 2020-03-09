package guiPieces;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;

import net.sf.image4j.codec.ico.ICODecoder;

import modelPieces.Weapon;

public class View extends JFrame implements Observer {
	
	private JMenuBar menuBar;
	private JMenu bestCombinationsMenu;
	private JMenuItem bcmIdealBurst, bcmIdealSustained, bcmSustainedWeakpoint, bcmSustainedWeakpointAccuracy, bcmIdealAdditional, bcmMaxDmg, 
					bcmMaxNumTargets, bcmDuration, bcmTTK, bcmOverkill, bcmAccuracy, bcmUtility;
	private JMenu difficultyScalingMenu;
	private ButtonGroup dsHazGroup, dsPCGroup;
	private JRadioButton dsHaz1, dsHaz2, dsHaz3, dsHaz4, dsHaz5, dsPC1, dsPC2, dsPC3, dsPC4;
	private JMenu exportMenu;
	private JMenuItem exportCurrent, exportAll;
	private JMenu miscMenu;	
	private JMenuItem miscWeaponTabScreenshot, miscExportCombination, miscLoadCombination, miscSuggestion;
	
	private Weapon[] drillerWeapons;
	private Weapon[] engineerWeapons;
	private Weapon[] gunnerWeapons;
	private Weapon[] scoutWeapons;
	
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
		
		// FAQ
		FAQ = GlossaryTextAndFAQText.getFAQText();
		classTabs.addTab("F.A.Q.", FAQ);
		
		// Glossary
		glossary = GlossaryTextAndFAQText.getGlossaryText();
		classTabs.addTab("Glossary", glossary);
		
		add(classTabs);
		setContentPane(classTabs);
		pack();
		setVisible(true);
	}

	private void constructMenu() {
		menuBar = new JMenuBar();
		
		// Best Combinations menu
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
		
		// Difficulty Scaling menu
		difficultyScalingMenu = new JMenu("Difficulty Scaling");
		
		JPanel dsPanel = new JPanel();
		dsPanel.setLayout(new BorderLayout());
		
		JPanel labelsPanel = new JPanel();
		labelsPanel.setLayout(new GridLayout(2, 1));
		JLabel hazLabel = new JLabel("Hazard Level:");
		labelsPanel.add(hazLabel);
		JLabel pcLabel = new JLabel("Player Count:");
		labelsPanel.add(pcLabel);
		dsPanel.add(labelsPanel, BorderLayout.WEST);
		
		JPanel radioButtonsPanel = new JPanel();
		radioButtonsPanel.setLayout(new GridLayout(2, 5));
		dsHazGroup = new ButtonGroup();
		dsHaz1 = new JRadioButton("1");
		dsHazGroup.add(dsHaz1);
		radioButtonsPanel.add(dsHaz1);
		dsHaz2 = new JRadioButton("2");
		dsHazGroup.add(dsHaz2);
		radioButtonsPanel.add(dsHaz2);
		dsHaz3 = new JRadioButton("3");
		dsHazGroup.add(dsHaz3);
		radioButtonsPanel.add(dsHaz3);
		dsHaz4 = new JRadioButton("4", true);
		dsHazGroup.add(dsHaz4);
		radioButtonsPanel.add(dsHaz4);
		dsHaz5 = new JRadioButton("5");
		dsHazGroup.add(dsHaz5);
		radioButtonsPanel.add(dsHaz5);
		
		dsPCGroup = new ButtonGroup();
		dsPC1 = new JRadioButton("1", true);
		dsPCGroup.add(dsPC1);
		radioButtonsPanel.add(dsPC1);
		dsPC2 = new JRadioButton("2");
		dsPCGroup.add(dsPC2);
		radioButtonsPanel.add(dsPC2);
		dsPC3 = new JRadioButton("3");
		dsPCGroup.add(dsPC3);
		radioButtonsPanel.add(dsPC3);
		dsPC4 = new JRadioButton("4");
		dsPCGroup.add(dsPC4);
		radioButtonsPanel.add(dsPC4);
		radioButtonsPanel.add(new JLabel());
		dsPanel.add(radioButtonsPanel, BorderLayout.CENTER);
		
		difficultyScalingMenu.add(dsPanel);
		menuBar.add(difficultyScalingMenu);
		
		// Export Stats to CSV menu
		exportMenu = new JMenu("Export Stats to CSV");
		exportCurrent = new JMenuItem("Export current weapon");
		exportMenu.add(exportCurrent);
		exportAll = new JMenuItem("Export all weapons");
		exportMenu.add(exportAll);
		menuBar.add(exportMenu);
		
		// Miscellaneous Actions menu
		miscMenu = new JMenu("Misc. Actions");
		miscWeaponTabScreenshot = new JMenuItem("Save screenshot of current build");
		miscMenu.add(miscWeaponTabScreenshot);
		miscExportCombination = new JMenuItem("Export current weapon combination");
		miscMenu.add(miscExportCombination);
		miscLoadCombination = new JMenuItem("Load combination for current weapon");
		miscMenu.add(miscLoadCombination);
		miscSuggestion = new JMenuItem("Suggest a change for this program");
		miscMenu.add(miscSuggestion);
		menuBar.add(miscMenu);
		
		setJMenuBar(menuBar);
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
	
	public JRadioButton getDSHaz1() {
		return dsHaz1;
	}
	public JRadioButton getDSHaz2() {
		return dsHaz2;
	}
	public JRadioButton getDSHaz3() {
		return dsHaz3;
	}
	public JRadioButton getDSHaz4() {
		return dsHaz4;
	}
	public JRadioButton getDSHaz5() {
		return dsHaz5;
	}
	public JRadioButton getDSPC1() {
		return dsPC1;
	}
	public JRadioButton getDSPC2() {
		return dsPC2;
	}
	public JRadioButton getDSPC3() {
		return dsPC3;
	}
	public JRadioButton getDSPC4() {
		return dsPC4;
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
		
		dsHaz1.addActionListener(parent);
		dsHaz2.addActionListener(parent);
		dsHaz3.addActionListener(parent);
		dsHaz4.addActionListener(parent);
		dsHaz5.addActionListener(parent);
		dsPC1.addActionListener(parent);
		dsPC2.addActionListener(parent);
		dsPC3.addActionListener(parent);
		dsPC4.addActionListener(parent);
		
		exportCurrent.addActionListener(parent);
		exportAll.addActionListener(parent);
		
		miscWeaponTabScreenshot.addActionListener(parent);
		miscExportCombination.addActionListener(parent);
		miscLoadCombination.addActionListener(parent);
		miscSuggestion.addActionListener(parent);
	}
	
	public void updateDifficultyScaling() {
		// TODO: this is a really sucky solution to update the Hazard Level/Player Count. I'd like to refactor this if possible.
		int i;
		for (i = 0; i < drillerWeapons.length; i++) {
			drillerTabs.setComponentAt(i, new WeaponTab(drillerWeapons[i]));
		}
		for (i = 0; i < engineerWeapons.length; i++) {
			engineerTabs.setComponentAt(i, new WeaponTab(engineerWeapons[i]));
		}
		for (i = 0; i < gunnerWeapons.length; i++) {
			gunnerTabs.setComponentAt(i, new WeaponTab(gunnerWeapons[i]));
		}
		for (i = 0; i < scoutWeapons.length; i++) {
			scoutTabs.setComponentAt(i, new WeaponTab(scoutWeapons[i]));
		}
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
}
