import java.util.ArrayList;
import org.bzdev.geom.*;


public class BuilderTest6 {

    private static void add(ArrayList<SplinePathBuilder.CPoint> list, int i,
			    boolean ctl)
    {
	switch (i) {
	case 0:
	    list.add(new SplinePathBuilder.CPoint
		     (SplinePathBuilder.CPointType.MOVE_TO, 0.0, 0.0));
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
	    list.add(new SplinePathBuilder.CPoint
		     (SplinePathBuilder.CPointType.MOVE_TO, 0.0, 0.0+1.0));
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
	    list.add(new SplinePathBuilder.CPoint
		     (SplinePathBuilder.CPointType.MOVE_TO, 0.0, 2.0));
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
	    list.add(new SplinePathBuilder.CPoint
		     (SplinePathBuilder.CPointType.MOVE_TO, 0.0, 3.0));
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
	    list.add(new SplinePathBuilder.CPoint
		     (SplinePathBuilder.CPointType.MOVE_TO_NEXT, 0.0, 4.0));
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
	    list.add(new SplinePathBuilder.CPoint
		     (SplinePathBuilder.CPointType.MOVE_TO, 0.0, 4.0));
	    
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
	SplinePathBuilder spb = new SplinePathBuilder();
	boolean ctl = (argv.length == 0) || argv[0].equalsIgnoreCase("true");
	boolean once = argv.length == 3;

	ArrayList<SplinePathBuilder.CPoint> list = new ArrayList<>(128);
	
	if (once) {
	    int i = Integer.parseInt(argv[1]);
	    int j = Integer.parseInt(argv[2]);
	    add(list, i, ctl);
	    add(list, j, ctl);
	} else {
	    boolean[] ctls = new boolean[argv.length == 0? 2: 1];
	    if (ctls.length == 1) {
		ctls[0] = ctl;
	    } else {
		ctls[0] = false;
		ctls[1] = true;
	    }
	    for (boolean flag: ctls) {
		for (int i = 0; i < 6; i++) {
		    for (int j = 0; j < 6; j++) {
			System.out.format("case %b %d %d: list index >= %d\n",
					  flag, i, j, list.size());
			add(list, i, flag);
			add(list, j, flag);
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


