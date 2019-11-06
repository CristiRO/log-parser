package parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileStream implements IDataStream{
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final String filePath;
	private Scanner fileScanner;
	
	public FileStream(String filePath) {
		this.filePath = filePath;
	}

	public boolean openStream() {
		FileInputStream inputStream = null;
		try {
		    inputStream = new FileInputStream(filePath);
		    fileScanner = new Scanner(inputStream, "UTF-8");
		    // note that Scanner suppresses exceptions
		    if (fileScanner.ioException() != null) {
		        throw fileScanner.ioException();
		    }
		} catch (FileNotFoundException e) {
			logger.error("File not found!", e);
			return false;
		} catch (IOException e) {
			logger.error("Error while passing the stream to the scanner");
			return false;
		}
		return true;
	}

	public Optional<String> getLine() {
		if (fileScanner.hasNextLine()) {
			return Optional.of(fileScanner.nextLine());
		} else {
			return Optional.empty();
		}
	}

	public void close() throws IOException {
		if (fileScanner != null) {
			fileScanner.close();
		}
	}
}
