scripting.importClass("org.bzdev.anim2d.Animation2D");
var out = scripting.getWriter();

a2d = new Animation2D(scripting, 1920, 1080, 10000.0, 400);
var scalefactor = 1080.0/15.0;
a2d.setRanges(0.0, 0.0, 0.35, 0.0, scalefactor, scalefactor);

alf = a2d.createFactory("org.bzdev.anim2d.AnimationLayer2DFactory");


layer = alf.createObject("layer", [
    {zorder: 0, visible: true},
    {withPrefix: "object", withIndex: [
	[{type: "TEXT", text: "Sample Text 1"},
	 {x: 10, y: 20},
	 {withPrefix: "fontParms", config: [
	     {name: "SANS_SARIF"},
	     {withPrefix: "color", 
	      red: 212, blue: 44, green: 174}
	 ]}
	],
	{type: "TEXT", config: [
	    {text: "Sample Test 2"},
	    {x: 10, y: 20},
	    {withPrefix: "fontParms", config: [
		{name: "SANS_SARIF"},
		{withPrefix: "color", 
		 red: 212, blue: 44, green: 174}
	    ]}
	]}
    ]}
]);

var iterator = a2d.getObjectsByZorder().iterator();
while (iterator.hasNext()) {
    var obj = iterator.next();
    scripting.getWriter().println(obj.getName());
}


