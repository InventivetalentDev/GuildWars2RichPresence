package org.inventivetalent.gw2rpc;

import com.google.gson.JsonObject;
import com.jagrosh.discordipc.entities.RichPresence;

import java.time.OffsetDateTime;

public class PresenceUpdater extends Thread {

	private final long UPDATE_RATE = 2000;

	final Gw2Rpc main;

	OffsetDateTime startTime = OffsetDateTime.now();
	boolean        active    = true;

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
		JsonObject mapData = main.idTranslator.getMapData(identity.get("map_id").getAsInt());

		main.dialog.labelContent.setText("Active!");

		RichPresence.Builder builder = new RichPresence.Builder();
		builder
				.setDetails("Playing as " + identity.get("name").getAsString() + ", a " + Gw2Rpc.RACES[identity.get("race").getAsInt()] + " " + Gw2Rpc.PROFESSIONS[identity.get("profession").getAsInt()])
				.setState("Currently exploring " + mapData.get("name").getAsString() + " in " + mapData.get("region_name").getAsString() + " (" + mapData.get("continent_name").getAsString() + ")")
				.setStartTimestamp(startTime)
				.setLargeImage("map_" + identity.get("map_id").getAsString(), mapData.get("name").getAsString())
				.setSmallImage("prof_" + identity.get("profession").getAsString(), Gw2Rpc.PROFESSIONS[identity.get("profession").getAsInt()]);
		main.client.sendRichPresence(builder.build());
	}

}
