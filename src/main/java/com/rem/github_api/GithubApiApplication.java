package com.rem.github_api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class GithubApiApplication implements  CommandLineRunner{

	private static final String GITHUB_API_URL = "https://api.github.com/users/";

	public static void main(String[] args) {

		SpringApplication.run(GithubApiApplication.class, args);

	}



	@Override
	public void run(String... args)  {

		if (args.length < 1){
			System.out.println("Usage: type your <username>");
			System.exit(1);
		}
		String username = args[0];

		try{
			FetchGitHubActivity(username);
		} catch (InterruptedException | IOException e) {
			System.out.println("Failed to get events: " + e.getMessage());
		}

	}

	private void FetchGitHubActivity(String username) throws IOException, InterruptedException {

		// Create an HttpClient
		HttpClient client = HttpClient.newHttpClient();

		// Build the request
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(GITHUB_API_URL + username + "/events"))
				.header("Accept", "application/json")
				.GET()
				.build();

		HttpResponse<String> connection;

		connection = client.send(request, HttpResponse.BodyHandlers.ofString());

		if (connection.statusCode() != 200) {
			System.out.println("Error: " + connection.statusCode());
			return;
		}

		//Get the input stream from the connection
		InputStream inputStream = new ByteArrayInputStream(connection.body().getBytes(StandardCharsets.UTF_8));

		// Process the input stream as needed
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		StringBuilder response = new StringBuilder();

		while ((line = reader.readLine()) != null) {
			response.append(line);
		}
		reader.close();
		parseAndDisplayActivity(response.toString());
	}

	static void parseAndDisplayActivity(String jsonResponse) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode events = objectMapper.readTree(jsonResponse);

		if(!events.isArray() || events.isEmpty()){
			System.out.println("No recent events.");
		}

		for(JsonNode event : events){
			String type = event.get("type").asText();
			String repoName = event.get("repo").get("name").asText();

			switch (type){
				case "PushEvent":
					System.out.println("Pushed to " + repoName);
				case "IssuesEvent":
					System.out.println("Opened an issue in " + repoName);
				case "WatchEvent":
					System.out.println("Starred "  + repoName);
				default:
					System.out.println("Performed "  + type + " on "  + repoName);
			}
		}

	}
}
