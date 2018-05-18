package org.inventivetalent.gw2rpc.region;

import java.util.Arrays;

public class Polygon {

	public Point[] bounds;

	public Polygon(Point[] bounds) {
		this.bounds = bounds;
	}

	public boolean contains(Point point) {
		boolean result = false;
		for (int i = 0, j = bounds.length - 1; i < bounds.length; j = i++) {
			if ((bounds[i].y > point.y) != (bounds[j].y > point.y) &&
					(point.x < (bounds[j].x - bounds[i].x) * (point.y - bounds[i].y) / (bounds[j].y - bounds[i].y) + bounds[i].x)) {
				result = !result;
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return "Polygon{" +
				"bounds=" + Arrays.toString(bounds) +
				'}';
	}
}
