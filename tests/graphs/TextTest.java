import org.bzdev.graphs.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.awt.geom.*;
import java.io.*;

import org.bzdev.gio.OutputStreamGraphics;
import org.bzdev.gio.ImageSequenceWriter;
import org.bzdev.gio.ISWriterOps;
import org.bzdev.gio.ImageOrientation;

public class TextTest {
    public static void main(String argv[]) throws Exception {
	Graph graph = new Graph();
	graph.setRanges(-100.0, 100.0, -200.0, 200.0);
	graph.setBackgroundColor(Color.BLACK);
	graph.clear();
	graph.setFontColor(Color.WHITE);
	graph.setFontJustification(Graph.Just.CENTER);
	graph.setFont(new Font("Helvetica", Font.BOLD, 26));

	graph.drawString("hello", 0.0, 0.0);

	graph.write("png", "TextTest.png");
    }
}
