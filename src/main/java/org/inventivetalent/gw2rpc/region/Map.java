package org.inventivetalent.gw2rpc.region;

import java.util.HashMap;

public class Map {

	public int id;
	public String name;
	public java.util.Map<Integer, Sector> sectors = new HashMap<>();
	public java.util.Map<Integer, PointOfInterest> pois = new HashMap<>();

	@Override
	public String toString() {
		return "Map{" +
				"id=" + id +
				", name='" + name + '\'' +
				", sectors=" + sectors +
				'}';
	}
}
