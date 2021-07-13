scripting.importClasses("org.bzdev.p3d", ["Model3D", "P3d.Rectangle"]);
scripting.importClass("org.bzdev.anim2d.Animation2D");
scripting.importClass("java.awt.Color");

// need an output stream named stl
if (typeof(stl) == "undefined") {
    throw "error: variable stl undefined";
}

// need a directory accessor for the animation.
if (typeof(da) == "undefined") {
    throw "error: directory-accesssor variable da undefined";
}

m3d = new Model3D();

z0 = 0.0;  // base
// Set parameters with defaults.
// first level
if (typeof(z1) == "undefined") z1 = 10.0;
// second level
if (typeof(z2) == "undefined") z2 = 20.0;

// hole width
if (typeof(w) == "undefined") w = 30.0;
// first level width
if (typeof(w1) == "undefined") w1 = 4.0;
// second level width
if (typeof(w2) == "undefined") w2 = 8.0;

// hole height;
if (typeof(h) == "undefined") h = 60.0;
// first level height;
if (typeof(h1) == "undefined") h1 = 4.0;
// second level height;
if (typeof(h2) == "undefined") h2 = 8.0;

// Add the top horizontal sections
y1 = h1 + h2;
y2 = y1 + h;
y3 = y2 + h2;
y4 = y3 + h1;

// Set various combinations of the following to false
// for debugging (to show only portions of the model)
top = true;
bottom = true;
sides1 = true;
sides2 = true;
inside1 = true;
inside2 = true;

verify = (top && bottom && sides1 && sides2
	  && inside1 && inside2)

if (top) {
    Rectangle.addH(m3d, z1, 0.0, 0.0, w1, h1);
    Rectangle.addH(m3d, z1, 0.0, h1, w1, y1);
    Rectangle.addH(m3d, z1, 0.0, y1, w1, y2);
    Rectangle.addH(m3d, z1, 0.0, y2, w1, y3);
    Rectangle.addH(m3d, z1, 0.0, y3, w1, y4);
}

if (bottom) {
    Rectangle.addFlippedH(m3d, z0, 0.0, 0.0, w1, h1);
    Rectangle.addFlippedH(m3d, z0, 0.0, h1, w1, y1);
    Rectangle.addFlippedH(m3d, z0, 0.0, y1, w1, y2);
    Rectangle.addFlippedH(m3d, z0, 0.0, y2, w1, y3);
    Rectangle.addFlippedH(m3d, z0, 0.0, y3, w1, y4);
}

x1 = w1 + 2 * w2 + w;
x2 = x1 + w1;

if (top) {
    Rectangle.addH(m3d, z1, x1, 0.0, x2, h1);
    Rectangle.addH(m3d, z1, x1, h1, x2, y1);
    Rectangle.addH(m3d, z1, x1, y1, x2, y2);
    Rectangle.addH(m3d, z1, x1, y2, x2, y3);
    Rectangle.addH(m3d, z1, x1, y3, x2, y4);
}

if (bottom) {
    Rectangle.addFlippedH(m3d, z0, x1, 0.0, x2, h1);
    Rectangle.addFlippedH(m3d, z0, x1, h1, x2, y1);
    Rectangle.addFlippedH(m3d, z0, x1, y1, x2, y2);
    Rectangle.addFlippedH(m3d, z0, x1, y2, x2, y3);
    Rectangle.addFlippedH(m3d, z0, x1, y3, x2, y4);
}

x3 = w1 + w2;
x4 = x3 + w;
x5 = x4 + w2;
x6 = x5 + w1;

if (top) {
    Rectangle.addH(m3d, z1, w1, 0.0, x3, h1);
    Rectangle.addH(m3d, z1, x3, 0.0, x4, h1);
    Rectangle.addH(m3d, z1, x4, 0.0, x5, h1);

    Rectangle.addH(m3d, z1, w1, y3, x3, y4);
    Rectangle.addH(m3d, z1, x3, y3, x4, y4);
    Rectangle.addH(m3d, z1, x4, y3, x5, y4);

    Rectangle.addH(m3d, z2, w1, h1, x3, y1);
    Rectangle.addH(m3d, z2, w1, y1, x3, y2);
    Rectangle.addH(m3d, z2, w1, y2, x3, y3);

    Rectangle.addH(m3d, z2, x4, h1, x5, y1);
    Rectangle.addH(m3d, z2, x4, y1, x5, y2);
    Rectangle.addH(m3d, z2, x4, y2, x5, y3);

    Rectangle.addH(m3d, z2, x3, h1, x4, y1);
    Rectangle.addH(m3d, z2, x3, y2, x4, y3);
}

