package org.inventivetalent.gw2rpc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class IdTranslator {

	Map<Integer, JsonObject> mapCache = new HashMap<>();

	public JsonObject getMapData(int id) {
		if (mapCache.containsKey(id)) { return mapCache.get(id); }
		JsonObject json = getData("/maps/" + id);
		if (json != null) {
			mapCache.put(id, json);
		}
		return json;
	}

	private JsonObject getData(String path) {
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL("https://api.guildwars2.com/v2" + path).openConnection();
			return new JsonParser().parse(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
