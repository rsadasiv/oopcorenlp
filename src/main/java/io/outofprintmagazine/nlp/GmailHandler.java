package io.outofprintmagazine.nlp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;

public class GmailHandler {

	private static final String APPLICATION_NAME = "oop-nlp";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "src/main/resources/tokens";

	/**
	 * Global instance of the scopes required by this quickstart. If modifying these
	 * scopes, delete your previously saved tokens/ folder.
	 */
	private static final List<String> SCOPES = Arrays.asList(GmailScopes.GMAIL_MODIFY);
	private static final String CREDENTIALS_FILE_PATH = "/client_secret_633995379051-ljccu6ri4kg4gkpoktlklbj7dmbudf4c.apps.googleusercontent.com.json";

	/**
	 * Creates an authorized Credential object.
	 * 
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException If the credentials.json file cannot be found.
	 */
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		// Load client secrets.
		InputStream in = GmailHandler.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES)
						.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
						.setAccessType("offline").build();
		return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("rsadasiv@gmail.com");
	}

	public static void main(String... args) throws IOException, GeneralSecurityException {
		// Build a new authorized API client service.
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

		// Print the labels in the user's account.
		String user = "rsadasiv@gmail.com";

		List<String> stopWords = Files.readAllLines(
				new File("C:\\Users\\rsada\\git\\oop_nlp\\similarity\\resources\\Email.txt").toPath(),
				Charset.defaultCharset());
		int i = 0;
		String nextPage = "";
		while (nextPage != null) {

			List<Message> messages = Arrays.asList();
			if (nextPage.equals("")) {
				ListMessagesResponse listMessagesResponse = service.users().messages().list(user)
						.setLabelIds(Arrays.asList("Label_5x")).execute();
				messages = listMessagesResponse.getMessages();
				nextPage = listMessagesResponse.getNextPageToken();
			} else {
				ListMessagesResponse listMessagesResponse = service.users().messages().list(user)
						.setLabelIds(Arrays.asList("Label_5x")).setPageToken(nextPage).execute();
				messages = listMessagesResponse.getMessages();
				nextPage = listMessagesResponse.getNextPageToken();
			}
			if (messages == null || messages.isEmpty()) {
				System.out.println("No Messages found.");
				break;
			} 
			else {
				for (Message messageId : messages) {
					try {
						i++;
						// System.out.printf("%s\n", messageId.getId());
						Message message = service.users().messages().get(user, messageId.getId()).execute();
						boolean fromIndi = false;
						boolean hasSubjectSubmission = false;
						for (MessagePartHeader header : message.getPayload().getHeaders()) {
							if (header.getName().equals("From")) {
								if ("The Editors <outofprintmagazine@gmail.com>".equals(header.getValue())) {
									fromIndi = true;
								}
							}
							if (header.getName().equals("Subject")) {
								if (header.getValue().toLowerCase().startsWith("fwd: submission")) {
									hasSubjectSubmission = true;
								}
								if (header.getValue().toLowerCase().startsWith("fwd: dna-out of print fiction")) {
									hasSubjectSubmission = true;
								}
	
							}
						}
						/*
						 * get author email and date
						 * ---------- Forwarded message ----------
	From: anindita deo <aninditadeo@gmail.com>
	Date: 14 June 2014 14:31
	Subject: Re: Submission: Short Fiction
	To: The Editors <outofprintmagazine@gmail.com>
						 */
						//if (fromIndi && hasSubjectSubmission) {
						if (fromIndi && message.getPayload().getParts() != null) {
							File submissionFile = new File("C:\\Users\\rsada\\git\\oop_nlp\\similarity\\resources\\Submissions\\" + messageId.getId() + ".txt");
						      // creates the file
							submissionFile.createNewFile();
							PrintWriter fout = new PrintWriter(submissionFile); 
							for (MessagePart part : message.getPayload().getParts()) {
								if (part.getMimeType().equals("text/plain")) {
									// System.out.println("Body:");
									String body = new String(part.getBody().decodeData());
									String lines[] = body.split("\\r?\\n");
									String prevLine = "";
	
									for (int idx=0;idx<lines.length;idx++) {
										String line = lines[idx];
										if (line.trim().equals("---------- Forwarded message ----------")) {
											fout.println(lines[++idx]);
											fout.println(lines[++idx]);
											fout.println(lines[++idx]);
											fout.println(lines[++idx]);
											prevLine = "";
											line = lines[++idx];
										}
										boolean shouldPrint = true;			
										for (String stopWord : stopWords) {
											if (line.matches(stopWord)) {
												shouldPrint = false;
												break;
											}
										}
										if (shouldPrint) {
											if (line.equals("") && line.equals(prevLine)) {
												//pass
											}
											else if (line.equals("")) {
												fout.println(line);
												fout.println(line);
											}
											else {
												fout.print(line.trim() + " ");
											}
											prevLine = line;
										}
									}
									System.out.println();
									System.out.println("----------------------------------               " 
											+ i
											+ "        ------------------------------------");
								      
	
								      fout.flush();
								      fout.close();
								}
							}
						}
					}
					catch (Exception e) {
						System.err.println(e.toString());
					}

				}
			}
		}
	}

}
