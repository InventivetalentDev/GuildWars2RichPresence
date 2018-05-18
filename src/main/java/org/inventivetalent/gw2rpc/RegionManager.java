package org.inventivetalent.gw2rpc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.inventivetalent.gw2rpc.region.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class RegionManager {

	final Gw2Rpc main;

	Set<Integer>                                           floorIds       = new HashSet<>();
	java.util.Map<Integer, java.util.Map<Integer, Region>> floorRegionMap = new HashMap<>();

	public RegionManager(Gw2Rpc main) {
		this.main = main;

		System.out.println("Downloading Region Data...");
		JsonArray floors = main.apiHelper.getFloors();
		floors.forEach((JsonElement e) -> {
			System.out.println("Floor #" + e.getAsInt());
			floorIds.add(e.getAsInt());
			this.floorRegionMap.put(e.getAsInt(), main.apiHelper.getParsedRegionData(e.getAsInt()));
		});
		System.out.println(this.floorRegionMap);
		//		this.regionMap = main.apiHelper.getParsedRegionData();
	}

	public PointOfInterest findClosestPoi(Set<Integer> floors, RegionLocationInfo info, Point point) {
		double smallestDistance = Integer.MAX_VALUE;
		PointOfInterest closestPoi = null;
		for (Integer floor : floors) {
			java.util.Map<Integer, Region> regionMap = this.floorRegionMap.get(floor);
			if (regionMap == null) { continue; }
			Region region = regionMap.get(info.region.id);
			Map map = region.maps.get(info.map.id);
			for (Iterator<PointOfInterest> it = map.pois.values().iterator(); it.hasNext(); ) {
				PointOfInterest poi = it.next();

				double distance = poi.coord.distance(point);
				if (distance < smallestDistance) {
					closestPoi = poi;
					smallestDistance = distance;
				}
			}
		}
		return closestPoi;
	}

	public RegionLocationInfo getLocationInfoByCoords(Set<Integer> floors, int regionId, int mapId, Point coords) {
		for (Integer floor : floors) {
			java.util.Map<Integer, Region> regionMap = this.floorRegionMap.get(floor);
			if (regionMap == null) { continue; }
			Region region = regionMap.get(regionId);
			if (region == null) { return null; }
			Map map = region.maps.get(mapId);
			if (map == null) { return null; }
			for (Iterator<Sector> it2 = map.sectors.values().iterator(); it2.hasNext(); ) {
				Sector sector = it2.next();
				if (sector.bounds.contains(coords)) {
					return new RegionLocationInfo(region, map, sector);
				}
			}
		}
		return null;
	}

	//	public RegionLocationInfo getLocationInfoByCoords(Point coords) {
	//		for (Iterator<Region> it = this.regionMap.values().iterator(); it.hasNext(); ) {
	//			Region region = it.next();
	//			for (Iterator<Map> it1 = region.maps.values().iterator(); it1.hasNext(); ) {
	//				Map map = it1.next();
	//				for (Iterator<Sector> it2 = map.sectors.values().iterator(); it2.hasNext(); ) {
	//					Sector sector = it2.next();
	//					if (sector.bounds.contains(coords)) {
	//						return new RegionLocationInfo(region, map, sector);
	//					}
	//				}
	//			}
	//		}
	//		return null;
	//	}

}
