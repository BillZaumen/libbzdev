import java.util.ArrayList;
import org.bzdev.geom.*;


public class BuilderTest7 {

    private static void add(ArrayList<SplinePathBuilder.CPoint> list, int i,
			    boolean ctl, boolean end)
    {
	switch (i) {
	case 0:
	    if (end) {
		list.add(new SplinePathBuilder.CPoint
			 (SplinePathBuilder.CPointType.SEG_END, 0.0, 0.0));
	    }
	    if (ctl) {
		list.add(new SplinePathBuilder.CPoint
			  (SplinePathBuilder.CPointType.CONTROL,
			   1.0/3.0, 0.0 ));
	    }

	    list.add(new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SPLINE, 1.0, 1.0 ));
	    list.add(new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SPLINE, 2.0 , 4.0));
	    list.add(new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SPLINE, 3.0, 9.0));
	    list.add(new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SPLINE, 4.0, 16.0));
	    if (ctl) {
		list.add(new SplinePathBuilder.CPoint
			  (SplinePathBuilder.CPointType.CONTROL,
			   4.0 + 2.0/3.0, 25.0));
	    }
	    list.add(new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SEG_END, 5.0, 25.0));
	    break;
	case 1:
	    if (end) {
		list.add(new SplinePathBuilder.CPoint
			 (SplinePathBuilder.CPointType.SEG_END, 0.0, 0.0+1.0));
	    }
	    if (ctl) {
		list.add(new SplinePathBuilder.CPoint
			  (SplinePathBuilder.CPointType.CONTROL,
			   1.0/3.0, 0.0+1.0 ));
	    }
	    list.add(new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SPLINE, 1.0, 1.0+1.0 ));
	    list.add(new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SPLINE, 2.0 , 4.0+1.0));
	    list.add(new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SPLINE, 3.0, 9.0+1.0));
	    list.add(new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SPLINE, 4.0, 16.0+1.0));
	    if (ctl) {
		list.add(new SplinePathBuilder.CPoint
			  (SplinePathBuilder.CPointType.CONTROL,
			   4.0 + 2.0/3.0, 25.0+1.0));
	    }
	    list.add(new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SEG_END, 5.0, 25.0+1.0));

	    break;
	case 2:
	    if (end) {
		list.add(new SplinePathBuilder.CPoint
			 (SplinePathBuilder.CPointType.SEG_END, 0.0, 2.0));
	    }
	    if (ctl) {
		list.add(new SplinePathBuilder.CPoint
			  (SplinePathBuilder.CPointType.CONTROL, 1.0/3.0, 2.0));
	    }
	    list.add(new SplinePathBuilder.CPoint
		     ((t) -> {return t;}, (t) -> {return 2.0 + t*t;},
		      1.0, 4.0, 3));
	    if (ctl) {
		list.add(new SplinePathBuilder.CPoint
			  (SplinePathBuilder.CPointType.CONTROL,
			   4.0 + 2.0/3.0, 25.0 + 2.0));
	    }
	    list.add(new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SEG_END, 5.0, 25.0+2.0));
	    break;
	case 3:
	    if (end) {
		list.add(new SplinePathBuilder.CPoint
			 (SplinePathBuilder.CPointType.SEG_END, 0.0, 3.0));
	    }
	    if (ctl) {
		list.add(new SplinePathBuilder.CPoint
			  (SplinePathBuilder.CPointType.CONTROL, 1.0/3.0, 3.0));
	    }
	    list.add(new SplinePathBuilder.CPoint
		     ((t) -> {return t;}, (t) -> {return 3.0 + t*t;},
		      1.0, 4.0, 3));
	    if (ctl) {
		list.add(new SplinePathBuilder.CPoint
			  (SplinePathBuilder.CPointType.CONTROL,
			   4.0 + 2.0/3.0, 25.0 + 3.0));
	    }
	    list.add(new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SEG_END, 5.0, 25.0+3.0));
	    break;
	case 4:
	    if (end) {
		list.add(new SplinePathBuilder.CPoint
			 (SplinePathBuilder.CPointType.SEG_END_NEXT, 0.0, 4.0));
	    }
	    if (ctl) {
		list.add(new SplinePathBuilder.CPoint
			  (SplinePathBuilder.CPointType.CONTROL, 1.0/3.0, 4.0));
	    }
	    list.add(new SplinePathBuilder.CPoint
		     ((t) -> {return t;}, (t) -> {return 4.0 + t*t;},
		      0.0, 5.0, 5));
	    if (ctl) {
		list.add(new SplinePathBuilder.CPoint
			 (SplinePathBuilder.CPointType.CONTROL,
			  4.0 + 2.0/3.0, 25.0 + 4.0));
	    }
	    list.add(new SplinePathBuilder.CPoint
		     (SplinePathBuilder.CPointType.SEG_END_PREV));
	    break;
	case 5:
	    if (end) {
		list.add(new SplinePathBuilder.CPoint
			 (SplinePathBuilder.CPointType.SEG_END, 0.0, 4.0));
	    }
	    
	    list.add(new SplinePathBuilder.CPoint
		     (SplinePathBuilder.CPointType.SEG_END_NEXT));
	    if (ctl) {
		list.add(new SplinePathBuilder.CPoint
			  (SplinePathBuilder.CPointType.CONTROL,
			   5+1.0/3.0, 4.0));
	    }
	    list.add(new SplinePathBuilder.CPoint
		     ((t) -> {return 5+t;}, (t) -> {return 4.0 + t*t;},
		      0.0, 5.0, 5));
	    if (ctl) {
		list.add(new SplinePathBuilder.CPoint
			  (SplinePathBuilder.CPointType.CONTROL,
			   5.0 + 4.0 + 2.0/3.0, 25.0 + 4.0));
	    }
	    list.add(new SplinePathBuilder.CPoint
		      (SplinePathBuilder.CPointType.SEG_END_PREV));
	    break;
	}
    }

    public static void main(String argv[]) throws Exception {
	BasicSplinePathBuilder spb = new BasicSplinePathBuilder();
	boolean ctl = (argv.length == 0) || argv[0].equalsIgnoreCase("true");
	boolean end = (argv.length == 0) || argv[1].equalsIgnoreCase("true");
	boolean once = argv.length == 4;

	ArrayList<SplinePathBuilder.CPoint> list = new ArrayList<>(128);
	
	if (once) {
	    int i = Integer.parseInt(argv[2]);
	    int j = Integer.parseInt(argv[3]);
	    add(list, i, ctl, end);
	    add(list, j, ctl, end);
	} else {
	    boolean[] ctls = new boolean[argv.length == 0? 2: 1];
	    if (ctls.length == 1) {
		ctls[0] = ctl;
	    } else {
		ctls[0] = false;
		ctls[1] = true;
	    }

	    boolean[] ends = new boolean[argv.length == 0? 2: 1];
	    if (ends.length == 1) {
		ends[0] = end;
	    } else {
		ends[0] = false;
		ends[1] = true;
	    }

	    list.add(new SplinePathBuilder.CPoint
		     (SplinePathBuilder.CPointType.MOVE_TO, 0.0, 0.0));
	    for (boolean flag1: ctls) {
		for (boolean flag2: ends) {
		    for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 6; j++) {
			    System.out
				.format("case %b %b %d %d: list index >= %d\n",
					flag1, flag2, i, j, list.size());
			    add(list, i, flag1, flag2);
			    add(list, j, flag1, flag2);
			}
		    }
		}
	    }
	}

	spb.initPath();
	spb.append(list);

	SplinePath2D path = spb.getPath();
	System.out.println(".... segments ...");
	Path2DInfo.printSegments(path);

	System.exit(0);
    }
}
