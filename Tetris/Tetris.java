package Tetris;

/*  This is a Tetris game
 * 
 * Figure 1 (orange) : .    .    O(A)
 *                     O(C) O(0) O(B)
 * 
 * Figure 2 (cyan)   : O(C) O(0) O(B) O(A)
 * 
 * Figure 3 (blue)   : O(A) .    .
 *                     O(B) O(0) O(C)
 * 
 * Figure 4 (purple) : .    O(B) .
 *                     O(A) O(0) O(C)
 * 
 * Figure 5 (red)    : O(A) O(B) .
 *                     .    O(0) O(C)
 * 
 * Figure 6 (yellow) : O   O
 *                     O   O
 * 
 * Figure 7 (green)  : .    O(0) O(C)
 *                     O(A) O(B) .
 * 
 * 
 * 
 * 
 *
 * 
 * 
 */
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;

public class Tetris extends JFrame implements ActionListener{
    Random random = new Random();

    JLabel tLine1;
    JLabel tLine2;

    JLabel nfLine1;
    JLabel nfLine2;
    JLabel nfLine3;
    JLabel nfLine4;

    JLabel[][] positions = new JLabel[18][10];
    JPanel positionPanel = new JPanel();

    ImageIcon[] figures = new ImageIcon[7];

    int currFigure;

    int currFigureX;
    int currFigureY;

    int[] posA;
    int[] posB;
    int[] posC;

    int[] stockedPosA;
    int[] stockedPosB;
    int[] stockedPosC;

    Timer fpsTimer;
    Timer mainGame;

    JLabel[][] stockedFigure = new JLabel[5][5];
    JPanel stockedPanel = new JPanel();

    int StockedFigure;

    ArrayList<ImageIcon> iconsList = new ArrayList<ImageIcon>();

    Boolean[][] confirmedFigures;

    int Ifigure;

    Boolean loose = false;
    JLabel lost;
    JLabel lost2;

    JLabel score;
    JLabel bestScoreLabel;
    int bestScore;
    int currScore = 0;

    String pathK = "score.txt";

