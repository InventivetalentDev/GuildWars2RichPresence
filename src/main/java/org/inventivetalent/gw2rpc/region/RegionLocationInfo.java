package org.inventivetalent.gw2rpc.region;

public class RegionLocationInfo {

	public Region region;
	public Map    map;
	public Sector sector;

	public RegionLocationInfo(Region region, Map map, Sector sector) {
		this.region = region;
		this.map = map;
		this.sector = sector;
	}

	@Override
	public String toString() {
		return "RegionLocationInfo{" +
				"region=" + region +
				", map=" + map +
				", sector=" + sector +
				'}';
	}
}
