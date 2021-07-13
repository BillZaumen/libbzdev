import org.bzdev.graphs.*;
import org.bzdev.gio.OutputStreamGraphics;
import org.bzdev.util.units.MKS;
import java.awt.Color;
import java.io.FileOutputStream;

public class ClockAxisBuilderTest {

    public static void main(String argv[]) throws Exception {
	Graph graph = new Graph(800, 600);
	graph.setRanges(0.0, MKS.hours(2.0), 0.0, 40.0);
	graph.setOffsets(75, 75);
	graph.setBackgroundColor(Color.WHITE);
	graph.clear();

	int level0 = 0;
	int level1 = 1;
	int level2 = 2;
	int level3 = 3;

	System.out.println("Graph 1");

	// case 1
	AxisBuilder.ClockTime ab =
	    new AxisBuilder.ClockTime(graph, 0.0, 0.0, MKS.hours(2.0), true,
				      "Axis Label");

	ab.setSpacings(AxisBuilder.Spacing.SECONDS,
		       AxisBuilder.Spacing.HOURS);

	ab.addTickSpec(level0, AxisBuilder.Spacing.HOURS, "%1$TH:%1$TM" );
	ab.addTickSpec(level1, AxisBuilder.Spacing.TEN_MINUTES, null);
	ab.addTickSpec(level2, AxisBuilder.Spacing.MINUTES, null);
	
	graph.draw(ab.createAxis());

	ab = new AxisBuilder.ClockTime(graph, 0.0, 10.0, MKS.hours(2.0), true,
				       null);

	ab.setSpacings(AxisBuilder.Spacing.SECONDS,
		       AxisBuilder.Spacing.HOURS);

	ab.addTickSpec(level0, AxisBuilder.Spacing.HOURS, "%1$TH:%1$TM" );
	ab.addTickSpec(level1, AxisBuilder.Spacing.TEN_MINUTES, null);
	ab.addTickSpec(level2, AxisBuilder.Spacing.MINUTES, null);
	ab.addTickSpec(level3, AxisBuilder.Spacing.THIRTY_SECONDS, null);
	
	graph.draw(ab.createAxis());

	ab = new AxisBuilder.ClockTime(graph, 0.0, 20.0, MKS.hours(2.0), true,
				       null);

	ab.setSpacings(AxisBuilder.Spacing.SECONDS,
		       AxisBuilder.Spacing.HOURS);

	ab.addTickSpec(level0, AxisBuilder.Spacing.HOURS, "%1$TH:%1$TM" );
	ab.addTickSpec(level1, AxisBuilder.Spacing.THIRTY_MINUTES, null);
	ab.addTickSpec(level2, AxisBuilder.Spacing.FIFTEEN_MINUTES, null);
	
	graph.draw(ab.createAxis());

	ab = new AxisBuilder.ClockTime(graph, 0.0, 30.0, MKS.hours(2.0), true,
				       null);

	ab.setSpacings(AxisBuilder.Spacing.MINUTES,
		       AxisBuilder.Spacing.HOURS);

	ab.addTickSpec(level0, AxisBuilder.Spacing.HOURS, "%1$TH:%1$TM" );
	ab.addTickSpec(level1, AxisBuilder.Spacing.TEN_MINUTES, null);
	ab.addTickSpec(level2, AxisBuilder.Spacing.FIVE_MINUTES, null);
	ab.addTickSpec(level3, AxisBuilder.Spacing.MINUTES, null);
	
	graph.draw(ab.createAxis());
	graph.write("png", "abclocktest1.png");

	System.out.println("Graph 2");

	graph = new Graph(2*800, 2*600);
	graph.setRanges(0.0, MKS.minutes(2.0), 0.0, 40.0);
	graph.setOffsets(75, 75);
	graph.setBackgroundColor(Color.WHITE);
	graph.clear();

	ab = new AxisBuilder.ClockTime(graph, 0.0, 0.0, MKS.hours(1.0), true,
				       "Label");
	ab.setSpacings(AxisBuilder.Spacing.SECONDS,
		       AxisBuilder.Spacing.MINUTES);
	ab.setNumberOfSteps(10);
	ab.addTickSpec(level0, AxisBuilder.Spacing.MINUTES, "%TT");
	ab.addTickSpec(level1, AxisBuilder.Spacing.TEN_SECONDS);
	ab.addTickSpec(level2, AxisBuilder.Spacing.SECONDS);
	ab.addTickSpec(level3, 5);
	graph.draw(ab.createAxis());

	ab = new AxisBuilder.ClockTime(graph, 0.0, 10.0, MKS.hours(1.0), true,
				       null);
	ab.setSpacings(AxisBuilder.Spacing.SECONDS,
		       AxisBuilder.Spacing.MINUTES);
	ab.setNumberOfSteps(10);
	ab.addTickSpec(level0, AxisBuilder.Spacing.MINUTES, "%TT");
	ab.addTickSpec(level1, AxisBuilder.Spacing.THIRTY_SECONDS, "%TT");
	ab.addTickSpec(level1, AxisBuilder.Spacing.TEN_SECONDS);
	ab.addTickSpec(level2, AxisBuilder.Spacing.SECONDS);
	ab.addTickSpec(level3, 2);
	graph.draw(ab.createAxis());

	ab = new AxisBuilder.ClockTime(graph, 0.0, 20.0, MKS.hours(1.0), true,
				       null);
	ab.setSpacings(AxisBuilder.Spacing.SECONDS,
		       AxisBuilder.Spacing.MINUTES);
	ab.setNumberOfSteps(10);
	ab.addTickSpec(level0, AxisBuilder.Spacing.MINUTES, "%TT");
	ab.addTickSpec(level1, AxisBuilder.Spacing.TEN_SECONDS);
	ab.addTickSpec(level1, AxisBuilder.Spacing.FIVE_SECONDS);
	ab.addTickSpec(level3, AxisBuilder.Spacing.SECONDS);
	graph.draw(ab.createAxis());

	ab = new AxisBuilder.ClockTime(graph, 0.0, 30.0, MKS.hours(1.0), true,
				       null);
	ab.setSpacings(AxisBuilder.Spacing.SECONDS,
		       AxisBuilder.Spacing.MINUTES);
	ab.setNumberOfSteps(10);
	ab.addTickSpec(level0, AxisBuilder.Spacing.MINUTES, "%TR");
	ab.addTickSpec(level1, AxisBuilder.Spacing.THIRTY_SECONDS);
	ab.addTickSpec(level1, AxisBuilder.Spacing.TEN_SECONDS);
	ab.addTickSpec(level3, AxisBuilder.Spacing.FIVE_SECONDS);
	graph.draw(ab.createAxis());

	graph.write("png", "abclocktest2.png");

	graph = new Graph(2*800, 2*600);
	graph.setRanges(0.0, MKS.days(2.0), 0.0, 40.0);
	graph.setOffsets(75, 75);
	graph.setBackgroundColor(Color.WHITE);
	graph.clear();
	ab = new AxisBuilder.ClockTime(graph, 0.0, 0.0, MKS.days(2.0), true,
				       null);
	ab.setSpacings(AxisBuilder.Spacing.MINUTES, AxisBuilder.Spacing.DAYS);
	ab.addTickSpec(level0, AxisBuilder.Spacing.DAYS, "Day %5$d");
	ab.addTickSpec(level0, AxisBuilder.Spacing.TWELVE_HOURS, "%TR");
	ab.addTickSpec(level1, AxisBuilder.Spacing.HOURS);
	ab.addTickSpec(level2, AxisBuilder.Spacing.TEN_MINUTES);
	graph.draw(ab.createAxis());

	ab = new AxisBuilder.ClockTime(graph, 0.0, 10.0, MKS.days(2.0), true,
				       null);
	ab.setSpacings(AxisBuilder.Spacing.MINUTES, AxisBuilder.Spacing.DAYS);
	ab.addTickSpec(level0, AxisBuilder.Spacing.DAYS, "Day %5$d");
	ab.addTickSpec(level0, AxisBuilder.Spacing.TWELVE_HOURS, "%TR");
	ab.addTickSpec(level1, AxisBuilder.Spacing.HOURS);
	ab.addTickSpec(level2, AxisBuilder.Spacing.TEN_MINUTES);
	ab.addTickSpec(level3, AxisBuilder.Spacing.FIVE_MINUTES);
	graph.draw(ab.createAxis());

	graph.write("png", "abclocktest3.png");

	FileOutputStream os = new FileOutputStream("abclocktest4.png");
	OutputStreamGraphics osg =
	    OutputStreamGraphics.newInstance(os, 1600, 400, "png");
	graph = new Graph(osg);
	graph.setRanges(0.0, MKS.hours(2.0), 0.0, 40.0);
	graph.setOffsets(75, 75);
	graph.setBackgroundColor(Color.WHITE);
	graph.clear();
	
	ab = new AxisBuilder.ClockTime(graph, 0.0, 0.0, MKS.hours(2.0), true,
				       null);

	ab.setSpacings(AxisBuilder.Spacing.SECONDS,
		       AxisBuilder.Spacing.HOURS);

	ab.addTickSpec(level0, AxisBuilder.Spacing.HOURS, "%1$TH:%1$TM" );
	ab.addTickSpec(level1, AxisBuilder.Spacing.THIRTY_MINUTES,
		       "%1$TR" );
	ab.addTickSpec(level1, AxisBuilder.Spacing.TEN_MINUTES, null);
	ab.addTickSpec(level2, AxisBuilder.Spacing.MINUTES, null);
	
	graph.draw(ab.createAxis());
	graph.write();
	osg.close();
	os.close();

	os = new FileOutputStream("abclocktest4.ps");
	osg = OutputStreamGraphics.newInstance(os, 1600, 400, "ps");
	graph = new Graph(osg);
	graph.setRanges(0.0, MKS.hours(2.0), 0.0, 40.0);
	graph.setOffsets(75, 75);
	graph.setBackgroundColor(Color.WHITE);
	graph.clear();
	
	ab = new AxisBuilder.ClockTime(graph, 0.0, 0.0, MKS.hours(2.0), true,
				       null);

	ab.setSpacings(AxisBuilder.Spacing.SECONDS,
		       AxisBuilder.Spacing.HOURS);

	ab.addTickSpec(level0, AxisBuilder.Spacing.HOURS, "%1$TH:%1$TM" );
	ab.addTickSpec(level1, AxisBuilder.Spacing.THIRTY_MINUTES,
		       "%1$TR" );
	ab.addTickSpec(level1, AxisBuilder.Spacing.TEN_MINUTES, null);
	ab.addTickSpec(level2, AxisBuilder.Spacing.MINUTES, null);
	
	graph.draw(ab.createAxis());
	graph.write();
	osg.close();
	os.close();

	System.exit(0);
    }
}