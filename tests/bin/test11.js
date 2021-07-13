// test importClass method for scripting

scripting.importClass("org.bzdev.anim2d", "Animation2D");
print(typeof (Animation2D));
print("\n");
a2d = new Animation2D(scripting);
print(typeof (a2d));
print("\n");

scripting.importClass("org.bzdev.graphs.Graph");
print("typeof(Graph) = " + typeof(Graph));
print("\n");
print("typeof(Graph.Axis) = " +  typeof(Graph.Axis));
print("\n");

scripting.importClass("java.util.Locale.Category");
print("typeof(Category) = " + typeof(Category));
print("\n");
print(Category.DISPLAY);
print("\n");
