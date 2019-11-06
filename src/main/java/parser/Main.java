package parser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	public static void main(String[] args) throws IOException {
		final Logger logger = LoggerFactory.getLogger(Main.class);
		final ILineParser parser = new ApiCommandsLineParser();
		final IDataStream stream = new FileStream("ApiCommands.log");
		final BufferedWriter writer = new BufferedWriter(new FileWriter("parsed_ApiCommands.log"));
		long nrOfLines = 0L;
		
		if (!stream.openStream()) {
			logger.error("Couldn't open stream");
			stream.close();
			writer.close();
			return;
		}
		Optional<String> logLine;
		while ((logLine = stream.getLine()).isPresent()) {
			final String line = logLine.get();
			final String parsedLine;
			try {
				if (!line.isEmpty()) {
					parsedLine = parser.parseLine(line).toString();
				} else {
					continue;
				}
			} catch (Exception e) {
				logger.error("Could not parse line: {}", line);
				stream.close();
				writer.close();
				throw new IllegalStateException(e);
			}
			writer.write(parsedLine);
			writer.write('\n');
			
			nrOfLines += 1L;
			if (nrOfLines % 10000 == 0) {
				logger.info("Parsed " + nrOfLines + " lines!");
			}
		}
		stream.close();
		writer.close();
		logger.info("DONE! In total " + nrOfLines + " lines were parsed!");
	}
}
