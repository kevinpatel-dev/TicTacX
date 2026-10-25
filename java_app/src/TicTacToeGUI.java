import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

public class TicTacToeGUI extends JFrame implements ActionListener {
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainContainer = new JPanel(cardLayout);
    
    // UI Colors (Dark Theme)
    private final Color BG_DARK = new Color(15, 23, 42);      // slate-900
    private final Color BG_CARD = new Color(30, 41, 59);      // slate-800
    private final Color TEXT_LIGHT = new Color(248, 250, 252); // slate-50
    private final Color COLOR_X = new Color(56, 189, 248);    // sky-400
    private final Color COLOR_O = new Color(244, 63, 94);     // rose-500
    private final Color COLOR_WIN = new Color(34, 197, 94);   // green-500

    private JTextField p1NameField = new JTextField("Kevin");
    private JTextField p2NameField = new JTextField("Bot");
    private JComboBox<String> gridSizeBox = new JComboBox<>(new String[]{"3x3", "4x4", "5x5"});
    private JComboBox<String> opponentBox = new JComboBox<>(new String[]{"vs AI", "vs Friend"});
    private JComboBox<String> difficultyBox = new JComboBox<>(new String[]{"Easy (Random)", "Medium (Block)"});
    
    private JPanel gamePanel = new JPanel(new BorderLayout());
    private JLabel statusLabel = new JLabel("", SwingConstants.CENTER);
    private JButton[][] buttons;
    private int size = 3;
    private boolean isPvP = false;
    private boolean isPlayer1Turn = true; 
    private boolean gameOver = false;
    private String p1Name, p2Name, difficulty;
    private final Random random = new Random();

    public TicTacToeGUI() {
        setTitle("TicTacX");
        setSize(500, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);

        mainContainer.add(createMenuPanel(), "Menu");
        mainContainer.add(gamePanel, "Game");
        add(mainContainer);
        cardLayout.show(mainContainer, "Menu");
    }

