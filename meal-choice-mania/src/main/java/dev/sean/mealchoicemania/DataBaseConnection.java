package dev.sean.mealchoicemania;

import java.sql.Connection;
import java.sql.DriverManager;
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

	//should we return user here?
	public boolean addUser(String username, String email, String password){
		String sql = "INSERT INTO User(username, email, password) values("
				+ "'"+username+"','"+email+"','"+password+"'"
				+ ")";

		return executeInsertQuery(sql);
	}
	
	public User searchUsers(String email, String password) {
		String sql = "SELECT id, username, email FROM User WHERE email='"+email+"' AND password='" + password + "'";
		System.out.println(sql);
		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(sql);
			result.next();
			User new_user = new User(result.getInt("id"), result.getString("username"), result.getString("email"));
			System.out.println(new_user.getEmail());
			return new_user;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	//executes any basic insert query into the db
	//returns boolean if successful
	public boolean executeInsertQuery(String query) {
		Statement statement;
		try {
			statement = connection.createStatement();
			statement.executeUpdate(query);
			return true;
		} catch (SQLException e) {
			System.out.println("Something went wrong " + e);
			// TODO Auto-generated catch block
			return false;
		}
	}
	
	//returns id of the room created
	//returns -1 if error
	//TODO - is it possible for first query to execute and 2nd to fail???
	public int createRoom(String room_name, int room_creator) {
		String sql = "INSERT INTO Room (name, room_creator) values('" + room_name + "', '"+room_creator+"')";
		String getId = "SELECT * FROM Room WHERE room_creator="+room_creator+" AND name='"+room_name+"'";
		boolean room_created = executeInsertQuery(sql);
		if(room_created) {
			System.out.println(getId);
			Statement statement;
			try {
				statement = connection.createStatement();
				ResultSet results = statement.executeQuery(getId);
				results.next();
				System.out.println("RESULT: " + results.getInt("id"));
				return results.getInt("id");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -1;
			}
		}
		return -1;
	}

	//creates new col in RoomUserPairing table
	public boolean joinRoom(int joiner_id, int room_id, int room_creator) {
		String sql = "INSERT INTO RoomUserPairing values("+joiner_id+","+room_id+ "," + room_creator+")";
		System.out.println(sql);
		return executeInsertQuery(sql);
	}
	
	

}
