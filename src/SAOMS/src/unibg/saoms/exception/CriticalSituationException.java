package unibg.saoms.exception;

/**
 *
 * @author Yamuna Maccarana
 * @author Umberto Paramento
 * 
 */

public class CriticalSituationException extends Exception {

	/**
	 * @author Yamuna Maccarana
	 * @author Umberto Paramento
	 */
	private static final long serialVersionUID = 967979291079455653L;
	
	public CriticalSituationException() {
        super("Fatal Error! Critical situation! Mainteinance needed!");
    }

}