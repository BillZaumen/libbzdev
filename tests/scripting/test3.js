if (typeof(err) == "undefined") {
    err = java.lang.System.err;
}
if (typeof(out) == "undefined") {
    out = java.lang.System.out;
}

try {
    sim = new org.bzdev.devqsim.Simulation(root);
    out.println("added simulation");
} catch (e) {
    out.println("could not add simulation");
}

try {
    dsim = new org.bzdev.drama.DramaSimulation(root);
    out.println("added drama simulation");
} catch (e) {
    out.println("could not add drama simulation");
}
