package com.senior.roadrunner.tools;

import java.util.Vector;

import com.senior.roadrunner.tools.Polygon.Builder;

public class PathArea {

	public static Vector<Polygon> createPathArea(Vector<Point> points) {
//		int count = 0;
//		Vector<Integer> seperatePoint = new Vector<Integer>();
		Vector<Polygon> polygons = new Vector<Polygon>();
//		Vector<Point> pointL = new Vector<Point>();
//		Vector<Point> pointR = new Vector<Point>();
//		Point endPoint = null;
//		Point startPoint = null;

		// for (int i = 7; i < points.size(); i++) {
		// Point intersectPoint = null;
		// Line lineL1 = new Line(points.get(i - 7), points.get(i - 6));
		// Line lineL2 = new Line(points.get(i - 6), points.get(i - 5));
		// Line lineL3 = new Line(points.get(i - 5), points.get(i - 4));
		// Line lineL4 = new Line(points.get(i - 4), points.get(i - 3));
		// Line lineL5 = new Line(points.get(i - 3), points.get(i - 2));
		// Line lineL6 = new Line(points.get(i - 2), points.get(i - 1));
		// Line lineL7 = new Line(points.get(i - 1), points.get(i));
		//
		// intersectPoint = lineL1.intersectLine(lineL2);
		// intersectPoint = lineL1.intersectLine(lineL3);
		// intersectPoint = lineL1.intersectLine(lineL4);
		// intersectPoint = lineL1.intersectLine(lineL5);
		// intersectPoint = lineL1.intersectLine(lineL6);
		// intersectPoint = lineL1.intersectLine(lineL7);
		//
		// if (intersectPoint != null) {
		// points.remove(i-6);
		// points.remove(i-5);
		// points.remove(i-4);
		// points.remove(i-3);
		// points.remove(i-2);
		// points.remove(i-1);
		// points.add(i-6,points.get(i - 7));
		// points.add(i-5,points.get(i - 7));
		// points.add(i-4,points.get(i - 7));
		// points.add(i-3,points.get(i - 7));
		// points.add(i-2,points.get(i - 7));
		// points.add(i-1,points.get(i - 7));
		//
		// }
		// }
		Vector<Point> pointsSegment = new Vector<Point>();
		for (int i = 2; i < points.size(); i++) {
			Point point1 = points.get(i - 2);
			Point point2 = points.get(i - 1);
			Point point3 = points.get(i);
			Line ln1 = new Line(point1, point2);
			Line ln2 = new Line(point2, point3);
			double angle = ln1.angleBetween2Lines(ln2);
			// System.out.println("angle : "+angle);
			pointsSegment.add(point1);
			if (angle >= 1.500 || angle <= -1.500) {
				pointsSegment.add(point2);
				Polygon polygon = createPolygon(pointsSegment);
				polygons.add(polygon);
				pointsSegment.clear();
			}
			if (i == points.size() - 1) {
				pointsSegment.add(point3);
				Polygon polygon = createPolygon(pointsSegment);
				polygons.add(polygon);
			}
		}
		return polygons;

		// for (int i = 1; i < points.size(); i++) {
		// Point point1 = points.get(i - 1);
		// Point point2 = points.get(i);
		// Line ln = new Line(point1, point2);
		// double indent = 0.00016531116145; // distance from central line
		// double length = Point.Distance(ln.getStart(), ln.getEnd());
		//
		// double dx_li = (ln.getEnd().x - ln.getStart().x) / length * indent;
		// double dy_li = (ln.getEnd().y - ln.getStart().y) / length * indent;
		//
		// // moved p1 point
		// double p1X = ln.getStart().x - dx_li;
		// double p1Y = ln.getStart().y - dy_li;
		//
		// // line moved to the left
		// double lX1 = ln.getStart().x - dy_li;
		// double lY1 = ln.getStart().y + dx_li;
		// double lX2 = ln.getEnd().x - dy_li;
		// double lY2 = ln.getEnd().y + dx_li;
		//
		// // moved p2 point
		// double p2X = ln.getEnd().x + dx_li;
		// double p2Y = ln.getEnd().y + dy_li;
		//
		// // line moved to the right
		// double rX1_ = ln.getStart().x + dy_li;
		// double rY1 = ln.getStart().y - dx_li;
		// double rX2 = ln.getEnd().x + dy_li;
		// double rY2 = ln.getEnd().y - dx_li;
		//
		// if (i == 1) {
		// startPoint = new Point(p1X, p1Y);
		// }
		// pointL.add(new Point(lX1, lY1));
		// pointL.add(new Point(lX2, lY2));
		//
		// pointR.add(new Point(rX1_, rY1));
		// pointR.add(new Point(rX2, rY2));
		// if (i == points.size() - 1) {
		// endPoint = new Point(p2X, p2Y);
		// }
		// if (i < points.size() - 1) {
		// Point point3 = points.get(i + 1);
		// Line ln2 = new Line(point2, point3);
		// double angle = ln.angleBetween2Lines(ln2);
		// // System.out.println("angle : "+angle);
		// if (angle >= 1.500 || angle <= -1.500) {
		// seperatePoint.add(i);
		// System.out.println((count++) + " angle " + i + " : "
		// + angle + " degree");
		// }
		// }
		//
		// }

		// Intersecttion clear
		// Vector<Point> intersectionL = new Vector<Point>();
		// Vector<Point> intersectionR = new Vector<Point>();
		// for (int i = 3; i < pointL.size(); i++) {
		// Point intersectPointL = null;
		// Line lineL1 = new Line(pointL.get(i - 3), pointL.get(i - 2));
		// Line lineL2 = new Line(pointL.get(i - 1), pointL.get(i));
		// intersectPointL = lineL1.intersectLine(lineL2);
		// if (i == 3) {
		// intersectionL.add(pointL.get(i - 3));
		// }
		// if (i == (pointL.size() - 1)) {
		// intersectionL.add(pointL.get(pointL.size() - 1));
		// }
		// if (intersectPointL != null && i != (pointL.size() - 1)) {
		// pointL.remove(i - 2);
		// pointL.add(i - 2, intersectPointL);
		// pointL.remove(i - 1);
		// pointL.add(i - 1, intersectPointL);
		//
		// intersectionL.add(intersectPointL);
		// }
		// Point intersectPointR = null;
		// Line lineR1 = new Line(pointR.get(i - 3), pointR.get(i - 2));
		// Line lineR2 = new Line(pointR.get(i - 1), pointR.get(i));
		// intersectPointR = lineR1.intersectLine(lineR2);
		// if (i == 3) {
		// intersectionR.add(pointR.get(i - 3));
		// }
		// if (i == (pointR.size() - 1)) {
		// intersectionR.add(pointR.get(pointR.size() - 1));
		// }
		// if (intersectPointR != null && i != (pointR.size() - 1)) {
		// pointR.remove(i - 2);
		// pointR.add(i - 2, intersectPointR);
		// pointR.remove(i - 1);
		// pointR.add(i - 1, intersectPointR);
		//
		// intersectionR.add(intersectPointR);
		// }
		// }

		// System.out.println("Polyline size : " + points.size());
		// System.out.println("Left line : " + pointL.size());
		// System.out.println("Right line : " + pointR.size());
		// // System.out.println("IntersectionL line : " +
		// intersectionL.size());
		// // System.out.println("IntersectionR line : " +
		// intersectionR.size());
		//
		// // Build polygon buffer from polyline
		// Builder builder = new Polygon.Builder();
		// builder.addVertex(startPoint);
		// for (int i = 0; i < pointL.size(); i++) {
		// builder.addVertex(pointL.get(i));
		// }
		// builder.addVertex(endPoint);
		// for (int i = pointR.size() - 1; i >= 0; i--) {
		// builder.addVertex(pointR.get(i));
		// }
		// builder.addVertex(startPoint);
		// Polygon polygon = builder.build();
		// polygons.add(polygon);

		//
		// return polygon;

		// splint polygon

		// for (int i = 1; i < seperatePoint.size(); i++) {
		// if (seperatePoint.get(i) - seperatePoint.get(i - 1) > 0) {
		//
		// if (i == 1) {
		// Builder builder = new Polygon.Builder();
		// builder.addVertex(startPoint);
		// for (int j = 0; j <= seperatePoint.get(0); j++) {
		// builder.addVertex(intersectionL.get(j));
		// }
		// for (int j = seperatePoint.get(0); j >= 0; j--) {
		// builder.addVertex(intersectionR.get(j));
		// }
		// polygons.add(builder.build());
		// }
		// if (i == seperatePoint.size() - 1) {
		// Builder builder = new Polygon.Builder();
		// for (int j = seperatePoint.get(seperatePoint.size() - 1); j <=
		// intersectionL
		// .size() - 1; j++) {
		// builder.addVertex(intersectionL.get(j));
		// System.out.println(intersectionL.get(j).x + "\t"
		// + intersectionL.get(j).y);
		// }
		// builder.addVertex(endPoint);
		// for (int j = intersectionR.size() - 1; j >= seperatePoint
		// .get(seperatePoint.size() - 1); j--) {
		// // System.out.println("Seperatpoint : " + j);
		//
		// builder.addVertex(intersectionR.get(j));
		// System.out.println(intersectionR.get(j).x + "\t"
		// + intersectionR.get(j).y);
		// }
		// polygons.add(builder.build());
		// }
		//
		// Builder builder = new Polygon.Builder();
		// for (int j = seperatePoint.get(i - 1); j <= seperatePoint
		// .get(i); j++) {
		// builder.addVertex(intersectionL.get(j));
		// }
		// for (int j = seperatePoint.get(i); j >= seperatePoint
		// .get(i - 1); j--) {
		// builder.addVertex(intersectionR.get(j));
		// }
		//
		// Polygon polygon = builder.build();
		// System.out.println(polygon.getSides().size());
		// polygons.add(polygon);
		// }
		// }
		// return polygons;
	}

