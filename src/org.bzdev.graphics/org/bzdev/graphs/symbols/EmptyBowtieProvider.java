package org.bzdev.graphs.symbols;
import org.bzdev.graphs.Graph;
import org.bzdev.graphs.spi.SymbolProvider;

/**
 * Provider class for an empty bow-tie symbol.
 */
public class EmptyBowtieProvider implements SymbolProvider
{
    public String getSymbolName() {return "EmptyBowtie";}
    public Class<? extends Graph.Symbol> getSymbolClass() {
	return EmptyBowtie.class;
    }
}
