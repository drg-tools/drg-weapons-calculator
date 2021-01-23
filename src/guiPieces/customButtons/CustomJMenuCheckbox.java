package guiPieces.customButtons;

import java.awt.event.MouseEvent;

import javax.swing.JCheckBoxMenuItem;

/*
	By default, JCheckBoxMenuItem closes the JMenu it's inside when it gets toggled. To override that, I found this article on StackOverflow: https://stackoverflow.com/a/34032642
*/
public class CustomJMenuCheckbox extends JCheckBoxMenuItem {
	private static final long serialVersionUID = 1L;
	
	public CustomJMenuCheckbox(String itemText) {
		super(itemText);
	}

	@Override
    protected void processMouseEvent(MouseEvent evt) {
        if (evt.getID() == MouseEvent.MOUSE_RELEASED && contains(evt.getPoint())) {
            doClick();
            setArmed(true);
        } else {
            super.processMouseEvent(evt);
        }
    }
}
