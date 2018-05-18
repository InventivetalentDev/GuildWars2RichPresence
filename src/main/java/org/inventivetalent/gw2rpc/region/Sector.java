package org.inventivetalent.gw2rpc.region;

public class Sector {

	public int     id;
	public String  name;
	public Point   coord;
	public Polygon bounds;

	@Override
	public String toString() {
		return "Sector{" +
				"id=" + id +
				", name='" + name + '\'' +
				", coord=" + coord +
				", bounds=" + bounds +
				'}';
	}

}
