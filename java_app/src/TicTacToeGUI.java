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
    
    // UI Colors (Black & Purple Theme)
    private final Color BG_BLACK = new Color(9, 9, 11);       // Deep black
    private final Color BG_PURPLE_DARK = new Color(26, 16, 37); // Very dark purple
    private final Color TEXT_LAVENDER = new Color(233, 213, 255); // Light purple text
    private final Color COLOR_P1 = new Color(192, 132, 252);  // Vibrant purple
    private final Color COLOR_P2 = new Color(244, 114, 182);  // Pink for contrast
    private final Color COLOR_WIN = new Color(107, 33, 168);  // Deep purple highlight

    // Menu Inputs
    private JTextField p1NameField = new JTextField("Kevin");
    private JTextField p1SymbolField = new JTextField("X"); // Default to X
    private JComboBox<String> opponentBox = new JComboBox<>(new String[]{"vs AI", "vs Friend"});
    private JTextField p2NameField = new JTextField("Bot");
    private JTextField p2SymbolField = new JTextField("O"); // Default to O
    private JComboBox<String> gridSizeBox = new JComboBox<>(new String[]{"3x3", "4x4", "5x5"});
    private JComboBox<String> difficultyBox = new JComboBox<>(new String[]{"Easy (Random)", "Medium (Block)"});
    
    // Game State
    private JPanel gamePanel = new JPanel(new BorderLayout());
    private JLabel statusLabel = new JLabel("", SwingConstants.CENTER);
    private JButton[][] buttons;
    private int[][] boardState; // 0 = empty, 1 = P1, 2 = P2/Bot
    private int size = 3;
    private boolean isPvP = false;
    private boolean isPlayer1Turn = true; 
    private boolean gameOver = false;
    private String p1Name, p2Name, p1Symbol, p2Symbol, difficulty;
    private final Random random = new Random();

    public TicTacToeGUI() {
        setTitle("TicTacX");
        setSize(550, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_BLACK);

        mainContainer.add(createMenuPanel(), "Menu");
        mainContainer.add(gamePanel, "Game");
        add(mainContainer);
        cardLayout.show(mainContainer, "Menu");
    }

    private void styleInput(JComponent comp) {
        comp.setBackground(BG_PURPLE_DARK);
        comp.setForeground(TEXT_LAVENDER);
        comp.setFont(new Font("SansSerif", Font.PLAIN, 15));
        if (comp instanceof JTextField) {
            ((JTextField) comp).setCaretColor(TEXT_LAVENDER);
            comp.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(88, 28, 135)), // Purple border
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
            ));
        }
    }

    private JPanel createMenuPanel() {
        JPanel menu = new JPanel(new GridBagLayout());
        menu.setBackground(BG_BLACK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("TicTacX", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 42));
        title.setForeground(COLOR_P1);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 30, 10);
        menu.add(title, gbc);

        gbc.insets = new Insets(8, 12, 8, 12);
        JLabel[] labels = {
            new JLabel("Player 1 Name:"), new JLabel("P1 Emoji:"),
            new JLabel("Opponent:"), 
            new JLabel("Player 2 Name:"), new JLabel("P2 Emoji:"),
            new JLabel("Grid Size:"), new JLabel("AI Difficulty:")
        };

        JComponent[] inputs = { p1NameField, p1SymbolField, opponentBox, p2NameField, p2SymbolField, gridSizeBox, difficultyBox };

        gbc.gridwidth = 1;
        for (int i = 0; i < labels.length; i++) {
            labels[i].setForeground(TEXT_LAVENDER);
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
            p2SymbolField.setText("O"); // Keep default as 'O' when switching modes
        });

        JButton startBtn = new JButton("Start Game");
        startBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
        startBtn.setBackground(COLOR_P1);
        startBtn.setForeground(BG_BLACK);
        startBtn.setFocusPainted(false);
        startBtn.addActionListener(e -> initGame());
        
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.insets = new Insets(30, 12, 12, 12);
        menu.add(startBtn, gbc);
        return menu;
    }

    private void initGame() {
        isPvP = opponentBox.getSelectedIndex() == 1;
        
        // Fetch inputs and trim whitespace
        p1Name = p1NameField.getText().trim();
        p2Name = p2NameField.getText().trim();
        p1Symbol = p1SymbolField.getText().trim();
        p2Symbol = p2SymbolField.getText().trim();
        
        // Fallback to defaults if fields are left completely blank
        if (p1Name.isEmpty()) p1Name = "Player 1";
        if (p2Name.isEmpty()) p2Name = isPvP ? "Player 2" : "Bot";
        if (p1Symbol.isEmpty()) p1Symbol = "X";
        if (p2Symbol.isEmpty()) p2Symbol = "O";

        size = gridSizeBox.getSelectedIndex() + 3; 
        difficulty = (String) difficultyBox.getSelectedItem();
        
        isPlayer1Turn = true;
        gameOver = false;
        boardState = new int[size][size]; 
        
        gamePanel.removeAll();
        gamePanel.setBackground(BG_BLACK);
        
        statusLabel.setText(p1Name + "'s Turn (" + p1Symbol + ")");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        statusLabel.setForeground(TEXT_LAVENDER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        gamePanel.add(statusLabel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(size, size, 8, 8));
        gridPanel.setBackground(BG_BLACK);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        buttons = new JButton[size][size];
        Font emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, size == 3 ? 56 : (size == 4 ? 42 : 32));

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                buttons[r][c] = new JButton("");
                buttons[r][c].setFont(emojiFont);
                buttons[r][c].setFocusPainted(false);
                buttons[r][c].setBackground(BG_PURPLE_DARK);
                buttons[r][c].setBorder(new LineBorder(new Color(88, 28, 135), 2));
                buttons[r][c].addActionListener(this);
                gridPanel.add(buttons[r][c]);
            }
        }
        gamePanel.add(gridPanel, BorderLayout.CENTER);

        JButton backBtn = new JButton("Abandon & Return to Menu");
        backBtn.setBackground(new Color(88, 28, 135));
        backBtn.setForeground(TEXT_LAVENDER);
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
        int r = -1, c = -1;
        
        // Find grid coordinates of the click
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (buttons[i][j] == clickedButton) {
                    r = i; c = j; break;
                }
            }
        }

        if (boardState[r][c] != 0) return; // Cell already taken

        processMove(r, c, isPlayer1Turn ? 1 : 2);

        if (checkWin(isPlayer1Turn ? 1 : 2, true)) {
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
            statusLabel.setText((isPlayer1Turn ? p1Name + " (" + p1Symbol + ")" : p2Name + " (" + p2Symbol + ")") + "'s Turn");
        } else if (!isPlayer1Turn) {
            statusLabel.setText("Bot is thinking...");
            Timer timer = new Timer(400, event -> {
                makeBotMove();
                if (checkWin(2, true)) {
                    endGame("Bot Wins!");
                    logToDatabase("Bot Win");
                } else if (isBoardFull()) {
                    endGame("It's a Draw!");
                    logToDatabase("Draw");
                } else {
                    isPlayer1Turn = true;
                    statusLabel.setText(p1Name + "'s Turn (" + p1Symbol + ")");
                }
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    private void processMove(int r, int c, int playerCode) {
        boardState[r][c] = playerCode;
        buttons[r][c].setText(playerCode == 1 ? p1Symbol : p2Symbol);
        buttons[r][c].setForeground(playerCode == 1 ? COLOR_P1 : COLOR_P2);
    }

    private void makeBotMove() {
        ArrayList<Point> emptyCells = new ArrayList<>();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (boardState[r][c] == 0) emptyCells.add(new Point(r, c));
            }
        }
        if (emptyCells.isEmpty()) return;

        Point choice = null;
        if (difficulty.contains("Medium")) {
            for (Point p : emptyCells) {
                boardState[p.x][p.y] = 1; // Simulate P1 move
                if (checkWin(1, false)) choice = p;
                boardState[p.x][p.y] = 0; // Revert
                if (choice != null) break;
            }
        }

        if (choice == null) choice = emptyCells.get(random.nextInt(emptyCells.size()));
        processMove(choice.x, choice.y, 2);
    }

    private boolean checkWin(int playerCode, boolean highlight) {
        for (int i = 0; i < size; i++) {
            boolean rowWin = true, colWin = true;
            for (int j = 0; j < size; j++) {
                if (boardState[i][j] != playerCode) rowWin = false;
                if (boardState[j][i] != playerCode) colWin = false;
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
            if (boardState[i][i] != playerCode) diag1 = false;
            if (boardState[i][size - 1 - i] != playerCode) diag2 = false;
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
                if (boardState[r][c] == 0) return false;
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