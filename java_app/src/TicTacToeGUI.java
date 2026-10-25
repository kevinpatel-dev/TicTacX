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
    
    // Theme Engine Variables
    private boolean isDarkMode = true;
    private Color bgMain, bgCard, textMain, borderMain, colorWinHighlight;
    private final Color COLOR_P1 = new Color(192, 132, 252);  // Vibrant purple
    private final Color COLOR_P2 = new Color(244, 114, 182);  // Pink

    // Menu Inputs
    private JTextField p1NameField = new JTextField("Kevin");
    private JTextField p1SymbolField = new JTextField("X");
    private JComboBox<String> opponentBox = new JComboBox<>(new String[]{"vs AI", "vs Friend"});
    private JTextField p2NameField = new JTextField("Bot");
    private JTextField p2SymbolField = new JTextField("O");
    private JComboBox<String> gridSizeBox = new JComboBox<>(new String[]{"3x3", "4x4", "5x5"});
    private JComboBox<String> difficultyBox = new JComboBox<>(new String[]{"Easy (Random)", "Medium (Block)"});
    
    // Game State
    private JPanel gamePanel = new JPanel(new BorderLayout());
    private JLabel statusLabel = new JLabel("", SwingConstants.CENTER);
    private JButton[][] buttons;
    private int[][] boardState; 
    private int size = 3;
    private boolean isPvP = false;
    private boolean isPlayer1Turn = true; 
    private boolean gameOver = false;
    private String p1Name, p2Name, p1Symbol, p2Symbol, difficulty;
    private final Random random = new Random();

    public TicTacToeGUI() {
        setTitle("TicTacX");
        setSize(550, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        updateThemeColors();
        
        mainContainer.add(createMenuPanel(), "Menu");
        mainContainer.add(gamePanel, "Game");
        add(mainContainer);
        cardLayout.show(mainContainer, "Menu");
    }

    private void updateThemeColors() {
        if (isDarkMode) {
            bgMain = new Color(9, 9, 11);          // Deep black
            bgCard = new Color(26, 16, 37);        // Dark purple
            textMain = new Color(233, 213, 255);   // Lavender
            borderMain = new Color(88, 28, 135);   // Purple outline
            colorWinHighlight = new Color(107, 33, 168); 
        } else {
            bgMain = new Color(248, 250, 252);     // Slate 50
            bgCard = new Color(243, 232, 255);     // Purple 100
            textMain = new Color(15, 23, 42);      // Slate 900
            borderMain = new Color(216, 180, 254); // Purple 300
            colorWinHighlight = new Color(233, 213, 255);
        }
        getContentPane().setBackground(bgMain);
    }

    private void styleInput(JComponent comp) {
        comp.setBackground(bgCard);
        comp.setForeground(textMain);
        comp.setFont(new Font("Dialog", Font.BOLD, 15));
        
        if (comp instanceof JTextField) {
            ((JTextField) comp).setCaretColor(textMain);
            comp.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderMain, 2), 
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
        } else if (comp instanceof JComboBox) {
            // Fixes dropdown visibility by adding padding
            comp.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderMain, 2), 
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
            ));
        }
    }

    private JPanel createMenuPanel() {
        JPanel menu = new JPanel(new GridBagLayout());
        menu.setBackground(bgMain);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Theme Switcher Button
        // Theme Switcher Button
        JButton themeBtn = new JButton(isDarkMode ? "☀️ Light Mode" : "🌙 Dark Mode");
        themeBtn.setFont(new Font("Dialog", Font.BOLD, 13));
        themeBtn.setBackground(bgCard);
        themeBtn.setForeground(textMain);
        themeBtn.setFocusPainted(false);
        // Added CompoundBorder to create internal padding (margins) inside the border
        themeBtn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(borderMain, 1, true), 
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        themeBtn.addActionListener(e -> {
            isDarkMode = !isDarkMode;
            updateThemeColors();
            mainContainer.removeAll();
            mainContainer.add(createMenuPanel(), "Menu");
            mainContainer.add(gamePanel, "Game");
            mainContainer.revalidate();
            mainContainer.repaint();
        });
        
        // Push the button to the top right with some margin
        gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.NORTHEAST; 
        gbc.fill = GridBagConstraints.NONE; gbc.insets = new Insets(0, 0, 20, 0);
        menu.add(themeBtn, gbc);

        JLabel title = new JLabel("TicTacX", SwingConstants.CENTER);
        title.setFont(new Font("Dialog", Font.BOLD, 46));
        title.setForeground(COLOR_P1);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 10, 25, 10);
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
            labels[i].setForeground(textMain);
            labels[i].setFont(new Font("Dialog", Font.BOLD, 14));
            styleInput(inputs[i]);
            gbc.gridx = 0; gbc.gridy++; menu.add(labels[i], gbc);
            gbc.gridx = 1; menu.add(inputs[i], gbc);
        }

        p2NameField.setEnabled(false);
        // Remove duplicate action listeners to prevent memory leaks on theme rebuild
        for (ActionListener al : opponentBox.getActionListeners()) {
            opponentBox.removeActionListener(al);
        }
        opponentBox.addActionListener(e -> {
            boolean isFriend = opponentBox.getSelectedIndex() == 1;
            p2NameField.setEnabled(isFriend);
            difficultyBox.setEnabled(!isFriend);
            p2NameField.setText(isFriend ? "Player 2" : "Bot");
            p2SymbolField.setText("O");
        });

        JButton startBtn = new JButton("Start Game");
        startBtn.setFont(new Font("Dialog", Font.BOLD, 18));
        startBtn.setBackground(COLOR_P1);
        startBtn.setForeground(new Color(9, 9, 11)); // Always dark text on vibrant purple
        startBtn.setFocusPainted(false);
        startBtn.addActionListener(e -> initGame());
        
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.insets = new Insets(30, 12, 12, 12);
        menu.add(startBtn, gbc);
        return menu;
    }

    private void initGame() {
        isPvP = opponentBox.getSelectedIndex() == 1;
        
        p1Name = p1NameField.getText().trim();
        p2Name = p2NameField.getText().trim();
        p1Symbol = p1SymbolField.getText().trim();
        p2Symbol = p2SymbolField.getText().trim();
        
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
        gamePanel.setBackground(bgMain);
        
        statusLabel.setText(p1Name + "'s Turn (" + p1Symbol + ")");
        statusLabel.setFont(new Font("Dialog", Font.BOLD, 22));
        statusLabel.setForeground(textMain);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        gamePanel.add(statusLabel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(size, size, 8, 8));
        gridPanel.setBackground(bgMain);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        buttons = new JButton[size][size];
        
        // Font sizing adjusted to prevent emoji clipping on Linux
        Font emojiFont = new Font(Font.SANS_SERIF, Font.PLAIN, size == 3 ? 48 : (size == 4 ? 38 : 28));

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                buttons[r][c] = new JButton("");
                buttons[r][c].setFont(emojiFont);
                buttons[r][c].setFocusPainted(false);
                buttons[r][c].setBackground(bgCard);
                buttons[r][c].setBorder(new LineBorder(borderMain, 2));
                buttons[r][c].setMargin(new Insets(0, 0, 0, 0)); // Prevents internal clipping
                buttons[r][c].addActionListener(this);
                gridPanel.add(buttons[r][c]);
            }
        }
        gamePanel.add(gridPanel, BorderLayout.CENTER);

        JButton backBtn = new JButton("Abandon & Return to Menu");
        backBtn.setBackground(bgCard);
        backBtn.setForeground(textMain);
        backBtn.setFont(new Font("Dialog", Font.BOLD, 14));
        backBtn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(borderMain, 2), 
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
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
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (buttons[i][j] == clickedButton) {
                    r = i; c = j; break;
                }
            }
        }

        if (boardState[r][c] != 0) return; 

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
                boardState[p.x][p.y] = 1; 
                if (checkWin(1, false)) choice = p;
                boardState[p.x][p.y] = 0; 
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
                if (highlight) for (int j = 0; j < size; j++) buttons[i][j].setBackground(colorWinHighlight);
                return true;
            }
            if (colWin) {
                if (highlight) for (int j = 0; j < size; j++) buttons[j][i].setBackground(colorWinHighlight);
                return true;
            }
        }

        boolean diag1 = true, diag2 = true;
        for (int i = 0; i < size; i++) {
            if (boardState[i][i] != playerCode) diag1 = false;
            if (boardState[i][size - 1 - i] != playerCode) diag2 = false;
        }
        if (diag1) {
            if (highlight) for (int i = 0; i < size; i++) buttons[i][i].setBackground(colorWinHighlight);
            return true;
        }
        if (diag2) {
            if (highlight) for (int i = 0; i < size; i++) buttons[i][size - 1 - i].setBackground(colorWinHighlight);
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