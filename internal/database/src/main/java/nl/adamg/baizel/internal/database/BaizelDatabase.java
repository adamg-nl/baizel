package nl.adamg.baizel.internal.database;

import org.sqlite.JDBC;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class BaizelDatabase {
    public static Connection connect(String url) throws SQLException {
        return JDBC.createConnection(url, new Properties());
    }
}
