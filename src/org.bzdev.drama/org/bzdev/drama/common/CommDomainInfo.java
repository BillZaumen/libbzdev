package org.bzdev.drama.common;

/**
 * Communication domain data used in determining connectivity.
 * This class is used as the return value of the GenericSimulation
 * methods named findCommDomain, which provides three domains:
 * a domain for a message source, a domain for a message recipient,
 * and the closest ancestor of both.  These domains are the values
 * of the methods {@link #getSourceDomain() getSourceDomain},
 * {@link #getDestDomain() getDestDomain}, and
 * {@link #getAncestorDomain() getAncestorDomain} respectively.
 */
public class CommDomainInfo <T> {
    private T sourceDomain;
    private T ancestorDomain;
    private T destDomain;
	
    /**
     * Constructor.
     * All domains must be equal in the case in which only
     * one domain participates.
     *
     * @param sd the domain used by the message source
     * @param pd the ancestor domain
     * @param dd the destination or next-hop domain
     */
    public CommDomainInfo(T sd, T pd, T dd) {
	sourceDomain = sd;
	ancestorDomain = pd;
	destDomain = dd;
    }

    /**
     * Get the source domain.
     * @return the source domain
     */
    public T getSourceDomain() {return sourceDomain;}

    /**
     * Get the ancestor domain.
     * @return the ancestor domain
     */
    public T getAncestorDomain() {return ancestorDomain;}

    /**
     * Get the destination or next hop domain.
     * @return the destination or next-hop domain
     */
    public T getDestDomain() {return destDomain;}
}

//  LocalWords:  GenericSimulation findCommDomain getSourceDomain sd
//  LocalWords:  getDestDomain getAncestorDomain
