print(typeof(scripting));
print(scripting);
if (typeof(eout) == "undefined") {
    eout = scripting.getErrorWriter();
}
if (typeof(out) == "undefined") {
    out = scripting.getWriter();
}


out.println("hello world");

try {
    fis = new java.io.FileOutputStream("junk");
} catch (err) {
    eout.println(err);
    eout.println("Error for 'junk' not expected");
}
