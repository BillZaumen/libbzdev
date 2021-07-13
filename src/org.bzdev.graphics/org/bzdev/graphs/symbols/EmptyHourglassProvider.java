package org.bzdev.graphs.symbols;
import org.bzdev.graphs.Graph;
import org.bzdev.graphs.spi.SymbolProvider;

/**
 * Provider class for an unfilled hourglass symbol.
 */
public class EmptyHourglassProvider implements SymbolProvider
{
    @Override
    public String getSymbolName() {return "EmptyHourglass";}

    @Override
    public Class<? extends Graph.Symbol> getSymbolClass() {
	return EmptyHourglass.class;
    }
}

//  LocalWords:  EmptyHourglass
