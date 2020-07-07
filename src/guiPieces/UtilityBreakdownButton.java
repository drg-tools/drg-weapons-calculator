package guiPieces;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import modelPieces.StatsRow;
import modelPieces.Weapon;

public class UtilityBreakdownButton extends JButton implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private JComponent parentComponent;
	private Weapon toDisplay;

	public UtilityBreakdownButton(JComponent parent, String textToDisplay, Weapon weaponWithStats) {
		parentComponent = parent;
		toDisplay = weaponWithStats;
		
		// Font color will be set by the parent WeaponTab, in constructCalculationsPanel()
		this.setBackground(GuiConstants.drgBackgroundBrown);
		this.setBorder(GuiConstants.orangeLine);
		
		this.setText(textToDisplay);
		this.setFont(GuiConstants.customFontBold);
		this.setHorizontalAlignment(SwingConstants.LEFT);
		this.addActionListener(this);
	}
	
	private JPanel getUtilityBreakdownPanel() {
		StatsRow[] utilityStats = toDisplay.utilityExplanation();
		
		JPanel toReturn = new JPanel();
		toReturn.setBackground(GuiConstants.drgBackgroundBrown);
		toReturn.setBorder(GuiConstants.blackLine);
		toReturn.setLayout(new BoxLayout(toReturn, BoxLayout.Y_AXIS));
		
		JPanel row, statIcon;
		JLabel statLabel, statValue;
		for (int i = 0; i < utilityStats.length; i++) {
			row = new JPanel();
			row.setOpaque(false);
			row.setLayout(new BorderLayout());
			
			statIcon = new StatsRowIconPanel(ButtonIcons.getModIcon(utilityStats[i].getIcon(), false));
			row.add(statIcon, BorderLayout.LINE_START);
			
			statLabel = new JLabel(utilityStats[i].getName());
			statLabel.setFont(GuiConstants.customFont);
			statLabel.setForeground(Color.white);
			row.add(statLabel, BorderLayout.CENTER);
			
			statValue = new JLabel(utilityStats[i].getValue());
			statValue.setFont(GuiConstants.customFont);
			statValue.setForeground(GuiConstants.drgRegularOrange);
			row.add(statValue, BorderLayout.LINE_END);
			
			row.setBorder(new EmptyBorder(0, GuiConstants.paddingPixels, 0, 2*GuiConstants.paddingPixels));
			
			toReturn.add(row);
		}
		
		return toReturn;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// Adapted from https://stackoverflow.com/a/13760416
		JOptionPane a = new JOptionPane(getUtilityBreakdownPanel(), JOptionPane.INFORMATION_MESSAGE);
		JDialog d = a.createDialog(null, "Utility Score Breakdown");
		d.setLocationRelativeTo(parentComponent);
		d.setVisible(true);
	}
}
