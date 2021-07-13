package org.bzdev.graphs.spi;
import org.bzdev.graphs.Graph;

/**
 * Service provider interface for Graph.SymbolFactory.
 * The fully qualified names of the classes implementing this interface
 * and appearing in a jar file should be placed in a file named
 * <blockquote>
 * META-INF/services/org.bzdev.graph.spi.SymbolProvider
 * </blockquote>
 * and that file should be included in the jar file. If the jar file is
 * a modular jar file, the module-info.java file should contain clauses
 * <BLOCKQUOTE><CODE></PRE>
 *     uses org.bzdev.graphs.spi.SymbolProvider;
 *     provides org.bzdev.graphs.spi.SymbolProvider with ... ;
 * </PRE></CODE></BLOCKQUOTE>
 * where "<CODE>...</CODE>" is a comma-separated list of the fully
 * qualified class names of all the symbol providers in this class.
 * @see org.bzdev.graphs.Graph.SymbolFactory
 * @see org.bzdev.graphs.Graph.Symbol
 */
public interface SymbolProvider {
    /**
     * Get the name of the symbol this provider supports
     * @return the name of the symbol this provider supports
     */
    String getSymbolName();
    /**
     * Get the subclass of Graph.Symbol for a provider.
     * @return the subclass of Graph.Symbol for a provider
     */
    Class<? extends Graph.Symbol> getSymbolClass();
}
