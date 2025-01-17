package me.darkmun.blockcitytycoonmine;

import me.darkmun.blockcitytycoonmine.durability.DurabilityBlock;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class Database {
    private Connection connection = null;
    private int keepAlive = 0;

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            Bukkit.getScheduler().cancelTask(keepAlive);
            java.util.Properties conProperties = new java.util.Properties();
            conProperties.put("user", "u95570_LRzuS0M9U7");
            conProperties.put("password", "uMGeUJbmt!oH^FYk^I1VSSTW");
            conProperties.put("autoReconnect", "true");
            String url = "jdbc:mysql://mysql2.joinserver.xyz:3306/s95570_BlockCityTycoon";
            connection = DriverManager.getConnection(url, conProperties);
            keepAlive = Bukkit.getScheduler().runTaskTimerAsynchronously(BlockCityTycoonMine.getPlugin(), () -> {
                try (PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM mine_data")) {
                    ps.executeQuery();
                } catch (SQLException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "Keep alive failed", ex);
                    ex.printStackTrace();
                }
            }, 0, 72000).getTaskId();
        }
        return connection;
    }

    public void initializeDatabase() throws SQLException {
        Statement statement = getConnection().createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS mine_data(UUID CHAR(36), blockId INT, material VARCHAR(16), PRIMARY KEY(UUID, blockId))";
        statement.execute(sql);
        statement.close();
    }

    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createOrUpdateDurabilityBlocks(UUID plUUID, List<DurabilityBlock> durabilityBlocks) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement("INSERT INTO mine_data(UUID, blockId, material) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE material = ?");
        for (DurabilityBlock block : durabilityBlocks) {
            if (block.getMaterial() != Material.STONE) {
                statement.setString(1, plUUID.toString());
                statement.setInt(2, block.getID());
                statement.setString(3, block.getMaterial().toString());
                statement.setString(4, block.getMaterial().toString());
                statement.executeUpdate();
            }
        }
        statement.close();
    }



    public Map<Integer, Material> readDurabilityBlocks(UUID plUUID) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM mine_data WHERE UUID = ?");
        statement.setString(1, plUUID.toString());

        ResultSet results = statement.executeQuery();
        Map<Integer, Material> durBlockData = new HashMap<>();
        while (results.next()) {
            int blockId = results.getInt("blockId");
            Material material = Material.valueOf(results.getString("material"));
            durBlockData.put(blockId, material);
        }

        results.close();
        statement.close();
        return durBlockData;
    }

    /*public void updateDurabilityBlocks(UUID plUUID, List<DurabilityBlock> durabilityBlocks) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement("UPDATE mine_data SET material = ? WHERE UUID = ? AND blockId = ?");
        for (DurabilityBlock block : durabilityBlocks) {
            if (block.getMaterial() != Material.STONE) {
                statement.setString(1, block.getMaterial().toString());
                statement.setString(2, plUUID.toString());
                statement.setInt(3, block.getID());
                statement.executeUpdate();
            }
        }
        statement.close();
    }*/
}