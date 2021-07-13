if (typeof(eout) == "undefined") {
    eout = scripting.getErrorWriter();
}
if (typeof(out) == "undefined") {
    out = scripting.getWriter();
}

// importPackage(org.bzdev.math);

// code that will fail, in order to test scrunner stacktrace option
try {
    x = org.bzdev.math.Functions.factorial(-10);
} catch (err) {
    out.println("found error");
    throw err;
}
