package org.inventivetalent.gw2rpc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.jagrosh.discordipc.entities.RichPresence;
import org.inventivetalent.gw2rpc.region.Point;
import org.inventivetalent.gw2rpc.region.PointOfInterest;
import org.inventivetalent.gw2rpc.region.RegionLocationInfo;

import java.time.OffsetDateTime;
import java.util.Set;

public class PresenceUpdater extends Thread {

	private static final long   UPDATE_RATE     = 2000;
	private static final double UNIT_MULTIPLIER = 1 / .0254;

	final Gw2Rpc main;

	OffsetDateTime startTime = OffsetDateTime.now();
	boolean        active    = true;

	Point              playerLocation;
	RegionLocationInfo locationInfo;
	PointOfInterest    closestPoi;
	int                locationUpdateCounter = 100;

	PresenceUpdater(Gw2Rpc main) {
		this.main = main;
	}

	@Override
	public void run() {
		try {
			while (active) {
				updatePresence();

				Thread.sleep(UPDATE_RATE);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	void kill() {
		this.active = false;
	}

	void updatePresence() {
		if (!main.processRunning) { return; }
		if (main.client == null) { return; }
		if (main.mumbleLink == null) { return; }

		JsonObject identity = main.mumbleLink.getIdentityJson();
		if (identity == null) { return; }
		JsonObject mapData = main.apiHelper.getMapData(identity.get("map_id").getAsInt());

		Point[] continentRect = new Point[] {
				Point.fromJson(mapData.get("continent_rect").getAsJsonArray().get(0).getAsJsonArray()),
				Point.fromJson(mapData.get("continent_rect").getAsJsonArray().get(1).getAsJsonArray())
		};
		Point[] mapRect = new Point[] {
				Point.fromJson(mapData.get("map_rect").getAsJsonArray().get(0).getAsJsonArray()),
				Point.fromJson(mapData.get("map_rect").getAsJsonArray().get(1).getAsJsonArray())
		};

		playerLocation = Point.fromMapCoords(continentRect, mapRect, new Point(main.mumbleLink.fAvatarPosition[0] * UNIT_MULTIPLIER, main.mumbleLink.fAvatarPosition[2] * UNIT_MULTIPLIER));

		locationUpdateCounter++;
		if (locationUpdateCounter >= 5) {
			Set<Integer> floorSet = new Gson().fromJson(mapData.get("floors").getAsJsonArray(), new TypeToken<Set<Integer>>() {
			}.getType());
			locationInfo = main.regionManager.getLocationInfoByCoords(floorSet, mapData.get("region_id").getAsInt(), identity.get("map_id").getAsInt(), playerLocation);
			closestPoi = main.regionManager.findClosestPoi(floorSet, locationInfo, playerLocation);

			locationUpdateCounter = 0;
		}

		if (!main.shouldShutdown) {
			main.dialog.labelContent.setText("<html><body>"
					+ "Active! <br/>"
					+ "<br/>Character:  " + identity.get("name").getAsString()
					+ "<br/>Race:       " + Gw2Rpc.RACES[identity.get("race").getAsInt()]
					+ "<br/>Profession: " + Gw2Rpc.PROFESSIONS[identity.get("profession").getAsInt()]
					+ "<br/>Continent:  " + mapData.get("continent_name").getAsString() + " (" + mapData.get("continent_id").getAsInt() + ")"
					+ "<br/>Region:     " + mapData.get("region_name").getAsString() + " (" + mapData.get("region_id").getAsInt() + ")"
					+ "<br/>Map:        " + mapData.get("name").getAsString() + " (" + mapData.get("id").getAsInt() + ")"
					+ "<br/>Sector:     " + (locationInfo != null ? locationInfo.sector.name + " (" + locationInfo.sector.id + ")" : "n/a")
					+ "<br/>POI:        " + (closestPoi != null ? (closestPoi.name + "  " + closestPoi.chatLink + " (" + closestPoi.type + ")") : "n/a")
					+ "<br/>Coords:     " + playerLocation.x + ", " + playerLocation.y
					+ "</body></html>");
		}

		String detailsFormat = "Playing as $name, a $race $profession in $map, $region";
		String stateFormat = "Currently exploring $sector, near $poi";

		RichPresence.Builder builder = new RichPresence.Builder();
		builder
				.setDetails(format(detailsFormat, locationInfo, identity, mapData, closestPoi, playerLocation))
				.setState(format(stateFormat, locationInfo, identity, mapData, closestPoi, playerLocation))
				.setStartTimestamp(startTime)
				.setLargeImage("map_" + identity.get("map_id").getAsString(), mapData.get("name").getAsString())
				.setSmallImage("prof_" + identity.get("profession").getAsString(), Gw2Rpc.PROFESSIONS[identity.get("profession").getAsInt()]);
		main.client.sendRichPresence(builder.build());
	}

	String format(String string, RegionLocationInfo locationInfo, JsonObject identity, JsonObject mapData, PointOfInterest closestPoi, Point playerLocation) {
		string = string.replace("$name", identity.get("name").getAsString());
		string = string.replace("$race", Gw2Rpc.RACES[identity.get("race").getAsInt()]);
		string = string.replace("$profession", Gw2Rpc.PROFESSIONS[identity.get("profession").getAsInt()]);
		string = string.replace("$map", mapData.get("name").getAsString());
		string = string.replace("$region", mapData.get("region_name").getAsString());
		string = string.replace("$continent", mapData.get("continent_name").getAsString());
		string = string.replace("$sector", (locationInfo != null ? locationInfo.sector.name : mapData.get("name").getAsString()));
		string = string.replace("$poi", closestPoi != null && "n/a".equals(closestPoi.name) ? closestPoi.name : "");
		string = string.replace("$poi_link", closestPoi != null ? closestPoi.chatLink : "n/a");
		string = string.replace("$x", String.valueOf(playerLocation.x));
		string = string.replace("$y", String.valueOf(playerLocation.y));
		return string;
	}

}