if (bottom) {
    Rectangle.addFlippedH(m3d, z0, w1, 0.0, x3, h1);
    Rectangle.addFlippedH(m3d, z0, x3, 0.0, x4, h1);
    Rectangle.addFlippedH(m3d, z0, x4, 0.0, x5, h1);

    Rectangle.addFlippedH(m3d, z0, w1, y3, x3, y4);
    Rectangle.addFlippedH(m3d, z0, x3, y3, x4, y4);
    Rectangle.addFlippedH(m3d, z0, x4, y3, x5, y4);

    Rectangle.addFlippedH(m3d, z0, w1, h1, x3, y1);
    Rectangle.addFlippedH(m3d, z0, w1, y1, x3, y2);
    Rectangle.addFlippedH(m3d, z0, w1, y2, x3, y3);

    Rectangle.addFlippedH(m3d, z0, x4, h1, x5, y1);
    Rectangle.addFlippedH(m3d, z0, x4, y1, x5, y2);
    Rectangle.addFlippedH(m3d, z0, x4, y2, x5, y3);

    Rectangle.addFlippedH(m3d, z0, x3, h1, x4, y1);
    Rectangle.addFlippedH(m3d, z0, x3, y2, x4, y3);
}

if (sides1) {
    Rectangle.addV(m3d, 0.0, 0.0, z0, w1, 0.0, z1);
    Rectangle.addV(m3d, w1, 0.0, z0, x3, 0.0, z1);
    Rectangle.addV(m3d, w1, h1, z1, x3, h1, z2);
    Rectangle.addV(m3d, x3, 0.0, z0, x4, 0.0, z1);
    Rectangle.addV(m3d, x3, h1, z1, x4, h1, z2);
    Rectangle.addV(m3d, x4, 0.0, z0, x5, 0.0, z1);
    Rectangle.addV(m3d, x4, h1, z1, x5, h1, z2);
    Rectangle.addV(m3d, x5, 0.0, z0, x6, 0.0, z1);

    Rectangle.addV(m3d, x6, 0.0, z0, x6, h1, z1);
    Rectangle.addV(m3d, x6, h1, z0, x6, y1, z1);
    Rectangle.addV(m3d, x5, h1, z1, x5, y1, z2);
    Rectangle.addV(m3d, x6, y1, z0, x6, y2, z1);
    Rectangle.addV(m3d, x5, y1, z1, x5, y2, z2);
    Rectangle.addV(m3d, x6, y2, z0, x6, y3, z1);
    Rectangle.addV(m3d, x5, y2, z1, x5, y3, z2);
    Rectangle.addV(m3d, x6, y3, z0, x6, y4, z1);
}

if (sides2) {
    Rectangle.addFlippedV(m3d, 0.0, y4, z0, w1, y4, z1);
    Rectangle.addFlippedV(m3d, w1, y4, z0, x3, y4, z1);
    Rectangle.addFlippedV(m3d, w1, y3, z1, x3, y3, z2);
    Rectangle.addFlippedV(m3d, x3, y4, z0, x4, y4, z1);
    Rectangle.addFlippedV(m3d, x3, y3, z1, x4, y3, z2);
    Rectangle.addFlippedV(m3d, x4, y4, z0, x5, y4, z1);
    Rectangle.addFlippedV(m3d, x4, y3, z1, x5, y3, z2);
    Rectangle.addFlippedV(m3d, x5, y4, z0, x6, y4, z1);

    Rectangle.addFlippedV(m3d, 0.0, 0.0, z0, 0.0, h1, z1);
    Rectangle.addFlippedV(m3d, 0.0, h1, z0, 0.0, y1, z1);
    Rectangle.addFlippedV(m3d, w1, h1, z1, w1, y1, z2);
    Rectangle.addFlippedV(m3d, 0.0, y1, z0, 0.0, y2, z1);
    Rectangle.addFlippedV(m3d, w1, y1, z1, w1, y2, z2);
    Rectangle.addFlippedV(m3d, 0.0, y2, z0, 0.0, y3, z1);
    Rectangle.addFlippedV(m3d, w1, y2, z1, w1, y3, z2);
    Rectangle.addFlippedV(m3d, 0.0, y3, z0, 0.0, y4, z1);
}

if (inside1) {
    Rectangle.addV(m3d, x3, y2, z0, x4, y2, z2);
    Rectangle.addV(m3d, x3, y1, z0, x3, y2, z2);
}

if (inside2) {
    Rectangle.addFlippedV(m3d, x3, y1, z0, x4, y1, z2);
    Rectangle.addFlippedV(m3d, x4, y1, z0, x4, y2, z2);
}

if (verify) {
    if (m3d.printable(java.lang.System.out)) {
	m3d.writeSTL("Lockpart STL File", stl);
    }
}
a2d = scripting.create(Animation2D.class, scripting, 700, 700, 30000.0, 1000);
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
				{"time": 2.0,
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

maxframes = a2d.estimateFrameCount(32.0);
a2d.initFrames(maxframes, "lp-", "png", da);
a2d.scheduleFrames(0, maxframes);
a2d.run();
