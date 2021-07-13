if (typeof(err) == "undefined") {
    err = scripting.getErrorWriter();
}
if (typeof(out) == "undefined") {
    out = scripting.getWriter();
}

nit = scripting.getNames().iterator();
while (nit.hasNext()) {
    out.println("next iteration: " + nit.next());
}
