package parser;

import org.json.JSONObject;

public interface ILineParser {
	JSONObject parseLine(String line);
}
