import org.bzdev.geom.*;
import org.bzdev.graphs.Graph;
import org.bzdev.math.RealValuedFunction;
import org.bzdev.math.stats.BasicStats;

import java.awt.*;
import java.awt.geom.*;
import java.io.File;
import java.util.function.Predicate;

public class Paths2DTest {
    static double radius = 50.0;

    static RealValuedFunction fx = new RealValuedFunction() {
	    public double valueAt(double t) {
		return radius * Math.cos(Math.toRadians(t));
	    }
	};

    static RealValuedFunction fy = new RealValuedFunction() {
	    public double valueAt(double t) {
		return radius * Math.sin(Math.toRadians(t));
	    }
	};

    static double data[] = {
	-30.0, -80.0,  // 0, 1,
	0.0, -80.0,    // 2, 3,
	50.0, -70.0,   // 4, 5,
	60.0, -60.0,   // 6, 7,
	-30.0, -75.0,   // 8, 9,
	0.0, -75.0,    // 10, 11,
	50.0, -85.0,   // 12, 13,
	60.0, -95.0,   // 14, 15,
	20.0, -60.0,    // 16, 17,
	50.0, -60.0,	// 18, 19,
	75.0, 0.0,	// 20, 21,
	75.0, 30.0,	// 22, 23,
	1.0, 0.0,	// 24, 25,
	0.0, 1.0,	// 26, 27,
	-60.0, 20.0,	// 28, 29,
	-60.0, 50.0,	// 30, 31,
	0.0, 75.0,	// 32, 33,
	30.0, 75.0,	// 34, 35,
	-20.0, -30.0,	// 36, 37,
	0.0, -30.0,	// 38, 39,
	0.0, 20.0,	// 40, 41,
	-20.0, 20.0,	// 42, 43,
	-1.0, 0,	// 44, 45,
	-20.0, -35.0,	// 46, 47,
	0.0, -35.0,	// 48, 49,
	-10.0, 27.5,	// 50, 51,
	-30.0, 27.5,	// 52, 53,
	-20.0, -25.0,	// 54, 55,
	0.0, -25.0,	// 56, 57,
	10.0, 35.0,	// 58, 59,
	-10.0, 35.0,	// 60, 61,
	-10.0, -20.0 - 3.0, 	// 62, 63,
	0.0, -20.0 - 3.0,	// 64, 65,
	-20.0, 0.0 - 3.0,	// 66, 67,
	-20.0, -10.0 - 3.0,	// 68, 69,
	0.0, -1.0,		// 70, 71,
	-90.0, -80.0,		// 72, 73
	-70, -80.0,		// 74, 75,
	-90.0, -50.0,		// 76, 77,
	-90.0, -70.0,		// 78,79,
	50.0, 40.0,		// 80, 81,
	70.0, 40.0,		// 82, 83,
	60.0, 60.0,		// 84, 85,
	60.0, 45.0,		// 86, 87,
	-70.0, 40.0,		// 88, 89,
	-50.0, 40.0,		// 90, 91,
	-80.0, 60.0,		// 92, 93,
	-80.0, 40.0,		// 94, 95,
	Math.sqrt(0.5), Math.sqrt(0.5), // 96, 97
	Math.sqrt(0.5), -Math.sqrt(0.5) // 98, 99
    };

