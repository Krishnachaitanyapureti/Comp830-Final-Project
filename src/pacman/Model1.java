package pacman;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Model1 extends JPanel implements ActionListener {

	private Dimension d;
    private final Font smallFont = new Font("Arial", Font.BOLD, 14);
    private boolean inGame = false;
    private boolean dying = false;

    private final int BLOCK = 24;
    private final int BLOCK_COUNT = 15;
    private final int SCREEN = BLOCK_COUNT * BLOCK;
    private final int MAX_GH = 12;
    private final int P_SPEED = 6;

    private int N_GHOSTS = 4;
    private int lives, score;
    private int[] dx, dy;
    private int[] g_x, g_y, g_dx, g_dy, G_Speed;

    private Image heart, ghost;
    private Image up, down, left, right;

    private int p_x, p_y, p_dx, p_dy;
    private int r_dx, r_dy;

    private final short level[] = {
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

    private int cur_spd = 3;
    private short[] sc_Data;
    private Timer timer;

    public Model1() {

        ImageUpload();
        initVariables();
        addKeyListener(new TAdapter());
        setFocusable(true);
        initGame();
    }
    
    
    
       private void initVariables() {

        sc_Data = new short[BLOCK_COUNT * BLOCK_COUNT];
        d = new Dimension(400, 400);
        g_x = new int[MAX_GH];
        g_dx = new int[MAX_GH];
        g_y = new int[MAX_GH];
        g_dy = new int[MAX_GH];
        G_Speed = new int[MAX_GH];
        dx = new int[3];
        dy = new int[3];
        
        timer = new Timer(60, this);
        timer.start();
    }

    private void playGame(Graphics2D g2d) {

        if (dying) {

            death();

        } else {

            pc_go();
            pc_draw(g2d);
            gh_go(g2d);
            seeMaze();
        }
    }

    private void showIntroScreen(Graphics2D g2d) {
 
    	String start = "Press SPACE to start";
        g2d.setColor(Color.red);
        g2d.drawString(start, (SCREEN)/4, 150);
    }

    private void Scoredisplay(Graphics2D g) {
        g.setFont(smallFont);
        g.setColor(new Color(5, 181, 79));
        String s = "Score: " + score;
        g.drawString(s, SCREEN / 2 + 96, SCREEN + 16);

        for (int i = 0; i < lives; i++) {
            g.drawImage(heart, i * 28 + 8, SCREEN + 1, this);
        }
    }

    private void seeMaze() {

        int i = 0;
        boolean finished = true;

        while (i < BLOCK_COUNT * BLOCK_COUNT && finished) {

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

            if (cur_spd < maxSpeed) {
                cur_spd++;
            }

            initialLevel();
        }
    }

    private void death() {

    	lives--;

        if (lives == 0) {
            inGame = false;
        }

        nextLevel();
    }

    private void gh_go(Graphics2D g2d) {

        int pos;
        int count;

        for (int i = 0; i < N_GHOSTS; i++) {
            if (g_x[i] % BLOCK == 0 && g_y[i] % BLOCK == 0) {
                pos = g_x[i] / BLOCK + BLOCK_COUNT * (int) (g_y[i] / BLOCK);

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
            
            gh_draw(g2d, g_x[i] + 1, g_y[i] + 1);

            if (p_x > (g_x[i] - 12) && p_x < (g_x[i] + 12)
                    && p_y > (g_y[i] - 12) && p_y < (g_y[i] + 12)
                    && inGame) {

                dying = true;
            }
        }
    }

    private void gh_draw(Graphics2D g2d, int a, int b) {
    	g2d.drawImage(ghost, a, b, this);
        }

    private void pc_go() {

        int pos;
        short ch;

        if (p_x % BLOCK == 0 && p_y % BLOCK == 0) {
            pos = p_x / BLOCK + BLOCK_COUNT * (int) (p_y / BLOCK);
            ch = sc_Data[pos];

            if ((ch & 16) != 0) {
                sc_Data[pos] = (short) (ch & 15);
                score++;
            }

            if (r_dx != 0 || r_dy != 0) {
                if (!((r_dx == -1 && r_dy == 0 && (ch & 1) != 0)
                        || (r_dx == 1 && r_dy == 0 && (ch & 4) != 0)
                        || (r_dx == 0 && r_dy == -1 && (ch & 2) != 0)
                        || (r_dx == 0 && r_dy == 1 && (ch & 8) != 0))) {
                    p_dx = r_dx;
                    p_dy = r_dy;
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

   
    private void Mazecreate(Graphics2D g2d) {

        short i = 0;
        int a, b;

        for (b = 0; b < SCREEN; b += BLOCK) {
            for (a = 0; a < SCREEN; a += BLOCK) {

                g2d.setColor(new Color(0,72,251));
                g2d.setStroke(new BasicStroke(5));
                
                if ((level[i] == 0)) { 
                	g2d.fillRect(a, b, BLOCK, BLOCK);
                 }

                if ((sc_Data[i] & 1) != 0) { 
                    g2d.drawLine(a, b, a, b + BLOCK - 1);
                }

                if ((sc_Data[i] & 2) != 0) { 
                    g2d.drawLine(a, b, a + BLOCK - 1, b);
                }

                if ((sc_Data[i] & 4) != 0) { 
                    g2d.drawLine(a + BLOCK - 1, b, a + BLOCK - 1,
                            b + BLOCK - 1);
                }

                if ((sc_Data[i] & 8) != 0) { 
                    g2d.drawLine(a, b + BLOCK - 1, a + BLOCK - 1,
                            b + BLOCK - 1);
                }

                if ((sc_Data[i] & 16) != 0) { 
                    g2d.setColor(new Color(255,255,255));
                    g2d.fillOval(a + 10, b + 10, 6, 6);
               }

                i++;
            }
        }
    }
    
    private void pc_draw(Graphics2D g2d) {

        if (r_dx == -1) {
        	g2d.drawImage(left, p_x + 1, p_y + 1, this);
        } else if (r_dx == 1) {
        	g2d.drawImage(right, p_x + 1, p_y + 1, this);
        } else if (r_dy == -1) {
        	g2d.drawImage(up, p_x + 1, p_y + 1, this);
        } else {
        	g2d.drawImage(down, p_x + 1, p_y + 1, this);
        }
    }

    private void ImageUpload() {
    	down = new ImageIcon("C:\\Users\\unhmguest\\Desktop\\Comp 830\\Project\\Pacman\\src\\images\\down.gif").getImage();
    	up = new ImageIcon("C:\\Users\\unhmguest\\Desktop\\Comp 830\\Project\\Pacman\\src\\images\\up.gif").getImage();
    	left = new ImageIcon("C:\\Users\\unhmguest\\Desktop\\Comp 830\\Project\\Pacman\\src\\images\\left.gif").getImage();
    	right = new ImageIcon("C:\\Users\\unhmguest\\Desktop\\Comp 830\\Project\\Pacman\\src\\images\\right.gif").getImage();
        heart = new ImageIcon("C:\\Users\\unhmguest\\Desktop\\Comp 830\\Project\\Pacman\\src\\images\\heart.png").getImage();
        ghost = new ImageIcon("C:\\Users\\unhmguest\\Desktop\\Comp 830\\Project\\Pacman\\src\\images\\ghost.gif").getImage();

    }

    private void initGame() {

    	lives = 5;
        score = 0;
        initialLevel();
        N_GHOSTS = 3;
        cur_spd = 3;
    }

    private void initialLevel() {

        int i;
        for (i = 0; i < BLOCK_COUNT * BLOCK_COUNT; i++) {
            sc_Data[i] = level[i];
        }

        nextLevel();
    }

    private void nextLevel() {

    	int dx = 1;
        int random;

        for (int i = 0; i < N_GHOSTS; i++) {

            g_y[i] = 4 * BLOCK; //start position
            g_x[i] = 4 * BLOCK;
            g_dy[i] = 0;
            g_dx[i] = dx;
            dx = -dx;
            random = (int) (Math.random() * (cur_spd + 1));

            if (random > cur_spd) {
                random = cur_spd;
            }

            G_Speed[i] = Speed[random];
        }

        p_x = 11 * BLOCK;  //start position
        p_y = 11 * BLOCK;
        p_dx = 0;	//reset direction move
        p_dy = 0;
        r_dx = 0;		// reset direction controls
        r_dy = 0;
        dying = false;
    }

 
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, d.width, d.height);

        Mazecreate(g2d);
        Scoredisplay(g2d);

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
                    r_dx = -1;
                    r_dy = 0;
                } else if (key == KeyEvent.VK_RIGHT) {
                    r_dx = 1;
                    r_dy = 0;
                } else if (key == KeyEvent.VK_UP) {
                    r_dx = 0;
                    r_dy = -1;
                } else if (key == KeyEvent.VK_DOWN) {
                    r_dx = 0;
                    r_dy = 1;
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