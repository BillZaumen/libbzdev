scripting.importClasses("org.bzdev.p3d", ["Model3D", "P3d.Rectangle"]);
scripting.importClass("org.bzdev.anim2d.Animation2D");
scripting.importClass("java.awt.Color");

/*
importPackage(org.bzdev.p3d);
importPackage(org.bzdev.anim2d);
importClass(java.awt.Color);
Rectangle = P3d.Rectangle;
*/

m3d = new Model3D();


a2d = scripting.create(Animation2D, scripting, 700, 700, 30000.0, 1000);
a2d.setBackgroundColor(Color.blue.darker().darker());

mvf = a2d.createFactory("org.bzdev.p3d.Model3DViewFactory");
mvf.setModel(m3d);

// create a view of the model.
mv = mvf.createObject("view",
		      [{"visible": true,
			"edgeColor.green": 255,
			"border": 50,
			"phi": 30.0,
			"theta": 30.0,
			"magnification": 0.9,
			"changeScale": false},
		       {"withPrefix": "timeline",
			withIndex: [
			    {"time2": 2.0,
			     "phiRate": 180.0/10.0},
			    {"time": 12.0,
			     "phiRate": 0.0},
			    {"time": 13.0,
			     "thetaRate": 90.0/5.0},
			    {"time": 18.0,
			     "thetaRate": 0.0,
			     "phiRate": 180.0/10.0},
			    {"time": 28.0,
			     "phiRate": 0.0},
			    {"time": 30.0,
			     "phi": 0.0,
			     "theta": 0.0,
			     "psi": 0.0,
			     "forceScaleChange": true}]}]);
