
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.allen.temporalintervalrelationships.Constraint;
import org.allen.temporalintervalrelationships.ConstraintNetwork;
import org.allen.temporalintervalrelationships.Node;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class AllenGUI extends JFrame {

	private static final long serialVersionUID = 1L;
	ArrayList<Statement> statements = new ArrayList<Statement>();
	ArrayList<Statement> statementsInferred = new ArrayList<Statement>();

	ConstraintNetwork<String> myConstraintNetwork = null;

	ArrayList<String> usedConceptNames = new ArrayList<String>();
	int ConceptNamePos = 65;

	ArrayList<Shape> shapes = new ArrayList<Shape>();
	ArrayList<String> itemNames = new ArrayList<String>();
	ArrayList<Boolean> itemToggles = new ArrayList<Boolean>();

	private javax.swing.JDialog AllenStatementsWindow;
	private javax.swing.JScrollPane jScrollPane2;
	private javax.swing.JTextArea allenStatementsViewer;

	// String theText = "";

	GUIHelper gh = new GUIHelper();

	public static void main(String[] args) {
		new AllenGUI();
	}

	public AllenGUI() {
		this.setSize(800, 600);
		this.setLocation(400, 100);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.add(new PaintSurface(), BorderLayout.CENTER);

		this.setTitle("AllenGUI: Graphical Temporal Modelling");

		/*
		 * JFrame theFrame = new JFrame(); theFrame.setTitle("Inferences");
		 * theFrame.setSize(310, 510); theFrame.setLocation(0, 0);
		 * theText.setSize(300, 500); JPanel mainPanel = new JPanel();
		 * mainPanel.add(theText); theFrame.getContentPane().add(mainPanel); //
		 * add the panel to the frame theFrame.pack();
		 * theFrame.setVisible(true);
		 */

		AllenStatementsWindow = new javax.swing.JDialog();
		jScrollPane2 = new javax.swing.JScrollPane();
		allenStatementsViewer = new javax.swing.JTextArea();

		AllenStatementsWindow.setTitle("Allen Statements");
		AllenStatementsWindow.setAlwaysOnTop(true);
		AllenStatementsWindow.setSize(new java.awt.Dimension(300, 300));

		allenStatementsViewer.setColumns(20);
		allenStatementsViewer.setRows(5);
		allenStatementsViewer.setToolTipText("");
		allenStatementsViewer.setWrapStyleWord(true);
		jScrollPane2.setViewportView(allenStatementsViewer);

		javax.swing.GroupLayout AllenStatementsWindowLayout = new javax.swing.GroupLayout(
				AllenStatementsWindow.getContentPane());
		AllenStatementsWindow.getContentPane().setLayout(AllenStatementsWindowLayout);
		AllenStatementsWindowLayout.setHorizontalGroup(
				AllenStatementsWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(AllenStatementsWindowLayout.createSequentialGroup().addContainerGap()
								.addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
								.addContainerGap()));
		AllenStatementsWindowLayout.setVerticalGroup(
				AllenStatementsWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(AllenStatementsWindowLayout.createSequentialGroup().addContainerGap()
								.addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
								.addContainerGap()));

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		AllenStatementsWindow.show();

		this.setVisible(true);

		try (BufferedReader br = new BufferedReader(new FileReader("MedicalConcepts.txt"))) {
			String line;
			int atConcept = 0;
			while ((line = br.readLine()) != null) {
				Shape r = gh.makeRectangle(10, 10 + (atConcept * 40), 210, 30);
				shapes.add(r);
				itemNames.add(line);
				itemToggles.add(false);
				atConcept++;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private class PaintSurface extends JComponent {

		private static final long serialVersionUID = 1L;

		Point startDraw, endDraw;
		Point startDrag, endDrag;
		Point startScale, endScale;
		Point startMouse, endMouse;
		int toDrag = -1, toScale = -1;

		int dragStartX = 0, dragStartY = 0;
		Shape selected;

		public PaintSurface() {
			this.addMouseListener(new MouseAdapter() {

				public void mousePressed(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						// System.out.println("mousePressed");
						toDrag = -1;
						toScale = -1;
						for (int a = 0; a < shapes.size(); a++) {
							Shape s = shapes.get(a);

							// User wants to drag an item:
							if (s.getBounds().contains(e.getX(), e.getY())) {
								// System.out.println("Dragging item " + a);
								toDrag = a;
								toScale = -1;
							}

							// User wants to scale an item:
							if (e.getX() >= s.getBounds().x + s.getBounds().width - 10
									&& e.getX() <= s.getBounds().x + s.getBounds().width
									&& e.getY() >= s.getBounds().y + s.getBounds().height - 10
									&& e.getY() <= s.getBounds().y + s.getBounds().height) {
								// System.out.println("Scaling item " + a);
								toScale = a;
								toDrag = -1;
							}

						}

						// get start position for drawing a new item:
						if (toDrag == -1 && toScale == -1) {
							startDraw = new Point(e.getX() - 10, e.getY() - 10);
							startMouse = new Point(e.getX(), e.getY());
							endDraw = startDraw;
							repaint();
						}

						// get start position for dragging an old item:
						if (toDrag != -1 && toScale == -1) {
							startDrag = new Point(e.getX(), e.getY());
							startMouse = new Point(e.getX(), e.getY());
							selected = shapes.get(toDrag);
							repaint();
						}

						// get start position for scaling an old item:
						if (toScale != -1 && toDrag == -1) {
							startScale = new Point(e.getX(), e.getY());
							startMouse = new Point(e.getX(), e.getY());
							selected = shapes.get(toScale);
							repaint();
						}

					}

					// Delete item:
					if (e.getButton() == MouseEvent.BUTTON3) {

						for (int a = 0; a < shapes.size(); a++) {
							Shape s = shapes.get(a);

							if (s.getBounds().contains(e.getX(), e.getY())) {
								// System.out.println("Deleting item " + a);
								shapes.remove(a);
								itemNames.remove(a);
								itemToggles.remove(a);
								repaint();
							}
						}
						generateOutput();
					}

					// Rename item:
					if (e.getButton() == MouseEvent.BUTTON2) {
						Boolean hitItem = false;
						for (int a = 0; a < shapes.size(); a++) {
							Shape s = shapes.get(a);
							if (s.getBounds().contains(e.getX(), e.getY())) {

								if (itemNames.get(a).startsWith("#")) {
									JOptionPane.showMessageDialog(null,
											"There is no need to rename link interval " + itemNames.get(a), "Info",
											JOptionPane.INFORMATION_MESSAGE);
								} else {

									String name = JOptionPane.showInputDialog("Please enter a name: ",
											itemNames.get(a).replaceAll("\\(.*\\)", "").trim());

									IntervalDescription id = new IntervalDescription(name);

									if (id.getEnumerator().equals("")) {

										if (!id.getOccuranceModifier().equals("")
												|| !id.getValueModifier().equals("")) {
											// id.setEnumerator("#" + a);
										}
									}

									itemNames.set(a, id.buildName());

									repaint();
									hitItem = true;
								}
							}
						}

						generateOutput();
					}
				}

				public void mouseReleased(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						System.out.println("mouseReleased");

						endMouse = new Point(e.getX(), e.getY());

						System.out.println(startMouse.x + " => " + endMouse.x);
						System.out.println(startMouse.y + " => " + endMouse.y);

						if (startMouse.x == endMouse.x && startMouse.y == endMouse.y) { // Toggling
																						// an
																						// item

							int item = 0;
							if (toDrag != -1)
								item = toDrag;
							if (toScale != -1)
								item = toScale;

							if (itemNames.get(item).startsWith("#")) {
								// JOptionPane.showMessageDialog(null, "Toggle "
								// + !itemToggles.get(item), "Toggle ",
								// JOptionPane.ERROR_MESSAGE);
								itemToggles.set(item, !itemToggles.get(item));

								if (itemToggles.get(item) && !itemNames.get(item).contains("_TOGGLED")) {
									itemNames.set(item, itemNames.get(item) + "_TOGGLED");
								}

								if (!itemToggles.get(item) && itemNames.get(item).contains("_TOGGLED")) {
									itemNames.set(item, itemNames.get(item).replaceAll("_TOGGLED", ""));
								}

							}

						}

						if (toDrag == -1 && toScale == -1) {
							Shape r = gh.makeRectangle(startDraw.x, startDraw.y, e.getX(), e.getY());
							shapes.add(r);

							if (r.getBounds().getWidth() <= 10 || r.getBounds().getHeight() <= 10) { // connector
																										// line
								itemNames.add(getNewConceptName("#"));
							} else {
								itemNames.add(getNewConceptName(""));
							}
							itemToggles.add(false);

							// long ms = System.currentTimeMillis();
							// itemNames.add("Item_" + ms);

							startDraw = null;
							endDraw = null;
							// System.out.println(shapes.size());
						}

						generateOutput();

					}

				}

				private void generateOutput() {
					repaint();
					storeGraph();

					String[] toDisplay = allenStatementsViewer.getText().split("\n");

					try {
						PrintWriter writer = new PrintWriter("TemporalLogic.txt", "UTF-8");

						for (int a = 0; a < toDisplay.length; a++) {
							// g.drawString(toDisplay[a], 10, a * 14 + 10 +
							// 7);
							writer.println(toDisplay[a]);
						}

						writer.close();
					} catch (Exception e1) {
						e1.printStackTrace();
					}

				}

			});

			// this is called as long as the mouse if pressed and moving:
			this.addMouseMotionListener(new MouseMotionAdapter() {

				public void mouseDragged(MouseEvent e) {

					// drawing a new item
					if (toDrag == -1 && toScale == -1) {
						endDraw = new Point(e.getX(), e.getY());
						repaint();

					} else if (toDrag != -1 && toScale == -1) { // dragging an
																// old item

						int dX = e.getX() - startDrag.x;
						int dY = e.getY() - startDrag.y;

						Shape r = gh.makeRectangle(selected.getBounds().x + e.getX() - startDrag.x - 5,
								selected.getBounds().y + e.getY() - startDrag.y - 5,
								selected.getBounds().x + selected.getBounds().width + e.getX() - startDrag.x - 5,
								selected.getBounds().y + selected.getBounds().height + e.getY() - startDrag.y - 5);

						shapes.set(toDrag, r);
						repaint();

					} else if (toDrag == -1 && toScale != -1) { // scaling an
																// old item
						int dX = e.getX() - startScale.x;
						int dY = e.getY() - startScale.y;

						// System.out.println(dX + " " + dY);
						Shape r = gh.makeRectangle(selected.getBounds().x - 5, selected.getBounds().y - 5,
								selected.getBounds().x + selected.getBounds().width + e.getX() - startScale.x - 5,
								selected.getBounds().y + selected.getBounds().height + e.getY() - startScale.y - 5);

						shapes.set(toScale, r);
						repaint();
					}

				}
			});
		}

		private void paintBackground(Graphics2D g2) {
			g2.setPaint(Color.LIGHT_GRAY);
			for (int i = 0; i < getSize().width; i += 10) {
				Shape line = new Line2D.Float(i, 0, i, getSize().height);
				g2.draw(line);
			}
			for (int i = 0; i < getSize().height; i += 10) {
				Shape line = new Line2D.Float(0, i, getSize().width, i);
				g2.draw(line);
			}
		}

		public void paint(Graphics g) {

			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			paintBackground(g2);

			g2.setStroke(new BasicStroke(2));
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

			for (int a = 0; a < shapes.size(); a++) {
				Shape s = shapes.get(a);

				if (s.getBounds().getWidth() <= 10 || s.getBounds().getHeight() <= 10
						|| itemNames.get(a).startsWith("#")) { // connector
					// line

					if (!itemToggles.get(a)) {
						g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
						g2.setPaint(Color.ORANGE);
						g2.draw(s);
						g2.fill(s);
					} else {
						g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
						g2.setPaint(Color.RED);
						g2.draw(s);
						g2.fill(s);
					}

					// Font f = new Font("Arial", Font.BOLD, 14);
					// g2.setFont(f);
					// g2.setPaint(Color.BLACK);

				} else { // time interval
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
					g2.setPaint(Color.BLACK);
					g2.draw(s);

					g2.setPaint(Color.BLUE);

					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));

					IntervalDescription id = new IntervalDescription(itemNames.get(a));

					if (id.isComparison()) {
						g2.setPaint(Color.MAGENTA);
					}

					if (id.isNegation()) {
						g2.setPaint(Color.RED);
					}

					if (id.isDuration())
						g2.setPaint(Color.DARK_GRAY);

					if (id.isTimeStamp()) {
						g2.setPaint(new Color(0, 150, 0));
					}

					g2.fill(s);

					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
					Font f = new Font("Arial", Font.BOLD, 20);
					g2.setFont(f);
					g2.setPaint(Color.WHITE);
					g.drawString(itemNames.get(a), s.getBounds().getBounds().x + 3,
							s.getBounds().getBounds().y + s.getBounds().getBounds().height - 3);

				}

				Shape drawer;

				drawer = gh.makeRectangle(s.getBounds().getBounds().x + s.getBounds().getBounds().width - 20,
						s.getBounds().getBounds().y + s.getBounds().getBounds().height - 20,
						s.getBounds().getBounds().x + s.getBounds().getBounds().width - 10,
						s.getBounds().getBounds().y + s.getBounds().getBounds().height - 10);

				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
				g2.setPaint(Color.WHITE);
				g2.fill(drawer);
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

			}

			if (startDraw != null && endDraw != null) {
				g2.setPaint(Color.LIGHT_GRAY);
				Shape r = gh.makeRectangle(startDraw.x, startDraw.y, endDraw.x, endDraw.y);
				g2.fill(r);
				g2.draw(r);
			}

			Font f = new Font("Arial", Font.BOLD, 14);
			g2.setFont(f);
			g2.setPaint(Color.RED);

		}

		private long getSeed(String string) {

			int seed = 0;

			for (int a = 0; a < string.length(); a++) {
				char character = string.charAt(a);
				seed += (int) character;
			}

			return seed;
		}

		private void storeGraph() {

			// System.out.println("----------------------------------------------");
			// System.out.println("Total items: " + shapes.size());

			System.out.println("\n\n=== PROCESSING GRAPH ===");

			allenStatementsViewer.setText("");

			myConstraintNetwork = new ConstraintNetwork<String>();

			statements.clear();
			for (int x = 0; x < shapes.size(); x++) {
				for (int y = 0; y < shapes.size(); y++) {
					if (x != y) {
						checkTouching(shapes.get(x), shapes.get(y), itemNames.get(x), itemNames.get(y));
					}
				}
			}

			ArrayList<Node<String>> nodes = myConstraintNetwork.getModeledNodes();

			System.out.println("Validating NOT statements ...");

			ArrayList<String> NotNodes1 = new ArrayList<String>();
			ArrayList<String> NotNodes2 = new ArrayList<String>();

			for (int a = 0; a < myConstraintNetwork.getModeledConstraints().size(); a++) {
				Constraint<String> c = myConstraintNetwork.getModeledConstraints().get(a);

				String source = c.getSourceNode().getIdentifier();
				String link = myConstraintNetwork.getConstraintStringFromConstraintShort(c.getConstraints()).toString();
				String target = c.getDestinationNode().getIdentifier();
				String stmt = source + " " + link + " " + target;

				if (source.startsWith("NO") || target.startsWith("NO")) {
					System.out.println("Remembering as a valid NOT statement: " + stmt);
				}

				NotNodes1.add(source);
				NotNodes2.add(target);
			}

			System.out.println("");

			// Now process the nodes coming from the inferencer:

			String tempText = "";
			String allStatements = "";

			for (int a = 0; a < nodes.size(); a++) {

				for (int b = a; b < nodes.size(); b++) {
					if (a != b) {

						boolean validStatement = true;

						Short n = myConstraintNetwork.getConstraintNetwork().get(a).get(b);

						// if (!nodes.get(a).getIdentifier().contains("#") &&
						// !nodes.get(b).getIdentifier().contains("#")) {

						String source = nodes.get(a).getIdentifier();
						String link = myConstraintNetwork.getConstraintStringFromConstraintShort(n).toString();
						String target = nodes.get(b).getIdentifier();

						AllenStatement temp = new AllenStatement(source + link + target);
						temp.toPreferred();

						source = temp.getInterval1().getOrigDescription();
						link = "[" + temp.getRelation() + "]";
						target = temp.getInterval2().getOrigDescription();

						// if (itemToggles.get(a)) link += "TOGGLED";
						// if (itemToggles.get(b)) link += "TOGGLED";

						String statement = source + " " + link + " " + target + "\n";
						allStatements += source + " " + link + " " + target + ";";

						// System.out.println("Testing: " + statement);

						// Do not show the generic Allen relation:
						// if (link.equals(
						// "[before, after, during, contains, overlaps,
						// overlapped by, meets, met by, starts, started by,
						// finishes, finished by, equals]")
						// || link.equals(
						// "[before, after, during, contains, overlaps,
						// overlapped_by, meets, met_by, starts, started_by,
						// finishes, finished_by, equals]")) {
						// validStatement = false;
						// }

						if (target.startsWith("#") || source.startsWith("#"))
							validStatement = false;

						
						// Test if a NOT node is to be processed:

						/*
						 
						if ((source.startsWith("NO") && !target.startsWith("#"))
								|| (target.startsWith("NO") && !source.startsWith("#"))) {

							validStatement = false;

							// Case 1: The statement was modeled by the user and
							// is therefore valid:

							for (int c = 0; c < NotNodes1.size(); c++) {
								if ((NotNodes1.get(c).equals(source) && NotNodes2.get(c).equals(target))
										|| (NotNodes2.get(c).equals(source) && NotNodes1.get(c).equals(target))) {
									validStatement = true;
								}
							}

							// Case 2: The statement was inferred and there is a
							// single hidden intermediate node between. It's
							// therefore valid:

							for (int c = 0; c < NotNodes1.size(); c++) {

								String X1 = NotNodes1.get(c);
								String Y1 = NotNodes2.get(c);

								for (int d = 0; d < NotNodes1.size(); d++) {

									String X2 = NotNodes1.get(d);
									String Y2 = NotNodes2.get(d);

									if (X1.equals(source) && Y1.equals(X2) && Y2.equals(target)) {
										validStatement = true;
									}
									if (X1.equals(target) && Y1.equals(X2) && Y2.equals(source)) {
										validStatement = true;
									}
								}
							}
						}
						*/
						
						
						
						if (validStatement) {
							System.out.print(statement);
							tempText += statement;
						}

					}
				}
			}

			// Convert before/after into precedes/succeeds where applicable:

			System.out.println("all Statements: " + allStatements);

			String[] sts = allStatements.split(";");

			// Build list of all Allen statements first:
			ArrayList<AllenStatement> ass = new ArrayList<AllenStatement>();
			for (int a = 0; a < sts.length; a++) {
				if (!sts[a].equals("")) {
					AllenStatement as = new AllenStatement(sts[a]);
					as.toPreferred();
					ass.add(as);
				}
			}

			// Then identify and replace the relevant ones:
			for (int a = 0; a < ass.size(); a++) {
				AllenStatement as = ass.get(a);

				if (as.getRelation().equals("meets")) {

					for (int b = 0; b < ass.size(); b++) {
						AllenStatement bs = ass.get(b);

						if (bs.getRelation().equals("meets")
								&& as.getInterval2().getOrigDescription().equals(bs.getInterval1().getOrigDescription())
								&& as.getInterval2().getOrigDescription().contains("_TOGGLED")) {

							System.out.println("Replacing: " + as.getInterval1().getOrigDescription() + " [before] "
									+ bs.getInterval2().getOrigDescription() + " with "
									+ as.getInterval1().getOrigDescription() + " [precedes] "
									+ bs.getInterval2().getOrigDescription());

							tempText = tempText.replaceAll(
									as.getInterval1().getOrigDescription() + " \\[before\\] "
											+ bs.getInterval2().getOrigDescription(),
									as.getInterval1().getOrigDescription() + " \\[precedes\\] "
											+ bs.getInterval2().getOrigDescription());

						}

					}

				}

			}

			// ----------

			allenStatementsViewer.setText(tempText);

			ArrayList<ArrayList<Short>> x = myConstraintNetwork.getConstraintNetwork();

			if (!myConstraintNetwork.pathConsistency()) {
				JOptionPane.showMessageDialog(null, "This graph is not consistent!", "Temporal Error",
						JOptionPane.ERROR_MESSAGE);
				allenStatementsViewer.setText("Temporal Error: The graph is not consistent!");
				this.repaint();
			}

		}
	}

	public void checkTouching(Shape shape1, Shape shape2, String t_name, String s_name) {

		int t_minus = shape1.getBounds().x / 10;
		int ty1 = shape1.getBounds().y / 10;
		int t_plus = (shape1.getBounds().x / 10) + (shape1.getBounds().width / 10);
		int ty2 = (shape1.getBounds().y / 10) + (shape1.getBounds().height / 10);

		if (shape1.getBounds().height / 10 == 1 || shape1.getBounds().width / 10 == 1) {
			// System.out.println(name1 + " isA Connector");
			// System.out.println("connector(" + t_name + ").");
		} else {
			// System.out.println(name1 + " isA Concept");
			// System.out.println("concept(" + t_name + ").");
		}

		int s_minus = shape2.getBounds().x / 10;
		int sy1 = shape2.getBounds().y / 10;
		int s_plus = (shape2.getBounds().x / 10) + (shape2.getBounds().width / 10);
		int sy2 = (shape2.getBounds().y / 10) + (shape2.getBounds().height / 10);

		if (shape2.getBounds().height / 10 == 1 || shape2.getBounds().width / 10 == 1) {
			// System.out.println(name2 + " isA Connector");
			// System.out.println("connector(" + s_name + ").");
		} else {
			// System.out.println(name2 + " isA Concept");
			// System.out.println("concept(" + s_name + ").");
		}

		if (isTouching(t_minus, ty1, t_plus, ty2, s_minus, sy1, s_plus, sy2)) {

			// t < s: t+ < s-

			if (t_plus < s_minus) {
				addStatement(t_name, "<", s_name);
				// addStatement(s_name, ">", t_name);
			}

			// t = s: (t- = s-) & (t+ = s+)

			if ((t_minus == s_minus) && (t_plus == s_plus)) {
				addStatement(t_name, "=", s_name);
			}

			// t overlaps s: (t- < s-) & (t+ > s-) & (t+ < s+)

			if ((t_minus < s_minus) && (t_plus > s_minus) && (t_plus < s_plus)) {
				addStatement(t_name, "o", s_name);
				// addStatement(s_name, "oi", t_name);
			}

			// t meets s: t+ = s-

			if (t_plus == s_minus) {
				addStatement(t_name, "m", s_name);
				// addStatement(s_name, "mi", t_name);
			}

			// t starts s: (t- = s-) & (t+ > s-) & (t+ < s+)

			if ((t_minus == s_minus) && (t_plus > s_minus) && (t_plus < s_plus)) {
				addStatement(t_name, "s", s_name);
				// addStatement(s_name, "si", t_name);
			}

			// t finishes s: (t+ = s+) & (t- > s+) & (t- < s-)

			if ((t_plus == s_plus) && (t_minus < s_plus) && (t_minus > s_minus)) {
				addStatement(t_name, "f", s_name);
				// addStatement(s_name, "fi", t_name);
			}

			// t during s: ((t- > s-) & (t+ =< s+)) or ((t- >= s-) && (t+ < s+))

			if (((t_minus > s_minus) && (t_plus <= s_plus)) || ((t_minus >= s_minus) && (t_plus < s_plus))) {
				addStatement(t_name, "d", s_name);
				// addStatement(s_name, "di", t_name);
			}

		}
	}

	public boolean isRelativeInterval(String in) {
		in = in.toLowerCase();
		if (in.contains(" year") || in.contains(" month") || in.contains(" week") || in.contains(" day")
				|| in.contains(" hour") || in.contains(" minute") || in.contains(" second")) {
			return true;
		}
		return false;
	}

	private void addStatement(String source, String relation, String target) {

		// System.out.println("Adding statement: " + source + " " + relation +
		// target);

		Node<String> nodeA = null;
		Node<String> nodeB = null;
		nodeA = new Node<String>(source);
		nodeB = new Node<String>(target);

		// Check if the nodes are already in the constraint network, if so,
		// reuse them:

		/*
		 * ArrayList<Constraint<String>> alreadyThere =
		 * myConstraintNetwork.getModeledConstraints(); for (int a = 0; a <
		 * alreadyThere.size(); a++) { String sourceNode =
		 * alreadyThere.get(a).getSourceNode().getIdentifier(); String
		 * targetNode =
		 * alreadyThere.get(a).getDestinationNode().getIdentifier(); if
		 * (sourceNode.equals(source)) nodeA =
		 * alreadyThere.get(a).getSourceNode(); if (targetNode.equals(target))
		 * nodeB = alreadyThere.get(a).getDestinationNode(); }
		 */

		ArrayList<Node<String>> nodes = myConstraintNetwork.getModeledNodes();
		for (int a = 0; a < nodes.size(); a++) {
			if (nodes.get(a).getIdentifier().equals(source)) {
				nodeA = nodes.get(a);
			}
			if (nodes.get(a).getIdentifier().equals(target)) {
				nodeB = nodes.get(a);
			}

		}

		myConstraintNetwork.addNode(nodeA);
		myConstraintNetwork.addNode(nodeB);

		Constraint<String> constraintAB = null;

		switch (relation) {
		case "<":
			constraintAB = new Constraint<String>(nodeA, nodeB, ConstraintNetwork.bin_before);
			break;
		case ">":
			constraintAB = new Constraint<String>(nodeA, nodeB, ConstraintNetwork.bin_after);
			break;
		case "d":
			constraintAB = new Constraint<String>(nodeA, nodeB, ConstraintNetwork.bin_during);
			break;
		case "con":
			constraintAB = new Constraint<String>(nodeA, nodeB, ConstraintNetwork.bin_contains);
			break;
		case "o":
			constraintAB = new Constraint<String>(nodeA, nodeB, ConstraintNetwork.bin_overlaps);
			break;
		case "oi":
			constraintAB = new Constraint<String>(nodeA, nodeB, ConstraintNetwork.bin_overlappedby);
			break;
		case "m":
			constraintAB = new Constraint<String>(nodeA, nodeB, ConstraintNetwork.bin_meets);
			break;
		case "mi":
			constraintAB = new Constraint<String>(nodeA, nodeB, ConstraintNetwork.bin_metby);
			break;
		case "s":
			constraintAB = new Constraint<String>(nodeA, nodeB, ConstraintNetwork.bin_starts);
			break;
		case "si":
			constraintAB = new Constraint<String>(nodeA, nodeB, ConstraintNetwork.bin_startedby);
			break;
		case "f":
			constraintAB = new Constraint<String>(nodeA, nodeB, ConstraintNetwork.bin_finishes);
			break;
		case "fi":
			constraintAB = new Constraint<String>(nodeA, nodeB, ConstraintNetwork.bin_finishedby);
			break;
		case "=":
			constraintAB = new Constraint<String>(nodeA, nodeB, ConstraintNetwork.bin_equals);
			break;
		default:
			System.out.println("Error with relation: " + source + " " + relation + " " + target);
			break;
		}

		myConstraintNetwork.addConstraint(constraintAB);

		// Statement s = new Statement(t_name, relation, s_name);
		// statements.add(s);

	}

	private boolean isTouching(int t_minus, int ty1, int t_plus, int ty2, int s_minus, int sy1, int s_plus, int sy2) {
		if ((s_plus == t_minus) || (t_plus == s_minus)) {
			if (((ty2 >= sy1) && (ty2 <= sy2)) || (ty1 >= sy1) && (ty1 <= sy2))
				return true;
			if (((sy2 >= ty1) && (sy2 <= ty2)) || (sy1 >= ty1) && (sy1 <= ty2))
				return true;
		}
		if ((sy2 == ty1) || (sy1 == ty2)) {
			if (((s_plus >= t_minus) && (s_plus <= t_plus)) || (s_minus >= t_minus) && (s_minus <= t_plus))
				return true;
			if (((t_plus >= s_minus) && (t_plus <= s_plus)) || (t_minus >= s_minus) && (t_minus <= s_plus))
				return true;
		}
		return false;
	}

	private String getNewConceptName(String prefix) {
		String name = Character.toString((char) ConceptNamePos);
		int size = 1;
		while (itemNames.contains(prefix + name)) {
			name = "";
			for (int a = 0; a < size; a++) {
				name += Character.toString((char) ConceptNamePos);
			}
			ConceptNamePos++;
			if (ConceptNamePos == 91) {
				ConceptNamePos = 65;
				size++;
			}
		}
		name = prefix + name;
		return name;
	}

}
