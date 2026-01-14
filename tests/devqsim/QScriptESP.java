import javax.script.*;
import org.bzdev.devqsim.*;
import org.bzdev.scripting.*;

public class QScriptESP {
    static public void main(String argv[]) throws Exception {
        DefaultScriptingContext dsc = new
            DefaultScriptingContext("ESP");
        ExtendedScriptingContext sc = new ExtendedScriptingContext(dsc);
	sc.putScriptObject("scripting", sc);
	Simulation sim = new Simulation(sc);
	sc.putScriptObject("sim", sim);
	sc.evalScript("import (\"org.bzdev.devqsim.Simulation\");"
		      + "import (\"org.bzdev.devqsim.FifoTaskQueue\");"
		      + "import (\"org.bzdev.devqsim.QueueServer\");"
		      + "import (\"org.bzdev.devqsim.FifoServerQueue\");"
		      + "import (\"org.bzdev.devqsim.LinearServerQueue\");"
		      + "import (\"org.bzdev.lang.Callable\");"
		      + "import (\"java.lang.Runnable\");");

	sc.evalScript("var out = global.getWriter();");
	
	FifoTaskQueue q = new FifoTaskQueue(sim, "q", true);
	sc.putScriptObject("q", q);
	System.out.println("--------- case 1 ------------");
	
	sc.evalScript("var obj1 = {call: function() {global.getWriter().println(\"hello\");}};");
	sc.evalScript("var obj2 = {call: function() {global.getWriter().println(\"goodbye\");}};");

	sc.evalScript("q.addCallObject(obj1, 10L);");
	q.addCallScript("global.getWriter().println(\"hello\");", 10L);
	sc.evalScript("q.addCallObject(obj2, 20L);");
	q.addCallScript("global.getWriter().println(\"hello\");", 20L);

	sim.run();
	System.out.println("current time = " + sim.currentTicks());

	System.out.println("--------- case 2 ------------");

	String task1 = "global.getWriter().println(\"hello1\");\n"
	    + "q.addCurrentTask(20);\n"
	    + "global.getWriter().println(\"in hello/goodbye 1\");\n"
	    + "q.addCurrentTask(20);\n"
	    +"global.getWriter().println(\"goodbye1\");";

	q.addTaskScript(task1, 10);

	String task2 = "global.getWriter().println(\"hello2\");\n"
	    + "q.addCurrentTask(20);\n"
	    + "global.getWriter().println(\"in hello/goodbye 2\");\n"
	    + "q.addCurrentTask(20);\n"
	    + "global.getWriter().println(\"goodbye2\");";

	q.addTaskScript(task2, 10);
	sim.run();
	// sc.evalScript("sim.run();");
	System.out.println("current time = " +sim.currentTicks());

	    final QueueServer qs1 = new QueueServer() {
		public long getInterval() {return 10;}
		};
	    final QueueServer qs2 = new QueueServer() {
		    public long getInterval() {return 15;}
		};
	    FifoServerQueue<QueueServer> sq = new 
		FifoServerQueue<QueueServer>(sim, "fifoSQ", true,
					     qs1, qs2);
	    sc.putScriptObject("sq", sq);
	    
 	    String sqcallString = "var sqcall = {interactWith: function(server) {\n"
		+"  global.getWriter().println(\"interval = \" +server.getInterval()"
		+"+ \", current time = \" + sim.currentTicks());\n"
		+"}, \n"
		+ "call: function() { global.getWriter().println(\"ok (call)\");}};";

	    sc.evalScript(sqcallString);
	    sc.evalScript("sq.addCallObject(sqcall, 10L);");
	    sc.evalScript("sq.addCallObject(sqcall, 10L);");
	    sc.evalScript("sq.addCallObject(sqcall, 10L);");
	    sc.evalScript("sq.addCallObject(sqcall, 10L);");

	    String sqrunString = "var sqrun = {interactWith: function(server) {\n"
		+"  global.getWriter().println(\"interval = \" +server.getInterval()"
		+"+ \", current time = \" + sim.currentTicks())\n"
		+"}, \n"
		+ "run: function() {global.getWriter().println(\"ok (run)\");}};";

	    sc.evalScript(sqrunString);
	    sc.evalScript("sq.addTaskObject(sqrun, 10L);");
	    sc.evalScript("sq.addTaskObject(sqrun, 10L);");
	    sc.evalScript("sq.addTaskObject(sqrun, 10L);");
	    sc.evalScript ("sq.addTaskObject(sqrun, 10L);");
	    sim.run();
	    System.out.println("current time = " +sim.currentTicks());

	    System.out.println("--------- case 4 ------------");
	    sc.evalScript("sq.addTaskObject(sqrun, 10L);");
	    sc.evalScript("sq.addTaskObject(sqrun, 10L);");
	    
	    String taskscript = "sq.addCurrentTaskScriptObject({\n"
		+"interactWith: function(server) {global.getWriter().println(\"interval = \""
		+" + server.getInterval());}\n"
		+"}, 10L);\n";
	    sim.scheduleTaskScript(taskscript, 0L);
	    sim.scheduleTaskScript(taskscript, 0L);
	    sim.run();
	    System.out.println("current time = " + sim.currentTicks());

	    System.out.println("--------- case 5 ------------");
	    System.out.println("test task-queue deletion - Policy WHEN_EMPTY");
	    sc.evalScript("q.addCallObject(obj1, 10L);");
	    q.addCallScript("global.getWriter().println(\"hello\");", 10);
	    sc.evalScript("q.addCallObject(obj2, 20L);");
	    q.addCallScript("global.getWriter().println(\"goodbye\");", 20);
	    q.delete();
	    sim.scheduleTaskScript
		("(q.addCallObject(obj2, 20L) != null) &&" 
		 +" `{global.getWriter().println(\"delete failed\");};",
		 0);
	    sim.run();
	    System.out.println("current time = " +sim.currentTicks());

	    System.out.println("--------- case 6 ------------");
	    System.out.println("test task-queue deletion - "
			       +"Policy MUST_BE_EMPTY");
	    q = new FifoTaskQueue(sim, "q", true);
	    sc.putScriptObject("q", q);
	    q.setDeletePolicy(QueueDeletePolicy.MUST_BE_EMPTY);
	    sc.evalScript("q.addCallObject(obj1, 10L);");
	    q.addCallScript("global.getWriter().println(\"hello\");", 10);
	    sc.evalScript("q.addCallObject(obj2, 20L);");
	    q.addCallScript("global.getWriter().println(\"goodbye\");", 20);
	    if (q.delete()) System.out.println("illegal deletion (size != 0)");
	    sim.run();
	    if (!q.delete()) System.out.println("deletion failed (size = 0)");
	    System.out.println("current time = " +sim.currentTicks());
	    
	    System.out.println("--------- case 7 ------------");
	    System.out.println("test server-queue deletion - "
			       +"Policy WHEN_EMPTY");
	    sc.evalScript("sq.addTaskObject(sqrun, 10L);");
	    sc.evalScript("sq.addTaskObject(sqrun, 10L);");
	    sc.evalScript("sq.addTaskObject(sqrun, 10L);");
	    sc.evalScript("sq.addTaskObject(sqrun, 10L);");
	    sq.delete();
	    sim.run();
	    sc.evalScript("(sq.addTaskObject(sqrun, 10L) != null) && " 
			  +"`{global.getWriter().println(\"delete failed\");};");
	    System.out.println("current time = " +sim.currentTicks());

	    System.out.println("--------- case 9 ------------");
	    System.out.println("test task-queue deletion - Policy WHEN_EMPTY");
	    System.out.println("deletion will be at while last scheduled event"
			       +" is running.");
	    q = new FifoTaskQueue(sim, "q", true);
	    sc.putScriptObject("q", q);
	    sc.evalScript("q.addCallObject(obj1, 10L);");
	    q.addCallScript("global.getWriter().println(\"hello\");", 10);
	    sc.evalScript("q.addCallObject(obj2, 20L);");
	    q.addCallScript("global.getWriter().println(\"goodbye\");", 20);
	    sim.run(50);
	    q.delete();
	    System.out.println("current time = " +sim.currentTicks()
			       +", q.isDeleted() = " + q.isDeleted());
	    sim.run();
	    System.out.println("current time = " +sim.currentTicks()
			       +", q.isDeleted() = " + q.isDeleted());
	    System.out.println("--------- case 10 -----------");
	    System.out.println("test server-queue deletion - "
			       +"Policy WHEN_EMPTY");
	    sq = new FifoServerQueue<QueueServer>(sim, "fifoSQ", true,
						  qs1, qs2);
	    System.out.println("deletion will be at while last scheduled event"
			       +" is running.");
	    sc.putScriptObject("sq", sq);
	    sc.evalScript("sq.addTaskObject(sqrun, 10L);");
	    sc.evalScript("sq.addTaskObject(sqrun, 10L);");
	    sc.evalScript("sq.addTaskObject(sqrun, 10L);");
	    sc.evalScript("sq.addTaskObject(sqrun, 10L);");
	    sim.run(40);
	    sq.delete();
	    System.out.println("current time = " +sim.currentTicks()
			       +", sq.isDeleted() = " + sq.isDeleted());
	    sim.run();
	    System.out.println("current time = " +sim.currentTicks()
			       +", sq.isDeleted() = " + sq.isDeleted());


   }
}
