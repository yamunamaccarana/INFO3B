package unibg.saoms.exception;

/**
 *
 * @author Yamuna Maccarana
 * @author Umberto Paramento
 * 
 */

public class BatteryOutOfBoundException extends Exception {

	/**
	 * @author Yamuna Maccarana
	 * @author Umberto Paramento
	 */
	private static final long serialVersionUID = 967979291079455653L;
	
	public BatteryOutOfBoundException() {
        super("Battery level out of bound (0 to 100)");
    }

}