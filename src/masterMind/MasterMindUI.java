package masterMind;

import main.BeliefBase;
import main.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MasterMindUI extends JPanel implements KeyListener {
    private JFrame mainFrame;

    private MasterMindGame game;

    private Color[] colors = { null, Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE, Color.PINK, Color.DARK_GRAY, Color.BLACK, Color.WHITE };
    private JButton[] buttons;
    private JButton AIMoveButton;

    private int screenWidth;
    private int screenHeight;

    private int[] guess = new int[MasterMindGame.CODE_LENGTH];
    private int guessNumber = 0;
    private boolean showCode = false;
    private MasterMindAI ai;
    private UI ui;

    public MasterMindUI(MasterMindGame game, BeliefBase beliefBase, UI ui) {
        this.game = game;

        ai = new MasterMindAI(beliefBase);

        this.ui = ui;

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = (int) screenSize.getWidth();
        screenHeight = (int) screenSize.getHeight();

        JPanel buttonPanel = new JPanel(new GridLayout(1,0));
        buttonPanel.setPreferredSize(new Dimension(screenWidth / 4, screenWidth / 32));
        buttons = new JButton[MasterMindGame.NUMBER_OF_COLORS];
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new JButton();
            buttons[i].setBackground(colors[i+1]);
            buttons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (game.getGameOver()) {
                        return;
                    }
                    guess[guessNumber++] = getMousePosition().x * MasterMindGame.NUMBER_OF_COLORS / (screenWidth / 4) + 1;
                    if (guessNumber == MasterMindGame.CODE_LENGTH) {
                        game.makeGuess(guess);
                        guess = new int[MasterMindGame.CODE_LENGTH];
                        guessNumber = 0;
                    }
                    repaint();
                }
            });
            buttonPanel.add(buttons[i]);
        }

        AIMoveButton = new JButton("AI move");
        AIMoveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!game.getGameOver()) {
                    int[] move = ai.makeMove(game.getTurn() == MasterMindGame.NUMBER_OF_GUESSES-1);
                    int[] feedback = game.makeGuess(move);
                    if (game.getTurn() != MasterMindGame.NUMBER_OF_GUESSES) {
                        ai.updateBeliefBase(move, feedback);
                        ui.updateList();
                    }
                    repaint();
                }
            }
        });
        add(AIMoveButton);

        setPreferredSize(new Dimension(screenWidth / 4, screenHeight * 3 / 4));
        setBackground(new Color(255, 150, 50));
        setLayout(new BorderLayout());
        add(buttonPanel, BorderLayout.SOUTH);

        mainFrame = new JFrame();
        mainFrame.setFocusable(true);
        setFocusable(true);
        mainFrame.addKeyListener(this);
        mainFrame.setTitle("Master Mind");
        mainFrame.add(this);
        mainFrame.pack();
        mainFrame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}

            @Override
            public void windowClosing(WindowEvent e) {
                beliefBase.reset();
                ui.updateList();
                ui.repaint();
            }

            @Override
            public void windowClosed(WindowEvent e) {}
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });
        //mainFrame.setLocationByPlatform(true);
        //mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2));

        int xGap = screenWidth / 38;
        int yGap = screenHeight / 21;

        int cellWidth = screenWidth / (7 * MasterMindGame.CODE_LENGTH);
        int cellHeight = screenHeight / (5 * MasterMindGame.NUMBER_OF_GUESSES);

        int feedbackRadius = cellHeight/2;

        // Draw code if all guesses have been used; otherwise draw question marks
        int[] code = game.getCode();
        for (int i = 0; i < MasterMindGame.CODE_LENGTH; i++) {
            int x = xGap/2 + i*xGap + i*xGap*3/4;
            int y = yGap;
            if (game.getGameOver() || showCode) {
                g2d.setColor(colors[code[i]]);
                g2d.fillRect(x, y, cellWidth, cellHeight);
            } else {
                g2d.setColor(Color.BLACK);
                g2d.fillRect(x, y, cellWidth, cellHeight);

                // Draw question mark
                g2d.setColor(Color.WHITE);
                int textHeight = cellHeight - cellHeight / 5;
                g2d.setFont(new Font("Sans Serif", Font.BOLD, textHeight));
                String text = "?";
                int textWidth = g2d.getFontMetrics().stringWidth(text);
                g2d.drawString(text, x + cellWidth/2 - textWidth/2, y + cellHeight/3 + textHeight/2);
            }
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x, y, cellWidth, cellHeight);
        }

        // Draw color cells
        int[][] colorCells = game.getGuessHistory();
        for (int i = 0; i < colorCells.length; i++) {
            for (int j = 0; j < colorCells[i].length; j++) {
                int x = xGap/2 + j*xGap + j*xGap*3/4;
                int y = 3*yGap + i*yGap;
                if (colorCells[colorCells.length-i-1][j] != MasterMindGame.Color.EMPTY.ordinal()) {
                    g2d.setColor(colors[colorCells[colorCells.length-i-1][j]]);
                    g2d.fillRect(x, y, cellWidth, cellHeight);
                }
                g2d.setColor(Color.BLACK);
                g2d.drawRect(x, y, cellWidth, cellHeight);
            }
        }

        // Draw feedback
        int[][] feedback = game.getFeedbackHistory();
        for (int i = 0; i < feedback.length; i++) {
            for (int j = 0; j < feedback[i].length; j++) {
                int x = xGap * 8 + (j%2)*xGap/3;
                int y = 3*yGap + i*yGap + (j/2)*yGap/3;
                if (feedback[feedback.length-i-1][j] != MasterMindGame.Color.EMPTY.ordinal()) {
                    g.setColor(colors[feedback[feedback.length-i-1][j]]);
                    g.fillOval(x, y, feedbackRadius, feedbackRadius);
                }
                g.setColor(Color.BLACK);
                g.drawOval(x, y, feedbackRadius, feedbackRadius);
            }
        }

        // Draw guess squares
        g2d.setStroke(new BasicStroke(3));
        for (int i = 0; i < MasterMindGame.CODE_LENGTH; i++) {
            int x = xGap/2 + i*xGap + i*xGap*3/4;
            int y = yGap*13 + yGap/2;
            if (guess[i] != 0) {
                g2d.setColor(colors[guess[i]]);
                g2d.fillRect(x, y, cellWidth, cellHeight);
            }
            g2d.setColor(Color.BLACK);
            g.drawRect(x, y, cellWidth, cellHeight);
        }

        // Draw AI move button
        int i = MasterMindGame.CODE_LENGTH;
        AIMoveButton.setBounds(xGap/2 + i*xGap + i*xGap*3/4, yGap*13 + yGap/2, cellWidth * 3 / 2, cellHeight * 3 / 2);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!game.getGameOver() && e.getKeyCode() == KeyEvent.VK_A) {
            int[] move = ai.makeMove(game.getTurn() == MasterMindGame.NUMBER_OF_GUESSES-1);
            int[] feedback = game.makeGuess(move);
            if (game.getTurn() != MasterMindGame.NUMBER_OF_GUESSES) {
                ai.updateBeliefBase(move, feedback);
                ui.updateList();
            }
            repaint();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    public void close() {
        mainFrame.dispatchEvent(new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING));
    }
}

