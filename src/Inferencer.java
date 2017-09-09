import java.lang.Thread.State;
import java.util.ArrayList;

public class Inferencer {

	private ArrayList<Statement> statements;
	private ArrayList<Statement> statementsInferred = new ArrayList<Statement>();

	CSVTool csv = new CSVTool("TransitivityTable.csv");

	public Inferencer(ArrayList<Statement> statements) {
		this.statements = statements;

		// for (int row = 0; row < 10; row++) {
		// for (int col = 0; col < 10; col++) {
		// System.out.print(csv.readCell(row, col) + "\t");
		// }
		// }
	}

	private String getTransitivity(Statement Ar1B, Statement Br2C) {
		int setRow = 0;
		int setCol = 0;
		String Evaluating = "";
		if (Ar1B.getObject().equals(Br2C.getSubject())) {
			Evaluating = "(" + Ar1B.toString() + ") and (" + Br2C.toString() + ")";
			for (int row = 2; row <= 14; row++) {
				if (csv.readCell(row, 1).equals(Ar1B.getPredicate()))
					setRow = row;
			}
			for (int col = 2; col <= 14; col++) {
				if (csv.readCell(1, col).equals(Br2C.getPredicate()))
					setCol = col;
			}
		}
		if (setRow != 0 && setCol != 0) {

			String cell = csv.readCell(setRow, setCol).trim();

			// String[] relations = cell.split(" ");
			String[] relations = new String[1];

			if (cell.contains(" "))
				cell = "(" + cell + ")";
			relations[0] = cell;

			if (!cell.equals("") /* && !cell.contains(" ") */) { // only process
																	// definitive
																	// facts!

				for (int a = 0; a < relations.length; a++) {
					Statement inferred = new Statement(Ar1B.getSubject(), relations[a], Br2C.getObject(), Evaluating);
					// System.out.print(" => ");
					// inferred.print();

					if (!contains(statementsInferred, inferred) && (Ar1B.getSubject() != Br2C.getObject()))
						statementsInferred.add(inferred);
				}
			}
		}
		return "";
	}

	private boolean contains(ArrayList<Statement> statements, Statement inferred) {
		for (int a = 0; a < statements.size(); a++) {

			if ((inferred.getSubject().equals(statements.get(a).getSubject()))
					&& (inferred.getObject().equals(statements.get(a).getObject()))
					&& (inferred.getPredicate().equals(statements.get(a).getPredicate())))
				return true;
		}
		return false;
	}

	public ArrayList<Statement> infer() {

		if (this.statements.size() > 0) {
			System.out.println("\nInput Statements:");
			for (int a = 0; a < this.statements.size(); a++) {
				// System.out.print(" ");
				this.statements.get(a).print(true, false);
			}
		}

		boolean inferring = true;

		while (inferring == true) {
			int infBefore = statementsInferred.size();

			// Process Allen's transitivity table:
			for (int a = 0; a < statements.size(); a++) {
				Statement s = statements.get(a);
				for (int b = 0; b < statements.size(); b++) {
					if (a != b) {
						Statement t = statements.get(b);
						getTransitivity(s, t);
					}
				}
			}

			// Add newly inferred statements to the normal statements:
			for (int a = 0; a < statementsInferred.size(); a++) {
				Statement s = statementsInferred.get(a);
				if (!contains(statements, s))
					statements.add(s);
			}

			int infAfter = statementsInferred.size();

			if (infBefore == infAfter)
				inferring = false;
		}

		if (this.statementsInferred.size() > 0) {
			System.out.println("\nInferred Statements:");
			for (int a = 0; a < this.statementsInferred.size(); a++) {
				// System.out.print(" ");
				this.statementsInferred.get(a).print(true, true);
			}
		}

		return statements;
	}
}
