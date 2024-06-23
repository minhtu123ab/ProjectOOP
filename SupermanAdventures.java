import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class SupermanAdventures extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 480;
    int boardHeight = 750;

    // Hình ảnh
    Image backgroundImg;
    Image supermanImg;
    Image topLaserImg;
    Image bottomLaserImg;
    Image fireworksImg;

    // Biến siêu nhân
    int supermanX = boardWidth / 8;
    int supermanY = boardWidth / 2;
    int supermanWidth = 60;
    int supermanHeight = 60;

    class Superman {
        int x = supermanX;
        int y = supermanY;
        int width = supermanWidth;
        int height = supermanHeight;
        Image img;

        Superman(Image img) {
            this.img = img;
        }
    }

    // Biến tia laser
    int laserX = boardWidth;
    int laserY = 0;
    int laserWidth = 64;
    int laserHeight = 512;

    class Laser {
        int x = laserX;
        int y = laserY;
        int width = laserWidth;
        int height = laserHeight;
        Image img;
        boolean passed = false;

        Laser(Image img) {
            this.img = img;
        }
    }

    // Logic game
    Superman superman;
    int velocityX = -4;
    int velocityY = 0;
    int gravity = 1;

    ArrayList<Laser> lasers;
    Random random = new Random();

    Timer gameLoop;
    Timer placeLaserTimer;
    boolean gameOver = false;
    double score = 0;
    double highScore = 0;

    // Hiệu ứng pháo hoa
    Timer fireworksTimer;
    boolean showFireworks = false;

    SupermanAdventures() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // Load hình ảnh
        backgroundImg = new ImageIcon(getClass().getResource("./background.png")).getImage();
        supermanImg = new ImageIcon(getClass().getResource("./superman.png")).getImage();
        topLaserImg = new ImageIcon(getClass().getResource("./toplaser.png")).getImage();
        bottomLaserImg = new ImageIcon(getClass().getResource("./bottomlaser.png")).getImage();
        fireworksImg = new ImageIcon(getClass().getResource("./fireworks.png")).getImage();

        // Khởi tạo siêu nhân và tia laser
        superman = new Superman(supermanImg);
        lasers = new ArrayList<Laser>();

        // Thiết lập hẹn giờ để đặt tia laser
        placeLaserTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placeLasers();
            }
        });

        // Thiết lập vòng lặp game
        gameLoop = new Timer(1000 / 60, this);

        // Thiết lập hẹn giờ cho hiệu ứng pháo hoa
        fireworksTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFireworks = false;
            }
        });
    }

    void startSupermanGame() {
        score = 0;
        gameOver = false;
        superman.y = supermanY;
        velocityY = 0;
        lasers.clear();
        placeLaserTimer.start();
        gameLoop.start();
    }

    void showStartSupermanDialog() {
        int option = JOptionPane.showOptionDialog(
                null,
                "Bắt đầu chơi?",
                "Superman Adventures",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[] { "Bắt đầu", "Thoát" },
                "Bắt đầu");
        if (option == JOptionPane.YES_OPTION) {
            startSupermanGame();
        } else {
            System.exit(0);
        }
    }

    void placeLasers() {
        int randomLaserY = (int) (laserY - laserHeight / 4 - Math.random() * (laserHeight / 2));
        int openingSpace = boardHeight / 4;

        Laser topLaser = new Laser(topLaserImg);
        topLaser.y = randomLaserY;
        lasers.add(topLaser);

        Laser bottomLaser = new Laser(bottomLaserImg);
        bottomLaser.y = topLaser.y + laserHeight + openingSpace;
        lasers.add(bottomLaser);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Vẽ background
        g.drawImage(backgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);

        // Vẽ siêu nhân
        g.drawImage(superman.img, superman.x, superman.y, superman.width, superman.height, null);

        // Vẽ tia laser
        for (int i = 0; i < lasers.size(); i++) {
            Laser laser = lasers.get(i);
            g.drawImage(laser.img, laser.x, laser.y, laser.width, laser.height, null);
        }

        // Vẽ điểm số
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 30));
        if (gameOver) {
            g.drawString("Game Over", 20, 30);
            g.drawString("Score: " + String.valueOf((int) score), 20, 60);
            g.drawString("High Score: " + String.valueOf((int) highScore), boardWidth - 200, 30);
        } else {
            g.drawString(String.valueOf((int) score), 10, 30);
        }

        // Hiệu ứng pháo hoa
        if (showFireworks) {
            g.drawImage(fireworksImg, 0, 0, boardWidth, boardHeight, null);
        }
    }

    public void move() {
        velocityY += gravity;
        superman.y += velocityY;
        superman.y = Math.max(superman.y, 0);

        for (int i = 0; i < lasers.size(); i++) {
            Laser laser = lasers.get(i);
            laser.x += velocityX;

            if (!laser.passed && superman.x > laser.x + laser.width) {
                score += 0.5;
                laser.passed = true;

                if (score > highScore) {
                    highScore = score;
                }
            }

            if (collision(superman, laser)) {
                gameOver = true;
                if (score > highScore) {
                    highScore = score;
                }
            }
        }

        if (superman.y > boardHeight) {
            gameOver = true;
        }
    }

    boolean collision(Superman a, Laser b) {
        return a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    void showCongratulationsDialog() {
        JOptionPane.showMessageDialog(
                null,
                "Chúc mừng! Bạn đã đạt điểm số cao nhất: " + (int) highScore,
                "Congratulations!",
                JOptionPane.INFORMATION_MESSAGE);
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placeLaserTimer.stop();
            gameLoop.stop();
            if (score < highScore) {
                showGameOverDialog();
            } else {
                highScore = score;
                showCongratulationsDialog();
                showFireworks = true;
                fireworksTimer.start();
            }
        }
    }

    void showGameOverDialog() {
        JOptionPane.showMessageDialog(
                null,
                "Game Over! Điểm số của bạn: " + (int) score,
                "Game Over",
                JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;

            if (gameOver) {
                showStartSupermanDialog();
            }
        }
    }

    // Không cần thiết
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Superman Adventures");
        SupermanAdventures game = new SupermanAdventures();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        game.showStartSupermanDialog();
    }
}