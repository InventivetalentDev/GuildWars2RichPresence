package org.inventivetalent.gw2rpc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.inventivetalent.gw2rpc.region.*;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class APIHelper {

	java.util.Map<Integer, JsonObject> mapCache = new HashMap<>();

	public JsonObject getMapData(int id) {
		if (mapCache.containsKey(id)) { return mapCache.get(id); }
		JsonObject json = getData("/maps/" + id).getAsJsonObject();
		if (json != null) {
			mapCache.put(id, json);
		}
		return json;
	}

	public JsonArray getFloors() {
		return getData("/continents/1/floors").getAsJsonArray();
	}

	public JsonArray getRegionData(int floor) {
		return getData("/continents/1/floors/" + floor + "/regions?ids=all").getAsJsonArray();
	}

	public java.util.Map<Integer, Region> getParsedRegionData(int floor) {
		JsonArray raw = getRegionData(floor);
		java.util.Map<Integer, Region> regionMap = new HashMap<>();

		raw.forEach((JsonElement json) -> {
			JsonObject regionObject = json.getAsJsonObject();
			Region region = new Region();
			region.id = regionObject.get("id").getAsInt();
			region.name = regionObject.get("name").getAsString();

			JsonObject rawMaps = regionObject.get("maps").getAsJsonObject();
			rawMaps.keySet().forEach((String mapId) -> {
				JsonObject mapObject = rawMaps.get(mapId).getAsJsonObject();
				Map map = new Map();
				map.id = mapObject.get("id").getAsInt();
				map.name = mapObject.has("name") ? mapObject.get("name").getAsString() : "";

				JsonObject rawPois = mapObject.get("points_of_interest").getAsJsonObject();
				rawPois.keySet().forEach((String poiId) -> {
					JsonObject poiObject = rawPois.get(poiId).getAsJsonObject();
					PointOfInterest poi = new PointOfInterest();
					poi.id = poiObject.get("id").getAsInt();
					poi.name = poiObject.has("name") ? poiObject.get("name").getAsString() : "n/a";
					poi.floor = poiObject.get("floor").getAsInt();
					poi.type = poiObject.get("type").getAsString();
					poi.coord = Point.fromJson(poiObject.get("coord").getAsJsonArray());
					poi.chatLink = poiObject.get("chat_link").getAsString();

					map.pois.put(poi.id, poi);
				});

				JsonObject rawSectors = mapObject.get("sectors").getAsJsonObject();
				rawSectors.keySet().forEach((String sectorId) -> {
					JsonObject sectorObject = rawSectors.get(sectorId).getAsJsonObject();
					Sector sector = new Sector();
					sector.id = sectorObject.get("id").getAsInt();
					sector.name = sectorObject.has("name") ? sectorObject.get("name").getAsString() : "";

					JsonArray rawBounds = sectorObject.get("bounds").getAsJsonArray();
					Point[] pointArray = new Point[rawBounds.size()];
					for (int i = 0; i < rawBounds.size(); i++) {
						JsonArray rawPoint = rawBounds.get(i).getAsJsonArray();
						pointArray[i] = new Point(rawPoint.get(0).getAsDouble(), rawPoint.get(1).getAsDouble());
					}
					sector.bounds = new Polygon(pointArray);

					map.sectors.put(sector.id, sector);
				});

				region.maps.put(map.id, map);
			});

			regionMap.put(region.id, region);
		});

		return regionMap;
	}

	private JsonElement getData(String path) {
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL("https://api.guildwars2.com/v2" + path).openConnection();
			return new JsonParser().parse(new InputStreamReader(connection.getInputStream()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
