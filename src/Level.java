import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Level extends JPanel implements KeyListener, MouseListener, MouseMotionListener, ActionListener {
	private Timer timer = new Timer(10, this);
	private static Insets insets;

	private MapBlock[][] map, startMap;
	private ArrayList<Point> startingPositions = new ArrayList<Point>();
	private ArrayList<Point> goalBlocks = new ArrayList<Point>();
	private JLabel[][] powerUps;
	private int[] storedPowerUps = { 0, 0, 0 };
	private Player player = new Player();
	
	private JLabel reset, growCounter, splitCounter, mergeCounter;
	private final Font labelFont = new Font("Courier New", Font.BOLD, MapBlock.SIZE);
	private final Color textColor = Color.WHITE;
	
//	private final HashMap<Point, Rectangle> sides = new HashMap<Point, Rectangle>() {
//		{
//			put(new Point(-1, -1), new Rectangle(0, 0, PlayerBlock.SIZE / 5, PlayerBlock.SIZE / 5));
//			put(new Point(0, -1), new Rectangle(0, 0, PlayerBlock.SIZE, PlayerBlock.SIZE / 5));
//			put(new Point(1, -1), new Rectangle(4 * PlayerBlock.SIZE / 5, 0, PlayerBlock.SIZE / 5, PlayerBlock.SIZE / 5));
//			put(new Point(1, 0), new Rectangle(4 * PlayerBlock.SIZE / 5, 0, PlayerBlock.SIZE / 5, PlayerBlock.SIZE));
//			put(new Point(1, 1), new Rectangle(4 * PlayerBlock.SIZE / 5, 4 * PlayerBlock.SIZE / 5, PlayerBlock.SIZE / 5, PlayerBlock.SIZE / 5));
//			put(new Point(0, 1), new Rectangle(0, 4 * PlayerBlock.SIZE / 5, PlayerBlock.SIZE, PlayerBlock.SIZE / 5));
//			put(new Point(-1, 1), new Rectangle(0, 4 * PlayerBlock.SIZE / 5, PlayerBlock.SIZE / 5, PlayerBlock.SIZE / 5));
//			put(new Point(-1, 0), new Rectangle(0, 0, PlayerBlock.SIZE / 5, PlayerBlock.SIZE));
//		}
//	};

	Level(String filePath) {
		setLayout(null);
		try {
			List<String> lines = Files.readAllLines(Paths.get(filePath));
			map = new MapBlock[lines.size()][lines.get(0).length()];
			startMap = new MapBlock[lines.size()][lines.get(0).length()];
			powerUps = new JLabel[map.length][map[0].length];
			for (int i = 0; i < lines.size(); i++) {
				char[] row = lines.get(i).toCharArray();
				for (int j = 0; j < row.length; j++) {
					char block = row[j];
					if (block == '.') {
						map[i][j] = new SpaceBlock();
					} else if (block == 'B') {
						map[i][j] = new SolidBlock();
					} else if (block == 'G') {
						map[i][j] = new GoalBlock();
						goalBlocks.add(new Point(j, i));
					} else if (block == 'R') {
						map[i][j] = new GrowPowerUp();
					} else if (block == 'S') {
						map[i][j] = new SplitPowerUp();
					} else if (block == 'M') {
						map[i][j] = new MergePowerUp();
					} else if (block == 'P') {
						map[i][j] = new SpaceBlock();
						player.addBlock(j, i, Color.RED);
						startingPositions.add(new Point(j, i));
					}
				}
			}
			addLabels();
			resetPowerUps();
			for (int i = 0; i < map.length; i++) {
				for (int j = 0; j < map[i].length; j++) {
					startMap[i][j] = map[i][j];
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void addLabels() throws MalformedURLException {
		reset = new JLabel("RESET");
		reset.setForeground(textColor);
		reset.setFont(labelFont);
		reset.addMouseListener(this);
		reset.setBounds(MapBlock.SIZE, 0, 3 * MapBlock.SIZE, MapBlock.SIZE);
		add(reset);
		
		ImageIcon gImgIcon = new ImageIcon(new File(new GrowPowerUp().imagePath()).toURI().toURL());
		gImgIcon.setImage(gImgIcon.getImage().getScaledInstance(MapBlock.SIZE, MapBlock.SIZE, Image.SCALE_DEFAULT));
		JLabel growIcon = new JLabel(gImgIcon);
		growIcon.setBounds(5 * MapBlock.SIZE, 0, MapBlock.SIZE, MapBlock.SIZE);
		add(growIcon);
		growCounter = new JLabel("x0");
		growCounter.setForeground(textColor);
		growCounter.setFont(labelFont);
		growCounter.addMouseListener(this);
		growCounter.setBounds(6 * MapBlock.SIZE, 0, 2 * MapBlock.SIZE, MapBlock.SIZE);
		add(growCounter);
		
		ImageIcon sImgIcon = new ImageIcon(new File(new SplitPowerUp().imagePath()).toURI().toURL());
		sImgIcon.setImage(sImgIcon.getImage().getScaledInstance(MapBlock.SIZE, MapBlock.SIZE, Image.SCALE_DEFAULT));
		JLabel splitIcon = new JLabel(sImgIcon);
		splitIcon.setBounds(8 * MapBlock.SIZE, 0, MapBlock.SIZE, MapBlock.SIZE);
		add(splitIcon);
		splitCounter = new JLabel("x0");
		splitCounter.setForeground(textColor);
		splitCounter.setFont(labelFont);
		splitCounter.addMouseListener(this);
		splitCounter.setBounds(9 * MapBlock.SIZE, 0, 2 * MapBlock.SIZE, MapBlock.SIZE);
		add(splitCounter);
		
		ImageIcon mImgIcon = new ImageIcon(new File(new MergePowerUp().imagePath()).toURI().toURL());
		mImgIcon.setImage(mImgIcon.getImage().getScaledInstance(MapBlock.SIZE, MapBlock.SIZE, Image.SCALE_DEFAULT));
		JLabel mergeIcon = new JLabel(mImgIcon);
		mergeIcon.setBounds(11 * MapBlock.SIZE, 0, MapBlock.SIZE, MapBlock.SIZE);
		add(mergeIcon);
		mergeCounter = new JLabel("x0");
		mergeCounter.setForeground(textColor);
		mergeCounter.setFont(labelFont);
		mergeCounter.addMouseListener(this);
		mergeCounter.setBounds(12 * MapBlock.SIZE, 0, 2 * MapBlock.SIZE, MapBlock.SIZE);
		add(mergeCounter);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				MapBlock block = map[i][j];
				g2.setColor(block.getColor());
				g2.fillRect(j * MapBlock.SIZE, i * MapBlock.SIZE, MapBlock.SIZE, MapBlock.SIZE);
			}
		}
		ArrayList<PlayerBlock> pBlocks = player.getBlocks();
		for (PlayerBlock pBlock : pBlocks) {
			g2.setColor(Color.RED);
			Point pixelCoords = pBlock.getPixelCoords();
			g2.fillRect(pixelCoords.x, pixelCoords.y, PlayerBlock.SIZE, PlayerBlock.SIZE);
//			g2.setColor(Color.RED.darker());
//			Point worldCoords = pBlock.getWorldCoords();
//			for (Point offset : sides.keySet()) {
//				if (!pBlocks.contains(new PlayerBlock(worldCoords.x + offset.x, worldCoords.y + offset.y))) {
//					Rectangle rect = sides.get(offset);
//					g2.fillRect(pixelCoords.x + rect.x, pixelCoords.y + rect.y, rect.width, rect.height);
//				}
//			}
		}
		int state = player.getState();
		if (state == Player.BUILDING) {
			PlayerBlock highlightedBlock = player.getHighlightedBlock();
			if (highlightedBlock != null) {
				Point highlightedPoint = highlightedBlock.getPixelCoords();
				g2.setColor(Color.PINK);
				g2.fillRect(highlightedPoint.x, highlightedPoint.y, PlayerBlock.SIZE, PlayerBlock.SIZE);
			}
		} else if (state == Player.SPLITTING) {
			Point[] splitLine = player.getSplitLine();
			if (splitLine != null) {
				g2.setColor(Color.WHITE);
				g2.setStroke(new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
				g2.drawLine(splitLine[0].x * PlayerBlock.SIZE, splitLine[0].y * PlayerBlock.SIZE,
						splitLine[1].x * PlayerBlock.SIZE, splitLine[1].y * PlayerBlock.SIZE);
			}
		} else if (state == Player.CHOOSING) {
			int chosenSide = player.getChosenSide();
			if (chosenSide != -1) {
				g2.setColor(Color.PINK);
				for (PlayerBlock pBlock : player.getSplitBlocks(chosenSide)) {
					Point pixelCoords = pBlock.getPixelCoords();
					g2.fillRect(pixelCoords.x, pixelCoords.y, PlayerBlock.SIZE, PlayerBlock.SIZE);
				}
			}
		}
		timer.start();
	}

	public void keyPressed(KeyEvent e) {
		if (player.getState() == Player.NORMAL) {
			if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
				player.setMovement(Movement.RIGHT);
			} else if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
				player.setMovement(Movement.LEFT);
			} else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
				ArrayList<PlayerBlock> mergedBlocks = player.merge(map);
				if (mergedBlocks.size() > 0 && storedPowerUps[2] > 0) {
					for (PlayerBlock pBlock : mergedBlocks) {
						Point worldCoords = pBlock.getWorldCoords();
						map[worldCoords.y][worldCoords.x] = new SpaceBlock();
						player.addBlock(worldCoords.x, worldCoords.y, Color.RED);
					}
					mergeCounter.setText("x" + --storedPowerUps[2]);
				}
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (player.getState() == Player.NORMAL) {
			if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
				player.setMovement(Movement.STILL_RIGHT);
			} else if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
				player.setMovement(Movement.STILL_LEFT);
			} else if (e.getKeyCode() == KeyEvent.VK_SPACE && player.getMovement() == Movement.STILL
					&& !player.isFalling() && storedPowerUps[0] > 0) {
				player.startBuilding(map);
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		if (arg0.getComponent() == reset) {
			System.out.println("reset");
			resetLevel();
			resetPowerUps();
		} else if (player.getMovement() == Movement.STILL && player.isFalling()) {
			int state = player.getState();
			if (state == Player.NORMAL) {
				if (SwingUtilities.isRightMouseButton(arg0) && storedPowerUps[0] > 0) {
					player.startBuilding(map);
				} else if (SwingUtilities.isMiddleMouseButton(arg0) && storedPowerUps[1] > 0) {
					player.startSplitting();
				}
			} else if (state == Player.BUILDING) {
				if (player.getHighlightedBlock() != null) {
					player.confirmBuild();
					growCounter.setText("x" + --storedPowerUps[0]);
				}
			} else if (state == Player.SPLITTING) {
				if (player.getSplitLine() != null) {
					player.splitIntoSides();
					splitCounter.setText("x" + --storedPowerUps[1]);
				}
			} else if (state == Player.CHOOSING) {
				int chosenSide = player.getChosenSide();
				if (chosenSide != -1) {
					ArrayList<PlayerBlock> abandonedBlocks = player.chooseSide(chosenSide);
					for (PlayerBlock pBlock : abandonedBlocks) {
						Point worldCoords = pBlock.getWorldCoords();
						map[worldCoords.y][worldCoords.x] = new CryingPlayerBlock();
					}
				}
			}
			mouseAction(arg0);
		}
	}

	public void resetLevel() {
		for (int i = 0; i < startMap.length; i++) {
			for (int j = 0; j < startMap[i].length; j++) {
				map[i][j] = startMap[i][j];
			}
		}
		player.resetPositions(startingPositions);
		resetPowerUps();
	}

	private void resetPowerUps() {
		try {
			for (int i = 0; i < map.length; i++) {
				for (int j = 0; j < map[0].length; j++) {
					if (map[i][j] instanceof PowerUp && powerUps[i][j] == null) {
						ImageIcon imgIcon = new ImageIcon(new File(((PowerUp) map[i][j]).imagePath()).toURI().toURL());
						imgIcon.setImage(
								imgIcon.getImage().getScaledInstance(MapBlock.SIZE, MapBlock.SIZE, Image.SCALE_DEFAULT));
						JLabel label = new JLabel(imgIcon);
						powerUps[i][j] = label;
						label.setBounds(j * MapBlock.SIZE, i * MapBlock.SIZE, MapBlock.SIZE, MapBlock.SIZE);
						add(label);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		storedPowerUps = new int[] {0,0,0};
		growCounter.setText("x0");
		splitCounter.setText("x0");
		mergeCounter.setText("x0");
	}

	public void pickUpPowerUp() {
		for (int i = 0; i < player.getBlocks().size(); i++) {
			PlayerBlock pBlock = player.getBlocks().get(i);
			Point check = new Point((int) (Math.round(pBlock.getPixelCoords().getX() / MapBlock.SIZE)),
					pBlock.getWorldCoords().y);
			if (player.isValidBlock(map, check.x, check.y) && powerUps[check.y][check.x] != null) {
				if (map[check.y][check.x] instanceof GrowPowerUp) {
					growCounter.setText("x" + ++storedPowerUps[0]);
				} else if (map[check.y][check.x] instanceof SplitPowerUp) {
					splitCounter.setText("x" + ++storedPowerUps[1]);
				} else if (map[check.y][check.x] instanceof MergePowerUp) {
					mergeCounter.setText("x" + ++storedPowerUps[2]);
				}
				remove(powerUps[check.y][check.x]);
				powerUps[check.y][check.x] = null;
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		mouseAction(arg0);
	}

	private void mouseAction(MouseEvent e) {
		int x = e.getX() - insets.left;
		int y = e.getY() - insets.top;
		int state = player.getState();
		if (state == Player.BUILDING) {
			player.highlightBlock(x, y);
		} else if (state == Player.SPLITTING) {
			player.highlightSplitLine(map, x, y);
		} else if (state == Player.CHOOSING) {
			player.setChosenSide(x, y);
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		player.move(map);
		pickUpPowerUp();
		if (player.isOutOfBounds(map)) {
			resetLevel();
		}
		if (player.reachedGoal(goalBlocks, map)) {
			System.out.println("checkpoint");
		}
		repaint();
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(map[0].length * MapBlock.SIZE, map.length * MapBlock.SIZE);
	}

	private static void runGame() {
		Level level = new Level("levels/level3.txt");
		JFrame frame = new JFrame("Level 3");
		frame.addKeyListener(level);
		frame.addMouseListener(level);
		frame.addMouseMotionListener(level);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(level);
		frame.pack();
		frame.setVisible(true);
		insets = frame.getInsets();
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				runGame();
			}
		});
	}
}