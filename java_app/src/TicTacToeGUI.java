import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

public class TicTacToeGUI extends JFrame implements ActionListener {
    private final JButton[][] buttons = new JButton[3][3];
    private boolean isPlayerTurn = true; // true = Player (X), false = Bot (O)
    private boolean gameOver = false;
    private final JLabel statusLabel = new JLabel("Your Turn (X)", SwingConstants.CENTER);
    private final Random random = new Random();

    public TicTacToeGUI() {
        setTitle("TicTacX - Classic 3x3");
        setSize(400, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header status bar
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(statusLabel, BorderLayout.NORTH);

        // 3x3 Grid panel
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(3, 3, 5, 5));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                buttons[row][col] = new JButton("");
                buttons[row][col].setFont(new Font("SansSerif", Font.BOLD, 36));
                buttons[row][col].setFocusPainted(false);
                buttons[row][col].addActionListener(this);
                gridPanel.add(buttons[row][col]);
            }
        }
        add(gridPanel, BorderLayout.CENTER);
        setLocationRelativeTo(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver || !isPlayerTurn) return;

        JButton clickedButton = (JButton) e.getSource();
        if (!clickedButton.getText().equals("")) return;

        // Player Move
        clickedButton.setText("X");
        clickedButton.setForeground(Color.BLUE);

        if (checkWin("X")) {
            endGame("You Win!");
            DatabaseManager.saveMatch("Human Player", "3x3 vs Bot", "Win");
            return;
        }

        if (isBoardFull()) {
            endGame("Draw!");
            DatabaseManager.saveMatch("Human Player", "3x3 vs Bot", "Draw");
            return;
        }

        // Trigger Bot Move
        isPlayerTurn = false;
        statusLabel.setText("Bot is thinking...");
        
        Timer timer = new Timer(400, event -> {
            makeBotMove();
            if (checkWin("O")) {
                endGame("Bot Wins!");
                DatabaseManager.saveMatch("Human Player", "3x3 vs Bot", "Loss");
            } else if (isBoardFull()) {
                endGame("Draw!");
                DatabaseManager.saveMatch("Human Player", "3x3 vs Bot", "Draw");
            } else {
                isPlayerTurn = true;
                statusLabel.setText("Your Turn (X)");
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void makeBotMove() {
        ArrayList<Point> emptyCells = new ArrayList<>();
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (buttons[r][c].getText().equals("")) {
                    emptyCells.add(new Point(r, c));
                }
            }
        }

        if (!emptyCells.isEmpty()) {
            Point choice = emptyCells.get(random.nextInt(emptyCells.size()));
            buttons[choice.x][choice.y].setText("O");
            buttons[choice.x][choice.y].setForeground(Color.RED);
        }
    }

    private boolean checkWin(String player) {
        // Rows and Columns
        for (int i = 0; i < 3; i++) {
            if (buttons[i][0].getText().equals(player) &&
                buttons[i][1].getText().equals(player) &&
                buttons[i][2].getText().equals(player)) return true;

            if (buttons[0][i].getText().equals(player) &&
                buttons[1][i].getText().equals(player) &&
                buttons[2][i].getText().equals(player)) return true;
        }
        // Diagonals
        if (buttons[0][0].getText().equals(player) &&
            buttons[1][1].getText().equals(player) &&
            buttons[2][2].getText().equals(player)) return true;

        if (buttons[0][2].getText().equals(player) &&
            buttons[1][1].getText().equals(player) &&
            buttons[2][0].getText().equals(player)) return true;

        return false;
    }

    private boolean isBoardFull() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (buttons[r][c].getText().equals("")) return false;
            }
        }
        return true;
    }

    private void endGame(String message) {
        gameOver = true;
        statusLabel.setText(message);
        JOptionPane.showMessageDialog(this, "Game Over: " + message);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TicTacToeGUI().setVisible(true);
        });
    }
}