package org.bzdev.graphs.symbols;
import org.bzdev.graphs.Graph;
import org.bzdev.graphs.spi.SymbolProvider;

/**
 * Provider for a filled bow-tie symbol.
 */
public class SolidBowtieProvider implements SymbolProvider
{
    public String getSymbolName() {return "SolidBowtie";}
    public Class<? extends Graph.Symbol> getSymbolClass() {
	return SolidBowtie.class;
    }
}

//  LocalWords:  SolidBowtie
