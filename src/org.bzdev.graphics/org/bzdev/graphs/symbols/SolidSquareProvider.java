package org.bzdev.graphs.symbols;
import org.bzdev.graphs.Graph;
import org.bzdev.graphs.spi.SymbolProvider;

/**
 * Provider for a filled square symbol.
 */
public class SolidSquareProvider implements SymbolProvider
{
    @Override
    public String getSymbolName() {return "SolidSquare";}

    @Override
    public Class<? extends Graph.Symbol> getSymbolClass() {
	return SolidSquare.class;
    }
}

//  LocalWords:  SolidSquare
