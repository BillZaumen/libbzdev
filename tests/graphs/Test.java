import org.bzdev.graphs.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.awt.geom.*;
import java.io.*;

import org.bzdev.gio.OutputStreamGraphics;
import org.bzdev.gio.ImageSequenceWriter;
import org.bzdev.gio.ISWriterOps;
import org.bzdev.gio.ImageOrientation;

public class Test {

    private static void printTransform(AffineTransform af) {
	double[] matrix = new double[6];
	af.getMatrix(matrix);
	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 3; j++) {
		int k = j * 2 + i;
		System.out.print(matrix[k] + " ");
	    }
	    System.out.println();
	}
    }

    private static void createShortGraph(Graph graph)
	throws Exception
    {
	// We substantially reduce the range so that the image will
	// be over twice as big, in order to make sure that
	// createGraphicsGCS works correctly.
	graph.setOffsets(75,75);
	graph.setRanges(0.0, 200.0, 0.0, 200.0);
	    Graph.Axis xAxis = new
		Graph.Axis(0.0, 0.0, Graph.Axis.Dir.HORIZONTAL_INCREASING,
			   200.0, 0.0, 10.0, false);
	    xAxis.addTick(new Graph.TickSpec(4.0, 1.0, 10, "%1.0f", 1.0));
	    xAxis.addTick(new Graph.TickSpec(2.0, 1.0, 5));
	    xAxis.addTick(new Graph.TickSpec(1.0, 1.0, 1));
	    xAxis.setWidth(4.0);
	    xAxis.setLabel("X Axis Label");

	    Graph.Axis yAxis = new
		Graph.Axis(0.0, 0.0, Graph.Axis.Dir.VERTICAL_INCREASING,
			   200.0, 0.0, 10.0, true);
	    yAxis.addTick(new Graph.TickSpec(4.0, 1.0, 10, "%1.0f", 1.0));
	    yAxis.addTick(new Graph.TickSpec(2.0, 1.0, 5));
	    yAxis.addTick(new Graph.TickSpec(1.0, 1.0, 1));
	    yAxis.setWidth(4.0);
	    yAxis.setLabel("Y Axis Label");

	    System.out.println("xAxis");
	    graph.draw(xAxis);

	    System.out.println("yAxis");
	    graph.draw(yAxis);
	    System.out.println("both Axis complete");

	    Graphics2D g2dgcs = graph.createGraphicsGCS();
	    g2dgcs.setColor(Color.BLACK);
	    g2dgcs.setStroke(new BasicStroke(10.0f, BasicStroke.CAP_BUTT,
					     BasicStroke.JOIN_MITER));
	    g2dgcs.draw(new Line2D.Double(10.0, 25.0, 100.0, 25.0));
	    g2dgcs.dispose();
    }

    private static void createGraph(Graph graph, boolean reflect)
	throws Exception
    {
	    graph.setOffsets(75,75);
	    if (reflect) {
		graph.setRanges(500.0, 0.0, 500.0, 0.0);
	    } else {
		graph.setRanges(0.0, 500.0, 0.0, 500.0);
		double xscale1 = graph.getXScale();
		double yscale1 = graph.getYScale();
		System.out.println("xscale1 = " + xscale1);
		System.out.println("yscale1 = " + yscale1);
		System.out.println("graph.getXLower() = " + graph.getXLower());
		System.out.println("graph.getXUpper() = " + graph.getXUpper());
		System.out.println("graph.getYLower() = " + graph.getYLower());
		System.out.println("graph.getYupper() = " + graph.getYUpper());
		graph.setRanges(0.0, 0.0, 0.0, 0.0, xscale1, yscale1);
		if (Math.abs(graph.getXUpper()- 500.0) > 1.e-10
		    || Math.abs(graph.getYUpper()- 500.0) > 1.e-10) {
		    System.out.println
			("error on setRanges using scale factors [1]");
		    System.out.println("xscale1 = " + xscale1);
		    System.out.println("yscale1 = " + yscale1);
		    System.out.println("graph.getXUpper() = "
				       + graph.getXUpper());
		    System.out.println("graph.getYupper() = "
				       + graph.getYUpper());
		}
		double xscale2 = graph.getXScale();
		double yscale2 = graph.getYScale();
		if (Math.abs(xscale1 - xscale2) > 1.e-10
		    || Math.abs(yscale1 - yscale2) > 1.e-10) {
		    System.out.println
			("error on setRanges using scale factors [2]");
		}
	    }

	    Graph.Axis xAxis = new 
		Graph.Axis(0.0, 0.0, Graph.Axis.Dir.HORIZONTAL_INCREASING,
			   500.0, 0.0, 10.0, reflect);
	    xAxis.addTick(new Graph.TickSpec(4.0, 1.0, 10, "%1.0f", 1.0));
	    xAxis.addTick(new Graph.TickSpec(2.0, 1.0, 5));
	    xAxis.addTick(new Graph.TickSpec(1.0, 1.0, 1));
	    xAxis.setWidth(4.0);
	    xAxis.setLabel("X Axis Label");

	    Graph.Axis yAxis = new 
		Graph.Axis(0.0, 0.0, Graph.Axis.Dir.VERTICAL_INCREASING,
			   500.0, 0.0, 10.0, !reflect);
	    yAxis.addTick(new Graph.TickSpec(4.0, 1.0, 10, "%1.0f", 1.0));
	    yAxis.addTick(new Graph.TickSpec(2.0, 1.0, 5));
	    yAxis.addTick(new Graph.TickSpec(1.0, 1.0, 1));
	    yAxis.setWidth(4.0);
	    yAxis.setLabel("Y Axis Label");
	    
	    System.out.println("xAxis");
	    graph.draw(xAxis);
	    
	    System.out.println("yAxis");
	    graph.draw(yAxis);
	    System.out.println("both Axis complete");

	    Graphics2D g2d = graph.createGraphics();

	    GraphicsConfiguration gc = g2d.getDeviceConfiguration();
	    if (gc != null) {
		Rectangle gcRect = gc.getBounds();
		System.out.println("gcRect.x = " + gcRect.x
				   + ", gcREct.y = " + gcRect.y
				   + ", gcREct.width = " + gcRect.width
				   + ", gcRect.height = " + gcRect.height);
	    } else {
		System.out.println("no graphics configuration");
	    }

	    g2d.setColor(Color.BLACK);

	    Image img = ImageIO.read(new File("testimg.png"));

	    Graphics2D g2dgcs = graph.createGraphicsGCS();
	    g2dgcs.setColor(Color.BLACK);
	    g2dgcs.setStroke(new BasicStroke(10.0f, BasicStroke.CAP_BUTT,
					     BasicStroke.JOIN_MITER));
	    g2dgcs.draw(new Line2D.Double(10.0, 25.0, 100.0, 25.0));
	    g2dgcs.dispose();

	    graph.drawImage(g2d, img, 150.0, 500.0, RefPointName.UPPER_LEFT,
			    0.0, 0.25, 0.25);

	    graph.drawImage(g2d, img, 150.0, 500.0, RefPointName.LOWER_LEFT,
			    0.0, 0.25, 0.25);

	    graph.drawImage(g2d, img, 150.0, 500.0, RefPointName.UPPER_RIGHT,
			    0.0, 0.25, 0.25);

	    graph.drawImage(g2d, img, 150.0, 500.0, RefPointName.LOWER_RIGHT,
			    0.0, 0.25, 0.25);

	    graph.drawImage(g2d, img, 150.0, 400.0, RefPointName.CENTER_LEFT,
			    0.0, 0.25, 0.25);
	    graph.drawImage(g2d, img, 150.0, 400.0, RefPointName.CENTER_RIGHT,
			    0.0, 0.25, 0.25);

	    graph.drawImage(g2d, img, 350.0, 100.0, RefPointName.UPPER_CENTER,
			    0.0, 0.25, 0.25);

	    graph.drawImage(g2d, img, 350.0, 100.0, RefPointName.LOWER_CENTER,
			    0.0, 0.25, 0.25);

	    graph.drawImage(g2d, img, 350.0, 200.0, RefPointName.CENTER,
			    0.0, 0.25, 0.125);

	    graph.drawImage(g2d, img, 300.0, 450.0, RefPointName.CENTER,
			    Math.PI/4.0, 0.25, 0.25);


	    graph.drawImage(g2d, img, 370.0, 250.0, RefPointName.LOWER_LEFT,
			    0.0, 0.25, 0.125);
	    graph.drawImage(g2d, img, 370.0, 250.0, RefPointName.LOWER_LEFT,
			    Math.PI/2.0, 0.25, 0.125);
	    graph.drawImage(g2d, img, 370.0, 250.0, RefPointName.LOWER_LEFT,
			    Math.PI, 0.25, 0.125);

	    graph.drawImage(g2d, img, 200.0, 100.0, RefPointName.CENTER,
			    0.0, 0.25, 0.25, false, true);

	    graph.drawImage(g2d, img, 50.0, 100.0, RefPointName.CENTER,
			    0.0, 0.25, 0.25, true, false);

	    graph.draw(g2d, new Line2D.Double(10.0, 10.0, 500.0, 10.0));
	    graph.draw(g2d, new Line2D.Double(10.0, 50.0, 500.0, 50.0));

	    graph.setFontJustification(reflect? Graph.Just.RIGHT:
				       Graph.Just.LEFT);
	    graph.drawString("Hello", 100.0, 100.0);
	    graph.setFontJustification(Graph.Just.CENTER);
	    graph.drawString("Hello", 100.0, 140.0);
	    graph.setFontJustification(reflect? Graph.Just.LEFT:
				       Graph.Just.RIGHT);
	    graph.drawString("Hello", 100.0, 180.0);
	    graph.setFontJustification(reflect?Graph.Just.RIGHT:
				       Graph.Just.LEFT);

	    graph.draw(g2d, new Line2D.Double(0.0, 200.0, 100.0, 200.0));
	    graph.drawString("age", 100.0, 200.0);

	    graph.draw(g2d, new Line2D.Double(0.0, 250.0, 100.0, 250.0));
	    graph.setFontBaseline(reflect? Graph.BLineP.BOTTOM:
				  Graph.BLineP.TOP);
	    graph.drawString("age", 100.0, 250.0);

	    graph.draw(g2d, new Line2D.Double(0.0, 300.0, 100.0, 300.0));
	    graph.setFontBaseline(reflect? Graph.BLineP.TOP:
				  Graph.BLineP.BOTTOM);
	    graph.drawString("age", 100.0, 300.0);
	    graph.setFontBaseline(Graph.BLineP.BASE);

	    graph.setFontJustification(reflect? Graph.Just.RIGHT:
				       Graph.Just.LEFT);
	    graph.setFontBaseline(Graph.BLineP.BASE);
	    graph.draw(g2d, new Line2D.Double(190.0, 300.0, 210.0, 300.0));
	    graph.setFontAngle(90.0);
	    graph.drawString("angle", 200.0, 300.0);
	    graph.setFontAngle(270.0);
	    graph.drawString("angle", 200.0, 300.0);
	    graph.draw(g2d, new Line2D.Double(300.0, 300.0, 320.0, 300.0));
	    graph.setFontJustification(Graph.Just.CENTER);
	    graph.setFontBaseline(reflect? Graph.BLineP.TOP:
				  Graph.BLineP.BOTTOM);
	    graph.setFontAngle(90.0);
	    graph.drawString("angle", 300.0, 300.0);
	    graph.setFontAngle(270.0);
	    graph.drawString("angle", 320.0, 300.0);

	    Ellipse2D ellipse = new Ellipse2D.Double(200.0, 150.0, 40.0, 40.0);
	    graph.draw(g2d, ellipse);
	    ellipse = new Ellipse2D.Double(200.0, 200.0, 40.0, 40.0);
	    graph.fill(g2d, ellipse);

	    graph.setFont(graph.getFontToFit("Fit Here",
					     new Point2D.Double(50.0, 330.0),
					     new Point2D.Double(150.0, 330.0)));
	    graph.setFontAngle(0.0);
	    graph.drawString("Fit Here", 100.0, 330.0);
	    graph.drawString("line 1\nline2", 100.0, 280.0);

	    graph.setFont(null);
	    graph.draw(g2d, new Line2D.Double(50.0, 330.0, 150.0, 330.0));

	    Graph graph2 = new Graph(graph, true);
	    g2d = graph2.createGraphics();
	    g2d.setColor(Color.BLACK);

	    if (reflect) {
		graph2.setRanges(1.0, 0.0, 145.0, 90.0);
	    } else {
		graph2.setRanges(0.0, 1.0, 90.0, 145.0);
	    }
	    graph2.draw(g2d,
		       new Line2D.Double(0.9, 100.0, 0.9, 140.0));

	    graph2.draw(g2d, new Line2D.Double(1.0, 94.0, 1.1, 94.0));


	    graph2.fill(g2d,
		       new Rectangle2D.Double(0.8, 130.0, 0.1, 10.0));

	    Graph.Axis yRAxis =
		new Graph.Axis(1.0, 90.0, Graph.Axis.Dir.VERTICAL_INCREASING,
			       55.0, 90.0, 1.0, reflect);

	    yRAxis.addTick(new Graph.TickSpec(4.0, 1.0, 10, "%1.0f", 1.0));
	    yRAxis.addTick(new Graph.TickSpec(2.0, 1.0, 5));
	    yRAxis.addTick(new Graph.TickSpec(1.0, 1.0, 1));
	    yRAxis.setWidth(4.0);
	    yRAxis.setLabel("YR Axis Label");
	    System.out.println("draw YR Axis");
	    graph2.draw(yRAxis);
	    System.out.println("YR Axis drawn");

	    Graph.UserDrawable ud = new Graph.UserDrawable() {
		    public Shape toShape(boolean xAxisPointsRight,
					 boolean yAxisPointsDown)
		    {
			Path2D.Double path = new Path2D.Double();

			double xsign = xAxisPointsRight? 1.0: -1.0;
			double ysign = yAxisPointsDown? 1.0: -1.0;

			path.moveTo(-xsign*10.0, ysign*10.0);
			path.lineTo(xsign*10.0, ysign*10.0);
			path.lineTo(0.0, -ysign*10.0);
			path.closePath();
			return path;
		    }
		};

	    graph.draw(g2d, ud, 250.0, 100.0);
	    graph.fill(g2d, ud, 250.0, 100.0);

	    double xsign = graph.xAxisPointsRight()? 1.0: -1.0;
	    double ysign = graph.yAxisPointsDown()? 1.0: -1.0;
	    g2d.translate(xsign*20.0, 0.0);
	    // should appear shifted right (left for 'reflect' case)
	    // 20.0 units in user space.
	    // so the symbols will just touch.
	    graph.draw(g2d, ud, 250.0, 100.0);
	    graph.fill(g2d, ud, 250.0, 100.0);
	    g2d.translate(0.0, ysign*20.0);
	    graph.draw(g2d, ud, 250.0, 100.0);
	    graph.fill(g2d, ud, 250.0, 100.0);
	    g2d.dispose();
    }

    private static void createGraphLogAxes(Graph graph, boolean reflect)
	throws Exception
    {
	    graph.setOffsets(75,75);
	    if (reflect) {
		graph.setRanges(3.0, 0.0, 3.0, 0.0);
	    } else {
		graph.setRanges(0.0, 3.0, 0.0, 3.0);
	    }
	    Graph.Axis xAxis = new
		Graph.LogAxis(0.0, 0.0, Graph.Axis.Dir.HORIZONTAL_INCREASING,
			      3.0, 1.0, reflect);
	    xAxis.addTick(new Graph.TickSpec(4.0, 1.0, 9, "%1.0f", 1.0));
	    xAxis.addTick(new Graph.TickSpec(3.0, 1.0, 9, 4));
	    xAxis.addTick(new Graph.TickSpec(2.0, 0.5, 1));
	    xAxis.addTick(new Graph.TickSpec(2.0, 1.0, 1, 0, 4));
	    xAxis.setWidth(4.0);
	    xAxis.setLabel("X Axis Label");

	    Graph.Axis yAxis = new
		Graph.LogAxis(0.0, 0.0, Graph.Axis.Dir.VERTICAL_INCREASING,
			      3.0, 1.0, !reflect);
	    yAxis.addTick(new Graph.TickSpec(4.0, 1.0, 9, "%1.0f", 1.0));
	    yAxis.addTick(new Graph.TickSpec(3.0, 1.0, 9, 4));
	    yAxis.addTick(new Graph.TickSpec(2.0, 0.5, 1));
	    yAxis.addTick(new Graph.TickSpec(2.0, 1.0, 1, 0, 4));
	    yAxis.setWidth(4.0);
	    yAxis.setLabel("Y Axis Label");

	    System.out.println("xAxis");
	    graph.draw(xAxis);

	    System.out.println("yAxis");
	    graph.draw(yAxis);
	    System.out.println("both Axis complete");
    }

    private static void createGraphRevAxes(Graph graph, boolean reflect)
	throws Exception
    {
	    graph.setOffsets(75,75);
	    if (reflect) {
		graph.setRanges(500.0, 0.0, 500.0, 0.0);
	    } else {
		graph.setRanges(0.0, 500.0, 0.0, 500.0);
	    }
	    Graph.Axis xAxis = new
		Graph.Axis(500.0, 0.0, Graph.Axis.Dir.HORIZONTAL_DECREASING,
			   500.0, 500.0, 10.0, !reflect);
	    xAxis.addTick(new Graph.TickSpec(4.0, 1.0, 10, "%1.0f", 1.0));
	    xAxis.addTick(new Graph.TickSpec(2.0, 1.0, 5));
	    xAxis.addTick(new Graph.TickSpec(1.0, 1.0, 1));
	    xAxis.setWidth(4.0);
	    xAxis.setLabel("X Axis Label");

	    Graph.Axis yAxis = new
		Graph.Axis(500.0, 500.0, Graph.Axis.Dir.VERTICAL_DECREASING,
			   500.0, 500.0, 10.0, !reflect);
	    yAxis.addTick(new Graph.TickSpec(4.0, 1.0, 10, "%1.0f", 1.0));
	    yAxis.addTick(new Graph.TickSpec(2.0, 1.0, 5));
	    yAxis.addTick(new Graph.TickSpec(1.0, 1.0, 1));
	    yAxis.setWidth(4.0);
	    yAxis.setLabel("Y Axis Label");
	    graph.draw(xAxis);
	    graph.draw(yAxis);
    }

    private static void createRevGraphLogAxes(Graph graph, boolean reflect)
	throws Exception
    {
	    graph.setOffsets(75,75);
	    if (reflect) {
		graph.setRanges(3.0, 0.0, 3.0, 0.0);
	    } else {
		graph.setRanges(0.0, 3.0, 0.0, 3.0);
	    }

	    Graph.Axis xAxis = new
		Graph.LogAxis(3.0, 0.0, Graph.Axis.Dir.HORIZONTAL_DECREASING,
			      3.0, 1.0, !reflect);
	    xAxis.addTick(new Graph.TickSpec(4.0, 1.0, 9, "%1.0f", 1.0));
	    xAxis.addTick(new Graph.TickSpec(3.0, 1.0, 9, 4));
	    xAxis.addTick(new Graph.TickSpec(2.0, 0.5, 1));
	    xAxis.addTick(new Graph.TickSpec(2.0, 1.0, 1, 0, 4));
	    xAxis.setWidth(4.0);
	    xAxis.setLabel("X Axis Label");

	    Graph.Axis yAxis = new
		Graph.LogAxis(0.0, 3.0, Graph.Axis.Dir.VERTICAL_DECREASING,
			      3.0, 1.0, reflect);
	    yAxis.addTick(new Graph.TickSpec(4.0, 1.0, 9, "%1.0f", 1.0));
	    yAxis.addTick(new Graph.TickSpec(3.0, 1.0, 9, 4));
	    yAxis.addTick(new Graph.TickSpec(2.0, 0.5, 1));
	    yAxis.addTick(new Graph.TickSpec(2.0, 1.0, 1, 0, 4));
	    yAxis.setWidth(4.0);
	    yAxis.setLabel("Y Axis Label");

	    System.out.println("xAxis");
	    graph.draw(xAxis);

	    System.out.println("yAxis");
	    graph.draw(yAxis);
	    System.out.println("both Axis complete");
    }

    private static void createSymbolGraph(Graph graph, Graph.Symbol symbol,
					  boolean reflect)
    {
	    graph.setOffsets(75,75);
	    if (reflect) {
		graph.setRanges(500.0, 0.0, 500.0, 0.0);
	    } else {
		graph.setRanges(0.0, 500.0, 0.0, 500.0);
	    }
	    graph.draw(symbol, 100.0, 100.0);
	    graph.drawEX(symbol, 200.0, 200.0, 40.0, 60.0);
	    graph.drawEY(symbol, 300.0, 300.0, 40.0, 60.0);
	    graph.drawEXY(symbol, 400.0, 400.0,
			  20.0, 30.0, 40.0, 60.0);
    }

    private static void createSymbolGraph(Graph graph, Graph.SymbolFactory sf)
    {
	    graph.setOffsets(75,75);
	    graph.setRanges(0.0, 100.0, 0.0, 100.0);
	    int k = 0;
	    for (String name: sf.getSymbolNames()) {
		int i = k % 10;
		int j = k / 10;
		Graph.Symbol symbol = sf.newSymbol(name);
		double x = 5.0 + i*10.0;
		double y = 5.0 + j + 10.0;
		graph.drawEXY(symbol, x, y, 4.0, 4.0);
		k++;
	    }
    }

    static void checkGraphImageTypes() throws Exception {
	if (Graph.ImageType.INT_RGB.getType() != BufferedImage.TYPE_INT_RGB) {
	    throw new Exception("bad type");
	}
	if (Graph.ImageType.INT_RGB.getType() != BufferedImage.TYPE_INT_RGB) {
	    throw new Exception("bad type");
	}
	if (Graph.ImageType.INT_ARGB_PRE.getType() !=
	    BufferedImage.TYPE_INT_ARGB_PRE) {
	    throw new Exception("bad type");
	}
	if (Graph.ImageType.INT_BGR.getType() != BufferedImage.TYPE_INT_BGR) {
	    throw new Exception("bad type");
	}
	if (Graph.ImageType.THREE_BYTE_BGR.getType() !=
	    BufferedImage.TYPE_3BYTE_BGR) {
	    throw new Exception("bad type");
	}
	if (Graph.ImageType.FOUR_BYTE_ABGR.getType() !=
	    BufferedImage.TYPE_4BYTE_ABGR) {
	    throw new Exception("bad type");
	}
	if (Graph.ImageType.FOUR_BYTE_ABGR_PRE.getType() !=
	    BufferedImage.TYPE_4BYTE_ABGR_PRE) {
	    throw new Exception("bad type");
	}
	if (Graph.ImageType.USHORT_565_RGB.getType() !=
	    BufferedImage.TYPE_USHORT_565_RGB) {
	    throw new Exception("bad type");
	}
	if (Graph.ImageType.USHORT_555_RGB.getType() !=
	    BufferedImage.TYPE_USHORT_555_RGB) {
	    throw new Exception("bad type");
	}
	if (Graph.ImageType.BYTE_GRAY.getType() !=
	    BufferedImage.TYPE_BYTE_GRAY) {
	    throw new Exception("bad type");
	}
	if (Graph.ImageType.USHORT_GRAY.getType() !=
	    BufferedImage.TYPE_USHORT_GRAY) {
	    throw new Exception("bad type");
	}
	if (Graph.ImageType.BYTE_BINARY.getType() !=
	    BufferedImage.TYPE_BYTE_BINARY) {
	    throw new Exception("bad type");
	}
	if (Graph.ImageType.BYTE_INDEXED.getType() !=
	    BufferedImage.TYPE_BYTE_INDEXED) {
	    throw new Exception("bad type");
	}
    }

    public static void main(String argv[]) throws Exception {
	try {
	    checkGraphImageTypes();


	    Graph graph = new Graph();
	    System.out.println("creating testGraph.png");
	    createGraph(graph, false);
	    graph.write("png", "testGraph.png");

	    // now try using OutputStreamGraphics

	    System.out.println("default png width = "
			       + OutputStreamGraphics
			       .getDefaultWidth("png",
						ImageOrientation.NORMAL));
	    System.out.println("default png height = "
			       + OutputStreamGraphics
			       .getDefaultHeight("png",
						 ImageOrientation.NORMAL));
	    System.out.println("default ps width = "
			       + OutputStreamGraphics
			       .getDefaultWidth("ps",
						ImageOrientation.NORMAL));
	    System.out.println("default ps height = "
			       + OutputStreamGraphics
			       .getDefaultHeight("ps",
						 ImageOrientation.NORMAL));

	    FileOutputStream os = new FileOutputStream("testGraph.ps");
	    OutputStreamGraphics osg = OutputStreamGraphics.newInstance
		(os, Graph.DEFAULT_WIDTH, Graph.DEFAULT_HEIGHT, "ps");
	    graph = new Graph(osg);
	    System.out.println("creating testGraph.ps");
	    createGraph(graph, false);
	    graph.write();

	    if (OutputStreamGraphics.getMediaTypeForImageType("svg") != null) {
		os = new FileOutputStream("testGraph.svg");
		osg = OutputStreamGraphics.newInstance
		    (os, Graph.DEFAULT_WIDTH, Graph.DEFAULT_HEIGHT, "svg");
		graph = new Graph(osg);
		System.out.println("creating testGraph.svg");
		createGraph(graph, false);
		graph.write();
	    }

	    os = new FileOutputStream("testGraph2.png");
	    osg = OutputStreamGraphics.newInstance
		(os, Graph.DEFAULT_WIDTH, Graph.DEFAULT_HEIGHT, 0.60, "png");
	    graph = new Graph(osg);
	    System.out.println("creating testGraph2.png");
	    createGraph(graph, false);
	    graph.write();

	    // now test log axes
	    graph = new Graph();
	    System.out.println("creating testLogAxis.png");
	    createGraphLogAxes(graph, false);
	    graph.write("png", "testLogAxis.png");

	    // now test reverse axes
	    graph = new Graph();
	    System.out.println("creating testRevAxes.png");
	    createGraphRevAxes(graph, false);
	    graph.write("png", "testRevAxes.png");

	    // now test reverse log axes
	    graph = new Graph();
	    System.out.println("creating testRevLogAxis.png");
	    createRevGraphLogAxes(graph, false);
	    graph.write("png", "testRevLogAxis.png");

	    // now repeat sequence but reflecting the graph by swapping
	    // startX with endX and startY with endY
	    System.out.println("creating testReflectGraph.png");
	    graph = new Graph();
	    createGraph(graph, true);
	    graph.write("png", "testReflectGraph.png");

	    System.out.println("creating testReflectLogAxis.png");
	    graph = new Graph();
	    createGraphLogAxes(graph, true);
	    graph.write("png", "testReflectLogAxis.png");

	    System.out.println("creating testReflectRevAxes.png");
	    graph = new Graph();
	    createGraphRevAxes(graph, true);
	    graph.write("png", "testReflectRevAxes.png");

	    graph = new Graph();
	    System.out.println("creating testReflectRevLogAxis.png");
	    createRevGraphLogAxes(graph, true);
	    graph.write("png", "testReflectRevLogAxis.png");


	    Graph.SymbolFactory sf = new Graph.SymbolFactory();
	    sf.setLineThickness(2.0);
	    sf.setColor(Color.BLACK);

	    System.out.println("symbol names:");
	    for (String name: sf.getSymbolNames()) {
		System.out.println("    " + name);
	    }
	    Graph.Symbol symbol = sf.newSymbol("SolidCircle");
	    if (symbol == null)
		throw new Exception("could not find SolidCircle");
	    System.out.println("creating testSymbolGraph.png");
	    graph = new Graph();
	    createSymbolGraph(graph, symbol, false);
	    graph.write("png", "testSymbolGraph.png");

	    symbol = sf.newSymbol("EmptyCircle");
	    System.out.println("creating testReflectSymbolGraph.png");
	    graph = new Graph();
	    createSymbolGraph(graph, symbol, true);
	    graph.write("png", "testReflectSymbolGraph.png");


	    symbol = sf.newSymbol("EmptySquare");
	    System.out.println("creating testSymbolGraph2.png");
	    graph = new Graph();
	    createSymbolGraph(graph, symbol, false);
	    graph.write("png", "testSymbolGraph2.png");

	    symbol = sf.newSymbol("SolidSquare");
	    System.out.println("creating testSymbolGraph3.png");
	    graph = new Graph();
	    createSymbolGraph(graph, symbol, false);
	    graph.write("png", "testSymbolGraph3.png");

	    graph = new Graph();
	    System.out.println("creating testSymbolGraph4.png");
	    createSymbolGraph(graph, sf);
	    graph.write("png", "testSymbolGraph4.png");

	    graph = new Graph();
	    System.out.println("creating testShortGraph.png");
	    createShortGraph(graph);
	    graph.write("png", "testShortGraph.png");

	    ISWriterOps isw  = new ImageSequenceWriter
		(new FileOutputStream("testGraph.isq"));
	    graph = new Graph(Graph.DEFAULT_WIDTH, Graph.DEFAULT_HEIGHT,
			      false, isw);
	    isw.addMetadata(Graph.DEFAULT_WIDTH, Graph.DEFAULT_HEIGHT,
			    "image/png");

	    graph.setOSGraphics(isw.nextOutputStreamGraphics("case 1"));
	    createGraph(graph, false);
	    graph.write();
	    graph.setOSGraphics(isw.nextOutputStreamGraphics("case 2"));
	    createGraph(graph, true);
	    graph.write();
	    isw.close();

	    graph = new Graph();
	    graph.setRanges(-100.0, 100.0, -200.0, 200.0);
	    System.out.println(graph.boundingBox(false));
	    Rectangle2D r2d = new Rectangle2D.Double(101.0, 201.0, 10.0, 20.0);
	    System.out.println(graph.maybeVisible(r2d, true));
	    r2d = new Rectangle2D.Double(701.0, 701.0, 10, 10);
	    System.out.println(graph.maybeVisible(r2d, false));
	    r2d = new Rectangle2D.Double(0.0, 0.0, 10.0, 20.0);
	    System.out.println(graph.maybeVisible(r2d, true));
	    System.out.println(graph.maybeVisible(r2d, false));
	    System.out.println(graph.boundingBox(true));
	    graph.setRotation(Math.PI/4, 0.0, 0.0);
	    System.out.println(graph.boundingBox(true));
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
