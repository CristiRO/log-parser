package parser;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	public static void main(String[] args) throws IOException {
		final Logger logger = LoggerFactory.getLogger(Main.class);
		final ILineParser parser = new ApiCommandsLineParser();
		final IDataStream stream = new FileStream("ApiCommands.log");
		
		final Socket socket = new Socket((String) null, 32774); // null indicates loopback host
		final DataOutputStream os = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

		long nrOfLines = 0L;
		
		if (!stream.openStream()) {
			logger.error("Couldn't open stream");
			stream.close();
			socket.close();
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
				socket.close();
				throw new IllegalStateException(e);
			}
			os.writeBytes(parsedLine);
			os.writeBytes("\n");
			
			nrOfLines += 1L;
			if (nrOfLines % 10000 == 0) {
				logger.info("Parsed " + nrOfLines + " lines!");
			}
		}
		stream.close();
		socket.close();
		logger.info("DONE! In total " + nrOfLines + " lines were parsed!");
	}
}
