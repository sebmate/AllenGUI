import java.awt.geom.Rectangle2D;

public class GUIHelper {

	Rectangle2D.Float makeRectangle(int x1, int y1, int x2, int y2) {

		int x1n = Math.round(Math.min(x1, x2) / 10) * 10 + 10;
		int y1n = Math.round(y1 / 10) * 10 + 10;
		int x2n = Math.round(Math.abs(x1 - x2) / 10) * 10;
		int y2n = Math.round(Math.abs(y1 - y2) / 10) * 10;

		if ((y2n > 10) && (x2n > 10)) {
			y2n = 30; // Block (Knoten)
		}
		if (y2n < 30) {
			y2n = 10; // Linie
		}
		if (x2n < 10) {
			x2n = 10;
		}

		return new Rectangle2D.Float(x1n, y1n, x2n, y2n);
	}

	Rectangle2D.Float makeRectangle2(int x1, int y1, int x2, int y2) {

		int x1n = Math.min(x1, x2) + 10;
		int y1n = y1 + 10;
		int x2n = Math.abs(x1 - x2);
		int y2n = Math.abs(y1 - y2);

		if ((y2n > 10) && (x2n > 10)) {
			y2n = 30; // Block (Knoten)
		}
		if (y2n < 30) {
			y2n = 10; // Linie
		}
		if (x2n < 10) {
			x2n = 10;
		}

		return new Rectangle2D.Float(x1n, y1n, x2n, y2n);
	}

}
