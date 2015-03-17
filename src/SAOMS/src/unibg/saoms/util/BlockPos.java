package unibg.saoms.util;

import java.io.Serializable;

/**
 * This class is used to divide space in blocks and identify them with x and y coordinates
 *  
 * @author Yamuna Maccarana
 * @author Umberto Paramento
 * 
 */

public class BlockPos implements Serializable{

	private static final long serialVersionUID = 1L;
	
	public int x;
	public int y;
	
	// Constructors----------------------------------------------------------
	/**
	 * Constructor method for BlockPos (empty)
	 */
	public BlockPos(){
		x=0;
		y=0;
	}
	
	/**
	 * Constructor method for BlockPos
	 */
	public BlockPos(int nx, int ny) {
		this.x=nx;
		this.y=ny;
	}
	
	/**
	 * Constructor method for BlockPos
	 */
	public BlockPos(BlockPos bp) {
		this.x=bp.x;
		this.y=bp.y;
	}
	
	// Methods----------------------------------------------------------
	@Override
	public String toString(){
		return "("+x+","+y+")";
	}
	
	public boolean equals(BlockPos b){
		if(this.x==b.getX() && this.y==b.getY()){
			return true;
		}
		else{
			return false;
		}
	}
	
	//Getters and Setters	----------------------------------------------------
	/**
	 * Getter method for the X coordinate 
	 * @return returns the X coordinate 
	 */
	public int getX() {
		return x;
	}

	/**
	 * Setter method for the X coordinate 
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * Getter method for the Y coordinate 
	 * @return returns the Y coordinate 
	 */
	public int getY() {
		return y;
	}

	/**
	 * Setter method for the Y coordinate 
	 */
	public void setY(int y) {
		this.y = y;
	}

}
