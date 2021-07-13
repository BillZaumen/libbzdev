if (typeof(err) == "undefined") {
    err = scripting.getErrorWriter();
}
if (typeof(output) == "undefined") {
    output = scripting.getWriter();
}

output.println(scripting);
output.println(typeof (scripting));

scripting.importClass("org.bzdev.devqsim.Simulation");
scripting.importClass("org.bzdev.devqsim.SimFunctionFactory");
scripting.importClass("org.bzdev.devqsim.SimFunctionTwoFactory");

sim = new Simulation(scripting);

sff = sim.createFactory("org.bzdev.devqsim.SimFunctionFactory");

function f(x) {return Math.sin(x);}
function fp(x) {return Math.cos(x);}

sff.set("fName", "f");
sff.set("fpName", "fp");

sf1a = sff.createObject("fs1");

output.println("sf1a.valueAt(0) = " + sf1a.valueAt(0));
output.println("sf1a.derivAt(0) = " + sf1a.derivAt(0));

sff.clear();

sff.set("object", {valueAt: function(x) {return Math.sin(x)},
		   derivAt: function (x) {return Math.cos(x)}});

sf1b = sff.createObject("fs2");

output.println("sf1b.valueAt(0) = " + sf1b.valueAt(0));
output.println("sf1b.derivAt(0) = " + sf1b.derivAt(0));

sff.clear();

sff.set("object", "({valueAt: function(x) {return Math.sin(x)},"
	+ "derivAt: function (x) {return Math.cos(x)}})");

sf1c = sff.createObject("sf1c");
output.println("sf1c.valueAt(0) = " + sf1c.valueAt(0));
output.println("sf1c.derivAt(0) = " + sf1c.derivAt(0));

sf2f = sim.createFactory(org.bzdev.devqsim.SimFunctionTwoFactory.class);

function f2(x,y) {return Math.sin(x)*Math.cos(y);}
function f2d1(x,y) {return Math.cos(x)*Math.cos(y);}
function f2d2(x,y) {return -Math.sin(x)*Math.sin(y);}

function f2d11(x,y){return -Math.sin(x)*Math.cos(y);}
function f2d12(x,y) {return -Math.cos(x)*Math.sin(y)};
function f2d21(x,y) {return -Math.cos(x)*Math.sin(y)};
function f2d22(x,y){return -Math.sin(x)*Math.cos(y);}



sf2f.set("fName", "f2");
sf2f.set("f1Name", "f2d1");
sf2f.set("f2Name", "f2d2");
sf2f.set("f11Name", "f2d11");
sf2f.set("f12Name", "f2d12");
sf2f.set("f21Name", "f2d21");
sf2f.set("f22Name", "f2d22");

sf2a = sf2f.createObject("sf2a");

var xx = Math.PI/4.0;
var yy = xx;
output.println("x = "  + xx);
output.println("y = "  + yy);

output.println("sf2a.valueAt(x,y) = " + sf2a.valueAt(xx, yy));
output.println("sf2a.deriv1At(x,y) = " + sf2a.deriv1At(xx, yy));
output.println("sf2a.deriv2At(x,y) = " + sf2a.deriv2At(xx, yy));
output.println("sf2a.deriv11At(x,y) = " + sf2a.deriv11At(xx, yy));
output.println("sf2a.deriv12At(x,y) = " + sf2a.deriv12At(xx, yy));
output.println("sf2a.deriv21At(x,y) = " + sf2a.deriv21At(xx, yy));
output.println("sf2a.deriv22At(x,y) = " + sf2a.deriv22At(xx, yy));


sf2f.clear();
sf2f.set("object", {valueAt: function(x, y) {return f2(x, y)},
		    deriv1At: function(x, y){return f2d1(x, y)},
		    deriv2At: function(x, y){return f2d2(x, y)},
		    deriv11At: function(x, y){return f2d11(x, y)},
		    deriv12At: function(x, y){return f2d12(x, y)},
		    deriv21At: function(x, y){return f2d21(x, y)},
		    deriv22At: function(x, y){return f2d22(x, y)}});

sf2b = sf2f.createObject("sf2b");

output.println("sf2b.valueAt(x,y) = " + sf2b.valueAt(xx, yy));
output.println("sf2b.deriv1At(x,y) = " + sf2b.deriv1At(xx, yy));
output.println("sf2b.deriv2At(x,y) = " + sf2b.deriv2At(xx, yy));
output.println("sf2b.deriv11At(x,y) = " + sf2b.deriv11At(xx, yy));
output.println("sf2b.deriv12At(x,y) = " + sf2b.deriv12At(xx, yy));
output.println("sf2b.deriv21At(x,y) = " + sf2b.deriv21At(xx, yy));
output.println("sf2b.deriv22At(x,y) = " + sf2b.deriv22At(xx, yy));

sf2f.clear();
sf2f.set("object", "({valueAt: function(x, y) {return f2(x, y)},"
		   + "deriv1At: function(x, y){return f2d1(x, y)},"
		   + "deriv2At: function(x, y){return f2d2(x, y)},"
		   + "deriv11At: function(x, y){return f2d11(x, y)},"
		   + "deriv12At: function(x, y){return f2d12(x, y)},"
		   + "deriv21At: function(x, y){return f2d21(x, y)},"
		   + "deriv22At: function(x, y){return f2d22(x, y)}})")
sf2c = sf2f.createObject("sf2c");

output.println("sf2c.valueAt(x,y) = " + sf2c.valueAt(xx, yy));
output.println("sf2c.deriv1At(x,y) = " + sf2c.deriv1At(xx, yy));
output.println("sf2c.deriv2At(x,y) = " + sf2c.deriv2At(xx, yy));
output.println("sf2c.deriv11At(x,y) = " + sf2c.deriv11At(xx, yy));
output.println("sf2c.deriv12At(x,y) = " + sf2c.deriv12At(xx, yy));
output.println("sf2c.deriv21At(x,y) = " + sf2c.deriv21At(xx, yy));
output.println("sf2c.deriv22At(x,y) = " + sf2c.deriv22At(xx, yy));
