import javax.script.*;

import org.bzdev.devqsim.*;

public class QScript {
    static public void main(String argv[]) {
	try {
	    ScriptEngineManager manager = new ScriptEngineManager();
	    final ScriptEngine engine = manager.getEngineByName("ECMAScript");
	    if (engine == null) {
		System.out.println("error - could not find engine");
	    }
	    final Bindings defaultBindings = 
		engine.getBindings(ScriptContext.ENGINE_SCOPE);
	    final ScriptEngine ourEngine = engine;
	    Simulation sim = new Simulation() {
		    protected ScriptEngine doGetScriptEngine() {
			return engine;
		    }
		    protected Bindings doGetDefaultBindings() {
			return defaultBindings;
		    }
		    protected String doGetScriptLanguage() {
			return "ECMAScript";
		    }
		};


	    engine.put("sim", sim);

	    FifoTaskQueue q = new FifoTaskQueue(sim, "q", true);
	    engine.put("q", q);

	    System.out.println("--------- case 1 ------------");
	    engine.eval("obj1 = {call: function(){java.lang.System.out.println(\"hello\");}}");
	    engine.eval("obj2 = {call: function(){java.lang.System.out.println(\"goodbye\");}}");

	    engine.eval("q.addCallObject(obj1, 10);");
	    q.addCallScript("java.lang.System.out.println(\"hello\");", 10);
	    engine.eval("q.addCallObject(obj2, 20);");
	    q.addCallScript("java.lang.System.out.println(\"goodbye\");", 20);

	    sim.run();
	    System.out.println("current time = " +sim.currentTicks());

	    System.out.println("--------- case 2 ------------");
	    String task = "java.lang.System.out.println(\"hello\"); \n q.addCurrentTask(20);\n"
		+"java.lang.System.out.println(\"goodbye\");";
	    q.addTaskScript(task, 10);
	    q.addTaskScript(task, 20);

	    String run = "r = {run: function(){java.lang.System.out.println(\"hello\");\n"
		+"q.addCurrentTask(20);\n java.lang.System.out.println(\"goodbye\");}}";
	    engine.eval(run);
	    String run2 = "r2 = {run: function(){java.lang.System.out.println(\"hello\");\n"
		+"q.addCurrentTaskObject(20);\n java.lang.System.out.println(\"goodbye\");}}";
	    engine.eval(run2);
	    engine.eval("q.addTaskObject(r, 10);");
	    engine.eval("q.addTaskObject(r2, 10);");
	    System.out.println("ready to run simulation for case 2");
	    sim.run();
	    System.out.println("current time = " +sim.currentTicks());

	    System.out.println("--------- case 3 ------------");
	    final QueueServer qs1 = new QueueServer() {
		public long getInterval() {return 10;}
		};
	    final QueueServer qs2 = new QueueServer() {
		    public long getInterval() {return 15;}
		};
	    FifoServerQueue<QueueServer> sq = new 
		FifoServerQueue<QueueServer>(sim, "fifoSQ", true,
					     qs1, qs2);
	    engine.put("sq", sq);

	    String sqcallString = "sqcall = {interactWith: function(server) {\n"
		+"  java.lang.System.out.println(\"interval = \" +server.getInterval()"
		+"+ \", current time = \" + sim.currentTicks());\n"
		+"}, \n"
		+ "call: function() { java.lang.System.out.println(\"ok (call)\");}};";

	    engine.eval(sqcallString);
	    engine.eval("sq.addCallObject(sqcall, 10);");
	    engine.eval("sq.addCallObject(sqcall, 10);");
	    engine.eval("sq.addCallObject(sqcall, 10);");
	    engine.eval("sq.addCallObject(sqcall, 10);");


	    String sqrunString = "sqrun = {interactWith: function(server) {\n"
		+"  java.lang.System.out.println(\"interval = \" +server.getInterval()"
		+"+ \", current time = \" + sim.currentTicks())\n"
		+"}, \n"
		+ "run: function() { java.lang.System.out.println(\"ok (run)\");}};";

	    engine.eval(sqrunString);
	    engine.eval("sq.addTaskObject(sqrun, 10);");
	    engine.eval("sq.addTaskObject(sqrun, 10);");
	    engine.eval("sq.addTaskObject(sqrun, 10);");
	    engine.eval("sq.addTaskObject(sqrun, 10);");
	    sim.run();

	    System.out.println("current time = " +sim.currentTicks());

	    System.out.println("--------- case 4 ------------");
	    engine.eval("sq.addTaskObject(sqrun, 10);");
	    engine.eval("sq.addTaskObject(sqrun, 10);");
	    
	    String taskscript = "sq.addCurrentTaskScriptObject({\n"
		+"interactWith: function(server) {java.lang.System.out.println(\"interval = \""
		+" + server.getInterval());}\n"
		+"}, 10);\n";
	    sim.scheduleTaskScript(taskscript, 0);
	    sim.scheduleTaskScript(taskscript, 0);
	    sim.run();
	    System.out.println("current time = " +sim.currentTicks());

	    System.out.println("--------- case 5 ------------");
	    System.out.println("test task-queue deletion - Policy WHEN_EMPTY");
	    engine.eval("q.addCallObject(obj1, 10);");
	    q.addCallScript("java.lang.System.out.println(\"hello\");", 10);
	    engine.eval("q.addCallObject(obj2, 20);");
	    q.addCallScript("java.lang.System.out.println(\"goodbye\");", 20);
	    q.delete();
	    sim.scheduleTaskScript
		("if (q.addCallObject(obj2, 20) != null)" 
		 +" java.lang.System.out.println(\"delete failed\");",
		 0);
	    sim.run();
	    System.out.println("current time = " +sim.currentTicks());

	    System.out.println("--------- case 6 ------------");
	    System.out.println("test task-queue deletion - "
			       +"Policy MUST_BE_EMPTY");
	    q = new FifoTaskQueue(sim, "q", true);
	    engine.put("q", q);
	    q.setDeletePolicy(QueueDeletePolicy.MUST_BE_EMPTY);
	    engine.eval("q.addCallObject(obj1, 10);");
	    q.addCallScript("java.lang.System.out.println(\"hello\");", 10);
	    engine.eval("q.addCallObject(obj2, 20);");
	    q.addCallScript("java.lang.System.out.println(\"goodbye\");", 20);
	    if (q.delete()) System.out.println("illegal deletion (size != 0)");
	    sim.run();
	    if (!q.delete()) System.out.println("deletion failed (size = 0)");
	    System.out.println("current time = " +sim.currentTicks());

	    System.out.println("--------- case 7 ------------");
	    System.out.println("test server-queue deletion - "
			       +"Policy WHEN_EMPTY");
	    engine.eval("sq.addTaskObject(sqrun, 10);");
	    engine.eval("sq.addTaskObject(sqrun, 10);");
	    engine.eval("sq.addTaskObject(sqrun, 10);");
	    engine.eval("sq.addTaskObject(sqrun, 10);");
	    sq.delete();
	    sim.run();
	    engine.eval("if(sq.addTaskObject(sqrun, 10) != null)" 
			+" java.lang.System.out.println(\"delete failed\");");
	    System.out.println("current time = " +sim.currentTicks());

	    System.out.println("--------- case 8 ------------");
	    System.out.println("test server-queue deletion - "
			       +"Policy MUST_BE_EMPTY");
	    sq = new FifoServerQueue<QueueServer>(sim, "fifoSQ", true,
						  qs1, qs2);
	    sq.setDeletePolicy(QueueDeletePolicy.MUST_BE_EMPTY);
	    engine.put("sq", sq);
	    engine.eval("sq.addTaskObject(sqrun, 10);");
	    engine.eval("sq.addTaskObject(sqrun, 10);");
	    engine.eval("sq.addTaskObject(sqrun, 10);");
	    engine.eval("sq.addTaskObject(sqrun, 10);");
	    if (sq.delete()) System.out.println("illegal deletion (size != 0)");
	    sim.run();
	    if (!sq.delete()) System.out.println("deletion failed (size = 0)");
	    System.out.println("current time = " +sim.currentTicks());

	    System.out.println("--------- case 9 ------------");
	    System.out.println("test task-queue deletion - Policy WHEN_EMPTY");
	    System.out.println("deletion will be at while last scheduled event"
			       +" is running.");
	    q = new FifoTaskQueue(sim, "q", true);
	    engine.put("q", q);
	    engine.eval("q.addCallObject(obj1, 10);");
	    q.addCallScript("java.lang.System.out.println(\"hello\");", 10);
	    engine.eval("q.addCallObject(obj2, 20);");
	    q.addCallScript("java.lang.System.out.println(\"goodbye\");", 20);
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
	    engine.put("sq", sq);
	    engine.eval("sq.addTaskObject(sqrun, 10);");
	    engine.eval("sq.addTaskObject(sqrun, 10);");
	    engine.eval("sq.addTaskObject(sqrun, 10);");
	    engine.eval("sq.addTaskObject(sqrun, 10);");
	    sim.run(40);
	    sq.delete();
	    System.out.println("current time = " +sim.currentTicks()
			       +", sq.isDeleted() = " + sq.isDeleted());
	    sim.run();
	    System.out.println("current time = " +sim.currentTicks()
			       +", sq.isDeleted() = " + sq.isDeleted());
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}

