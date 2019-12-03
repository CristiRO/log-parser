package parser;

import org.json.JSONObject;

public interface LogLineParser {
	JSONObject parseLine(String line);
}
