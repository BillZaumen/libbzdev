package org.bzdev.graphs.symbols;
import org.bzdev.graphs.Graph;
import org.bzdev.graphs.spi.SymbolProvider;

/**
 * Symbol provider for an empty-circle symbol.
 */
public class EmptyCircleProvider implements SymbolProvider
{
    @Override
    public String getSymbolName() {return "EmptyCircle";}

    @Override
    public Class<? extends Graph.Symbol> getSymbolClass() {
	return EmptyCircle.class;
    }
}
