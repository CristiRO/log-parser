package parser;

import java.io.DataOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogProcessor {
	private final InputDataStream inputStream;
	private final LogLineParser lineParser;
	private final DataOutputStream outputStream;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Creates a LogProcessor.
	 * 
	 * @param inputStream		Stream representing the input. Should NOT be open and will be closed after the
	 * 							processor finishes to process the whole log stream
	 * @param lineParser		LineParser implementation
	 * @param outputStream		Stream representing the output. Should be open and will not be closed.
	 */
	public LogProcessor(InputDataStream inputStream, LogLineParser lineParser, DataOutputStream outputStream) {
		this.inputStream = inputStream;
		this.lineParser = lineParser;
		this.outputStream = outputStream;
	}
	
	public void processLogStream() {
		try {
			processStream();
			inputStream.close();
		} catch (IOException e) {
			logger.error("Got an IOException while processing a batch of logs", e);
		}
	}
	
	private long processStream() throws IOException {
		long totalLinesProcessed = 0L;
		long totalLines = 0L;
		Optional<String> logLine;
		if (!inputStream.openStream()) {
			logger.error("Couldn't open stream");
			return totalLinesProcessed;
		}
		
		final Instant start = Instant.now();
		while ((logLine = inputStream.getLine()).isPresent()) {
			totalLines += 1;
			final String line = logLine.get();
			final String parsedLine;
			try {
				if (!line.isEmpty()) {
					parsedLine = lineParser.parseLine(line).toString();
				} else {
					continue;
				}
			} catch (Exception e) {
				logger.error("Could not parse line: {}", line);
				// TODO: add logic to parse ciphered log lines files
				//throw new IllegalStateException(e);
				continue;
			}
			
			outputStream.writeBytes(parsedLine + '\n');
			
			totalLinesProcessed += 1L;
			if (totalLinesProcessed % 10000 == 0) {
				logger.info("Parsed " + totalLinesProcessed + " lines!");
			}
		}
		
		final Instant stop = Instant.now();
		final long milis = stop.toEpochMilli() - start.toEpochMilli();
		logger.info("Processed a batch of logs: {} lines in {} s; average is {}", totalLinesProcessed, milis,
				((float) totalLinesProcessed) / (milis / 1000L));
		logger.info("Total number of lines {}", totalLines);
		
		return totalLinesProcessed;
	}
}
