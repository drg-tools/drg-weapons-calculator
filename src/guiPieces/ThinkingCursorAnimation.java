package guiPieces;

import java.awt.Cursor;

import javax.swing.JFrame;

// Adapted from http://www.java2s.com/Tutorial/Java/0240__Swing/ChangeCursorinathreadforanimation.htm
public class ThinkingCursorAnimation implements Runnable {
	
	private JFrame guiToChangeCursor;
	private boolean animate;
	private int msDelayBetweenFrames;
	private Cursor[] animatedCursorFrames;
	
	public ThinkingCursorAnimation(JFrame gui) {
		guiToChangeCursor = gui;
		animate = false;
		msDelayBetweenFrames = 100;
		animatedCursorFrames = CustomCursors.thinkingCursor;
	}
	
	public void toggleAnimation() {
		animate = !animate;
	}

	@Override
	public void run() {
		int counter = 0;
		while (animate) {
			guiToChangeCursor.setCursor(animatedCursorFrames[counter]);
			counter = (counter + 1) % animatedCursorFrames.length;
			
			try {
				Thread.currentThread().sleep(msDelayBetweenFrames);
			} 
			catch (Exception e) {
			
			}
		}
		
		// After the while loop is done, set it back to the normal Cursor
		guiToChangeCursor.setCursor(CustomCursors.defaultCursor);
	}

}
