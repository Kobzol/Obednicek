package com.ghrabuvka.obednicek;

import java.io.Serializable;

public class Obed implements Serializable{

	
	private static final long serialVersionUID = 2L;
	public int ID;
	public String Name, Stav, pStav1, pStav2, Objednano, omezObj, konecObj, konecCod;
	public String[] Properties = new String[8];
	public Den Datum;
	
	public Obed (int id, Den datum)
	{
		this.ID = id;
		this.Datum = datum;
	}
}
