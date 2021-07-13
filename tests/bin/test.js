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
    out.println("should have seen an error for trustLevel < 2 and no -t");
} catch (err) {
    eout.println(err);
    eout.println("Error for 'junk' expected");
}
