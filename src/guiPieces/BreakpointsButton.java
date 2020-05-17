package guiPieces;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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

public class BreakpointsButton extends JButton implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private JComponent parentComponent;
	private Weapon toDisplay;

	public BreakpointsButton(JComponent parent, String textToDisplay, Weapon weaponWithStats) {
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
	
	private JPanel getBreakpointsPanel() {
		StatsRow[] breakpoints = toDisplay.breakpointsExplanation();
		
		JPanel toReturn = new JPanel();
		toReturn.setPreferredSize(new Dimension(300, 160));
		toReturn.setBackground(GuiConstants.drgBackgroundBrown);
		toReturn.setBorder(GuiConstants.blackLine);
		toReturn.setLayout(new BoxLayout(toReturn, BoxLayout.Y_AXIS));
		
		JPanel row;
		JLabel statLabel, statValue;
		int paddingPixels = 2*GuiConstants.paddingPixels;
		for (int i = 0; i < breakpoints.length; i++) {
			row = new JPanel();
			row.setOpaque(false);
			row.setLayout(new BorderLayout());
			
			statLabel = new JLabel(breakpoints[i].getName());
			statLabel.setFont(GuiConstants.customFont);
			statLabel.setForeground(Color.white);
			// Left-pad the label text
			statLabel.setBorder(new EmptyBorder(0, paddingPixels, 0, 0));
			row.add(statLabel, BorderLayout.LINE_START);
			
			statValue = new JLabel(breakpoints[i].getValue());
			statValue.setFont(GuiConstants.customFont);
			statValue.setForeground(GuiConstants.drgRegularOrange);
			// Right-pad the value text
			statValue.setBorder(new EmptyBorder(0, 0, 0, paddingPixels));
			row.add(statValue, BorderLayout.LINE_END);
			
			toReturn.add(row);
		}
		
		return toReturn;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// Adapted from https://stackoverflow.com/a/13760416
		JOptionPane a = new JOptionPane(getBreakpointsPanel(), JOptionPane.INFORMATION_MESSAGE);
		JDialog d = a.createDialog(null, "Breakpoints");
		d.setLocationRelativeTo(parentComponent);
		d.setVisible(true);
	}
}
