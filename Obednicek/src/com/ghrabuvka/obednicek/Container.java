package com.ghrabuvka.obednicek;

public class Container {

	public int ID, denID, obedID;
	public boolean isValid;
	public String name;
	
	public Container(int id, int den, int obed, boolean valid)
	{
		this.denID = den;
		this.obedID = obed;
		this.isValid = valid;
		this.ID = id;
	}
}