    public static void tests(int angle, boolean reflect) throws Exception {
	SplinePathBuilder spb = new SplinePathBuilder();

	double[] coords = new double[data.length];
	if (angle == 0 && reflect == false) {
	    System.arraycopy(data, 0, coords, 0, data.length);
	} else {
	    AffineTransform af = AffineTransform.getRotateInstance
		(Math.toRadians(angle));
	    if (reflect) af.scale(-1.0, 1.0);
	    af.transform(data, 0, coords, 0, data.length/2);
	}

	radius = 50.0;
	SplinePathBuilder.CPoint circlePoints[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO_NEXT),
	    new SplinePathBuilder.CPoint(fx, fy, 0.0, 360.0, 2*36),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE)
	};

	spb.append(circlePoints);
	Path2D circle = spb.getPath();

	Graph graph = new Graph(1050,1050);
	graph.setOffsets(25,25);
	graph.setRanges(-100.0, 100.0, -100.0, 100.0);
	
	if (angle > 0) {
	    double theta = Math.toRadians(angle);
	    // tweak the graph so the affine transform is undone when
	    // the graph is displayed.
	    graph.setRotation(theta, 0.0, 0.0);
	}

	Graphics2D g2d = graph.createGraphics();

	g2d.setColor(Color.GREEN);
	g2d.setStroke(new BasicStroke(4.0F));
	graph.draw(g2d, circle);


	if (angle == -1) {
	    System.out.println("creating arc given an angle ...");
	    Path2D circArc1 = Paths2D.createArc(0.0, 0.0,
						fx.valueAt(45.0),
						fy.valueAt(45.0),
						Math.toRadians(20.0),
						Math.toRadians(20.0));
	    Path2D circArc2 = Paths2D.createArc(0.0, 0.0,
						fx.valueAt(90.0),
						fy.valueAt(90.0),
						Math.toRadians(20.0),
						Math.toRadians(20.1));
	    Path2D circArc3 = Paths2D.createArc(0.0, 0.0,
						fx.valueAt(135.0),
						fy.valueAt(135.0),
						Math.toRadians(20.0),
						Math.toRadians(19.9));
	    // These lengths should be nearly identical.
	    double elen = Math.toRadians(20.0)*radius;
	    double len1 = Path2DInfo.pathLength(circArc1);
	    double len2 = Path2DInfo.pathLength(circArc2);
	    double len3 = Path2DInfo.pathLength(circArc3);
	    System.out.println("len 1 = " + len1 + ", expected " + elen);
	    System.out.println("len 2 = " + len2 + ", expected " + elen);
	    System.out.println("len 3 = " + len3 + ", expected " + elen);
	    if (Math.abs(len1 - elen) > 1.e-5
		|| Math.abs(len2 - elen) > 1.e-6
		|| Math.abs(len3 - elen) > 1.e-6) {
		throw new Exception("lengths differ by more than expected");
	    }
	    Path2DInfo.printSegments(circArc3);

	    Path2D circArc4 = Paths2D.createArc(0.0, 0.0,
						fx.valueAt(180.0),
						fy.valueAt(180.0),
						Math.toRadians(20.0));
	    Path2D circArc5 = Paths2D.createArc(0.0, 0.0,
						fx.valueAt(225.0),
						fy.valueAt(225.0),
						Math.toRadians(20.0));
	    Path2D circArc6 = Paths2D.createArc(0.0, 0.0,
						fx.valueAt(270.0),
						fy.valueAt(270.0),
						Math.toRadians(20.0));
	    Path2D circArc7 = Paths2D.createArc(0.0, 0.0,
						fx.valueAt(315.0),
						fy.valueAt(315.0),
						Math.toRadians(20.0));
	    Path2D circArc8 = Paths2D.createArc(0.0, 0.0,
						fx.valueAt(0.0),
						fy.valueAt(0.0),
						Math.toRadians(20.0));

	    Path2D circArc1r = Paths2D.createArc(0.0, 0.0,
						 fx.valueAt(45.0),
						 fy.valueAt(45.0),
						 Math.toRadians(-20.0));
	    Path2D circArc2r = Paths2D.createArc(0.0, 0.0,
						 fx.valueAt(90.0),
						 fy.valueAt(90.0),
						 Math.toRadians(-20.0));
	    Path2D circArc3r = Paths2D.createArc(0.0, 0.0,
						 fx.valueAt(135.0),
						 fy.valueAt(135.0),
						 Math.toRadians(-20.0));
	    Path2D circArc4r = Paths2D.createArc(0.0, 0.0,
						 fx.valueAt(180.0),
						 fy.valueAt(180.0),
						 Math.toRadians(-20.0));
	    Path2D circArc5r = Paths2D.createArc(0.0, 0.0,
						 fx.valueAt(225.0),
						 fy.valueAt(225.0),
						 Math.toRadians(-20.0));
	    Path2D circArc6r = Paths2D.createArc(0.0, 0.0,
						 fx.valueAt(270.0),
						 fy.valueAt(270.0),
						 Math.toRadians(-20.0));
	    Path2D circArc7r = Paths2D.createArc(0.0, 0.0,
						 fx.valueAt(315.0),
						 fy.valueAt(315.0),
						 Math.toRadians(-20.0));
	    Path2D circArc8r = Paths2D.createArc(0.0, 0.0,
						 fx.valueAt(0.0),
						 fy.valueAt(0.0),
						 Math.toRadians(-20.0));
	
	    radius = 25.0;
	    Path2D circArc9 = Paths2D.createArc(0.0, 0.0,
						fx.valueAt(45.0),
						fy.valueAt(45.0),
						Math.toRadians(90.0));

	    radius = 40.0;
	    Path2D circArc10 = Paths2D.createArc(0.0, 0.0,
						 fx.valueAt(45.0),
						 fy.valueAt(45.0),
						 Math.toRadians(280.0));
	    radius = 60.0;
	    Path2D circArc11 = Paths2D.createArc(0.0, 0.0,
						 fx.valueAt(45.0),
						 fy.valueAt(45.0),
						 Math.toRadians(360.0));

	    graph.draw(g2d, circArc1);
	    graph.draw(g2d, circArc2);
	    graph.draw(g2d, circArc3);
	    graph.draw(g2d, circArc4);
	    graph.draw(g2d, circArc5);
	    graph.draw(g2d, circArc6);
	    graph.draw(g2d, circArc7);
	    graph.draw(g2d, circArc8);
	    graph.draw(g2d, circArc9);
	    graph.draw(g2d, circArc10);
	    graph.draw(g2d, circArc11);
	    System.out.println("circArc11 circumference: "
			       + Path2DInfo.pathLength(circArc11)
			       + ", expecting " + (Math.PI * 2 * radius));

	    g2d.setColor(Color.BLUE);
	    graph.draw(g2d, circArc1r);
	    graph.draw(g2d, circArc2r);
	    graph.draw(g2d, circArc3r);
	    graph.draw(g2d, circArc4r);
	    graph.draw(g2d, circArc5r);
	    graph.draw(g2d, circArc6r);
	    graph.draw(g2d, circArc7r);
	    graph.draw(g2d, circArc8r);

	    double sign = reflect? -1.0: 1.0;
	    Path2D lpath1 = new Path2D.Double();
	    lpath1.moveTo(0.0, -90.0);
	    lpath1.lineTo(sign*30.0, -90.0);
	    Path2D lpath2 = new Path2D.Double();
	    lpath2.moveTo(sign*55.0, -70.0);
	    lpath2.lineTo(sign*55.0, -50.0);
	    Path2D epath = Paths2D.createArc(lpath1, lpath2);
	    g2d.setColor(Color.BLACK);
	    graph.draw(g2d, lpath1);
	    graph.draw(g2d, epath);
	    graph.draw(g2d, lpath2);

	    Path2D arc1 = Paths2D.createArc(0.0, 0.0, 70.0, 0.0,
					    Math.toRadians(45.0));
	    double theta = Math.toRadians(135);
	    Path2D arc2 = Paths2D.createArc(0.0, 0.0,
					    70.0*Math.cos(theta),
					    70.0*Math.sin(theta),
					    Math.toRadians(45.0));
	    Path2D carc = Paths2D.createArc(arc1, arc2);
	    graph.draw(g2d, arc1);
	    graph.draw(g2d, carc);
	    graph.draw(g2d, arc2);

	    double coords1[] = {
		// AB
		-95.0, -90.0,
		-90.0, -90.0,
		-70.0, -70.0,
		-75.0, -65.0,
		// CD
		-70.0, -90.0,
		-60.0, -90.0,
		-80.0, -70.0,
		-90.0, -80.0,

		// EF
		-90.0, 60.0,
		-80.0, 60.0,
		-80.0, 80.0,
		-90.0, 90.0,
		// GH
		-75.0, 60.0,
		-70.0, 60.0,
		-70.0, 80.0,
		-80.0, 70.0,
	    };
	    double[] coords2 = new double[coords1.length];

	    if (reflect) {
		System.out.println("reflected");
		AffineTransform af =
		    AffineTransform.getScaleInstance(-1.0, 1.0);
		af.transform(coords1, 0, coords2, 0, coords1.length/2);
	    } else {
		System.arraycopy(coords1, 0, coords2, 0, coords1.length);
	    }

	    System.out.println("AB case");
	    Path2D lineA = new Path2D.Double();
	    lineA.moveTo(coords2[0], coords2[1]);
	    lineA.lineTo(coords2[2], coords2[3]);
	    Path2D lineB = new Path2D.Double();
	    lineB.moveTo(coords2[4], coords2[5]);
	    lineB.lineTo(coords2[6], coords2[7]);
	    Path2D pathAB = Paths2D.createArc(lineA, lineB);

	    System.out.println("CD case");
	    Path2D lineC = new Path2D.Double();
	    lineC.moveTo(coords2[8], coords2[9]);
	    lineC.lineTo(coords2[10], coords2[11]);
	    Path2D lineD = new Path2D.Double();
	    lineD.moveTo(coords2[12], coords2[13]);
	    lineD.lineTo(coords2[14], coords2[15]);
	    Path2D pathCD = Paths2D.createArc(lineC, lineD);

	    System.out.println("EF case");
	    Path2D lineE = new Path2D.Double();
	    lineE.moveTo(coords2[16], coords2[17]);
	    lineE.lineTo(coords2[18], coords2[19]);
	    Path2D lineF = new Path2D.Double();
	    lineF.moveTo(coords2[20], coords2[21]);
	    lineF.lineTo(coords2[22], coords2[23]);
	    Path2D pathEF = Paths2D.createArc(lineE, lineF);

	    System.out.println("GH case");
	    Path2D lineG = new Path2D.Double();
	    lineG.moveTo(coords2[24], coords2[25]);
	    lineG.lineTo(coords2[26], coords2[27]);
	    Path2D lineH = new Path2D.Double();
	    lineH.moveTo(coords2[28], coords2[29]);
	    lineH.lineTo(coords2[30], coords2[31]);
	    Path2D pathGH = Paths2D.createArc(lineG, lineH);

	    g2d.setColor(Color.GREEN);
	    g2d.setStroke(new BasicStroke(4.0F));
	    graph.draw(g2d, lineA);
	    graph.draw(g2d, lineB);
	    graph.draw(g2d, lineC);
	    graph.draw(g2d, lineD);
	    graph.draw(g2d, lineE);
	    graph.draw(g2d, lineF);
	    graph.draw(g2d, lineG);
	    graph.draw(g2d, lineH);
	    g2d.setColor(Color.BLACK);
	    g2d.setStroke(new BasicStroke(2.0F));
	    graph.draw(g2d, lineA);
	    graph.draw(g2d, pathAB);
	    graph.draw(g2d, lineB);
	    graph.draw(g2d, lineC);
	    graph.draw(g2d, pathCD);
	    graph.draw(g2d, lineD);

	    graph.draw(g2d, lineE);
	    graph.draw(g2d, pathEF);
	    graph.draw(g2d, lineF);
	    graph.draw(g2d, lineG);
	    graph.draw(g2d, pathGH);
	    graph.draw(g2d, lineH);

	    graph.write("png", new File("paths2d" + (reflect? "R":"")
					+ ".png"));
	    return;
	}

	Path2D line1 = new BasicSplinePath2D();
	line1.moveTo(coords[0], coords[1]/*-30.0, -80.0*/);
	line1.lineTo(coords[2], coords[3]/*0.0, -80.0*/);
	Path2D line2 = new BasicSplinePath2D();
	line2.moveTo(coords[4], coords[5]/*50.0, -70.0*/);
	line2.lineTo(coords[6], coords[7]/*60.0, -60.0*/);

	Path2D line3 = new BasicSplinePath2D();
	line3.moveTo(coords[8], coords[9]/*-30.0, -75.0*/);
	line3.lineTo(coords[10], coords[11]/*0.0, -75.0*/);
	Path2D line4 = new BasicSplinePath2D();
	line4.moveTo(coords[12], coords[13]/*50.0, -85.0*/);
	line4.lineTo(coords[14], coords[15]/*60.0, -95.0*/);

	Path2D cpath = Paths2D.createArc(coords[2], coords[3],/*0.0, -80.0,*/
					   coords[24], coords[25], /*1.0, 0.0,*/
					   coords[4], coords[5],/*50.0, -70.0,*/
					   coords[96], coords[97]
					   /*Math.sqrt(0.5), Math.sqrt(0.5)*/);
	
	Path2D cpath2 =
	    Paths2D.createArc(coords[10], coords[11],/*0.0, -75.0,*/
				coords[24], coords[25],/*1.0, 0.0,*/
				coords[12], coords[13],/*50.0, -85.0,*/
				coords[98], coords[99]
				/*Math.sqrt(0.5), -Math.sqrt(0.5)*/);


	Path2D line5 = new BasicSplinePath2D();
	line5.moveTo(coords[16], coords[17]/*20.0, -60.0*/);
	line5.lineTo(coords[18], coords[19]/*50.0, -60.0*/);
	Path2D line6 = new BasicSplinePath2D();
	line6.moveTo(coords[20], coords[21]/*75.0, 0.0*/);
	line6.lineTo(coords[22], coords[23]/*75.0, 30.0*/);
	int oz = 24;
	int zo = 26;
	Path2D cpath3 = Paths2D.createArc(coords[18], coords[19],
					    /*50.0, -60.0,*/
					    coords[oz], coords[oz+1],
					    /*1.0, 0.0,*/
					    coords[20], coords[21],
					    /*75.0, 0.0,*/
					    coords[zo], coords[zo+1]
					    /*0.0, 1.0*/);

	Path2D line7 = new BasicSplinePath2D();
	line7.moveTo(coords[28], coords[29]/*-60.0, 20.0*/);
	line7.lineTo(coords[30], coords[31]/*-60.0, 50.0*/);
	Path2D line8 = new BasicSplinePath2D();
	line8.moveTo(coords[32], coords[33]/*0.0, 75.0*/);
	line8.lineTo(coords[34], coords[35]/*30.0, 75.0*/);
	Path2D cpath4 = Paths2D.createArc(coords[30], coords[31],
					    /*-60.0, 50.0,*/
					    coords[zo], coords[zo+1],
					    /*0.0, 1.0,*/
					    coords[32], coords[33],
					    /*0.0, 75.0,*/
					    coords[oz], coords[oz+1]
					    /*1.0, 0.0*/);
	if (cpath4 == null) throw new Exception("cpath4 == null");

	Path2D line9 = new BasicSplinePath2D();
	line9.moveTo(coords[36], coords[37]/*-20.0, -30.0*/);
	line9.lineTo(coords[38], coords[39]/*0.0, -30.0*/);
	Path2D line10 = new BasicSplinePath2D();
	line10.moveTo(coords[40], coords[41]/*0.0, 20.0*/);
	line10.lineTo(coords[42], coords[43]/*-20.0, 20.0*/);
	int moz = 44;
	Path2D cpath5 = Paths2D.createArc(coords[38], coords[39],
					    /*0.0, -30.0,*/
					    coords[oz], coords[oz+1],
					    /*1.0, 0.0,*/
					    coords[40], coords[41],
					    /*0.0, 20.0,*/
					    coords[moz], coords[moz+1]
					    /*-1.0, 0*/);

	Path2D line11 = new BasicSplinePath2D();
	line11.moveTo(coords[46], coords[47]/*-20.0, -35.0*/);
	line11.lineTo(coords[48], coords[49]/*0.0, -35.0*/);
	Path2D line12 = new BasicSplinePath2D();
	line12.moveTo(coords[50], coords[51]/*-10.0, 27.5*/);
	line12.lineTo(coords[52], coords[53]/*-30.0, 27.5*/);
	System.out.println("creating path 6");
	Path2D cpath6 = Paths2D.createArc(coords[48], coords[49],
					    /*0.0, -35.0,*/
					    coords[oz], coords[oz+1],
					    /*1.0, 0.0,*/
					    coords[50], coords[51],
					    /*-10.0, 27.5,*/
					    coords[moz], coords[moz+1]
					    /*-1.0, 0*/);

	Path2D line13 = new BasicSplinePath2D();
	line13.moveTo(coords[54], coords[55]/*-20.0, -25.0*/);
	line13.lineTo(coords[56], coords[57]/*0.0, -25.0*/);
	Path2D line14 = new BasicSplinePath2D();
	line14.moveTo(coords[58], coords[59]/*10.0, 35.0*/);
	line14.lineTo(coords[60], coords[61]/*-10.0, 35.0*/);
	System.out.println("creating path 7");
	System.out.format("t1 = (%g, %g); t2 = (%g, %g)\n",
			  coords[oz], coords[oz+1],
			  coords[moz], coords[moz+1]);
	Path2D cpath7 = Paths2D.createArc(coords[56], coords[57],
					    /*0.0, -25.0,*/
					    coords[oz], coords[oz+1],
					    /*1.0, 0.0,*/
					    coords[58], coords[59],
					    /*10.0, 35.0,*/
					    coords[moz], coords[moz+1]
					    /*-1.0, 0*/);
	if (cpath7 == null) {
	    System.out.println("cpath7 = null");
	    System.exit(1);
	}

	Path2D line15 = new BasicSplinePath2D();
	line15.moveTo(coords[62], coords[63]/*-10.0, -20.0 - 3.0*/);
	line15.lineTo(coords[64], coords[65]/*0.0, -20.0 - 3.0*/);
	Path2D line16 = new BasicSplinePath2D();
	line16.moveTo(coords[66], coords[67]/*-20.0, 0.0 - 3.0*/);
	line16.lineTo(coords[68], coords[69]/*-20.0, -10.0 - 3.0*/);
	System.out.println("creating path 8");
	int zmo = 70;
	Path2D cpath8 = Paths2D.createArc(coords[64], coords[65],
					    /*0.0, -20.0 - 3.0,*/
					    coords[oz], coords[oz+1],
					    /*1.0, 0.0,*/
					    coords[66], coords[67],
					    /*-20.0, 0.0 - 3.0,*/
					    coords[zmo], coords[zmo+1]
					    /*0.0, -1.0*/);

	Path2D line17 = new BasicSplinePath2D();
	line17.moveTo(coords[72], coords[73]/*-90.0, -80.0*/);
	line17.lineTo(coords[74], coords[75]/*-70, -80.0*/);
	Path2D line18 = new BasicSplinePath2D();
	line18.moveTo(coords[76], coords[77]/*-90.0, -50.0*/);
	line18.lineTo(coords[78], coords[79]/*-90.0, -70.0*/);
	System.out.println("creating path 9");
	Path2D cpath9 = Paths2D.createArc(coords[74], coords[75],
					    /*-70.0, -80.0,*/
					    coords[oz], coords[oz+1],
					    /*1.0, 0.0,*/
					    coords[76], coords[77],
					    /*-90.0, -50.0,*/
					    coords[zmo], coords[zmo+1]
					    /*0.0, -1.0*/);

	Path2D line19 = new BasicSplinePath2D();
	line19.moveTo(coords[80], coords[81]/*50.0, 40.0*/);
	line19.lineTo(coords[82], coords[83]/*70.0, 40.0*/);
	Path2D line20 = new BasicSplinePath2D();
	line20.moveTo(coords[84], coords[85]/*60.0, 60.0*/);
	line20.lineTo(coords[86], coords[87]/*60.0, 45.0*/);
	System.out.println("creating path 10");
	Path2D cpath10 = Paths2D.createArc(coords[82], coords[83],
					     /*70.0, 40.0,*/
					     coords[oz], coords[oz+1],
					     /*1.0, 0.0,*/
					     coords[84], coords[85],
					     /*60.0, 70.0,*/
					     coords[zmo], coords[zmo+1]
					     /*0.0, -1.0*/);


	Path2D line21 = new BasicSplinePath2D();
	line21.moveTo(coords[88], coords[89]/*-70.0, 40.0*/);
	line21.lineTo(coords[90], coords[91]/*-50.0, 40.0*/);
	Path2D line22 = new BasicSplinePath2D();
	line22.moveTo(coords[92], coords[93]/*-80.0, 60.0*/);
	line22.lineTo(coords[94], coords[95]/*-80.0, 40.0*/);
	System.out.println("creating path 11");
	Path2D cpath11 = Paths2D.createArc(coords[90], coords[91],
					     /*-50.0, 40.0,*/
					     coords[oz], coords[oz+1],
					     /*1.0, 0.0,*/
					     coords[92], coords[93],
					     /*-90.0, 60.0,*/
					     coords[zmo], coords[zmo+1]
					     /*0.0, -1.0*/);

	g2d.setStroke(new BasicStroke(6.0F));
	g2d.setColor(new Color(0, 0, 255, 64));
	graph.draw(g2d, line1);
	graph.draw(g2d, line3);
	graph.draw(g2d, line5);
	graph.draw(g2d, line7);
	graph.draw(g2d, line9);
	graph.draw(g2d, line11);
	graph.draw(g2d, line13);
	graph.draw(g2d, line15);
	graph.draw(g2d, line17);
	graph.draw(g2d, line19);
	graph.draw(g2d, line21);

	g2d.setStroke(new BasicStroke(4.0F));
	g2d.setColor(Color.GREEN);
	graph.draw(g2d, line2);
	graph.draw(g2d, line4);
	graph.draw(g2d, line6);
	graph.draw(g2d, line8);
	graph.draw(g2d, line10);
	graph.draw(g2d, line12);
	graph.draw(g2d, line14);
	graph.draw(g2d, line16);
	graph.draw(g2d, line18);
	graph.draw(g2d, line20);
	graph.draw(g2d, line22);

	g2d.setStroke(new BasicStroke(2.0F));
	g2d.setColor(Color.BLACK);
	graph.draw(g2d, line1);
	graph.draw(g2d, cpath);
	graph.draw(g2d, line2);
	graph.draw(g2d, line3);
	graph.draw(g2d, cpath2);
	graph.draw(g2d, line4);
	graph.draw(g2d, line5);
	graph.draw(g2d, cpath3);
	graph.draw(g2d, line6);
	graph.draw(g2d, line7);
	graph.draw(g2d, cpath4);
	graph.draw(g2d, line8);
	graph.draw(g2d, line9);
	graph.draw(g2d, cpath5);
	graph.draw(g2d, line10);
	graph.draw(g2d, line11);
	graph.draw(g2d, cpath6);
	graph.draw(g2d, line12);
	graph.draw(g2d, line13);
	graph.draw(g2d, cpath7);
	graph.draw(g2d, line14);
	graph.draw(g2d, line15);
	graph.draw(g2d, cpath8);
	graph.draw(g2d, line16);
	graph.draw(g2d, line17);
	graph.draw(g2d, cpath9);
	graph.draw(g2d, line18);
	graph.draw(g2d, line19);
	graph.draw(g2d, cpath10);
	graph.draw(g2d, line20);
	graph.draw(g2d, line21);
	graph.draw(g2d, cpath11);
	graph.draw(g2d, line22);
	g2d.setColor(Color.RED);
	
	graph.write("png", new File("paths2d" + angle + (reflect? "R": "")
				    + ".png"));
    }


    public static void tests2(boolean reflect) throws Exception {

	System.out.println("test2");
	Graph graph = new Graph(1050,1050);
	graph.setOffsets(25,25);
	graph.setRanges(-100.0, 100.0, -100.0, 100.0);
	Graphics2D g2d = graph.createGraphics();

	double[] deltas = {0.0, -1.0, 1.0};
	double offset = 70.0;
	for (double delta: deltas) {
	    System.out.println("... delta = " + delta);
	    double doffset = 5*delta;
	    double offset1 = offset + doffset;
	    double coords1[] = {
		// AB
		-95.0, -90.0 + doffset,
		-90.0, -90.0 + doffset,
		-70.0+delta, -70.0 + doffset,
		-75.0+delta, -65.0 + doffset,
		// CD
		offset - 70.0, -90.0 + doffset,
		offset - 60.0 + delta, -90.0 + doffset,
		offset - 80.0, -70.0  + doffset,
		offset - 90.0, -80.0 + doffset,

		// EF
		-90.0, 60.0 + doffset,
		-80.0 + delta, 60.0 + doffset,
		-80.0, 80.0 + doffset,
		-90.0, 90.0 + doffset,
		// GH
		offset - 75.0, doffset + 60.0,
		offset - 70.0 + delta, doffset + 60.0,
		offset - 70.0, doffset + 80.0,
		offset - 80.0, doffset + 70.0,
	    };
	    double[] coords2 = new double[coords1.length];
	    if (reflect) {
		System.out.println("reflected");
		AffineTransform af =
		    AffineTransform.getScaleInstance(-1.0, 1.0);
		af.transform(coords1, 0, coords2, 0, coords1.length/2);
	    } else {
		System.arraycopy(coords1, 0, coords2, 0, coords1.length);
	    }
	    System.out.println("AB case");
	    Path2D lineA = new Path2D.Double();
	    lineA.moveTo(coords2[0], coords2[1]);
	    lineA.lineTo(coords2[2], coords2[3]);
	    Path2D lineB = new Path2D.Double();
	    lineB.moveTo(coords2[4], coords2[5]);
	    lineB.lineTo(coords2[6], coords2[7]);
	    Path2D pathAB = Paths2D.createArc(lineA, lineB);

	    System.out.println("CD case");
	    Path2D lineC = new Path2D.Double();
	    lineC.moveTo(coords2[8], coords2[9]);
	    lineC.lineTo(coords2[10], coords2[11]);
	    Path2D lineD = new Path2D.Double();
	    lineD.moveTo(coords2[12], coords2[13]);
	    lineD.lineTo(coords2[14], coords2[15]);
	    Path2D pathCD = Paths2D.createArc(lineC, lineD);

	    System.out.println("EF case");
	    Path2D lineE = new Path2D.Double();
	    lineE.moveTo(coords2[16], coords2[17]);
	    lineE.lineTo(coords2[18], coords2[19]);
	    Path2D lineF = new Path2D.Double();
	    lineF.moveTo(coords2[20], coords2[21]);
	    lineF.lineTo(coords2[22], coords2[23]);
	    Path2D pathEF = Paths2D.createArc(lineE, lineF);

	    System.out.println("GH case");
	    Path2D lineG = new Path2D.Double();
	    lineG.moveTo(coords2[24], coords2[25]);
	    lineG.lineTo(coords2[26], coords2[27]);
	    Path2D lineH = new Path2D.Double();
	    lineH.moveTo(coords2[28], coords2[29]);
	    lineH.lineTo(coords2[30], coords2[31]);
	    Path2D pathGH = Paths2D.createArc(lineG, lineH);

	    g2d.setColor(Color.GREEN);
	    g2d.setStroke(new BasicStroke(4.0F));
	    graph.draw(g2d, lineA);
	    graph.draw(g2d, lineB);
	    graph.draw(g2d, lineC);
	    graph.draw(g2d, lineD);
	    graph.draw(g2d, lineE);
	    graph.draw(g2d, lineF);
	    graph.draw(g2d, lineG);
	    graph.draw(g2d, lineH);
	    g2d.setColor(Color.BLACK);
	    g2d.setStroke(new BasicStroke(2.0F));
	    graph.draw(g2d, lineA);
	    graph.draw(g2d, pathAB);
	    graph.draw(g2d, lineB);
	    graph.draw(g2d, lineC);
	    graph.draw(g2d, pathCD);
	    graph.draw(g2d, lineD);

	    graph.draw(g2d, lineE);
	    graph.draw(g2d, pathEF);
	    graph.draw(g2d, lineF);
	    graph.draw(g2d, lineG);
	    graph.draw(g2d, pathGH);
	    graph.draw(g2d, lineH);

	}
	if (!reflect) {
	    // Test of variants that use paths and points
	    Path2D path = new Path2D.Double();
	    path.moveTo(-30.0, 0.0);
	    path.lineTo(-10.0, 0.0);
	    Point2D point = new Point2D.Double(10.0, 20.0);
	    Path2D arc = Paths2D.createArc(path, point);
	    g2d.setColor(Color.GREEN);
	    graph.draw(g2d, path);
	    g2d.setColor(Color.BLACK);
	    graph.draw(g2d, arc);
	}
	graph.write("png", new File("paths2dB" + (reflect? "R": "")
				    + ".png"));
    }

    private static class ConditionHandler {
	double x0 = 0.0, y0 = 0.0;
	double[] tcoords = new double[6];

	private static final double UVALS[] = {
	    0.0, 0.25, 0.5, 0.75, 1.0
	};

	void setup(double[] coords) {
	    x0 = coords[0];
	    y0 = coords[1];
	    System.arraycopy(coords, 2, tcoords, 0, coords.length - 2);
	}

	private Predicate<double[]> condition = (coords) -> {
	    setup(coords);

	    int type = (coords.length == 8)? PathIterator.SEG_QUADTO:
	    PathIterator.SEG_CUBICTO;

	    for (double u: UVALS) {
		if (!Path2DInfo.curvatureExists(u, x0, y0, type, tcoords)) {
		    return false;
		}
	    }

	    double mean = 0.0;
	    double sumsq = 0.0;
	    for (double u: UVALS) {
		double kappa = Path2DInfo.curvature(u, x0, y0, type, tcoords);
		mean += kappa;
		sumsq += kappa*kappa;
	    }
	    mean /= UVALS.length;
	    double variance = sumsq /UVALS.length - mean*mean;
	    double sdev  = Math.sqrt(variance);
	    mean = Math.abs(mean);
	    double length = Path2DInfo.segmentLength(type, x0, y0, coords);
	    // return !((sdev*20 < mean) || (mean*length > 5.0))
	    return (sdev*20.0 > mean) && (mean*length < 5.0);
	};

	public Predicate<double[]> getCondition() {
	    return condition;
	}
    }

    public static void main(String argv[]) throws Exception {
	tests(-1, false);
	tests(-1, true);

	tests2(false);
	tests2(true);
	for (int angle = 0; angle < 360; angle += 30) {
	    tests(angle, false);
	    tests(angle, true);
	}
	for (int angle = 45; angle < 360; angle += 90) {
	    tests(angle, false);
	    tests(angle, true);
	}

	// estimate errors for default maxDelta value
	System.out.println("------------");
	System.out.println("Circular Path:");

	Path2D path = Paths2D.createArc(0.0, 0.0, 1.0, 0.0,
					2*Math.PI);
	double[] tangent = new double[4];
	Path2DInfo.getTangent(path, Path2DInfo.Location.START, tangent, 0);
	Path2DInfo.getTangent(path, Path2DInfo.Location.END, tangent, 2);
	System.out.format("starting tangent: (%s,%s)\n",
			   tangent[0], tangent[1]);
	System.out.format("ending tangent: (%s,%s)\n", tangent[2], tangent[3]);
	double minr = 200.0;
	double maxr = 0.0;
	BasicStats stats = new BasicStats.Population();
	BasicStats cstats = new BasicStats.Population();
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(path)) {
	    int type = entry.getType();
	    Point2D start = entry.getStart();
	    if (start == null) continue;
	    double[] coords = entry.getCoords();
	    double[] nv = new double[2];
	    for (int i = 0; i < 1000; i++) {
		double u = i/1000.0;
		double x = Path2DInfo.getX(u, start.getX(), start.getY(),
					   type, coords);
		double y = Path2DInfo.getY(u, start.getX(), start.getY(),
					   type, coords);
		double r = Math.sqrt(x*x + y*y);
		if (r < minr) minr = r;
		if (r > maxr) maxr = r;
		stats.add(r);
		Path2DInfo.getNormal(u, nv, 0, start.getX(), start.getY(),
				     type, coords);
		double curvature =
		    Path2DInfo.curvature(u, start.getX(), start.getY(),
					 type, coords);
		cstats.add(curvature);
		double rc = 1.0/Math.abs(curvature);
		double xc = nv[0]/curvature;
		double yc = nv[1]/curvature;
	    }
	}
	System.out.println("Variation from a circle of radius 1:");
	System.out.format("mean-1 = %g, sdev = %g, 1-min = %s, max-1 = %s\n",
			  stats.getMean()-1.0, stats.getSDev(),
			  1.0-minr, maxr-1.0);
	System.out.format("Curvature: mean-1 = %g, sdev = %g, expected = 1.0\n",
			  cstats.getMean() - 1.0, cstats.getSDev());

	System.out.println("--------");
	System.out.println("Ellipse2D.Double as a circle");
	Ellipse2D circ = new Ellipse2D.Double(-1.0, -1.0, 2.0, 2.0);
	path = new Path2D.Double(circ);
	minr = 200;
	maxr = 0.0;
	stats = new BasicStats.Population();
	cstats = new BasicStats.Population();
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(path)) {
	    int type = entry.getType();
	    Point2D start = entry.getStart();
	    if (start == null) continue;
	    if (type == PathIterator.SEG_CLOSE) continue;
	    double[] coords = entry.getCoords();
	    double[] nv = new double[2];
	    for (int i = 0; i < 1000; i++) {
		double u = i/1000.0;
		double x = Path2DInfo.getX(u, start.getX(), start.getY(),
					   type, coords);
		double y = Path2DInfo.getY(u, start.getX(), start.getY(),
					   type, coords);
		double r = Math.sqrt(x*x + y*y);
		if (r < minr) minr = r;
		if (r > maxr) maxr = r;
		stats.add(r);
		Path2DInfo.getNormal(u, nv, 0, start.getX(), start.getY(),
				     type, coords);
		double curvature =
		    Path2DInfo.curvature(u, start.getX(), start.getY(),
					 type, coords);
		cstats.add(curvature);
		double rc = 1.0/Math.abs(curvature);
		double xc = nv[0]/curvature;
		double yc = nv[1]/curvature;
	    }
	}
	System.out.println("Variation from a circle of radius 1:");
	System.out.format("mean-1 = %g, sdev = %g, 1-min = %s, max-1 = %s\n",
			  stats.getMean()-1.0, stats.getSDev(),
			  1.0-minr, maxr-1.0);
	System.out.format("Curvature: mean-1 = %g, sdev = %g, expected = 1.0\n",
			  cstats.getMean() - 1.0, cstats.getSDev());

	System.out.println("----------------------");
	System.out.println("reverse test");
	Path2D path1 = new Path2D.Double();
	path1.moveTo(0.0, 0.0);
	path1.lineTo(1.0, 2.0);
	path1.quadTo(1.3, 2.4, 2.0, 3.0);
	path1.curveTo(2.3, 3.3, 4.5, 4.6, 5.0, 5.5);
	Path2D path1r = Paths2D.reverse(path1);
	Path2DInfo.printSegments(path1r);
	System.out.println("... closed path:");
	path1.closePath();
	path1r = Paths2D.reverse(path1);
	Path2DInfo.printSegments(path1r);
	System.out.println("-----------------------");
	System.out.println("clockwise arc test");
	BasicSplinePath2D spath = new BasicSplinePath2D();
	spath.moveTo(0.0, 200.0);
	spath.lineTo(400.0, 200.0);
	spath.append(Paths2D.createArc(spath, 100.0, false, Math.PI/2), true);
	Path2DInfo.printSegments(spath);

	Graph graph = new Graph(1000,1000);
	graph.setOffsets(0, 0);
	graph.setRanges(0.0, 1000.0, 0.0, 1000.0);
	Graphics2D g2d = graph.createGraphics();

	Graph graph2 = new Graph(1000,1000);
	graph2.setOffsets(0, 0);
	graph2.setRanges(0.0, 1000.0, 0.0, 1000.0);
	Graphics2D g2d2 = graph2.createGraphics();

	Graph graph3 = new Graph(1000,1000);
	graph3.setOffsets(0, 0);
	graph3.setRanges(0.0, 1000.0, 0.0, 1000.0);
	Graphics2D g2d3 = graph3.createGraphics();


	Path2D cpath = new Path2D.Double();
	Path2D cpath2 = new Path2D.Double();
	cpath.moveTo(100.0, 100.0);
	cpath.lineTo(150.0, 150.0);
	cpath.lineTo(200.0, 150.0);
	cpath.lineTo(250.0, 200.0);
	cpath.lineTo(300.0, 250.0);
	cpath.lineTo(400.0, 425.0);
	cpath.curveTo(450.0, 460.0, 500.0, 460.0, 550.0, 460.0);
	cpath.lineTo(650.0, 460.0);
	if (false) {
	    cpath2.moveTo(650.0, 460.0);
	    cpath2.curveTo(750.0, 540.0, 750.0, 655.0, 650.0, 700.0);
	} else {
	    cpath.curveTo(750.0, 540.0, 750.0, 655.0, 650.0, 700.0);
	}
	cpath.lineTo(550.0, 700.0);
	if (false) {
	    cpath2.moveTo(550.0, 700.0);
	    cpath2.curveTo(450.0, 650.0, 450.0, 600.0, 400.0, 550.0);
	} else {
	    cpath.curveTo(450.0, 650.0, 450.0, 600.0, 400.0, 550.0);
	}
	cpath.lineTo(300.0, 550.0);
	cpath.curveTo(250.0, 550.0, 200.0, 600.0, 200.0, 650.0);
	cpath.curveTo(200.0, 750.0, 300.0, 750.0, 400.0, 700.0);
	cpath.curveTo(450.0, 700.0, 500.0,750.0, 500.0, 800.0);
	cpath.curveTo(550.0, 850.0, 600.0, 850.0, 700.0, 800.0);
	cpath.lineTo(800.0, 800.0);


	Path2D opath = Paths2D.offsetBy(cpath, 20.0, 15.0, true);
	Path2D opath3 = Paths2D.offsetBy(cpath, 20.0, 0.0, true);

	ConditionHandler ch = new ConditionHandler();
	PathIterator pit = cpath.getPathIterator(null);
	Path2D tpath = new Path2D.Double(pit.getWindingRule());
	pit = new ConditionalPathIterator2D(pit, ch.getCondition(), 3);
	tpath.append(pit, false);

	g2d.setColor(Color.BLUE);
	g2d.setStroke(new BasicStroke(4.0F));
	graph.draw(g2d, tpath);
	g2d.setColor(Color.GREEN);
	g2d.setStroke(new BasicStroke(2.0F));
	graph.draw(g2d, cpath);
	g2d.setColor(Color.BLUE);
	graph.draw(g2d, cpath2);
	g2d.setColor(Color.BLACK);
	graph.draw(g2d, opath);

	graph3.draw(g2d3, cpath);
	graph3.draw(g2d3, opath3);
	g2d3.setColor(Color.BLUE);
	g2d3.setStroke(new BasicStroke(4.0F));
	graph3.draw(g2d3, cpath);
	g2d3.setColor(Color.BLACK);
	g2d3.setStroke(new BasicStroke(2.0F));
	graph3.draw(g2d3, opath3);

	g2d2.setColor(Color.BLACK);
	g2d2.setStroke(new BasicStroke(2.0F));
	graph2.draw(g2d2, cpath);
	graph2.draw(g2d2, opath);
	g2d2.setColor(Color.GREEN);
	graph2.draw(g2d2, Paths2D.offsetBy(cpath, 20.0, 15.0, 10.0, true));
	g2d2.setColor(Color.BLUE);
	graph2.draw(g2d2, Paths2D.offsetBy(cpath, 20.0, 15.0, 7.5, false));
	g2d2.setColor(Color.BLACK);

	cpath = Paths2D.createArc(700.0, 250.0,
				  700.0, 100.0,
				  2*Math.PI);
	cpath.closePath();
	opath = Paths2D.offsetBy(cpath, 20.0, 15.0, true);
	graph.draw(g2d, opath);
	graph2.draw(g2d2, opath);
	graph2.draw(g2d2, cpath);
	g2d.setColor(Color.GREEN);
	graph.draw(g2d, cpath);


	cpath2 = new Path2D.Double();
	cpath2.moveTo(25.0, 600.0);
	cpath2.curveTo(25.0, 900.0, 225.0, 600.0, 225.0, 950.0);

	Path2D cpath3 = new Path2D.Double();
	cpath3.moveTo(25.0, 250.0);
	cpath3.lineTo(50.0, 250.0);
	Path2D apath = Paths2D.createArc(cpath3, 100.0, true, Math.PI/2);
	cpath3.append(apath, true);
	apath = Paths2D.createArc(cpath3, 100.0, false, Math.PI/2);
	cpath3.append(apath, true);
	Point2D end = cpath3.getCurrentPoint();
	cpath3.lineTo(end.getX() + 50.0, end.getY());

	g2d.setColor(Color.GREEN);
	graph.draw(g2d, cpath2);
	graph.draw(g2d, cpath3);
	graph2.draw(g2d2, cpath2);
	graph2.draw(g2d2, cpath3);
	opath = Paths2D.offsetBy(cpath2, 20.0, 15.0, true);
	g2d.setColor(Color.BLACK);
	graph.draw(g2d, opath);
	graph2.draw(g2d2, opath);
	g2d2.setColor(Color.GREEN);
	graph2.draw(g2d2, Paths2D.offsetBy(cpath, 20.0, 15.0, 10.0, true));
	graph2.draw(g2d2, Paths2D.offsetBy(cpath2, 20.0, 15.0, 10.0, true));
	g2d2.setColor(Color.BLUE);
	graph2.draw(g2d2, Paths2D.offsetBy(cpath, 20.0, 15.0, 7.5, false));
	graph2.draw(g2d2, Paths2D.offsetBy(cpath2, 20.0, 15.0, 7.5, false));
	g2d2.setColor(Color.BLACK);

	opath = Paths2D.offsetBy(cpath3, 20.0, 15.0, true);
	graph.draw(g2d, opath);
	graph2.draw(g2d2, opath);
	g2d2.setColor(Color.GREEN);
	graph2.draw(g2d2, Paths2D.offsetBy(cpath3, 20.0, 15.0, 10.0, true));
	g2d2.setColor(Color.BLUE);
	graph2.draw(g2d2, Paths2D.offsetBy(cpath3, 20.0, 15.0, 7.5, false));
	g2d2.setColor(Color.BLACK);

	Path2D cpath4 = new Path2D.Double();
	cpath4.moveTo(50.0, 50.0);
	cpath4.lineTo(450.0, 50.0);
	cpath4.lineTo(450.0, 350.0);
	opath = Paths2D.offsetBy(cpath3, 20.0, 15.0, true);
	g2d.setColor(Color.GREEN);
	graph.draw(g2d, cpath4);
	g2d.setColor(Color.BLACK);
	graph.draw(g2d,Paths2D.offsetBy(cpath4, 20.0, 15.0, true));

	g2d.setColor(Color.BLACK);
	graph3.draw(g2d3, Paths2D.offsetBy(cpath4, 40.0, 0.0, false,
					   Math.PI/4));
	g2d3.setColor(Color.GREEN);
	graph3.draw(g2d3, cpath4);

	g2d.setColor(Color.BLACK);

	tpath = Paths2D.offsetBy(cpath4, 40.0, 0.0, false, Math.PI/4);
	int ccount = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(tpath)) {
	    if (entry.getType() == PathIterator.SEG_CUBICTO) ccount++;
	}
	System.out.println("tpath size: "
			   + Path2DInfo.getEntries(tpath).size()
			   + ", ccount = " + ccount);
	tpath = Paths2D.offsetBy(cpath4, 40.0, 0.0, false, Math.PI/8);
	ccount = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(tpath)) {
	    if (entry.getType() == PathIterator.SEG_CUBICTO) ccount++;
	}
	System.out.println("tpath size: "
			   + Path2DInfo.getEntries(tpath).size()
			   + ", ccount = " + ccount);
	graph.write("png", new File("Paths2DOffset.png"));
	graph2.write("png", new File("Paths2DOffset2.png"));
	graph3.write("png", new File("Paths2DOffset3.png"));

    }
}
