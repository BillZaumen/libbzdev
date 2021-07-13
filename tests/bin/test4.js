if (typeof(err) == "undefined") {
    err = scripting.getErrorWriter();
}
if (typeof(out) == "undefined") {
    out = scripting.getWriter();
}

raf.seek(1);
out.println("second character = " + raf.read());
raf.seek(1);
ch = raf.read();
raf.seek(1);
raf.write(ch+1);
raf.seek(1);
out.println("second character = " + raf.read());

