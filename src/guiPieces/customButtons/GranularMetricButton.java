package guiPieces.customButtons;

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

import guiPieces.GuiConstants;
import guiPieces.StatsRowIconPanel;
import guiPieces.customButtons.ButtonIcons.modIcons;
import modelPieces.StatsRow;

public class GranularMetricButton extends JButton implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private JComponent parentComponent;
	private String popoutTitle;
	private StatsRow[] toDisplay;

	public GranularMetricButton(JComponent parent, String textToDisplay, String title, StatsRow[] granularStats) {
		parentComponent = parent;
		popoutTitle = title;
		toDisplay = granularStats;
		
		// Font color will be set by the parent WeaponTab, in constructCalculationsPanel()
		this.setBackground(GuiConstants.drgBackgroundBrown);
		this.setBorder(GuiConstants.orangeLine);
		
		this.setText(textToDisplay);
		this.setFont(GuiConstants.customFontBold);
		this.setHorizontalAlignment(SwingConstants.LEFT);
		this.addActionListener(this);
	}
	
	private JPanel getGranularStatsPanel() {JPanel toReturn = new JPanel();
		toReturn.setBackground(GuiConstants.drgBackgroundBrown);
		toReturn.setBorder(GuiConstants.blackLine);
		toReturn.setLayout(new BoxLayout(toReturn, BoxLayout.Y_AXIS));
		
		modIcons statsRowIcon;
		JPanel row, statIcon;
		JLabel statLabel, statValue;
		for (int i = 0; i < toDisplay.length; i++) {
			row = new JPanel();
			row.setOpaque(false);
			row.setLayout(new BorderLayout());
			
			statLabel = new JLabel(toDisplay[i].getName());
			statLabel.setFont(GuiConstants.customFont);
			statLabel.setForeground(Color.white);
			
			statsRowIcon = toDisplay[i].getIcon();
			if (statsRowIcon != null) {
				statIcon = new StatsRowIconPanel(ButtonIcons.getModIcon(statsRowIcon, false));
				row.add(statIcon, BorderLayout.LINE_START);
				row.add(statLabel, BorderLayout.CENTER);
			}
			else {
				row.add(statLabel, BorderLayout.LINE_START);
			}
			
			statValue = new JLabel(toDisplay[i].getValue());
			statValue.setFont(GuiConstants.customFont);
			statValue.setForeground(GuiConstants.drgRegularOrange);
			row.add(statValue, BorderLayout.LINE_END);
			
			row.setBorder(new EmptyBorder(0, GuiConstants.paddingPixels, 0, GuiConstants.paddingPixels));
			
			toReturn.add(row);
		}
		
		return toReturn;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// Adapted from https://stackoverflow.com/a/13760416
		JOptionPane a = new JOptionPane(getGranularStatsPanel(), JOptionPane.INFORMATION_MESSAGE);
		JDialog d = a.createDialog(null, popoutTitle);
		d.setLocationRelativeTo(parentComponent);
		d.setVisible(true);
	}
}
