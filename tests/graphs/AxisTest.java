import org.bzdev.graphs.*;
import org.bzdev.gio.*;
import org.bzdev.imageio.*;
import org.bzdev.math.*;
import org.bzdev.math.stats.ChiSquareStat;
import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.Locale;

// Test to see if each axis prints labels and tick marks correctly.
// This was based on a program that behaved incorrectly.
public class AxisTest {

    public static void main(String[] argv) throws Exception {

	String filename = argv[0];
	// smode means to turn on scaling
	boolean smode = (argv.length > 1);
	String itype = OutputStreamGraphics.getImageTypeForFile(filename);
	OutputStream os = new FileOutputStream(filename);
	OutputStreamGraphics osg =
	    OutputStreamGraphics.newInstance(os,800, 600, itype);

	Graph graph = new Graph(osg);
	graph.setRanges(-3.0, 1.0, 30.0, 70.0);
	graph.setOffsets(100, 50, 100, 50);

	Graph.Axis xAxis =
	    new Graph.LogAxis(-3.0, 30.0,
			      Graph.Axis.Dir.HORIZONTAL_INCREASING,
			      4.0, 1.0, false);
	if (smode) xAxis.setAxisScale(0.1);
	xAxis.setWidth(3.0);
	xAxis.addTick(new Graph.TickSpec(5.0, 1.0, 9,
					 "%4.3f", 5.0));
	xAxis.setLabel("Test label for the X axis to see if it is centered)");
	xAxis.setLabelOffset(10.0);

	Graph.Axis yAxis = new Graph.Axis(-3.0, 30.0,
					  Graph.Axis.Dir.VERTICAL_INCREASING,
					  40.0, 30.0, 10.0, true);
	if (smode) yAxis.setAxisScale(10.0);
	yAxis.setWidth(3.0);
	yAxis.addTick(new Graph.TickSpec(5.0, 1.0, 1, "%3.0f", 5.0));
	yAxis.setLabel("TestLabel for the Y axis to see if it is centered");
	yAxis.setLabelOffset(10.0);
	graph.draw(xAxis);
	graph.draw(yAxis);

	Graph.FontParms fp = graph.getFontParms();
	fp.setAngle(90.0);
	fp.setJustification(Graph.Just.CENTER);
	fp.setBaseline(Graph.BLineP.TOP);
	graph.drawString("50", -2.9, 50.0, fp);
	

	graph.write();
	System.exit(0);
    }
}
