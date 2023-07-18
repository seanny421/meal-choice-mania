package dev.sean.mealchoicemania;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

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
//		if(username == "" || email == "" || password == "") {return false;}//check for null values first
		String query = "INSERT INTO User (username, email, password) values (?, ?, ?)";
		return insertQuery(query, new ArrayList<>(Arrays.asList(username, email, password)));
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
		return insertQuery(sql, new ArrayList<>(Arrays.asList(joiner_id, room_id, room_creator)));
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

	public boolean createPoll(String name, int roomid, int poll_creator, LocalDateTime expire_date) {
		String sql = "INSERT INTO Poll (name, roomid, poll_creator, expire_date) values (?,?,?,?)";
		return insertQuery(sql, new ArrayList<>(Arrays.asList(name, roomid, poll_creator, expire_date.toString())));
	}

	public boolean createPollOptions(int pollid, String text) {
		String sql = "INSERT INTO PollOption (pollid, text) values (?, ?)";
		return insertQuery(sql, new ArrayList<>(Arrays.asList(pollid, text)));
	}
	
	//returns PollOption id
	//only need this as we will be fetching all polloption objects to show to user anyway
	public int getUserVote(int user_id, int poll_id) {
		String sql = "SELECT * FROM UserPollPairing WHERE userid=? AND pollid=?";
		try {
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1, user_id);
			statement.setInt(2, poll_id);
			ResultSet result = statement.executeQuery();
			result.next();
			int option_id = result.getInt("polloptionid");
			return option_id;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}

	}
	
	public boolean vote(int polloption_id, int pollid, int user_id ) {
		String checkDateTime = "SELECT * FROM Poll WHERE id=?";
		try {//check if poll has expired and user is allowed to vote in it
			PreparedStatement statement = connection.prepareStatement(checkDateTime);
			statement.setInt(1, pollid);
			ResultSet result = statement.executeQuery();
			result.next();
			LocalDateTime expire_date = LocalDateTime.parse(result.getDate("expire_date") + "T" + result.getTime("expire_date")); 
			int roomid = result.getInt("roomid");
			statement = connection.prepareStatement("SELECT * FROM RoomUserPairing WHERE roomid=? AND userid=?");
			statement.setInt(1, roomid);
			statement.setInt(2, user_id);
			result = statement.executeQuery();
			if(LocalDateTime.now().isAfter(expire_date) || !result.next()) {return false;}

		} catch(SQLException e) {
			System.out.println(e);
			return false;
		}
		String updateQuery = "UPDATE PollOption SET votes = votes + 1 WHERE id=?";
		String insertVote = "INSERT INTO UserPollPairing (userid, pollid, polloptionid) values (?, ?, ?)";
		//check against duplicate votes
		if(insertQuery(insertVote, new ArrayList<>(Arrays.asList(user_id, pollid, polloption_id)))) {
			try {//increment votes
				PreparedStatement statement = connection.prepareStatement(updateQuery);
				statement.setInt(1, polloption_id);
				int row = statement.executeUpdate();
				if(row == 0) {return false;}
				return true;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println(e.toString());
				System.out.println("LINE 167");
				//check for differing vote and change if needed;

				return false;
			}
		}
		return false;
	}
	
	//update userpollpairing and polloption tables
	public boolean changeVote(int user_id, int poll_id, int polloption_id) {
		String sql = "UPDATE UserPollPairing SET polloptionid=? WHERE userid=? AND pollid=?";
		return false;
//		return insertQuery(sql, new ArrayList<>(Arrays.asList(polloption_id, user_id, poll_id)));
//		try {
//			PreparedStatement statement = connection.prepareStatement(sql);
//			int	row = statement.executeUpdate();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return false;
		
	}
	
	
	//only accepts int and string inputs
	public boolean insertQuery(String query, ArrayList<Object> inputs ) {
		try {
			PreparedStatement statement = connection.prepareStatement(query);
			for(int i = 0; i < inputs.size(); i++) {
				String type = inputs.get(i).getClass().toString();
				switch(type) { // database only accepts String and Integer
				case "class java.lang.String":
					statement.setString(i+1, (String) inputs.get(i));
					break;
				case "class java.lang.Integer":
					statement.setInt(i+1, (int) inputs.get(i));
				}

			}
			int row = statement.executeUpdate();
			if(row == 0) {return false;}
			return true;
		} catch(SQLException e) {
			System.out.println("LINE 214");
			System.out.println(e);
			return false;
		}
	}
}
