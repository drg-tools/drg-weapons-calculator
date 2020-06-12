package guiPieces;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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

import modelPieces.Weapon;
import utilities.ResourceLoader;

public class View extends JFrame implements Observer {
	
	private JMenuBar menuBar;
	private JMenu overallBestCombinationsMenu;
	private JMenuItem[] overallBestCombinations;
	private JMenu subsetBestCombinationsMenu;
	private JMenuItem[] subsetBestCombinations;
	private JMenu difficultyScalingMenu;
	private ButtonGroup dsHazGroup, dsPCGroup;
	private JRadioButton dsHaz1, dsHaz2, dsHaz3, dsHaz4, dsHaz5, dsPC1, dsPC2, dsPC3, dsPC4;
	private JMenu exportMenu;
	private JMenuItem exportCurrent, exportAll, exportMySQL;
	private JMenu miscMenu;	
	private JMenuItem miscWeaponTabScreenshot, miscExportCombination, miscLoadCombination, miscSuggestion;
	
	private Weapon[] drillerWeapons;
	private Weapon[] engineerWeapons;
	private Weapon[] gunnerWeapons;
	private Weapon[] scoutWeapons;
	
	private JTabbedPane mainTabs;
	private JTabbedPane drillerTabs;
	private JTabbedPane engineerTabs;
	private JTabbedPane gunnerTabs;
	private JTabbedPane scoutTabs;
	private JTabbedPane infoTabs;
	
	private ThinkingCursorAnimation TCA;
	
	/*
		It looks like they use the paid-for font "Aktiv Grotesk Cd". However, to keep this free, I'm choosing to use font "Roboto Condensed" because it's an open-source font
	*/
	
	public View(Weapon[] dWeapons, Weapon[] eWeapons, Weapon[] gWeapons, Weapon[] sWeapons) {
		drillerWeapons = dWeapons;
		engineerWeapons = eWeapons;
		gunnerWeapons = gWeapons;
		scoutWeapons = sWeapons;
		
		TCA = new ThinkingCursorAnimation(this);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("MeatShield's DRG DPS Calculator (DRG Update 31.0)");
		setPreferredSize(new Dimension(1500, 950));
		
		// Add the icon
		setIconImages(ResourceLoader.loadIcoFile("/images/meatShield_composite.ico"));
		
		// Set the custom cursor
		setCursor(CustomCursors.defaultCursor);
		
		constructMenu();
		
		mainTabs = new JTabbedPane();
		
		// Driller
		drillerTabs = new JTabbedPane();
		for (int i = 0; i < drillerWeapons.length; i++) {
			drillerWeapons[i].addObserver(this);
			drillerTabs.addTab(drillerWeapons[i].getFullName(), new WeaponTab(drillerWeapons[i]));
		}
		mainTabs.addTab("Driller", drillerTabs);
		
		// Engineer
		engineerTabs = new JTabbedPane();
		for (int i = 0; i < engineerWeapons.length; i++) {
			engineerWeapons[i].addObserver(this);
			engineerTabs.addTab(engineerWeapons[i].getFullName(), new WeaponTab(engineerWeapons[i]));
		}
		mainTabs.addTab("Engineer", engineerTabs);
		
		// Gunner
		gunnerTabs = new JTabbedPane();
		for (int i = 0; i < gunnerWeapons.length; i++) {
			gunnerWeapons[i].addObserver(this);
			gunnerTabs.addTab(gunnerWeapons[i].getFullName(), new WeaponTab(gunnerWeapons[i]));
		}
		mainTabs.addTab("Gunner", gunnerTabs);
		
		// Scout
		scoutTabs = new JTabbedPane();
		for (int i = 0; i < scoutWeapons.length; i++) {
			scoutWeapons[i].addObserver(this);
			scoutTabs.addTab(scoutWeapons[i].getFullName(), new WeaponTab(scoutWeapons[i]));
		}
		mainTabs.addTab("Scout", scoutTabs);
		
		// Information
		infoTabs = new JTabbedPane();
		infoTabs.addTab("Metrics Explanation", InformationTabsText.getMetricsExplanation());
		infoTabs.addTab("F.A.Q.", InformationTabsText.getFAQText());
		infoTabs.addTab("Glossary", InformationTabsText.getGlossaryText());
		infoTabs.addTab("Acknowledgements", InformationTabsText.getAcknowledgementsText());
		mainTabs.addTab("Information", infoTabs);
		
		add(mainTabs);
		setContentPane(mainTabs);
		pack();
		
		// Have this automatically open in the center of the screen
		setLocationRelativeTo(null);
		
		setVisible(true);
	}
	
