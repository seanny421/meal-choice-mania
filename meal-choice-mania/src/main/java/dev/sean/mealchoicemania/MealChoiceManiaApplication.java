package dev.sean.mealchoicemania;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class MealChoiceManiaApplication {
	DataBaseConnection db_connection = new DataBaseConnection();

	public static void main(String[] args) {
		SpringApplication.run(MealChoiceManiaApplication.class, args);
	}
	
	@GetMapping
	public String root() {
		return "Hello World";
	}
	
	
	@GetMapping("/users")
	public ArrayList<User> getUsers(){
		ArrayList<User> users;

		try {
			users = db_connection.getUsers();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			users = new ArrayList<>();
		}
		return users;
	}
	
	@GetMapping("/users/add")
	public boolean addUser(@RequestParam(required=true) String username, String email, String password) {
		return db_connection.addUser(username, email, password);
	}

	@PostMapping(path = "/users/add", consumes= {MediaType.APPLICATION_JSON_VALUE})
	public boolean createUser(@RequestBody String request) {
		JSONObject request_object = new JSONObject(request);
		String username = request_object.getString("username");
		String email = request_object.getString("email");
		String password = request_object.getString("password");
		
		return db_connection.addUser(username, email, password);
	}
	
	@PostMapping(path = "users/get", consumes= {MediaType.APPLICATION_JSON_VALUE})
	public User searchUsers(@RequestBody String request) {//should be email and password
		JSONObject request_object = new JSONObject(request);
		return db_connection.searchUsers(request_object.getString("email"), request_object.getString("password"));
	}
	
	//we need a room name and a creator
	//returns the id of the room we created (so creator auto joins room)
	@PostMapping(path = "room/create", consumes= {MediaType.APPLICATION_JSON_VALUE})
	public int createNewRoom(@RequestBody String request) {
		JSONObject request_object = new JSONObject(request);
		int room_creator = request_object.getInt("room_creator");
		String name = request_object.getString("name");
		System.out.println(room_creator + " created the room " + room_creator);
		return db_connection.createRoom(name, room_creator);
	}
	
	@PostMapping(path="room/join", consumes= {MediaType.APPLICATION_JSON_VALUE})
	public boolean joinRoom(@RequestBody String request) {
		JSONObject request_object = new JSONObject(request);
		int user_id = request_object.getInt("user_id");
		int room_id = request_object.getInt("room_id");
		int room_creator = request_object.getInt("room_creator");
		return db_connection.joinRoom(user_id, room_id, room_creator);
	}

	@PostMapping(path="room/delete", consumes= {MediaType.APPLICATION_JSON_VALUE})
	public boolean deleteRoom(@RequestBody String request) {
		JSONObject request_object = new JSONObject(request);
		int room_id = request_object.getInt("room_id");
		//int password = request_object.getInt("password"); - could we use password, lookup room id, get the room_creator and then check passwords against eachother
		return db_connection.deleteRoom(room_id);
	}
	@PostMapping(path="room/leave", consumes= {MediaType.APPLICATION_JSON_VALUE})
	public boolean leaveRoom(@RequestBody String request) {
		JSONObject request_object = new JSONObject(request);
		int user_id = request_object.getInt("user_id");
		int room_id = request_object.getInt("room_id");
		//int password = request_object.getInt("password"); - could we use password, lookup room id, get the room_creator and then check passwords against eachother
		return db_connection.leaveRoom(user_id, room_id);
	}
	@PostMapping(path="poll/create", consumes= {MediaType.APPLICATION_JSON_VALUE})
	public boolean createPoll(@RequestBody String request) {
		JSONObject request_object = new JSONObject(request);
		String name = request_object.getString("name");
		int room_id = request_object.getInt("room_id");
		int poll_creator = request_object.getInt("poll_creator");
		String expire_date = request_object.getString("expire_date");//TODO - we convert back to string later (is this needed? Good for format check maybe?)
		LocalDateTime e = LocalDateTime.parse(expire_date);
		return db_connection.createPoll(name, room_id, poll_creator, e);
	}

	@PostMapping(path="polloption/create", consumes= {MediaType.APPLICATION_JSON_VALUE})
	public boolean createPollOptions(@RequestBody String request) {
		JSONObject request_object = new JSONObject(request);
		int poll_id = request_object.getInt("poll_id");
		String text = request_object.getString("text");
		return db_connection.createPollOptions(poll_id, text);
	}
	@PostMapping(path="polloption/vote", consumes= {MediaType.APPLICATION_JSON_VALUE})
	public boolean addVote(@RequestBody String request) {
		JSONObject request_object = new JSONObject(request);
		int polloption_id = request_object.getInt("polloption_id");
		int poll_id = request_object.getInt("poll_id");
		int user_id = request_object.getInt("user_id");
		return db_connection.vote(polloption_id, poll_id, user_id);
	}
	@PostMapping(path="poll/getvote", consumes= {MediaType.APPLICATION_JSON_VALUE})
	public int getUserVote(@RequestBody String request) {
		JSONObject request_object = new JSONObject(request);
		int user_id = request_object.getInt("user_id");
		int poll_id = request_object.getInt("poll_id");
		return db_connection.getUserVote(user_id, poll_id);
	}
	//get or post request????
	@GetMapping("/poll/count")
	public int getVotes(@RequestParam(required=true) int poll_id, int polloption_id) {
		return db_connection.getVotes(poll_id,polloption_id);
	}

	@PostMapping(path="poll/changevote", consumes= {MediaType.APPLICATION_JSON_VALUE})
	public boolean changeVote(@RequestBody String request) {
		JSONObject request_object = new JSONObject(request);
		int user_id = request_object.getInt("user_id");
		int poll_id = request_object.getInt("poll_id");
		int polloption_id = request_object.getInt("polloption_id");
		return db_connection.changeVote(user_id, poll_id, polloption_id);
	}
	@GetMapping("/poll/options")
	public ArrayList<PollOption> getPollOptions(@RequestParam(required=true) int pollid){
		return db_connection.getPollOptions(pollid);
	}
	
	
	@GetMapping("/delete")
	public boolean deleteEverything() {
		return db_connection.deleteEverything();
	}


	


}
