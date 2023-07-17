package dev.sean.mealchoicemania;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DataBaseConnection {
	Connection connection;
	
	public DataBaseConnection() {
		try {
			this.connection = createConnection();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Couldn't make db connection" + e);
			e.printStackTrace();
		} catch (SQLException e) {
			System.out.println("Couldn't make db connection" + e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Connection createConnection() throws ClassNotFoundException, SQLException {
		Password p = new Password();
		String url = "jdbc:mysql://localhost:3306/MealChoiceMania"; // 
		String username = "root";
		String password = p.getString();
		Connection con = DriverManager.getConnection(url, username, password);
		Class.forName("com.mysql.cj.jdbc.Driver");
		return con;
	}
	
	//don't need to sanitise this, probably shouldn't even have this
	public ArrayList<User> getUsers() throws SQLException{
		ArrayList<User> response = new ArrayList<>();//list of users to respond with
		String query = "SELECT * FROM User";
		Statement statement = connection.createStatement();
		ResultSet results = statement.executeQuery(query);
		
		while(results.next()) {
			User new_user = new User(results.getInt("id"), results.getString("username"), results.getString("email"));
			response.add(new_user);
		}
		return response;
	}

	//sanitised - should we return user?
	public boolean addUser(String username, String email, String password){
		if(username == "" || email == "" || password == "") {return false;}//check for null values first
		String query = "INSERT INTO User (username, email, password) values (?, ?, ?)";
		try {
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, username);
			statement.setString(2, email);
			statement.setString(3, password);
			int row = statement.executeUpdate();
			
			System.out.println(row);
			return true;
		} catch(SQLException e) {
			System.out.println(e);
			return false;
		}

	}
	
	//sanitised
	public User searchUsers(String email, String password) {
		String sql = "SELECT id, username, email FROM User WHERE email=? AND password=?";
		try {
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1,  email);
			statement.setString(2,  password);
			ResultSet result = statement.executeQuery();
			result.next();
			User new_user = new User(result.getInt("id"), result.getString("email"), result.getString("username"));
			return new_user;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	//sanitised
	//returns id of the room created or -1 if error
	//TODO - is it possible for first query to execute and 2nd to fail???
	public int createRoom(String room_name, int room_creator) {
		String sql = "INSERT INTO Room (name, room_creator) values (?, ?)";
		String getId = "SELECT * FROM Room WHERE room_creator=? AND name=?";
		try {
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, room_name);
			statement.setInt(2, room_creator);
			int row = statement.executeUpdate();
			if(row == 1){
				statement = connection.prepareStatement(getId);
				statement.setInt(1, room_creator);
				statement.setString(2, room_name);
				ResultSet results = statement.executeQuery();
				if(results.next()) {
					return results.getInt("id");
				}
			}
			return -1;
		} catch(SQLException e) {
			System.out.println(e);
			return -1;
		}
	}

	//sanitised
	//creates new col in RoomUserPairing table
	public boolean joinRoom(int joiner_id, int room_id, int room_creator) {
		String sql = "INSERT INTO RoomUserPairing (userid, roomid, room_creator) values (?, ?, ?)";
		try {
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1, joiner_id);
			statement.setInt(2, room_id);
			statement.setInt(3, room_creator);
			int row = statement.executeUpdate();
			if(row == 0) {return false;}
			return true;
			
		} catch(SQLException e) {
			System.out.println(e);
			return false;
		}
	}
	
	//sanitised
	public boolean deleteRoom(int room_id) {
		String sql = "DELETE FROM Room WHERE id = ?";
		try {
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1, room_id);
			int row = statement.executeUpdate();
			if(row == 0) {return false;}
			return true;
		} catch(SQLException e) {
			System.out.println(e);
			return false;
		}
	}
	
	public boolean leaveRoom(int user_id_leaving, int room_id) {
		String sql = "DELETE FROM RoomUserPairing WHERE roomid=? AND userid=?";
		try {
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1, room_id);
			statement.setInt(2, user_id_leaving);
			int row = statement.executeUpdate();
			if(row == 0) {return false;}
			return true;
		} catch(SQLException e) {
			System.out.println(e);
			return false;
		}
		
	}

}
