import org.bzdev.p3d.*;
import java.util.List;

public class ETest1 {
    public static void main(String argv[]) throws Exception {
	Model3D tm3d = new Model3D();
	// Triangles touch at a single point:
	// (7.724030017852783, -104.71210479736328, 0.18403615057468414)
	// and for one, the Z component of the other two vertices is
	// ~-12 and the other ~+12, so they clearly meet at a vertex.
	// The call to verifyEmbedded2DManifold should therefore return
	// null.
	tm3d.addTriangle(7.751552581787109,
			 -105.14988708496094, -12.309081077575684,
			 10.351327896118164,
			 -105.09880065917969, -12.239604949951172,
			 7.724030017852783, -104.71210479736328,
			 0.18403615057468414);
	tm3d.addTriangle(7.724030017852783, -104.71210479736328,
			 0.18403615057468414,
			 7.696506977081299, -104.27432250976562,
			 12.677153587341309,
			 5.1425957679748535,-104.56514739990234,
			 12.620352745056152);
	List<Model3D.Triangle> tlist =
	    tm3d.verifyEmbedded2DManifold();
	if (tlist != null) {
	    System.out.println("tm3d not embedded?");
	}
    }
}
