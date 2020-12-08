package pacman;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Model extends JPanel implements ActionListener {

	private Dimension d;
    private final Font smallFont = new Font("Arial", Font.BOLD, 14);
    private boolean inGame = false;
    private boolean dying = false;

    private final int BLOCK = 24;
    private final int N_BLOCKS = 15;
    private final int SCREEN = N_BLOCKS * BLOCK;
    private final int MAX_GH = 12;
    private final int P_SPEED = 6;

    private int N_GHOSTS = 4;
    private int lives, score;
    private int[] dx, dy;
    private int[] g_x, g_y, g_dx, g_dy, G_Speed;

    private Image heart, ghost;
    private Image up, down, left, right;

    private int p_x, p_y, p_dx, p_dy;
    private int req_dx, req_dy;

    private final short levelData[] = {
    	19, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
        17, 16, 16, 16, 16, 24, 16, 16, 16, 16, 16, 16, 16, 16, 20,
        25, 24, 24, 24, 28, 0, 17, 16, 16, 16, 16, 16, 16, 16, 20,
        0,  0,  0,  0,  0,  0, 17, 16, 16, 16, 16, 16, 16, 16, 20,
        19, 18, 18, 18, 18, 18, 16, 16, 16, 16, 24, 24, 24, 24, 20,
        17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
        17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
        17, 16, 16, 16, 24, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
        17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 18, 18, 18, 18, 20,
        17, 24, 24, 28, 0, 25, 24, 24, 16, 16, 16, 16, 16, 16, 20,
        21, 0,  0,  0,  0,  0,  0,   0, 17, 16, 16, 16, 16, 16, 20,
        17, 18, 18, 22, 0, 19, 18, 18, 16, 16, 16, 16, 16, 16, 20,
        17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
        17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
        25, 24, 24, 24, 26, 24, 24, 24, 24, 24, 24, 24, 24, 24, 28
    };

    private final int Speed[] = {1, 2, 3, 4, 6, 8};
    private final int maxSpeed = 6;

    private int currentSpeed = 3;
    private short[] sc_Data;
    private Timer timer;

    public Model() {

        loadImages();
        initVariables();
        addKeyListener(new TAdapter());
        setFocusable(true);
        initGame();
    }
    
    
    
       private void initVariables() {

        sc_Data = new short[N_BLOCKS * N_BLOCKS];
        d = new Dimension(400, 400);
        g_x = new int[MAX_GH];
        g_dx = new int[MAX_GH];
        g_y = new int[MAX_GH];
        g_dy = new int[MAX_GH];
        G_Speed = new int[MAX_GH];
        dx = new int[4];
        dy = new int[4];
        
        timer = new Timer(40, this);
        timer.start();
    }

    private void playGame(Graphics2D g2d) {

        if (dying) {

            death();

        } else {

            movePacman();
            drawPacman(g2d);
            moveGhosts(g2d);
            checkMaze();
        }
    }

    private void showIntroScreen(Graphics2D g2d) {
 
    	String start = "Press SPACE to start";
        g2d.setColor(Color.red);
        g2d.drawString(start, (SCREEN)/4, 150);
    }

    private void drawScore(Graphics2D g) {
        g.setFont(smallFont);
        g.setColor(new Color(5, 181, 79));
        String s = "Score: " + score;
        g.drawString(s, SCREEN / 2 + 96, SCREEN + 16);

        for (int i = 0; i < lives; i++) {
            g.drawImage(heart, i * 28 + 8, SCREEN + 1, this);
        }
    }

    private void checkMaze() {

        int i = 0;
        boolean finished = true;

        while (i < N_BLOCKS * N_BLOCKS && finished) {

            if ((sc_Data[i]) != 0) {
                finished = false;
            }

            i++;
        }

        if (finished) {

            score += 50;

            if (N_GHOSTS < MAX_GH) {
                N_GHOSTS++;
            }

            if (currentSpeed < maxSpeed) {
                currentSpeed++;
            }

            initLevel();
        }
    }

    private void death() {

    	lives--;

        if (lives == 0) {
            inGame = false;
        }

        continueLevel();
    }

    private void moveGhosts(Graphics2D g2d) {

        int pos;
        int count;

        for (int i = 0; i < N_GHOSTS; i++) {
            if (g_x[i] % BLOCK == 0 && g_y[i] % BLOCK == 0) {
                pos = g_x[i] / BLOCK + N_BLOCKS * (int) (g_y[i] / BLOCK);

                count = 0;

                if ((sc_Data[pos] & 1) == 0 && g_dx[i] != 1) {
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }

                if ((sc_Data[pos] & 2) == 0 && g_dy[i] != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }

                if ((sc_Data[pos] & 4) == 0 && g_dx[i] != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }

                if ((sc_Data[pos] & 8) == 0 && g_dy[i] != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }

                if (count == 0) {

                    if ((sc_Data[pos] & 15) == 15) {
                        g_dx[i] = 0;
                        g_dy[i] = 0;
                    } else {
                        g_dx[i] = -g_dx[i];
                        g_dy[i] = -g_dy[i];
                    }

                } else {

                    count = (int) (Math.random() * count);

                    if (count > 3) {
                        count = 3;
                    }

                    g_dx[i] = dx[count];
                    g_dy[i] = dy[count];
                }

            }

            g_x[i] = g_x[i] + (g_dx[i] * G_Speed[i]);
            g_y[i] = g_y[i] + (g_dy[i] * G_Speed[i]);
            
            drawGhost(g2d, g_x[i] + 1, g_y[i] + 1);

            if (p_x > (g_x[i] - 12) && p_x < (g_x[i] + 12)
                    && p_y > (g_y[i] - 12) && p_y < (g_y[i] + 12)
                    && inGame) {

                dying = true;
            }
        }
    }

    private void drawGhost(Graphics2D g2d, int x, int y) {
    	g2d.drawImage(ghost, x, y, this);
        }

    private void movePacman() {

        int pos;
        short ch;

        if (p_x % BLOCK == 0 && p_y % BLOCK == 0) {
            pos = p_x / BLOCK + N_BLOCKS * (int) (p_y / BLOCK);
            ch = sc_Data[pos];

            if ((ch & 16) != 0) {
                sc_Data[pos] = (short) (ch & 15);
                score++;
            }

            if (req_dx != 0 || req_dy != 0) {
                if (!((req_dx == -1 && req_dy == 0 && (ch & 1) != 0)
                        || (req_dx == 1 && req_dy == 0 && (ch & 4) != 0)
                        || (req_dx == 0 && req_dy == -1 && (ch & 2) != 0)
                        || (req_dx == 0 && req_dy == 1 && (ch & 8) != 0))) {
                    p_dx = req_dx;
                    p_dy = req_dy;
                }
            }

            // Check for standstill
            if ((p_dx == -1 && p_dy == 0 && (ch & 1) != 0)
                    || (p_dx == 1 && p_dy == 0 && (ch & 4) != 0)
                    || (p_dx == 0 && p_dy == -1 && (ch & 2) != 0)
                    || (p_dx == 0 && p_dy == 1 && (ch & 8) != 0)) {
                p_dx = 0;
                p_dy = 0;
            }
        } 
        p_x = p_x + P_SPEED * p_dx;
        p_y = p_y + P_SPEED * p_dy;
    }

    private void drawPacman(Graphics2D g2d) {

        if (req_dx == -1) {
        	g2d.drawImage(left, p_x + 1, p_y + 1, this);
        } else if (req_dx == 1) {
        	g2d.drawImage(right, p_x + 1, p_y + 1, this);
        } else if (req_dy == -1) {
        	g2d.drawImage(up, p_x + 1, p_y + 1, this);
        } else {
        	g2d.drawImage(down, p_x + 1, p_y + 1, this);
        }
    }

    private void drawMaze(Graphics2D g2d) {

        short i = 0;
        int x, y;

        for (y = 0; y < SCREEN; y += BLOCK) {
            for (x = 0; x < SCREEN; x += BLOCK) {

                g2d.setColor(new Color(0,72,251));
                g2d.setStroke(new BasicStroke(5));
                
                if ((levelData[i] == 0)) { 
                	g2d.fillRect(x, y, BLOCK, BLOCK);
                 }

                if ((sc_Data[i] & 1) != 0) { 
                    g2d.drawLine(x, y, x, y + BLOCK - 1);
                }

                if ((sc_Data[i] & 2) != 0) { 
                    g2d.drawLine(x, y, x + BLOCK - 1, y);
                }

                if ((sc_Data[i] & 4) != 0) { 
                    g2d.drawLine(x + BLOCK - 1, y, x + BLOCK - 1,
                            y + BLOCK - 1);
                }

                if ((sc_Data[i] & 8) != 0) { 
                    g2d.drawLine(x, y + BLOCK - 1, x + BLOCK - 1,
                            y + BLOCK - 1);
                }

                if ((sc_Data[i] & 16) != 0) { 
                    g2d.setColor(new Color(255,255,255));
                    g2d.fillOval(x + 10, y + 10, 6, 6);
               }

                i++;
            }
        }
    }
    
    private void loadImages() {
    	down = new ImageIcon("C:\\Users\\unhmguest\\Desktop\\Comp 830\\Project\\Pacman\\src\\images\\down.gif").getImage();
    	up = new ImageIcon("C:\\Users\\unhmguest\\Desktop\\Comp 830\\Project\\Pacman\\src\\images\\up.gif").getImage();
    	left = new ImageIcon("C:\\Users\\unhmguest\\Desktop\\Comp 830\\Project\\Pacman\\src\\images\\left.gif").getImage();
    	right = new ImageIcon("C:\\Users\\unhmguest\\Desktop\\Comp 830\\Project\\Pacman\\src\\images\\right.gif").getImage();
        heart = new ImageIcon("C:\\Users\\unhmguest\\Desktop\\Comp 830\\Project\\Pacman\\src\\images\\heart.png").getImage();
        ghost = new ImageIcon("C:\\Users\\unhmguest\\Desktop\\Comp 830\\Project\\Pacman\\src\\images\\ghost.gif").getImage();

    }

    private void initGame() {

    	lives = 3;
        score = 0;
        initLevel();
        N_GHOSTS = 4;
        currentSpeed = 2;
    }

    private void initLevel() {

        int i;
        for (i = 0; i < N_BLOCKS * N_BLOCKS; i++) {
            sc_Data[i] = levelData[i];
        }

        continueLevel();
    }

    private void continueLevel() {

    	int dx = 1;
        int random;

        for (int i = 0; i < N_GHOSTS; i++) {

            g_y[i] = 4 * BLOCK; //start position
            g_x[i] = 4 * BLOCK;
            g_dy[i] = 0;
            g_dx[i] = dx;
            dx = -dx;
            random = (int) (Math.random() * (currentSpeed + 1));

            if (random > currentSpeed) {
                random = currentSpeed;
            }

            G_Speed[i] = Speed[random];
        }

        p_x = 11 * BLOCK;  //start position
        p_y = 11 * BLOCK;
        p_dx = 0;	//reset direction move
        p_dy = 0;
        req_dx = 0;		// reset direction controls
        req_dy = 0;
        dying = false;
    }

 
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, d.width, d.height);

        drawMaze(g2d);
        drawScore(g2d);

        if (inGame) {
            playGame(g2d);
        } else {
            showIntroScreen(g2d);
        }

        g2d.dispose();
    }


    //controls
    class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if (inGame) {
                if (key == KeyEvent.VK_LEFT) {
                    req_dx = -1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_RIGHT) {
                    req_dx = 1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_UP) {
                    req_dx = 0;
                    req_dy = -1;
                } else if (key == KeyEvent.VK_DOWN) {
                    req_dx = 0;
                    req_dy = 1;
                } else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    inGame = false;
                } 
            } else {
                if (key == KeyEvent.VK_SPACE) {
                    inGame = true;
                    initGame();
                }
            }
        }
}

	
    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
		
	}