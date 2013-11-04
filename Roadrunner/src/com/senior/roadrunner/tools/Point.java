package com.senior.roadrunner.tools;

/**
 * Point on 2D landscape
 * 
 * @author Roman Kushnarenko (sromku@gmail.com)</br>
 */
public class Point {

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double x;
	public double y;

	public static double Distance(Point start,Point end) {
		double dx = start.x - end.x;
		double dy = start.y -end.y;
		double distance = Math.sqrt((dx*dx)+(dy*dy));
		return distance;
	}

	@Override
	public String toString() {
		return String.format("(%.2f,%.2f)", x, y);
	}
}