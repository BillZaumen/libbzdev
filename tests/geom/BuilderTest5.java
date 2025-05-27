import java.util.ArrayList;
import org.bzdev.geom.*;

public class BuilderTest5 {
    public static void main(String argv[]) throws Exception {
	BasicSplinePathBuilder spb = new BasicSplinePathBuilder();
	boolean ctl = (argv.length == 0) || argv[0].equalsIgnoreCase("true");

	ArrayList<SplinePathBuilder.CPoint> list = new ArrayList<>(128);
	if (true) {
	    list.add(new SplinePathBuilder.CPoint
		     (SplinePathBuilder.CPointType.MOVE_TO, 0.0, 0.0));
	    if (ctl) {
		list.add( new SplinePathBuilder.CPoint
			  (SplinePathBuilder.CPointType.CONTROL,
			   1.0/3.0, 0.0 ));
	    }
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SPLINE, 1.0, 1.0 ));
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SPLINE, 2.0 , 4.0));
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SPLINE, 3.0, 9.0));
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SPLINE, 4.0, 16.0));
	    if (ctl) {
		list.add( new SplinePathBuilder.CPoint
			  (SplinePathBuilder.CPointType.CONTROL,
			   4.0 + 2.0/3.0, 25.0));
	    }
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SEG_END, 5.0, 25.0));

	    list.add(new SplinePathBuilder.CPoint
		     (SplinePathBuilder.CPointType.MOVE_TO, 0.0, 0.0+1.0));
	    if (ctl) {
		list.add( new SplinePathBuilder.CPoint
			  (SplinePathBuilder.CPointType.CONTROL,
			   1.0/3.0, 0.0+1.0 ));
	    }
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SPLINE, 1.0, 1.0+1.0 ));
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SPLINE, 2.0 , 4.0+1.0));
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SPLINE, 3.0, 9.0+1.0));
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SPLINE, 4.0, 16.0+1.0));
	    if (ctl) {
		list.add( new SplinePathBuilder.CPoint
			  (SplinePathBuilder.CPointType.CONTROL,
			   4.0 + 2.0/3.0, 25.0+1.0));
	    }
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SEG_END, 5.0, 25.0+1.0));

	    list.add(new SplinePathBuilder.CPoint
		     (SplinePathBuilder.CPointType.MOVE_TO, 0.0, 2.0));
	    if (ctl) {
		list.add( new SplinePathBuilder.CPoint
			  (SplinePathBuilder.CPointType.CONTROL, 1.0/3.0, 2.0));
	    }
	    list.add(new SplinePathBuilder.CPoint
		     ((t) -> {return t;}, (t) -> {return 2.0 + t*t;},
		      1.0, 4.0, 3));
	    if (ctl) {
		list.add( new SplinePathBuilder.CPoint
			  (SplinePathBuilder.CPointType.CONTROL,
			   4.0 + 2.0/3.0, 25.0 + 2.0));
	    }
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SEG_END, 5.0, 25.0+2.0));

	    list.add(new SplinePathBuilder.CPoint
		     (SplinePathBuilder.CPointType.MOVE_TO, 0.0, 3.0));
	    if (ctl) {
		list.add( new SplinePathBuilder.CPoint
			  (SplinePathBuilder.CPointType.CONTROL, 1.0/3.0, 3.0));
	    }
	    list.add(new SplinePathBuilder.CPoint
		     ((t) -> {return t;}, (t) -> {return 3.0 + t*t;},
		      1.0, 4.0, 3));
	    if (ctl) {
		list.add( new SplinePathBuilder.CPoint
			  (SplinePathBuilder.CPointType.CONTROL,
			   4.0 + 2.0/3.0, 25.0 + 3.0));
	    }
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SEG_END, 5.0, 25.0+3.0));

	    list.add(new SplinePathBuilder.CPoint
		     (SplinePathBuilder.CPointType.MOVE_TO_NEXT, 0.0, 4.0));
	    if (ctl) {
		list.add( new SplinePathBuilder.CPoint
			  (SplinePathBuilder.CPointType.CONTROL, 1.0/3.0, 4.0));
	    }
	    list.add(new SplinePathBuilder.CPoint
		     ((t) -> {return t;}, (t) -> {return 4.0 + t*t;},
		      0.0, 5.0, 5));
	    if (ctl) {
		list.add( new SplinePathBuilder.CPoint
			  (SplinePathBuilder.CPointType.CONTROL,
			   4.0 + 2.0/3.0, 25.0 + 4.0));
	    }
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SEG_END_PREV));

	    list.add(new SplinePathBuilder.CPoint
		     (SplinePathBuilder.CPointType.MOVE_TO, 0.0, 4.0));
	    
	    list.add(new SplinePathBuilder.CPoint
		     (SplinePathBuilder.CPointType.SEG_END_NEXT));
	    if (ctl) {
		list.add( new SplinePathBuilder.CPoint
			  (SplinePathBuilder.CPointType.CONTROL,
			   5+1.0/3.0, 4.0));
	    }
	    list.add(new SplinePathBuilder.CPoint
		     ((t) -> {return 5+t;}, (t) -> {return 4.0 + t*t;},
		      0.0, 5.0, 5));
	    if (ctl) {
		list.add( new SplinePathBuilder.CPoint
			  (SplinePathBuilder.CPointType.CONTROL,
			   5.0 + 4.0 + 2.0/3.0, 25.0 + 4.0));
	    }
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SEG_END_PREV));
	} else {
	}

	spb.initPath();
	spb.append(list);

	BasicSplinePath2D path = spb.getPath();
	System.out.println(".... segments ...");
	Path2DInfo.printSegments(path);

	System.out.println("... try a closed path");
	list.clear();
	spb = new BasicSplinePathBuilder();
	list.add(new SplinePathBuilder.CPoint
		 (SplinePathBuilder.CPointType.MOVE_TO, 0.0, 0.0));
	if (ctl) {
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.CONTROL,
		       1.0/3.0, 0.0 ));
	}
	list.add( new SplinePathBuilder.CPoint
		  (SplinePathBuilder.CPointType.SPLINE, 1.0, 1.0 ));
	list.add( new SplinePathBuilder.CPoint
		  (SplinePathBuilder.CPointType.SPLINE, 2.0 , 4.0));
	list.add( new SplinePathBuilder.CPoint
		  (SplinePathBuilder.CPointType.SPLINE, 3.0, 9.0));
	list.add( new SplinePathBuilder.CPoint
		  (SplinePathBuilder.CPointType.SPLINE, 4.0, 16.0));
	if (ctl) {
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.CONTROL,
		       4.0 + 2.0/3.0, 25.0));
	}
	list.add(new SplinePathBuilder.CPoint
		 (SplinePathBuilder.CPointType.CLOSE));
	spb.initPath();
	spb.append(list);
	path = spb.getPath();
	System.out.println(".... segments ...");
	Path2DInfo.printSegments(path);	

	    
	if (ctl) {
	    System.out.println("... just  an initial control point");
	    list.clear();
	    spb = new BasicSplinePathBuilder();
	    list.add(new SplinePathBuilder.CPoint
		     (SplinePathBuilder.CPointType.MOVE_TO, 0.0, 0.0));
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.CONTROL,
		       1.0/3.0, 0.0 ));
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SPLINE, 1.0, 1.0 ));
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SPLINE, 2.0 , 4.0));
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SPLINE, 3.0, 9.0));
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SPLINE, 4.0, 16.0));
	    list.add(new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.CLOSE));
	    spb.initPath();
	    spb.append(list);
	    path = spb.getPath();
	    System.out.println(".... segments ...");
	    Path2DInfo.printSegments(path);	

	    System.out.println("... just a final control point");
	    list.clear();
	    spb = new BasicSplinePathBuilder();
	    list.add(new SplinePathBuilder.CPoint
		     (SplinePathBuilder.CPointType.MOVE_TO, 0.0, 0.0));
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SPLINE, 1.0, 1.0 ));
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SPLINE, 2.0 , 4.0));
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SPLINE, 3.0, 9.0));
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SPLINE, 4.0, 16.0));
	    list.add( new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.CONTROL,
		       4.0 + 2.0/3.0, 25.0));
	    list.add(new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.CLOSE));
	    spb.initPath();
	    spb.append(list);
	    path = spb.getPath();
	    System.out.println(".... segments ...");
	    Path2DInfo.printSegments(path);	

	}

	System.exit(0);
    }
}


