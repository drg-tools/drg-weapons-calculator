package guiPieces.accuracyEstimator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import guiPieces.GuiConstants;
import modelPieces.Weapon;

public class AccuracyVisualizerButton extends JButton implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private JComponent parentComponent;
	private Weapon toDisplay;

	public AccuracyVisualizerButton(JComponent parent, String textToDisplay, Weapon weaponWithStats) {
		parentComponent = parent;
		toDisplay = weaponWithStats;
		
		this.setBackground(GuiConstants.drgBackgroundBrown);
		this.setForeground(GuiConstants.drgHighlightedYellow);
		this.setBorder(GuiConstants.orangeLine);
		
		this.setText(textToDisplay);
		this.setFont(GuiConstants.customFontBold);
		this.setHorizontalAlignment(SwingConstants.LEFT);
		this.addActionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// Adapted from https://stackoverflow.com/a/13760416
		JOptionPane a = new JOptionPane(toDisplay.getVisualizerPanel(), JOptionPane.INFORMATION_MESSAGE);
		JDialog d = a.createDialog(null, "Accuracy Visualizer");
		d.setLocationRelativeTo(parentComponent);
		d.setVisible(true);
	}
}
