package org.shayd1.db;

import org.shayd1.model.Coordinates;
import org.shayd1.model.Location;
import org.shayd1.model.Route;

import java.sql.*;
import java.util.LinkedHashMap;

public class RouteRepository {
    private final DatabaseManager db;

    public RouteRepository(DatabaseManager db) {
        this.db = db;
    }

    public LinkedHashMap<Integer, Route> loadCollection() throws SQLException {
        LinkedHashMap<Integer, Route> routes = new LinkedHashMap<>();
        String sql = "SELECT * FROM routes;";

        try (Connection conn = db.getConnection(); Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                int key = rs.getInt("key_id");
                Route route = mapRowToRoute(rs);
                routes.put(key, route);
            }
        }
        return routes;
    }

    public boolean insert(int key, Route route, long ownerId) throws SQLException {

        String sql = """
        INSERT INTO routes (key_id, name, coord_x, coord_y, creation_date,
        from_x, from_y, from_z, from_name, to_x, to_y, to_z, to_name, distance, owner_id)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, key);
            ps.setString(2, route.getName());
            ps.setDouble(3, route.getCoordinates().getX());
            ps.setFloat(4, route.getCoordinates().getY());
            ps.setTimestamp(5, Timestamp.valueOf(route.getCreationDate().atStartOfDay()));
            setLocation(ps, 6, route.getFrom());
            setLocation(ps, 10, route.getTo());
            ps.setFloat(14, route.getDistance());
            ps.setLong(15, ownerId);

            route.setOwnerId(ownerId);
            route.setKey((long) key);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(int key, Route route, long ownerId) throws SQLException {
        if (!checkOwnership(key, ownerId)) return false;

        String sql = """
        UPDATE routes SET
            name = ?, coord_x = ?, coord_y = ?,
            from_x = ?, from_y = ?, from_z = ?, from_name = ?,
            to_x = ?, to_y = ?, to_z = ?, to_name = ?,
            distance = ?
        WHERE key_id = ? AND owner_id = ?;
        """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, route.getName());
            ps.setDouble(2, route.getCoordinates().getX());
            ps.setFloat(3, route.getCoordinates().getY());
            setLocation(ps, 4, route.getFrom());
            setLocation(ps, 8, route.getTo());
            ps.setFloat(12, route.getDistance());
            ps.setInt(13, key);
            ps.setLong(14, ownerId);

            route.setKey((long) key);
            route.setOwnerId(ownerId);

            return ps.executeUpdate() > 0;
        }
    }

    private void setLocation(PreparedStatement ps, int idx, Location loc) throws SQLException {
        if (loc == null) {
            ps.setNull(idx,     Types.DOUBLE);
            ps.setNull(idx + 1, Types.DOUBLE);
            ps.setNull(idx + 2, Types.INTEGER);
            ps.setNull(idx + 3, Types.VARCHAR);
        } else {
            ps.setDouble(idx, loc.getX());
            ps.setDouble(idx + 1, loc.getIntY());
            if (loc.getIntZ() == null) {
                ps.setNull(idx + 2, Types.INTEGER);
            } else {
                ps.setInt(idx + 2, loc.getIntZ());
            }
            if (loc.getName() == null) {
                ps.setNull(idx + 3, Types.VARCHAR);
            } else {
                ps.setString(idx + 3, loc.getName());
            }
        }
    }

    public boolean removeByKey(int key, long ownerId) throws SQLException {
        String sql = "DELETE FROM routes WHERE key_id=? AND owner_id=?;";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, key);
            ps.setLong(2, ownerId);
            return ps.executeUpdate() > 0;
        }
    }

    public int removeGreaterKey(int key, long ownerId) throws SQLException {
        String sql = "DELETE FROM routes WHERE key_id>? AND owner_id=?;";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, key);
            ps.setLong(2, ownerId);
            return ps.executeUpdate();
        }
    }

    public int removeGreater(Route route, long ownerId) throws SQLException {
        String sql = "DELETE FROM routes WHERE distance>? AND owner_id=?";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setFloat(1, route.getDistance());
            ps.setLong(2, ownerId);
            return ps.executeUpdate();
        }
    }

    public void clear(long ownerId) throws SQLException {
        String sql = "DELETE FROM routes WHERE owner_id=?";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, ownerId);
            ps.executeUpdate();
        }
    }

    public boolean replaceIfLower(int key, Route route, long ownerId) throws SQLException {
        String sql = "SELECT distance FROM routes WHERE key_id=? AND owner_id=?;";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, key);
            ps.setLong(2, ownerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                float oldDistance = rs.getFloat("distance");
                if (route.getDistance() < oldDistance) {
                    return update(key, route, ownerId);
                }
            }
            return false;
        }
    }

    public boolean checkOwnership(int key, long ownerId) throws SQLException {
        String sql = "SELECT 1 FROM routes WHERE key_id=? and owner_id=?;";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, key);
            ps.setLong(2, ownerId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    public Route mapRowToRoute(ResultSet rs) throws SQLException {
        Route route = new Route();
        route.setKey(rs.getLong("key_id"));
        route.setOwnerId(rs.getLong("owner_id"));
        route.setName(rs.getString("name"));

        // Coordinates
        Coordinates coords = new Coordinates(
                rs.getFloat("coord_x"),
                rs.getInt("coord_y")
        );
        route.setCoordinates(coords);

        // Dates
        Timestamp ts = rs.getTimestamp("creation_date");
        if (ts != null) route.setCreationDate(ts.toLocalDateTime().toLocalDate());

        route.setFrom(readLocation(rs, "from_x", "from_y", "from_z", "from_name"));
        route.setTo(readLocation(rs, "to_x", "to_y", "to_z", "to_name"));

        route.setDistance(rs.getLong("distance"));

        return route;
    }

    private Location readLocation(ResultSet rs, String xCol, String yCol, String zCol, String nameCol) throws SQLException {
        double x = rs.getDouble(xCol);
        if (rs.wasNull()) {
            return null;
        }
        double y = rs.getDouble(yCol);
        int zRaw = rs.getInt(zCol);
        Integer z = rs.wasNull() ? null : zRaw;
        String name = rs.getString(nameCol);
        return new Location(x, y, z, name);
    }
}
