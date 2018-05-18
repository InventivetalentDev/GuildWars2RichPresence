package org.inventivetalent.gw2rpc.region;

import com.google.gson.JsonArray;

public class Point {
	public final double x;
	public final double y;

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double distance(Point other) {
		return Math.pow((this.x - other.x), 2) + Math.pow((this.y - other.y), 2);
	}

	public double distanceSqrt(Point other) {
		return Math.sqrt(distance(other));
	}

	@Override
	public String toString() {
		return "Point{" +
				"x=" + x +
				", y=" + y +
				'}';
	}

	public static Point fromMapCoords(Point[] continentRect, Point[] mapRect, Point coords) {
		return new Point(
				Math.round(continentRect[0].x + (continentRect[1].x - continentRect[0].x) * (coords.x - mapRect[0].x) / (mapRect[1].x - mapRect[0].x)),
				Math.round(continentRect[0].y + (continentRect[1].y - continentRect[0].y) * (1 - (coords.y - mapRect[0].y) / (mapRect[1].y - mapRect[0].y)))
		);
	}

	public static Point fromJson(JsonArray array) {
		return new Point(array.get(0).getAsDouble(), array.get(1).getAsDouble());
	}
}