	public void activateThinkingCursor() {
		TCA.toggleAnimation();
		new Thread(TCA).start();
	}
	
	public void deactivateThinkingCursor() {
		TCA.toggleAnimation();
	}

	private void constructMenu() {
		menuBar = new JMenuBar();
		
		// Overall Best Combinations menu
		overallBestCombinations = new JMenuItem[15];
		overallBestCombinations[0] = new JMenuItem("Best Ideal Burst DPS");
		overallBestCombinations[1] = new JMenuItem("Best Ideal Sustained DPS");
		overallBestCombinations[2] = new JMenuItem("Best Sustained + Weakpoint DPS");
		overallBestCombinations[3] = new JMenuItem("Best Sustained + Weakpoint + Accuracy DPS");
		overallBestCombinations[4] = new JMenuItem("Best Additional Target DPS");
		overallBestCombinations[5] = new JMenuItem("Most Number of Targets Hit");
		overallBestCombinations[6] = new JMenuItem("Most Multi-Target Damage");
		overallBestCombinations[7] = new JMenuItem("Most Ammo Efficient");
		overallBestCombinations[8] = new JMenuItem("Highest General Accuracy");
		overallBestCombinations[9] = new JMenuItem("Highest Weakpoint Accuracy");
		overallBestCombinations[10] = new JMenuItem("Longest Firing Duration");
		overallBestCombinations[11] = new JMenuItem("Lowest Avg Overkill");
		overallBestCombinations[12] = new JMenuItem("Fastest Avg Time To Kill");
		overallBestCombinations[13] = new JMenuItem("Lowest Breakpoints");
		overallBestCombinations[14] = new JMenuItem("Most Utility");
		
		// Subset Best Combinations menu
		subsetBestCombinations = new JMenuItem[15];
		subsetBestCombinations[0] = new JMenuItem("Best Ideal Burst DPS");
		subsetBestCombinations[1] = new JMenuItem("Best Ideal Sustained DPS");
		subsetBestCombinations[2] = new JMenuItem("Best Sustained + Weakpoint DPS");
		subsetBestCombinations[3] = new JMenuItem("Best Sustained + Weakpoint + Accuracy DPS");
		subsetBestCombinations[4] = new JMenuItem("Best Additional Target DPS");
		subsetBestCombinations[5] = new JMenuItem("Most Number of Targets Hit");
		subsetBestCombinations[6] = new JMenuItem("Most Multi-Target Damage");
		subsetBestCombinations[7] = new JMenuItem("Most Ammo Efficient");
		subsetBestCombinations[8] = new JMenuItem("Highest General Accuracy");
		subsetBestCombinations[9] = new JMenuItem("Highest Weakpoint Accuracy");
		subsetBestCombinations[10] = new JMenuItem("Longest Firing Duration");
		subsetBestCombinations[11] = new JMenuItem("Lowest Avg Overkill");
		subsetBestCombinations[12] = new JMenuItem("Fastest Avg Time To Kill");
		subsetBestCombinations[13] = new JMenuItem("Lowest Breakpoints");
		subsetBestCombinations[14] = new JMenuItem("Most Utility");
		
		overallBestCombinationsMenu = new JMenu("Best Combinations (All)");
		subsetBestCombinationsMenu = new JMenu("Best Combinations (Subset)");
		
		// This for loop depends on overallBestCombinations and subsetBestCombinations being the same length
		for (int i = 0; i < overallBestCombinations.length; i++) {
			overallBestCombinationsMenu.add(overallBestCombinations[i]);
			subsetBestCombinationsMenu.add(subsetBestCombinations[i]);
		}
		menuBar.add(overallBestCombinationsMenu);
		menuBar.add(subsetBestCombinationsMenu);
		
		// Difficulty Scaling menu
		difficultyScalingMenu = new JMenu("Difficulty Scaling");
		
		JPanel dsPanel = new JPanel();
		dsPanel.setLayout(new BorderLayout());
		
		JPanel labelsPanel = new JPanel();
		labelsPanel.setLayout(new GridLayout(2, 1));
		JLabel hazLabel = new JLabel("  Hazard Level:");
		labelsPanel.add(hazLabel);
		JLabel pcLabel = new JLabel("  Player Count:");
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
		dsPC1 = new JRadioButton("1");
		dsPCGroup.add(dsPC1);
		radioButtonsPanel.add(dsPC1);
		dsPC2 = new JRadioButton("2");
		dsPCGroup.add(dsPC2);
		radioButtonsPanel.add(dsPC2);
		dsPC3 = new JRadioButton("3");
		dsPCGroup.add(dsPC3);
		radioButtonsPanel.add(dsPC3);
		dsPC4 = new JRadioButton("4", true);
		dsPCGroup.add(dsPC4);
		radioButtonsPanel.add(dsPC4);
		radioButtonsPanel.add(new JLabel());
		dsPanel.add(radioButtonsPanel, BorderLayout.CENTER);
		
		difficultyScalingMenu.add(dsPanel);
		menuBar.add(difficultyScalingMenu);
		
		// Export Stats to CSV menu
		exportMenu = new JMenu("Export Data");
		exportCurrent = new JMenuItem("Export current weapon to CSV");
		exportMenu.add(exportCurrent);
		exportAll = new JMenuItem("Export all weapons to CSV");
		exportMenu.add(exportAll);
		exportMySQL = new JMenuItem("Export all weapons to MySQL");
		exportMenu.add(exportMySQL);
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
	public JMenuItem getOverallBestCombination(int index) {
		if (index < 0 || index > overallBestCombinations.length - 1) {
			return null;
		}
		
		return overallBestCombinations[index];
	}
	
	public JMenuItem getSubsetBestCombination(int index) {
		if (index < 0 || index > subsetBestCombinations.length - 1) {
			return null;
		}
		
		return subsetBestCombinations[index];
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
	public JMenuItem getExportMySQL() {
		return exportMySQL;
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
		return mainTabs.getSelectedIndex();
	}
	
	public int getCurrentWeaponIndex() {
		int classIndex = mainTabs.getSelectedIndex();
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
		for (int i = 0; i < overallBestCombinations.length; i++) {
			overallBestCombinations[i].addActionListener(parent);
			subsetBestCombinations[i].addActionListener(parent);
		}
		
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
		exportMySQL.addActionListener(parent);
		
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
		String packageName, weaponName;
		if (o instanceof Weapon) {
			packageName = ((Weapon) o).getDwarfClass();
			weaponName = ((Weapon) o).getFullName();
		}
		else {
			return;
		}
		
		if (packageName == "Driller") {
			for (int i = 0; i < drillerWeapons.length; i++) {
				if (drillerWeapons[i].getFullName() == weaponName) {
					drillerTabs.setComponentAt(i, new WeaponTab(drillerWeapons[i]));
					break;
				}
			}
		}
		else if (packageName == "Engineer") {
			for (int i = 0; i < engineerWeapons.length; i++) {
				if (engineerWeapons[i].getFullName() == weaponName) {
					engineerTabs.setComponentAt(i, new WeaponTab(engineerWeapons[i]));
					break;
				}
			}
		}
		else if (packageName == "Gunner") {
			for (int i = 0; i < gunnerWeapons.length; i++) {
				if (gunnerWeapons[i].getFullName() == weaponName) {
					gunnerTabs.setComponentAt(i, new WeaponTab(gunnerWeapons[i]));
					break;
				}
			}
		}
		else if (packageName == "Scout") {
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
		BufferedImage img = new BufferedImage(mainTabs.getWidth(), mainTabs.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = img.createGraphics();
		mainTabs.printAll(g2d);
		g2d.dispose();
		return img;
	}
}
