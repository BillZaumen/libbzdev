listener = new Packages.ScriptingTest4$Adapter
                     (scripting, {method1: function(str, val) {
				      file = new
				      java.io.FileOutputStream("test5.out");
				      file.close();
				 }});

tl = new java.lang.String("test string");
