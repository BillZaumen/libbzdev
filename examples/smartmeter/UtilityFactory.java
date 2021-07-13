import org.bzdev.drama.*;
import org.bzdev.obnaming.annotations.*;
import org.bzdev.math.rv.*;

/**
 * Factory for instances of Utility.
 * <P>
 * Parameters:
 * <ul>
 *   <li> <code>domainMember</code>. Used to configure an instance of
 *        DomainMember for handle domain membership. 
 *   <li> <code>domain</code>. Used to specify a domain that can added
 *        or removed from the actor's domain set. For this parameter,
 *        the key is the domain and the value is a boolean indicating
 *        that is true if conditions for that domain should be
 *        tracked; false otherwise. If a specified domain was already
 *        joined by a shared domain member, an explicit request to
 *        join that domain will be ignored when an actor is created.
 * </ul>
 */
@FactoryParmManager("UtilityFactoryParmManager")
public class UtilityFactory extends AbstractActorFactory<Utility> {
    
    /**
     * Constructor for service provider.
     */
    public UtilityFactory() {
	this(null);
    }

    /**
     * Constructor.
     * @param sim the simulation
     */
    public UtilityFactory(DramaSimulation sim) {
	super(sim);
    }
    @Override
    protected Utility newObject(String name) {
	return new Utility(getSimulation(), name, willIntern());
    }
}
