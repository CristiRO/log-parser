package parser;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

public class ApiCommandsLineParser implements ILineParser{
	final private Pattern requestIdPattern = Pattern.compile("\\[(\\d+)\\]");
	final private Pattern userPattern = Pattern.compile("User=(\\p{Alnum}+)");
	
	final private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("eee MMM dd HH:mm:ss zzz yyyy");

	public JSONObject parseLine(String line) {
		final Map<String, String> result = new HashMap<>();
		final String[] tokens = line.split("[ \\t\\n\\r]+", 0);
		
		result.put("request_id", getRequestId(tokens[0]));
		
		final String dateString = String.format("%s %s %s %s %s %s",
				tokens[1], tokens[2], tokens[3], tokens[4], tokens[5], tokens[6]);
		result.put("timestamp", getTimestamp(dateString));
		
		result.put("command_type", tokens[7]);

		// TODO: revisit this part of the code to be more pluggable in case a new log line type comes up
		if ("Call".equals(tokens[7])) {
			result.putAll(parseCallCommand(tokens));
		} else if ("LocalCall".equals(tokens[7])) {
			result.put("arg_1", tokens[10]);
			result.put("arg_2", tokens[11]);
		} else if ("TokLogin".equals(tokens[7])) {
			result.put("elapsed", tokens[8].substring("elapsed=".length()));
			result.put("user", getUser(tokens[9]));
			result.put("arg_1", tokens[10].substring("args=".length()));
			result.put("arg_2", tokens[11]);
		} else if ("TokFail".equals(tokens[7])) {
			result.put("elapsed", tokens[8].substring("elapsed=".length()));
			result.put("arg_1", tokens[9].substring("args=".length()));
			result.put("arg_2", tokens[10]);
		} else {
			throw new UnsupportedOperationException();
		}
		
		return new JSONObject(result);
	}
	
	private String getRequestId(String token) {
		final Matcher m = requestIdPattern.matcher(token);
		m.find();
		return m.group(1);
	}
	
	private String getTimestamp(String dateTimeString) {
		final ZonedDateTime dateTime = ZonedDateTime.parse(dateTimeString, timeFormatter);
		return ((Long) dateTime.toEpochSecond()).toString();
	}
	
	private Map<String, String> parseCallCommand(String[] tokens) {
		final Map<String, String> result = new HashMap<>();
		result.put("user", getUser(tokens[8]));
		
		if (tokens[9].startsWith("elapsed")) {
			result.put("elapsed", tokens[9].substring("elapsed=".length()));
			if (tokens.length == 10) {
				return result;
			}
		} else if (tokens[9].startsWith("access")){
			result.put("call_command", "access");
		} else {
			result.put("call_command", tokens[9]);
			return result;
		}
		
		if ("read".equals(tokens[10])) {
			result.put("access_type", tokens[10]);
		} else {
			result.put("path", tokens[10]);
			return result;
		}
		
		result.put("path", tokens[11]);
		
		int fileIndex = tokens[11].lastIndexOf('/');
		result.put("file", tokens[11].substring(fileIndex + 1));
		
		result.put("param_1", tokens[12]);
		result.put("param_2", tokens[13]);
		result.put("param_3", tokens[14]);
		result.put("param_4", tokens[15]);
		result.put("param_5", tokens[16]);
		
		return result;
	}
	
	private String getUser(String userLog) {
		final Matcher m = userPattern.matcher(userLog);
		m.find();
		return m.group(1);
	}
}
