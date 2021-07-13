if (typeof(err) == "undefined") {
    err = scripting.getErrorWriter();
}
if (typeof(out) == "undefined") {
    out = scripting.getWriter();
}

fa = da.createFileAccessor("foo.txt", "rw");

os = fa.getOutputStream();
os.write(101);
os.close();
is = fa.getInputStream();
val = is.read();
out.println("wrote 101, read " + val);