	@SuppressWarnings("unused")
	private static Polygon createPolygon(Vector<Point> pointsSegment) {

		Point startPoint = null;
		Point endPoint = null;
		Vector<Point> pointL = new Vector<Point>();
		Vector<Point> pointR = new Vector<Point>();
		Builder builder = new Polygon.Builder();
		// Buffer line segments
		for (int i = 1; i < pointsSegment.size(); i++) {
			Point point1 = pointsSegment.get(i - 1);
			Point point2 = pointsSegment.get(i);
			Line ln = new Line(point1, point2);
			double indent = 0.00025931116145; // distance from central line 0.00016531116145
			double length = Point.Distance(ln.getStart(), ln.getEnd());

			double dx_li = (ln.getEnd().x - ln.getStart().x) / length * indent;
			double dy_li = (ln.getEnd().y - ln.getStart().y) / length * indent;

			// moved p1 point
			double p1X = ln.getStart().x - dx_li;
			double p1Y = ln.getStart().y - dy_li;

			// line moved to the left
			double lX1 = ln.getStart().x - dy_li;
			double lY1 = ln.getStart().y + dx_li;
			double lX2 = ln.getEnd().x - dy_li;
			double lY2 = ln.getEnd().y + dx_li;

			// moved p2 point
			double p2X = ln.getEnd().x + dx_li;
			double p2Y = ln.getEnd().y + dy_li;

			// line moved to the right
			double rX1_ = ln.getStart().x + dy_li;
			double rY1 = ln.getStart().y - dx_li;
			double rX2 = ln.getEnd().x + dy_li;
			double rY2 = ln.getEnd().y - dx_li;

			if (i == 1) {
				startPoint = new Point(p1X, p1Y);
			}
			// pointL.add(new Point(p1X, p1Y));
			pointL.add(new Point(lX1, lY1));
			pointL.add(new Point(lX2, lY2));

			pointR.add(new Point(rX1_, rY1));
			pointR.add(new Point(rX2, rY2));
			// pointR.add(new Point(p2X, p2Y));
			if (i == pointsSegment.size() - 1) {
				endPoint = new Point(p2X, p2Y);
			}

		}
		// Clear intersection line
		Vector<Point> intersectionL = intersectionClean(pointL);
		Vector<Point> intersectionR = intersectionClean(pointR);
		// Build Polygon buffer
		// builder.addVertex(startPoint);
		for (int i = 0; i < intersectionL.size(); i++) {
			builder.addVertex(intersectionL.get(i));
		}
		for (int i = intersectionR.size() - 1; i >= 0; i--) {
			builder.addVertex(intersectionR.get(i));
		}
		// builder.addVertex(endPoint);

		return builder.build();
	}

