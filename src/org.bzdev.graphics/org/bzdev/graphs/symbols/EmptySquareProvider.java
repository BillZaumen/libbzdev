package org.bzdev.graphs.symbols;
import org.bzdev.graphs.Graph;
import org.bzdev.graphs.spi.SymbolProvider;

/**
 * Provider for an unfilled square symbol.
 */
public class EmptySquareProvider implements SymbolProvider
{
    @Override
    public String getSymbolName() {return "EmptySquare";}

    @Override
    public Class<? extends Graph.Symbol> getSymbolClass() {
	return EmptySquare.class;
    }
}

//  LocalWords:  EmptySquare
