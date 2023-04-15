package masterMind;

import java.util.Random;

public class MasterMindGame {
    public static final int CODE_LENGTH = 4;
    public static final int NUMBER_OF_COLORS = 6;
    public static final int NUMBER_OF_GUESSES = 10;

    public enum Color { EMPTY, RED, YELLOW, GREEN, BLUE, PINK, GRAY, BLACK, WHITE };

    private int[] code;
    private int[] guess;
    private int turn;
    private int[][] guessHistory;
    private int[][] feedbackHistory;

    private boolean gameOver;

    private Random r = new Random();

    public MasterMindGame() {
        resetGame();
    }

    public void resetGame() {
        code = randomizeCode();
        guess = new int[CODE_LENGTH];
        turn = 0;
        guessHistory = new int[NUMBER_OF_GUESSES][CODE_LENGTH];
        feedbackHistory = new int[NUMBER_OF_GUESSES][CODE_LENGTH];
        gameOver = false;
    }

    private int[] randomizeCode() {
        int[] newCode = new int[CODE_LENGTH];
        for (int i = 0; i < CODE_LENGTH; i++) {
            newCode[i] = r.nextInt(NUMBER_OF_COLORS) + 1;
        }
        return newCode;
    }

    public int[] makeGuess(final int[] guess) {

        gameOver = true;
        for (int i = 0; i < CODE_LENGTH; i++) {
            if (guess[i] != code[i]) {
                gameOver = false;
            }
        }

        int[] feedback = new int[CODE_LENGTH];
        int correctPlacement = 0;
        int correctColors = 0;
        int[] guessCopy = guess.clone();
        int[] codeCopy = code.clone();

        // Check correct placements
        for (int i = 0; i < CODE_LENGTH; i++) {
            if (guessCopy[i] == codeCopy[i]) {
                guessCopy[i] = -1;
                codeCopy[i] = -2;
                correctPlacement++;
            }
        }

        // Check correct colors
        for (int i = 0; i < CODE_LENGTH; i++) {
            for (int j = 0; j < CODE_LENGTH; j++) {
                if (i != j && guessCopy[i] == codeCopy[j]) {
                    guessCopy[i] = -1;
                    codeCopy[j] = -2;
                    correctColors++;
                    break;
                }
            }
        }

        // Add correct placements to feedback
        for (int i = 0; i < correctPlacement; i++) {
            feedback[i] = Color.RED.ordinal();
        }

        // Add correct colors to feedback
        for (int i = correctPlacement; i < correctPlacement + correctColors; i++) {
            feedback[i] = Color.WHITE.ordinal();
        }

        // Update history values
        guessHistory[turn] = guess;
        feedbackHistory[turn] = feedback;
        turn++;

        if (turn == NUMBER_OF_GUESSES) {
            gameOver = true;
        }

        return feedback;
    }

    public int[] getCode() {
        return code;
    }

    public int[] getGuess() {
        return guess;
    }

    public int getTurn() {
        return turn;
    }

    public int[][] getGuessHistory() {
        return guessHistory;
    }

    public int[][] getFeedbackHistory() {
        return feedbackHistory;
    }

    public boolean getGameOver() {
        return gameOver;
    }
}
