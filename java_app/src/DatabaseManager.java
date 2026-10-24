import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_PATH = System.getProperty("user.home") + "/Projects/TicTacX/db.sqlite3";
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;

    // Explicitly force JVM to register the SQLite driver
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found on classpath!");
            e.printStackTrace();
        }
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS matches ("
                   + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                   + "player_name TEXT NOT NULL, "
                   + "game_mode TEXT NOT NULL, "
                   + "result TEXT NOT NULL, "
                   + "played_at DATETIME DEFAULT CURRENT_TIMESTAMP"
                   + ");";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Database init error: " + e.getMessage());
        }
    }

    public static void saveMatch(String playerName, String gameMode, String result) {
        initializeDatabase();
        String sql = "INSERT INTO matches(player_name, game_mode, result) VALUES(?, ?, ?)";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, playerName);
            pstmt.setString(2, gameMode);
            pstmt.setString(3, result);
            pstmt.executeUpdate();
            System.out.println("Match saved successfully to: " + DB_PATH);
            
        } catch (SQLException e) {
            System.err.println("Database save error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}