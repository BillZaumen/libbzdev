// test importClass method for scripting

scripting.importClasses("org.bzdev.anim2d", "Animation2D");

print(typeof (Animation2D));
print("\n");

scripting.importClasses("org.bzdev.graphs", ["Graph"]);

print("typeof(Graph) = " + typeof(Graph));
print("\n");
print("typeof(Graph.Axis) = " +  typeof(Graph.Axis));
print("\n");
