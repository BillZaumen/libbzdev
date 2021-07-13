if (typeof(err) == "undefined") {
    err = scripting.getErrorWriter();
}
if (typeof(out) == "undefined") {
    out = scripting.getWriter();
}

input = new java.io.InputStreamReader(is, "UTF-8");
reader = new java.io.LineNumberReader(input);

out.println(reader.readLine());

