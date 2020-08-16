package guiPieces;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import modelPieces.Weapon;

public class AccuracySliderButton extends JButton implements ActionListener, ChangeListener {
	private static final long serialVersionUID = 1L;
	
	private JComponent parentComponent;
	private Weapon toUpdate;

	public AccuracySliderButton(JComponent parent, String textToDisplay, Weapon weaponWithAccuracy) {
		parentComponent = parent;
		toUpdate = weaponWithAccuracy;
		
		// Font color will be set by the parent WeaponTab, in constructCalculationsPanel()
		this.setBackground(GuiConstants.drgBackgroundBrown);
		this.setBorder(GuiConstants.orangeLine);
		
		this.setText(textToDisplay);
		this.setFont(GuiConstants.customFontBold);
		this.setHorizontalAlignment(SwingConstants.LEFT);
		this.addActionListener(this);
	}
	
	// Adapted from https://docs.oracle.com/javase/tutorial/uiswing/components/slider.html
	private JPanel getAccuracySliderPanel() {
		JPanel toReturn = new JPanel();
		toReturn.setLayout(new BoxLayout(toReturn, BoxLayout.Y_AXIS));
		
		// Add JLabel for instructions/description
		String longText = "Change the distance that's used to estimate the Accuracy metrics";
		JLabel description = new JLabel(HoverText.breakLongToolTipString(longText, 40));
		description.setBorder(new EmptyBorder(0, 0, 2*GuiConstants.paddingPixels, 0));
		toReturn.add(description);
		
		// Add slider
		int currentDistance = (int) toUpdate.getAccuracyDistance();
		JSlider accuracyDistanceSlider = new JSlider(JSlider.HORIZONTAL, 1, 19, currentDistance);
		accuracyDistanceSlider.setMajorTickSpacing(3);
		accuracyDistanceSlider.setMajorTickSpacing(1);
		accuracyDistanceSlider.setPaintTicks(true);
		accuracyDistanceSlider.setPaintLabels(true);

		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(1, new JLabel("1m"));
		labelTable.put(4, new JLabel("4m"));
		labelTable.put(7, new JLabel("7m"));
		labelTable.put(10, new JLabel("10m"));
		labelTable.put(13, new JLabel("13m"));
		labelTable.put(16, new JLabel("16m"));
		labelTable.put(19, new JLabel("19m"));
		accuracyDistanceSlider.setLabelTable(labelTable);
		
		toReturn.add(accuracyDistanceSlider);
		
		// Enable listener for slider to make it update model in background
		accuracyDistanceSlider.addChangeListener(this);
		
		return toReturn;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// Adapted from https://stackoverflow.com/a/13760416
		JOptionPane a = new JOptionPane(getAccuracySliderPanel(), JOptionPane.INFORMATION_MESSAGE);
		JDialog d = a.createDialog(null, "AccuracyEstimator Distance");
		d.setLocationRelativeTo(parentComponent);
		d.setVisible(true);
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
	    JSlider source = (JSlider) e.getSource();
	    if (!source.getValueIsAdjusting()) {
	        toUpdate.setAccuracyDistance(source.getValue());
	    }
	}
}
