package org.bzdev.graphs.symbols;
import org.bzdev.graphs.Graph;
import org.bzdev.graphs.spi.SymbolProvider;

/**
 * Provider class for a filled circle.
 */
public class SolidCircleProvider implements SymbolProvider
{
    @Override
    public String getSymbolName() {return "SolidCircle";}

    @Override
    public Class<? extends Graph.Symbol> getSymbolClass() {
	return SolidCircle.class;
    }
}

//  LocalWords:  SolidCircle
