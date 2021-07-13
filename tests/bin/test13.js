// test createFactory method (3 arguments) for scripting

scripting.importClasses("org.bzdev.anim2d", "Animation2D");

a2d = scripting.create("org.bzdev.anim2d.Animation2D", scripting);
pkg = "org.bzdev.anim2d";
factory2 = a2d.createFactory("org.bzdev.anim2d.AnimationLayer2DFactory");

a2d.createFactory("factory", pkg, "AnimationLayer2DFactory");


print(factory);
print("\n");
print(factory2);
print("\n");

spec = [["alf", "AnimationLayer2DFactory"],
	["apf", "AnimationPath2DFactory"]]
for each (var entry in spec) {
    a2d.createFactory(entry[0], pkg, entry[1]);
}