    private void styleInput(JComponent comp) {
        comp.setBackground(BG_CARD);
        comp.setForeground(TEXT_LIGHT);
        comp.setFont(new Font("SansSerif", Font.PLAIN, 14));
        if (comp instanceof JTextField) {
            ((JTextField) comp).setCaretColor(TEXT_LIGHT);
            comp.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(51, 65, 85)), 
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
            ));
        }
    }

    private JPanel createMenuPanel() {
        JPanel menu = new JPanel(new GridBagLayout());
        menu.setBackground(BG_DARK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("TicTacX", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 38));
        title.setForeground(COLOR_X);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        menu.add(title, gbc);

        JLabel[] labels = {
            new JLabel("Player 1 Name:"), new JLabel("Opponent:"),
            new JLabel("Player 2 Name:"), new JLabel("Grid Size:"), new JLabel("AI Difficulty:")
        };

        JComponent[] inputs = { p1NameField, opponentBox, p2NameField, gridSizeBox, difficultyBox };

        gbc.gridwidth = 1;
        for (int i = 0; i < labels.length; i++) {
            labels[i].setForeground(TEXT_LIGHT);
            labels[i].setFont(new Font("SansSerif", Font.BOLD, 14));
            styleInput(inputs[i]);
            gbc.gridx = 0; gbc.gridy++; menu.add(labels[i], gbc);
            gbc.gridx = 1; menu.add(inputs[i], gbc);
        }

        p2NameField.setEnabled(false);
        opponentBox.addActionListener(e -> {
            boolean isFriend = opponentBox.getSelectedIndex() == 1;
            p2NameField.setEnabled(isFriend);
            difficultyBox.setEnabled(!isFriend);
            p2NameField.setText(isFriend ? "Player 2" : "Bot");
        });

        JButton startBtn = new JButton("Start Game");
        startBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        startBtn.setBackground(COLOR_X);
        startBtn.setForeground(BG_DARK);
        startBtn.setFocusPainted(false);
        startBtn.addActionListener(e -> initGame());
        
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.insets = new Insets(30, 12, 12, 12);
        menu.add(startBtn, gbc);
        return menu;
    }

    private void initGame() {
        p1Name = p1NameField.getText().trim();
        p2Name = p2NameField.getText().trim();
        isPvP = opponentBox.getSelectedIndex() == 1;
        size = gridSizeBox.getSelectedIndex() + 3; 
        difficulty = (String) difficultyBox.getSelectedItem();
        
        isPlayer1Turn = true;
        gameOver = false;
        
        gamePanel.removeAll();
        gamePanel.setBackground(BG_DARK);
        
        statusLabel.setText(p1Name + "'s Turn (X)");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        statusLabel.setForeground(TEXT_LIGHT);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        gamePanel.add(statusLabel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(size, size, 8, 8));
        gridPanel.setBackground(BG_DARK);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        buttons = new JButton[size][size];
        Font buttonFont = new Font("SansSerif", Font.BOLD, size == 3 ? 56 : (size == 4 ? 42 : 32));

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                buttons[r][c] = new JButton("");
                buttons[r][c].setFont(buttonFont);
                buttons[r][c].setFocusPainted(false);
                buttons[r][c].setBackground(BG_CARD);
                buttons[r][c].setBorder(new LineBorder(new Color(51, 65, 85), 2));
                buttons[r][c].addActionListener(this);
                gridPanel.add(buttons[r][c]);
            }
        }
        gamePanel.add(gridPanel, BorderLayout.CENTER);

        JButton backBtn = new JButton("Abandon & Return to Menu");
        backBtn.setBackground(new Color(51, 65, 85));
        backBtn.setForeground(TEXT_LIGHT);
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> cardLayout.show(mainContainer, "Menu"));
        gamePanel.add(backBtn, BorderLayout.SOUTH);

        gamePanel.revalidate();
        gamePanel.repaint();
        cardLayout.show(mainContainer, "Game");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver || (!isPlayer1Turn && !isPvP)) return;

        JButton clickedButton = (JButton) e.getSource();
        if (!clickedButton.getText().equals("")) return; 

        processMove(clickedButton, isPlayer1Turn ? "X" : "O", isPlayer1Turn ? COLOR_X : COLOR_O);

        if (checkWin(isPlayer1Turn ? "X" : "O", true)) {
            String winner = isPlayer1Turn ? p1Name : p2Name;
            endGame(winner + " Wins!");
            logToDatabase(isPlayer1Turn ? "P1 Win" : (isPvP ? "P2 Win" : "Bot Win"));
            return;
        }

        if (isBoardFull()) {
            endGame("It's a Draw!");
            logToDatabase("Draw");
            return;
        }

        isPlayer1Turn = !isPlayer1Turn;

        if (isPvP) {
            statusLabel.setText((isPlayer1Turn ? p1Name + " (X)" : p2Name + " (O)") + "'s Turn");
        } else if (!isPlayer1Turn) {
            statusLabel.setText("Bot is thinking...");
            Timer timer = new Timer(400, event -> {
                makeBotMove();
                if (checkWin("O", true)) {
                    endGame("Bot Wins!");
                    logToDatabase("Bot Win");
                } else if (isBoardFull()) {
                    endGame("It's a Draw!");
                    logToDatabase("Draw");
                } else {
                    isPlayer1Turn = true;
                    statusLabel.setText(p1Name + "'s Turn (X)");
                }
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    private void processMove(JButton btn, String text, Color color) {
        btn.setText(text);
        btn.setForeground(color);
    }

    private void makeBotMove() {
        ArrayList<Point> emptyCells = new ArrayList<>();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (buttons[r][c].getText().equals("")) emptyCells.add(new Point(r, c));
            }
        }
        if (emptyCells.isEmpty()) return;

        Point choice = null;
        if (difficulty.contains("Medium")) {
            for (Point p : emptyCells) {
                buttons[p.x][p.y].setText("X");
                if (checkWin("X", false)) choice = p;
                buttons[p.x][p.y].setText(""); 
                if (choice != null) break;
            }
        }

        if (choice == null) choice = emptyCells.get(random.nextInt(emptyCells.size()));
        processMove(buttons[choice.x][choice.y], "O", COLOR_O);
    }

    private boolean checkWin(String player, boolean highlight) {
        for (int i = 0; i < size; i++) {
            boolean rowWin = true, colWin = true;
            for (int j = 0; j < size; j++) {
                if (!buttons[i][j].getText().equals(player)) rowWin = false;
                if (!buttons[j][i].getText().equals(player)) colWin = false;
            }
            if (rowWin) {
                if (highlight) for (int j = 0; j < size; j++) buttons[i][j].setBackground(COLOR_WIN);
                return true;
            }
            if (colWin) {
                if (highlight) for (int j = 0; j < size; j++) buttons[j][i].setBackground(COLOR_WIN);
                return true;
            }
        }

        boolean diag1 = true, diag2 = true;
        for (int i = 0; i < size; i++) {
            if (!buttons[i][i].getText().equals(player)) diag1 = false;
            if (!buttons[i][size - 1 - i].getText().equals(player)) diag2 = false;
        }
        if (diag1) {
            if (highlight) for (int i = 0; i < size; i++) buttons[i][i].setBackground(COLOR_WIN);
            return true;
        }
        if (diag2) {
            if (highlight) for (int i = 0; i < size; i++) buttons[i][size - 1 - i].setBackground(COLOR_WIN);
            return true;
        }
        return false;
    }

    private boolean isBoardFull() {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (buttons[r][c].getText().equals("")) return false;
            }
        }
        return true;
    }

    private void endGame(String message) {
        gameOver = true;
        statusLabel.setText(message);
    }

    private void logToDatabase(String result) {
        String matchPlayers = isPvP ? p1Name + " vs " + p2Name : p1Name + " vs Bot";
        String gridMode = size + "x" + size + (isPvP ? " PvP" : " " + difficulty.split(" ")[0]);
        DatabaseManager.saveMatch(matchPlayers, gridMode, result);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TicTacToeGUI().setVisible(true));
    }
}