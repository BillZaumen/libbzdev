// test to see if --codebase .../lsnof.jar loaded that
// file.  We just pick a class in it and print the
// location of its code source.

if (typeof(err) == "undefined") {
    err = scripting.getErrorWriter();
}
if (typeof(out) == "undefined") {
    out = scripting.getWriter();
}

sc = new org.bzdev.devqsim.Simulation(scripting).getClass();
out.println(sc.getProtectionDomain().getCodeSource().getLocation().toString());


fp = (new org.bzdev.bin.lsnof.FactoryPrinter()).getClass();

out.println(fp.getProtectionDomain().getCodeSource().getLocation().toString());
