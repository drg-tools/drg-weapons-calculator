package guiPieces;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;

import utilities.ResourceLoader;

public class CustomCursors {
	public static Cursor defaultCursor = Toolkit.getDefaultToolkit().createCustomCursor(ResourceLoader.loadIcoFile("/images/drg_default_cursor.ico").get(0), new Point(2, 13), "drg_default");
	public static Cursor defaultCursorPlusQuestionMark = Toolkit.getDefaultToolkit().createCustomCursor(ResourceLoader.loadIcoFile("/images/drg_cursor_with_qmark.ico").get(0), new Point(2, 13), "drg_question_mark");
}