	private static Vector<Point> intersectionClean(Vector<Point> pointLR) {
//		Vector<Point> intersectionPoints = new Vector<Point>();

		if (pointLR == null) {
			return null;
		}

//		intersectionPoints.add(pointLR.get(0));
		for (int i = 3; i < pointLR.size(); i++) {
			Point intersectPoint = null;
			Line lineL1 = new Line(pointLR.get(i - 3), pointLR.get(i - 2));
			Line lineL2 = new Line(pointLR.get(i - 1), pointLR.get(i));
			intersectPoint = lineL1.intersectLine(lineL2);

			if (intersectPoint != null && i != (pointLR.size() - 1)) {
				pointLR.remove(i - 2);
				pointLR.add(i - 2, intersectPoint);
				pointLR.remove(i - 1);
				pointLR.add(i - 1, intersectPoint);
			}
			// if(intersectPoint!=null)
			// intersectionPoints.add(intersectPoint);
		}
		// intersectionPoints.add(pointLR.get(pointLR.size() - 1));
		return pointLR;
	}

	public static Polygon circleBuffer(Point point) {
		if (point == null)
			return null;
		double bufferRadius = 0.00028931116145;
		double a = point.x;
		double b = point.y;
		int n = 8;
		Builder builder = new Polygon.Builder();
		for (int i = 0; i < n; i++) {
			double t = 2 * Math.PI * i / n;
			double x = a + bufferRadius * Math.cos(t);
			double y = b + bufferRadius * Math.sin(t);
			builder.addVertex(new Point(x,y));
		}
		Polygon polygon = builder.build();
		return polygon;

	}
}
