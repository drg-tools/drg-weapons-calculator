package utilities;

import java.util.ArrayList;

public class ConditionalArrayList<E> extends ArrayList<E> {
	private static final long serialVersionUID = 1L;

	public void conditionalAdd(E toAdd, boolean condition) {
		if (condition) {
			add(toAdd);
		}
	}
}
