// test createFactory method (3 arguments) for scripting

scripting.importClasses("org.bzdev.anim2d", "Animation2D");

a2d = new org.bzdev.anim2d.Animation2D(scripting);
print ("created a2d");
print("\n");
a2d.createFactories("org.bzdev.anim2d", {
    alf: "AnimationLayer2DFactory",
    apf: "AnimationPath2DFactory"
});
print("list factories created") ; print("\n");
print(alf);
print("\n");
print(apf)
print("\n");


