if (typeof(err) == "undefined") {
    err = scripting.getErrorWriter();
}
if (typeof(out) == "undefined") {
    out = scripting.getWriter();
}

scripting.importClass("org.bzdev.util.SciFormatter");
scripting.importClass("org.bzdev.util.VarArgsFormatter");
scripting.importClass("java.util.Locale");

formatter = new SciFormatter();
out.println(formatter.format("hello"));

formatter = new SciFormatter();
out.println(formatter.format("%.2f", 10.0));

formatter = new SciFormatter();
out.println(formatter.format("%.2f %.2f", 10.0, 20.0));

out.println("try an array of two objects, both numbers");
array = scripting.createArray("java.lang.Object", 2);
array[0] = 10.0; array[1] = 20.0;
out.println(array.class);
out.println("array length = " +array.length);
out.println(array[0].class);
out.println(array[0].class);

formatter = new SciFormatter();
out.println(formatter.format("%.2f %.2f", array));

out.println("change locale");
locale = Locale.FRANCE;

formatter = new SciFormatter();
out.println(formatter.format(locale, "%.2f", 10.0));

formatter = new SciFormatter();
out.println(formatter.format(locale, "%.2f %.2f", 10.0, 20.0));

array = scripting.createArray("java.lang.Object", 2);
array[0] = 10.0; array[1] = 20.0;
formatter = new SciFormatter();
out.println(formatter.format(locale, "%.2f %.2f", array));

out.println("-------- now try VarArgsFormatter ---------");

formatter = new VarArgsFormatter();
out.println(formatter.format("hello"));

formatter = new VarArgsFormatter();
out.println(formatter.format("%.2f", 10.0));

formatter = new VarArgsFormatter();
out.println(formatter.format("%.2f %.2f", 10.0, 20.0));

array = scripting.createArray("java.lang.Object", 2);
array[0] = 10.0; array[1] = 20.0;
formatter = new VarArgsFormatter();
out.println(formatter.format("%.2f %.2f", array));

locale = Locale.FRANCE;

formatter = new VarArgsFormatter();
out.println(formatter.format(locale, "%.2f", 10.0));

formatter = new VarArgsFormatter();
out.println(formatter.format(locale, "%.2f %.2f", 10.0, 20.0));

array = scripting.createArray("java.lang.Object", 2);
array[0] = 10.0; array[1] = 20.0;
formatter = new VarArgsFormatter();
out.println(formatter.format(locale, "%.2f %.2f", array));
