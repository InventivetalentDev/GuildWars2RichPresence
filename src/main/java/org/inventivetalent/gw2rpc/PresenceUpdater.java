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

	public static String DETAILS_FORMAT = "Playing as $name, a $race $profession in $map, $region";
	public static String STATE_FORMAT   = "Currently exploring $sector, near $poi";

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
			main.dialog.statusLabel.setText("Active!");
			main.dialog.character.setText(identity.get("name").getAsString());
			main.dialog.race.setText(Gw2Rpc.RACES[identity.get("race").getAsInt()]);
			main.dialog.raceId.setText(identity.get("race").getAsString());
			main.dialog.profession.setText(Gw2Rpc.PROFESSIONS[identity.get("profession").getAsInt()]);
			main.dialog.professionId.setText(identity.get("profession").getAsString());
			main.dialog.continent.setText(mapData.get("continent_name").getAsString());
			main.dialog.continentId.setText(mapData.get("continent_id").getAsString());
			main.dialog.region.setText(mapData.get("region_name").getAsString());
			main.dialog.regionId.setText(mapData.get("region_id").getAsString());
			main.dialog.map.setText(mapData.get("name").getAsString());
			main.dialog.mapId.setText(mapData.get("id").getAsString());
			main.dialog.sector.setText((locationInfo != null ? locationInfo.sector.name : "n/a"));
			main.dialog.sectorId.setText((locationInfo != null ? String.valueOf(locationInfo.sector.id) : "n/a"));
			main.dialog.poi.setText((closestPoi != null ? closestPoi.name : "n/a"));
			main.dialog.poiChat.setText((closestPoi != null ? closestPoi.chatLink : "n/a"));
			main.dialog.coordsX.setText(String.valueOf(playerLocation.x));
			main.dialog.coordsY.setText(String.valueOf(playerLocation.y));
		}

		String detailsFormat = main.dialog.detailsFormat.getText();
		String stateFormat = main.dialog.stateFormat.getText();

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
		string = string.replace("$poi", closestPoi != null ? closestPoi.name : "n/a");
		string = string.replace("$poi_link", closestPoi != null ? closestPoi.chatLink : "n/a");
		string = string.replace("$x", String.valueOf(playerLocation.x));
		string = string.replace("$y", String.valueOf(playerLocation.y));
		return string;
	}

}