    Tetris() throws UnsupportedAudioFileException, IOException, LineUnavailableException{
        File file = new File("GuiImage/TetrisSound.wav");
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
        
        Clip clip = AudioSystem.getClip();
        clip.open(audioStream);
        clip.setLoopPoints(0, 0); 

        clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                clip.setFramePosition(0);
                clip.start();
            }
        });

        clip.start();
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(600, 540);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.getContentPane().setBackground(Color.black);
        this.setResizable(false);
        this.setLayout(null);
        this.setVisible(true);

        fpsTimer = new Timer(5, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (currScore > bestScore) {
                    bestScore = currScore;
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathK))){
                        writer.write(String.valueOf(currScore));
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
               removeLine();
               score.setText("Score: " + currScore);
               bestScoreLabel.setText("Best score: " + bestScore);
               placeFigure(posA, posB, posC, new int[]{currFigureY, currFigureX}, currFigure);
               stockedPosA = posA.clone();
               stockedPosB = posB.clone();
               stockedPosC = posC.clone();
               update();
            }
            
        });

        mainGame = new Timer(1000, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                removeFigure(posA, posB, posC, new int[]{currFigureY, currFigureX});
                if (posA[0] == 16 | posB[0] == 16 | posC[0] == 16) {
                    confirmPlaceFigure();
                    return;
                } 
                try {
                    if (iconsList.contains(positions[posA[0]+1][posA[1]].getIcon())) {
                        confirmPlaceFigure();
                        return;
                    }
                } catch (Exception e2) {
                    // TODO: handle exception
                }
                try {
                    if (iconsList.contains(positions[posB[0]+1][posB[1]].getIcon())) {
                        confirmPlaceFigure();
                        return;
                    }
                } catch (Exception e2) {
                    // TODO: handle exception
                }
                try {
                    if (iconsList.contains(positions[posC[0]+1][posC[1]].getIcon())) {
                        confirmPlaceFigure();
                        return;
                    }
                } catch (Exception e2) {
                    // TODO: handle exception
                }
                try {
                    if (iconsList.contains(positions[currFigureY+1][currFigureX].getIcon())) {
                        confirmPlaceFigure();
                        return;
                    }
                } catch (Exception e2) {
                    // TODO: handle exception
                }
                posA[0]++;
                posB[0]++;
                posC[0]++;
                currFigureY++;

                //System.out.println(posA[0] + " " + posA[1]);
                //System.out.println(posB[0] + " " + posB[1]);
                //System.out.println(posC[0] + " " + posC[1]);
            }
            
        });

        Ifigure = random.nextInt((7 - 1) + 1) + 1;

        StockedFigure = Ifigure;

        createLabels();
        testFigure();

        fpsTimer.start();
        mainGame.start();
    }

    public void drawStockedFigure(int figure) {
        for (int i = 0; i < stockedFigure.length; i++) {
            for (int j = 0; j < stockedFigure[i].length; j++) {
                try {
                    stockedFigure[i][j].setIcon(null);
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }
        switch (figure) {
            case 1:
                stockedPanel.setLocation(500, 32);
                stockedFigure[1][1].setIcon(figures[figure-1]);
                stockedFigure[2][1].setIcon(figures[figure-1]);
                stockedFigure[3][1].setIcon(figures[figure-1]);
                stockedFigure[3][2].setIcon(figures[figure-1]);
                break;

            case 2:
                stockedPanel.setLocation(492, 39);
                stockedFigure[0][2].setIcon(figures[figure-1]);
                stockedFigure[1][2].setIcon(figures[figure-1]);
                stockedFigure[2][2].setIcon(figures[figure-1]);
                stockedFigure[3][2].setIcon(figures[figure-1]);
                break;
            
            case 3:
                stockedPanel.setLocation(497, 32);
                stockedFigure[1][2].setIcon(figures[figure-1]);
                stockedFigure[3][1].setIcon(figures[figure-1]);
                stockedFigure[2][2].setIcon(figures[figure-1]);
                stockedFigure[3][2].setIcon(figures[figure-1]);
                break;

            case 4:
                stockedPanel.setLocation(492, 27);
                stockedFigure[3][1].setIcon(figures[figure-1]);
                stockedFigure[3][2].setIcon(figures[figure-1]);
                stockedFigure[3][3].setIcon(figures[figure-1]);
                stockedFigure[2][2].setIcon(figures[figure-1]);
                break;
            
            case 5:
                stockedPanel.setLocation(492, 27);
                stockedFigure[2][1].setIcon(figures[figure-1]);
                stockedFigure[2][2].setIcon(figures[figure-1]);
                stockedFigure[3][2].setIcon(figures[figure-1]);
                stockedFigure[3][3].setIcon(figures[figure-1]);
                break;

            case 6:
                stockedPanel.setLocation(498, 27);
                stockedFigure[2][1].setIcon(figures[figure-1]);
                stockedFigure[2][2].setIcon(figures[figure-1]);
                stockedFigure[3][1].setIcon(figures[figure-1]);
                stockedFigure[3][2].setIcon(figures[figure-1]);
                break;
            case 7: 
                stockedPanel.setLocation(492, 27);
                stockedFigure[3][1].setIcon(figures[figure-1]);
                stockedFigure[3][2].setIcon(figures[figure-1]);
                stockedFigure[2][2].setIcon(figures[figure-1]);
                stockedFigure[2][3].setIcon(figures[figure-1]);
                break;
        }
    }

    public void confirmPlaceFigure() {
        positions[posA[0]][posA[1]].setIcon(figures[currFigure-1]);
        positions[posB[0]][posB[1]].setIcon(figures[currFigure-1]);
        positions[posC[0]][posC[1]].setIcon(figures[currFigure-1]);
        positions[currFigureY][currFigureX].setIcon(figures[currFigure-1]);

        confirmedFigures[posA[0]][posA[1]] = true;
        confirmedFigures[posB[0]][posB[1]] = true;
        confirmedFigures[posC[0]][posC[1]] = true;
        confirmedFigures[currFigureY][currFigureX] = true;

        testFigure();
    }

    public void update() {
        this.repaint();
        this.revalidate();
    }

    public void removeFigure(int[] posA, int[] posB, int[] posC, int[] currPos) {
        positions[posA[0]][posA[1]].setIcon(null);
        positions[posB[0]][posB[1]].setIcon(null);
        positions[posC[0]][posC[1]].setIcon(null);
        positions[currPos[0]][currPos[1]].setIcon(null);
    }

    public void placeFigure(int[] posA, int[] posB, int[] posC, int[] currPos, int figure) {
        positions[posA[0]][posA[1]].setIcon(figures[figure-1]);
        positions[posB[0]][posB[1]].setIcon(figures[figure-1]);
        positions[posC[0]][posC[1]].setIcon(figures[figure-1]);
        positions[currPos[0]][currPos[1]].setIcon(figures[figure-1]);
    }

    public void drawFigure(int posX, int posY, int figure) {
        try {
            if (iconsList.contains(positions[posY][posX].getIcon())) {
                lost.setVisible(true);
                lost2.setVisible(true);
                
                fpsTimer.stop();
                mainGame.stop();

                loose = true;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        switch (figure) {
            case 1:
                positions[posY][posX].setIcon(figures[figure-1]);

                positions[posY-1][posX+1].setIcon(figures[figure-1]);
                posA = new int[]{posY-1, posX+1};

                positions[posY][posX+1].setIcon(figures[figure-1]);
                posB = new int[]{posY, posX+1};

                positions[posY][posX-1].setIcon(figures[figure-1]);
                posC = new int[]{posY, posX-1};

                break;
        
            case 2:
                positions[posY][posX].setIcon(figures[figure-1]);

                positions[posY][posX+2].setIcon(figures[figure-1]);
                posA = new int[]{posY, posX+2};

                positions[posY][posX+1].setIcon(figures[figure-1]);
                posB = new int[]{posY, posX+1};

                positions[posY][posX-1].setIcon(figures[figure-1]);
                posC = new int[]{posY, posX-1};
                break;
            
            case 3:
                positions[posY][posX].setIcon(figures[figure-1]);

                positions[posY-1][posX-1].setIcon(figures[figure-1]);
                posA = new int[]{posY-1, posX-1};

                positions[posY][posX-1].setIcon(figures[figure-1]);
                posB = new int[]{posY, posX-1};

                positions[posY][posX+1].setIcon(figures[figure-1]);
                posC = new int[]{posY, posX+1};

                break;
        
            case 4:
                positions[posY][posX].setIcon(figures[figure-1]);

                positions[posY][posX-1].setIcon(figures[figure-1]);
                posA = new int[]{posY, posX-1};

                positions[posY-1][posX].setIcon(figures[figure-1]);
                posB = new int[]{posY-1, posX};

                positions[posY][posX+1].setIcon(figures[figure-1]);
                posC = new int[]{posY, posX+1};

                break;

            case 5:
                positions[posY][posX].setIcon(figures[figure-1]);
                
                positions[posY-1][posX-1].setIcon(figures[figure-1]);
                posA = new int[]{posY-1, posX-1};

                positions[posY-1][posX].setIcon(figures[figure-1]);
                posB = new int[]{posY-1, posX};

                positions[posY][posX+1].setIcon(figures[figure-1]);
                posC = new int[]{posY, posX+1};

                break;
            
            case 6:
                positions[posY][posX].setIcon(figures[figure-1]);
               
                positions[posY-1][posX+1].setIcon(figures[figure-1]);
                posA = new int[]{posY-1, posX+1};

                positions[posY][posX+1].setIcon(figures[figure-1]);
                posB = new int[]{posY, posX+1};

                positions[posY-1][posX].setIcon(figures[figure-1]);
                posC = new int[]{posY-1, posX};

                break;
        
            case 7:
                positions[posY][posX].setIcon(figures[figure-1]);

                positions[posY+1][posX-1].setIcon(figures[figure-1]);
                posA = new int[]{posY+1, posX-1};

                positions[posY+1][posX].setIcon(figures[figure-1]);
                posB = new int[]{posY+1, posX};

                positions[posY][posX+1].setIcon(figures[figure-1]);
                posC = new int[]{posY, posX+1};

                break;
        }
        currFigureX = posX;
        currFigureY = posY;
        currFigure = figure;
        System.out.println(figure);

        this.repaint();
        this.revalidate();
    }

    public void createLabels() {
        


        confirmedFigures = new Boolean[18][10];
        for (int i = 0; i < confirmedFigures.length; i++) {
            for (int j = 0; j < confirmedFigures[i].length; j++) {
                confirmedFigures[i][j] = false;
            }
        }
        try {
            this.remove(tLine1);
            this.remove(tLine2);

            this.remove(nfLine1);
            this.remove(nfLine2);
            this.remove(nfLine3);
            this.remove(nfLine4);

            this.remove(positionPanel);
        } catch (Exception e) {
            // TODO: handle exception
        }

        for (int i = 0; i < figures.length; i++) {
            ImageIcon TempIcon = new ImageIcon("GuiImage/Figure" + (i+1) + ".png");
            Image temp = TempIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            figures[i] = new ImageIcon(temp);
            iconsList.add(figures[i]);
        }

        tLine1 = new JLabel();
        tLine1.setBackground(Color.white);
        tLine1.setBounds(149, 0, 1, this.getHeight());
        tLine1.setOpaque(true);

        tLine2 = new JLabel();
        tLine2.setBackground(Color.white);
        tLine2.setBounds(450, 0, 1, this.getHeight());
        tLine2.setOpaque(true);

        // Space between the 2 lines : 300 pixels

        stockedPanel.setBounds(492, 32, 62, 62);

        stockedPanel.setOpaque(false);
        stockedPanel.setLayout(new GridLayout(5, 5, 1, 1));

        for (int i = 0; i < stockedFigure.length; i++) {
            for (int j = 0; j < stockedFigure[i].length; j++) {
                stockedFigure[i][j] = new JLabel();
                stockedPanel.add(stockedFigure[i][j]);
            }
        }

        nfLine1 = new JLabel();
        nfLine1.setBackground(Color.white);
        nfLine1.setBounds(484, 24, 75, 2);
        nfLine1.setOpaque(true);

        nfLine2 = new JLabel();
        nfLine2.setBackground(Color.white);
        nfLine2.setBounds(484, 99, 77, 2);
        nfLine2.setOpaque(true);

        nfLine3 = new JLabel();
        nfLine3.setBackground(Color.white);
        nfLine3.setBounds(484, 24, 2, 75);
        nfLine3.setOpaque(true);

        nfLine4 = new JLabel();
        nfLine4.setBackground(Color.white);
        nfLine4.setBounds(559, 24, 2, 75);
        nfLine4.setOpaque(true);

        // Space between the 2 lines of the box : 75 pixels
        
        positionPanel = new JPanel();
        positionPanel.setLayout(new GridLayout(18, 40));
        positionPanel.setBackground(Color.gray);
        positionPanel.setBounds(150, 0, 300, this.getHeight());

        for (int i = 0; i < positions.length; i++) {
            for (int j = 0; j < positions[i].length; j++) {
                positions[i][j] = new JLabel();
                //positions[i][j].setIcon(figures[4]);
                positionPanel.add(positions[i][j]);
            }
        }

        String pathK = "score.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(pathK))){
            bestScore = Integer.parseInt(reader.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }

        bestScoreLabel = new JLabel();
        bestScoreLabel.setFont(new Font("Calibri", Font.PLAIN, 15));
        bestScoreLabel.setBounds(450, 100, 150, 50);
        bestScoreLabel.setBackground(Color.black);
        bestScoreLabel.setForeground(Color.white);
        bestScoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        bestScoreLabel.setOpaque(true);
        bestScoreLabel.setText("Best score: " + bestScore);

        score = new JLabel();
        score.setFont(new Font("Calibri", Font.PLAIN, 15));
        score.setBounds(450, 150, 150, 50);
        score.setBackground(Color.black);
        score.setForeground(Color.white);
        score.setHorizontalAlignment(SwingConstants.CENTER);
        score.setOpaque(true);
        score.setText("Score: " + currScore);

        lost = new JLabel();
        lost.setBounds(150, 100, 300, 50);
        lost.setHorizontalAlignment(SwingConstants.CENTER);
        lost.setBackground(Color.gray);
        lost.setFont(new Font("Calibri", 0, 25));
        lost.setForeground(Color.white);
        lost.setOpaque(true);
        lost.setVisible(false);
        lost.setText("You lost! Press \"enter\" to ");

        lost2 = new JLabel();
        lost2.setBounds(150, 140, 300, 50);
        lost2.setHorizontalAlignment(SwingConstants.CENTER);
        lost2.setBackground(Color.gray);
        lost2.setFont(new Font("Calibri", 0, 25));
        lost2.setForeground(Color.white);
        lost2.setOpaque(true);
        lost2.setVisible(false);
        lost2.setText("restart the game ");

        tLine1.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "reset");
        tLine1.getActionMap().put("reset", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGame();
            }
        });

        tLine1.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "rotateFigure");
        tLine1.getActionMap().put("rotateFigure", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (loose) {
                    return;
                }
                rotateFigure();
            }
        });

        tLine1.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "goRight");
        tLine1.getActionMap().put("goRight", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (loose) {
                    return;
                }
                if (posA[1] + 1 > 9) {
                    return;
                }
                if (posB[1] + 1 > 9) {
                    return;
                }
                if (posC[1] + 1 > 9) {
                    return;
                }
                removeFigure(posA, posB, posC, new int[]{currFigureY, currFigureX});
                try {
                    if (iconsList.contains(positions[posA[0]][posA[1]+1].getIcon())) {
                        return;
                    }
                } catch (Exception e2) {
                    // TODO: handle exception
                }
                try {
                    if (iconsList.contains(positions[posB[0]][posB[1]+1].getIcon())) {
                        return;
                    }
                } catch (Exception e2) {
                    // TODO: handle exception
                }
                try {
                    if (iconsList.contains(positions[posC[0]][posC[1]+1].getIcon())) {
                        return;
                    }
                } catch (Exception e2) {
                    // TODO: handle exception
                }
                try {
                    if (iconsList.contains(positions[currFigureY][currFigureX+1].getIcon())) {
                        return;
                    }
                } catch (Exception e2) {
                    // TODO: handle exception
                }
                posA[1]++;
                posB[1]++;
                posC[1]++;
                currFigureX++;
            }
        });

        tLine1.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "goLeft");
        tLine1.getActionMap().put("goLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (loose) {
                    return;
                }
                if (posA[1] - 1 < 0) {
                    return;
                }
                if (posB[1] - 1 < 0) {
                    return;
                }
                if (posC[1] - 1 < 0) {
                    return;
                }
                removeFigure(posA, posB, posC, new int[]{currFigureY, currFigureX});
                try {
                    if (iconsList.contains(positions[posA[0]][posA[1]-1].getIcon())) {
                        return;
                    }
                } catch (Exception e2) {
                    // TODO: handle exception
                }
                try {
                    if (iconsList.contains(positions[posB[0]][posB[1]-1].getIcon())) {
                        return;
                    }
                } catch (Exception e2) {
                    // TODO: handle exception
                }
                try {
                    if (iconsList.contains(positions[posC[0]][posC[1]-1].getIcon())) {
                        return;
                    }
                } catch (Exception e2) {
                    // TODO: handle exception
                }
                try {
                    if (iconsList.contains(positions[currFigureY][currFigureX-1].getIcon())) {
                        return;
                    }
                } catch (Exception e2) {
                    // TODO: handle exception
                }
                posA[1]--;
                posB[1]--;
                posC[1]--;
                currFigureX--;
            }
        });

        tLine1.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "goDown");
        tLine1.getActionMap().put("goDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (loose) {
                    return;
                }
                removeFigure(posA, posB, posC, new int[]{currFigureY, currFigureX});
                if (posA[0] == 16 || posB[0] == 16 || posC[0] == 16) {
                    confirmPlaceFigure();
                    return;
                } 
                try {
                    if (iconsList.contains(positions[posA[0]+1][posA[1]].getIcon())) {
                        confirmPlaceFigure();
                        return;
                    }
                } catch (Exception e2) {
                    // TODO: handle exception
                }
                try {
                    if (iconsList.contains(positions[posB[0]+1][posB[1]].getIcon())) {
                        confirmPlaceFigure();
                        return;
                    }
                } catch (Exception e2) {
                    // TODO: handle exception
                }
                try {
                    if (iconsList.contains(positions[posC[0]+1][posC[1]].getIcon())) {
                        confirmPlaceFigure();
                        return;
                    }
                } catch (Exception e2) {
                    // TODO: handle exception
                }
                try {
                    if (iconsList.contains(positions[currFigureY+1][currFigureX].getIcon())) {
                        confirmPlaceFigure();
                        return;
                    }
                } catch (Exception e2) {
                    // TODO: handle exception
                }
                posA[0]++;
                posB[0]++;
                posC[0]++;
                currFigureY++;
            }
        });

        

        this.add(tLine1);
        this.add(tLine2);

        this.add(stockedPanel);

        this.add(nfLine1);
        this.add(nfLine2);
        this.add(nfLine3);
        this.add(nfLine4);

        this.add(lost);
        this.add(lost2);

        this.add(score);
        this.add(bestScoreLabel);

        this.add(positionPanel);

        this.repaint();
        this.revalidate();
    }

    public void resetGame() {
        if (currScore > bestScore) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathK))){
                writer.write(currScore);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        currScore = 0;
        lost.setVisible(false);
        lost2.setVisible(false);
        for (int i = 0; i < positions.length; i++) {
            for (int j = 0; j < positions[i].length; j++) {
                positions[i][j].setIcon(null);
            }
        }
        for (int i = 0; i < stockedFigure.length; i++) {
            for (int j = 0; j < stockedFigure[i].length; j++) {
                stockedFigure[i][j].setIcon(null);
            }
        }
        positions[posA[0]][posA[1]].setIcon(null);
        positions[posB[0]][posB[1]].setIcon(null);
        positions[posC[0]][posC[1]].setIcon(null);
        positions[currFigureY][currFigureX].setIcon(null);

        loose = false;

        Ifigure = random.nextInt((7 - 1) + 1) + 1;

        StockedFigure = Ifigure;

        testFigure();

        fpsTimer.start();
        mainGame.start();
    }

    public void removeLine() {
        int i = positions.length-1;
        for ( ; i >= 0; i--) {
            if (confirmedFigures[i][0] && confirmedFigures[i][1] && confirmedFigures[i][2] && confirmedFigures[i][3] && 
                confirmedFigures[i][4] && confirmedFigures[i][5] && confirmedFigures[i][6] && confirmedFigures[i][7] && 
                confirmedFigures[i][8] && confirmedFigures[i][9]) {
                currScore += 25;
                for (int j = 0; j < positions[i].length; j++) {
                    positions[i][j].setIcon(null);
                    confirmedFigures[i][j] = false;
                }
                System.out.println(i);
                break;
            }
        }
        for ( ; i >= 0; i--) {
            for (int j = 0; j < positions[i].length; j++) {
                try {
                    if (confirmedFigures[i][j]) {
                        Icon currentIcon = positions[i][j].getIcon();
                        positions[i][j].setIcon(null);
                        confirmedFigures[i][j] = false;
                        positions[i+1][j].setIcon(currentIcon);
                        confirmedFigures[i+1][j] = true;
                    }
                } catch (Exception e) {
                    
                }
            }
        }

        for (int j = 0; j < positions.length; j++) {
           for (int j2 = 0; j2 < positions[j].length; j2++) {
              if (posA[0] != j && posB[0] != j && posC[0] != j && posA[1] != j2 && posB[1] != j2 && posC[1] != j2) {
                 try {
                    if (iconsList.contains(positions[j][j2].getIcon())) {
                        confirmedFigures[j][j2] = true;
                    }
                 } catch (Exception e) {
                    confirmedFigures[j][j2] = false;
                 }
              }
           } 
        }
    }

    /* Board: . . . . . . . . . .  ->  . . . . . . . . . .  ->  . . . . . . . . . .  ->  . . . . . . . . . .
     *        . . . . . . . . . .      . . . . . . . . . .      . . . . . . . . . .      . . . . . . . . . .
     *        . . . . . . . . . .      . . . . . . . . . .      . . . . . . . . . .      . . . . . . . . . .
     *        . . . . A B . . . .      . . . . . . A . . .      . . . . . . . . . .      . . . . . C . . . .
     *        . . . . . 0 C . . .      . . . . . 0 B . . .      . . . . C 0 . . . .      . . . . B 0 . . . .
     *        . . . . . . . . . .      . . . . . C . . . .      . . . . . B A . . .      . . . . A . . . . .
     *        . . . . . . . . . .      . . . . . . . . . .      . . . . . . . . . .      . . . . . . . . . .
     *        A=                       B=                       C=  
     *        A places:                B places:                C places:
     *        . . . . . . . . . .      . . . . . . . . . .      . . . . . . . . . .      . . . . . . . . . .
     *        . . . . . . . . . .      . . . . . . . . . .      . . . . . . . . . .      . . . . . . . . . .
     *        . . . ? . ? . . . .      . . . . ? . . . . .      . . . . ? . . . . .      . . . . . . . . . .
     *        . . . . 0 . . . . .      . . . ? 0 ? . . . .      . . . ? 0 ? . . . .      . . . . . . . . . .
     *        . . . ? . ? . . . .      . . . . ? . . . . .      . . . . ? . . . . .      . . . . . . . . . .
     *        . . . . . . . . . .      . . . . . . . . . .      . . . . . . . . . .      . . . . . . . . . .
     *        . . . . . . . . . .      . . . . . . . . . .      . . . . . . . . . .      . . . . . . . . . .
     *        . . . . . . . . . .      . . . . . . . . . .      . . . . . . . . . .      . . . . . . . . . .
     *        . . . . . . . . . .      . . . . . . . . . .      . . . . . . . . . .      . . . . . . . . . .
     *        . . . . . . . . . .      . . . . . . . . . .      . . . . . . . . . .      . . . . . . . . . .
     *        . . . . . . . . . .      . . . . . . . . . .      . . . . . . . . . .      . . . . . . . . . .
     */

    public void rotateFigure() {
        removeFigure(posA, posB, posC, new int[]{currFigureY, currFigureX});
        switch (currFigure) {
            case 1:
                if (posA[0] == currFigureY-1 && posA[1] == currFigureX+1) {
                    posA[0] += 2;
                } else if (posA[0] == currFigureY+1 && posA[1] == currFigureX+1) {
                    posA[1] -= 2;
                } else if (posA[0] == currFigureY+1 && posA[1] == currFigureX-1) {
                    posA[0] -= 2;
                } else {
                    posA[1] += 2;
                }

                if (posB[0] == currFigureY && posB[1] == currFigureX-1) {
                    posB[0] -= 1;
                    posB[1] += 1;
                } else if (posB[0] == currFigureY-1 && posB[1] == currFigureX) {
                    posB[0] += 1;
                    posB[1] += 1;
                } else if (posB[0] == currFigureY && posB[1] == currFigureX+1) {
                    posB[0] += 1;
                    posB[1] -= 1;
                } else {
                    posB[0] -= 1;
                    posB[1] -= 1;
                }

                if (posC[0] == currFigureY && posC[1] == currFigureX-1) {
                    posC[0] -= 1;
                    posC[1] += 1;
                } else if (posC[0] == currFigureY-1 && posC[1] == currFigureX) {
                    posC[0] += 1;
                    posC[1] += 1;
                } else if (posC[0] == currFigureY && posC[1] == currFigureX+1) {
                    posC[0] += 1;
                    posC[1] -= 1;
                } else {
                    posC[0] -= 1;
                    posC[1] -= 1;
                }
                break;
        
            case 2:
                if (posA[0] == currFigureY && posA[1] == currFigureX+2) {
                    posA[0] += 2;
                    posA[1] -= 2;
                } else if (posA[0] == currFigureY+2 && posA[1] == currFigureX) {
                    posA[0] -= 2;
                    posA[1] -= 2;
                } else if (posA[0] == currFigureY && posA[1] == currFigureX-2) {
                    posA[0] -= 2;
                    posA[1] += 2;
                } else {
                    posA[0] += 2;
                    posA[1] += 2;
                }

                if (posB[0] == currFigureY && posB[1] == currFigureX-1) {
                    posB[0] -= 1;
                    posB[1] += 1;
                } else if (posB[0] == currFigureY-1 && posB[1] == currFigureX) {
                    posB[0] += 1;
                    posB[1] += 1;
                } else if (posB[0] == currFigureY && posB[1] == currFigureX+1) {
                    posB[0] += 1;
                    posB[1] -= 1;
                } else {
                    posB[0] -= 1;
                    posB[1] -= 1;
                }

                if (posC[0] == currFigureY && posC[1] == currFigureX-1) {
                    posC[0] -= 1;
                    posC[1] += 1;
                } else if (posC[0] == currFigureY-1 && posC[1] == currFigureX) {
                    posC[0] += 1;
                    posC[1] += 1;
                } else if (posC[0] == currFigureY && posC[1] == currFigureX+1) {
                    posC[0] += 1;
                    posC[1] -= 1;
                } else {
                    posC[0] -= 1;
                    posC[1] -= 1;
                }
                break;
            
            case 3:
                if (posA[0] == currFigureY-1 && posA[1] == currFigureX+1) {
                    posA[0] += 2;
                } else if (posA[0] == currFigureY+1 && posA[1] == currFigureX+1) {
                    posA[1] -= 2;
                } else if (posA[0] == currFigureY+1 && posA[1] == currFigureX-1) {
                    posA[0] -= 2;
                } else {
                    posA[1] += 2;
                }

                if (posB[0] == currFigureY && posB[1] == currFigureX-1) {
                    posB[0] -= 1;
                    posB[1] += 1;
                } else if (posB[0] == currFigureY-1 && posB[1] == currFigureX) {
                    posB[0] += 1;
                    posB[1] += 1;
                } else if (posB[0] == currFigureY && posB[1] == currFigureX+1) {
                    posB[0] += 1;
                    posB[1] -= 1;
                } else {
                    posB[0] -= 1;
                    posB[1] -= 1;
                }

                if (posC[0] == currFigureY && posC[1] == currFigureX-1) {
                    posC[0] -= 1;
                    posC[1] += 1;
                } else if (posC[0] == currFigureY-1 && posC[1] == currFigureX) {
                    posC[0] += 1;
                    posC[1] += 1;
                } else if (posC[0] == currFigureY && posC[1] == currFigureX+1) {
                    posC[0] += 1;
                    posC[1] -= 1;
                } else {
                    posC[0] -= 1;
                    posC[1] -= 1;
                }
                break;
        
            case 4:
                if (posA[0] == currFigureY && posA[1] == currFigureX-1) {
                    posA[0] -= 1;
                    posA[1] += 1;
                } else if (posA[0] == currFigureY-1 && posA[1] == currFigureX) {
                    posA[0] += 1;
                    posA[1] += 1;
                } else if (posA[0] == currFigureY && posA[1] == currFigureX+1) {
                    posA[0] += 1;
                    posA[1] -= 1;
                } else {
                    posA[0] -= 1;
                    posA[1] -= 1;
                }

                if (posB[0] == currFigureY && posB[1] == currFigureX-1) {
                    posB[0] -= 1;
                    posB[1] += 1;
                } else if (posB[0] == currFigureY-1 && posB[1] == currFigureX) {
                    posB[0] += 1;
                    posB[1] += 1;
                } else if (posB[0] == currFigureY && posB[1] == currFigureX+1) {
                    posB[0] += 1;
                    posB[1] -= 1;
                } else {
                    posB[0] -= 1;
                    posB[1] -= 1;
                }

                if (posC[0] == currFigureY && posC[1] == currFigureX-1) {
                    posC[0] -= 1;
                    posC[1] += 1;
                } else if (posC[0] == currFigureY-1 && posC[1] == currFigureX) {
                    posC[0] += 1;
                    posC[1] += 1;
                } else if (posC[0] == currFigureY && posC[1] == currFigureX+1) {
                    posC[0] += 1;
                    posC[1] -= 1;
                } else {
                    posC[0] -= 1;
                    posC[1] -= 1;
                }
                break;

            case 5:
                if (posA[0] == currFigureY-1 && posA[1] == currFigureX-1) {
                    posA[1] += 2;
                } else if (posA[0] == currFigureY-1 && posA[1] == currFigureX+1) {
                    posA[0] += 2;
                } else if (posA[0] == currFigureY+1 && posA[1] == currFigureX+1) {
                    posA[1] -= 2;
                } else {
                    posA[0] -= 2;
                }

                if (posB[0] == currFigureY && posB[1] == currFigureX-1) {
                    posB[0] -= 1;
                    posB[1] += 1;
                } else if (posB[0] == currFigureY-1 && posB[1] == currFigureX) {
                    posB[0] += 1;
                    posB[1] += 1;
                } else if (posB[0] == currFigureY && posB[1] == currFigureX+1) {
                    posB[0] += 1;
                    posB[1] -= 1;
                } else {
                    posB[0] -= 1;
                    posB[1] -= 1;
                }

                if (posC[0] == currFigureY && posC[1] == currFigureX-1) {
                    posC[0] -= 1;
                    posC[1] += 1;
                } else if (posC[0] == currFigureY-1 && posC[1] == currFigureX) {
                    posC[0] += 1;
                    posC[1] += 1;
                } else if (posC[0] == currFigureY && posC[1] == currFigureX+1) {
                    posC[0] += 1;
                    posC[1] -= 1;
                } else {
                    posC[0] -= 1;
                    posC[1] -= 1;
                }
                break;
        
            case 7:
                if (posA[0] == currFigureY-1 && posA[1] == currFigureX-1) {
                    posA[1] += 2;
                } else if (posA[0] == currFigureY-1 && posA[1] == currFigureX+1) {
                    posA[0] += 2;
                } else if (posA[0] == currFigureY+1 && posA[1] == currFigureX+1) {
                    posA[1] -= 2;
                } else {
                    posA[0] -= 2;
                }

                if (posB[0] == currFigureY && posB[1] == currFigureX-1) {
                    posB[0] -= 1;
                    posB[1] += 1;
                } else if (posB[0] == currFigureY-1 && posB[1] == currFigureX) {
                    posB[0] += 1;
                    posB[1] += 1;
                } else if (posB[0] == currFigureY && posB[1] == currFigureX+1) {
                    posB[0] += 1;
                    posB[1] -= 1;
                } else {
                    posB[0] -= 1;
                    posB[1] -= 1;
                }

                if (posC[0] == currFigureY && posC[1] == currFigureX-1) {
                    posC[0] -= 1;
                    posC[1] += 1;
                } else if (posC[0] == currFigureY-1 && posC[1] == currFigureX) {
                    posC[0] += 1;
                    posC[1] += 1;
                } else if (posC[0] == currFigureY && posC[1] == currFigureX+1) {
                    posC[0] += 1;
                    posC[1] -= 1;
                } else {
                    posC[0] -= 1;
                    posC[1] -= 1;
                }
                break;
        }

        if (posA[1] > 9) {
            posA = stockedPosA.clone();
            posB = stockedPosB.clone();
            posC = stockedPosC.clone();
        }
        if (posB[1] > 9) {
            posA = stockedPosA.clone();
            posB = stockedPosB.clone();
            posC = stockedPosC.clone();
        }
        if (posC[1] > 9) {
            posA = stockedPosA.clone();
            posB = stockedPosB.clone();
            posC = stockedPosC.clone();
        }
        if (posA[1] < 0) {
            posA = stockedPosA.clone();
            posB = stockedPosB.clone();
            posC = stockedPosC.clone();
        }
        if (posB[1] < 0) {
            posA = stockedPosA.clone();
            posB = stockedPosB.clone();
            posC = stockedPosC.clone();
        }
        if (posC[1] < 0) {
            posA = stockedPosA.clone();
            posB = stockedPosB.clone();
            posC = stockedPosC.clone();
        }
        if (posA[0] > 16 ) {
            posA = stockedPosA.clone();
            posB = stockedPosB.clone();
            posC = stockedPosC.clone();
        }
        if (posB[0] > 16 ) {
            posA = stockedPosA.clone();
            posB = stockedPosB.clone();
            posC = stockedPosC.clone();
        }
        if (posC[0] > 16 ) {
            posA = stockedPosA.clone();
            posB = stockedPosB.clone();
            posC = stockedPosC.clone();
        }
        try {
            if (iconsList.contains(positions[posA[0]][posA[1]].getIcon())) {
                posA = stockedPosA.clone();
                posB = stockedPosB.clone();
                posC = stockedPosC.clone();
                return;
            }
        } catch (Exception e2) {
            // TODO: handle exception
        }
        try {
            if (iconsList.contains(positions[posB[0]][posB[1]].getIcon())) {
                posA = stockedPosA.clone();
                posB = stockedPosB.clone();
                posC = stockedPosC.clone();
                return;
            }
        } catch (Exception e2) {
            // TODO: handle exception
        }
        try {
            if (iconsList.contains(positions[posC[0]][posC[1]].getIcon())) {
                posA = stockedPosA.clone();
                posB = stockedPosB.clone();
                posC = stockedPosC.clone();
                return;
            }
        } catch (Exception e2) {
            // TODO: handle exception
        }
    }

    public void testFigure() {
        drawFigure(4, 2, StockedFigure);

        Ifigure = random.nextInt((7 - 1) + 1) + 1;

        StockedFigure = Ifigure;

        drawStockedFigure(StockedFigure);
        
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Unimplemented method 'actionPerformed'");
    }
}
