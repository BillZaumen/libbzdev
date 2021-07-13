package org.bzdev.graphs.symbols;
import org.bzdev.graphs.Graph;
import org.bzdev.graphs.spi.SymbolProvider;

/**
 * Provider for a filled hourglass symbol.
 */
public class SolidHourglassProvider implements SymbolProvider
{
    @Override
    public String getSymbolName() {return "SolidHourglass";}

    @Override
    public Class<? extends Graph.Symbol> getSymbolClass() {
	return SolidHourglass.class;
    }
}

//  LocalWords:  SolidHourglass
