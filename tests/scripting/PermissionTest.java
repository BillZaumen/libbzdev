import org.bzdev.scripting.*;

public class PermissionTest {

    public static void main(String argv[]) throws Exception {
	ScriptingContextPermission granted =
	    new ScriptingContextPermission("org.bzdev.devqsim.Simulation");

	ScriptingContextPermission trusted  =
	    new ScriptingContextPermission("org.bzdev.devqsim.Simulation",
					   "trusted");
	System.out.println("trusted.getActions() = " + trusted.getActions());

	ScriptingContextPermission untrusted  =
	    new ScriptingContextPermission("org.bzdev.devqsim.Simulation");

	ScriptingContextPermission swapbindings =
	    new ScriptingContextPermission("org.bzdev.devqsim.Simulation",
					   "swapbindings");

	ScriptingContextPermission privileged =
	    new ScriptingContextPermission("org.bzdev.devqsim.Simulation",
					   "privileged");

	System.out.println("untrusted.getActions() = "
			   + untrusted.getActions());

	System.out.println("granted & trusted: "
			   + granted.implies(trusted));

	System.out.println("granted & untrusted: "
			   + granted.implies(untrusted));

	System.out.println("granted & swapbindings: "
			   + granted.implies(swapbindings));

	ScriptingContextPermission tgranted =
	    new ScriptingContextPermission("org.bzdev.devqsim.Simulation",
					   "trusted");

	System.out.println("tgranted & trusted: "
			   + tgranted.implies(trusted));

	System.out.println("tgranted & untrusted: "
			   + tgranted.implies(untrusted));

	System.out.println("tgranted & swapbindings: "
			   + tgranted.implies(swapbindings));

	ScriptingContextPermission sgranted =
	    new ScriptingContextPermission("org.bzdev.devqsim.Simulation",
					   "swapbindings");
	ScriptingContextPermission stgranted =
	    new ScriptingContextPermission("org.bzdev.devqsim.Simulation",
					   "swapbindings,trusted");

	System.out.println("sgranted & trusted: "
			   + sgranted.implies(trusted));
	System.out.println("sgranted & untrusted: "
			   + sgranted.implies(untrusted));
	System.out.println("sgranted & swapbindings: "
			   + sgranted.implies(swapbindings));
	
	System.out.println("stgranted & trusted: "
			   + stgranted.implies(trusted));
	System.out.println("stgranted & untrusted: "
			   + stgranted.implies(untrusted));
	System.out.println("stgranted & swapbindings: "
			   + stgranted.implies(swapbindings));


	ScriptingContextPermission pgranted =
	    new ScriptingContextPermission("org.bzdev.devqsim.Simulation",
					   "privileged");
	ScriptingContextPermission spgranted =
	    new ScriptingContextPermission("org.bzdev.devqsim.Simulation",
					   "swapbindings,privileged");

	ScriptingContextPermission tpgranted =
	    new ScriptingContextPermission("org.bzdev.devqsim.Simulation",
					   "trusted,privileged");

	ScriptingContextPermission stpgranted =
	    new ScriptingContextPermission("org.bzdev.devqsim.Simulation",
					   "trusted,swapbindings,privileged");
	System.out.println("granted & privileged: "
			   +granted.implies(privileged));

	System.out.println("tgranted & privileged: "
			   +tgranted.implies(privileged));

	System.out.println("sgranted & privileged: "
			   +sgranted.implies(privileged));


	System.out.println("stgranted & privileged: "
			   +stgranted.implies(privileged));

	System.out.println("pgranted & privileged: "
			   +pgranted.implies(privileged));

	System.out.println("tpgranted & privileged: "
			   +tpgranted.implies(privileged));

	System.out.println("spgranted & privileged: "
			   +spgranted.implies(privileged));

	System.out.println("stpgranted & privileged: "
			   +stpgranted.implies(privileged));

	System.out.println("pgranted & trusted: "
			   +pgranted.implies(trusted));

	System.out.println("tpgranted & trusted: "
			   +tpgranted.implies(trusted));

	System.out.println("spgranted & trusted: "
			   +spgranted.implies(trusted));

	System.out.println("stpgranted & trusted: "
			   +stpgranted.implies(trusted));

	System.out.println("stgranted & untrusted: "
			   +stgranted.implies(untrusted));

	System.out.println("pgranted & untrusted: "
			   +pgranted.implies(untrusted));

	System.out.println("tpgranted & untrusted: "
			   +tpgranted.implies(untrusted));

	System.out.println("spgranted & untrusted: "
			   +spgranted.implies(untrusted));

	System.out.println("stpgranted & untrusted: "
			   +stpgranted.implies(privileged));

	System.out.println("pgranted & swapbindings: "
			   +pgranted.implies(swapbindings));

	System.out.println("tpgranted & swapbindings: "
			   +tpgranted.implies(swapbindings));

	System.out.println("spgranted & swapbindings: "
			   +spgranted.implies(swapbindings));

	System.out.println("stpgranted & swapbindings: "
			   +stpgranted.implies(privileged));


	System.exit(0);
    }
}
