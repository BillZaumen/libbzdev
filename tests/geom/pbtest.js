// This file can be run using scrunner. The two javascript
// functions are copied from the SplinePathBuilder.xml file
// so that these functions can be tested independently. One
// should double check that the copies in this file are up to
// date before running a test.

function configPathBuilderAux(pb, spec) {
    if (Array.isArray(spec)) {
	for (var i = 0; i < spec.length; i++) {
	    if (Array.isArray(spec[i])) {
		this.configPathBuilderAux(pb, spec[i]);
	    } else if (spec[i].type == "CLOSE" || spec[i].type
		       == pb.constantCLOSE()) {
		pb.append(pb.createCPointClose());
	    } else if (spec[i].type == "MOVE_TO" || spec[i].type
		       == pb.constantMOVE_TO()){
		pb.append(pb.createCPointMoveTo(spec[i].x, spec[i].y));
	    } else if (spec[i].type == "SEG_END" || spec[i].type
		       == pb.constantSEG_END()){
		pb.append(pb.createCPointSegEnd(spec[i].x, spec[i].y));
	    } else if (spec.type == "CONTROL" || spec[i].type
		       == pb.constantCONTROL()){
		pb.append(pb.createCPointControl(spec[i].x, spec[i].y));
	    } else if (spec.type == "SPLINE" || spec[i].type
		       == pb.constantSPLINE()) {
		pb.append(pb.createCPointSpline(spec[i].x, spec[i].y));
	    } else {
		throw "bad type: " + spec.type + " at index " +i;
	    }
	}
    }
}

function configurePathBuilder(pb, windingRule, spec) {
    var wr = 0;
    if (windingRule == "WIND_EVEN_ODD" ||
	windingRule == java.awt.geom.Path2D.WIND_EVEN_ODD ||
	windingRule == pb.constantWIND_EVEN_ODD()) {
	wr = pb.constantWIND_EVEN_ODD();
    } else if (windingRule == "WIND_NON_ZERO" ||
	       windingRule == java.awt.geom.Path2D.WIND_NON_ZERO ||
	       windingRule == pb.constantWIND_NON_ZERO()) {
	wr = pb.constantWIND_NON_ZERO();
    } else {
	throw "bad winding rule: " + windingRule;
    }
    pb.initPath(wr);
    configPathBuilderAux(pb, spec);
}

var path1 = [
    {type: "MOVE_TO", x: 20.0, y: 30.0},
    {type: "SEG_END", x: 50.0, y: 60.0}];

var path2 = [
    {type: "MOVE_TO", x: 120.0, y: 130.0},
    {type: "SEG_END", x: 150.0, y: 160.0}];

var pathspec = [path1, path2]

var pb = new org.bzdev.geom.SplinePathBuilder();
java.lang.System.out.println("typeof pb = " + pb.getClass());

configurePathBuilder(pb, "WIND_EVEN_ODD", pathspec);

var path = pb.getPath();
org.bzdev.geom.Path2DInfo.printSegments(path);

var out = scripting.getWriter();

// now test the actual implementation
out.println();
out.println("Now use PathBuilder");
out.println();
pb = new org.bzdev.geom.SplinePathBuilder(scripting);
pb.configure("WIND_EVEN_ODD", pathspec);
path = pb.getPath();
org.bzdev.geom.Path2DInfo.printSegments(path);

