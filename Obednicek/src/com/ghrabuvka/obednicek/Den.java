package com.ghrabuvka.obednicek;

import java.io.Serializable;
import java.util.ArrayList;

public class Den implements Serializable{

	private static final long serialVersionUID = 1L;
	public int ID;
	public String Name;
	public ArrayList<Obed> Obedy = new ArrayList<Obed>();
	
	public Den(int id, String name)
	{
		this.ID = id;
		this.Name = name;
	}
	
	public void zmenStav(boolean stav, int index)
	{
		for (int i = 0; i < 2; i++)
		{
			this.Obedy.get(i).Stav = "O";
		}
		if (stav == true)
		{
			this.Obedy.get(index).Stav = "P";
		}
	}
	
	public void addObed(Obed obed)
	{
		if (this.Obedy.size() < 2)
		{
			this.Obedy.add(obed);
		}
	}
}