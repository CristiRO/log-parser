package parser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileStream implements InputDataStream{
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final File file;
	private BufferedReader reader;
	
	public FileStream(File file) {
		this.file = file;
	}

	@Override
	public boolean openStream() {
		logger.info("Opening file {}", file.getAbsolutePath());
		try {
			String extension = getExtensionOfFile();
			if (!"log".equals(extension)) {
				// default to compressed file
				FileInputStream fin = new FileInputStream(file);
			    BufferedInputStream bis = new BufferedInputStream(fin);
			    CompressorInputStream input = new CompressorStreamFactory(true).createCompressorInputStream(CompressorStreamFactory.BZIP2, bis);
			    reader = new BufferedReader(new InputStreamReader(input));
			} else {
				reader = new BufferedReader(new FileReader(file));
			}
		} catch (FileNotFoundException e) {
			logger.error("File not found!", e);
			return false;
		} catch (CompressorException e) {
			logger.error("Compression error!", e);
			return false;
		}
		return true;
	}

	@Override
	public Optional<String> getLine() {
		String currentLine;
		try {
			if ((currentLine = reader.readLine()) != null) {
				return Optional.of(currentLine);
			} else {
				return Optional.empty();
			}
		} catch (IOException e) {
			logger.error("Couldn't read line in file {}", file.getPath());
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void close() throws IOException {
		if (reader != null) {
			reader.close();
		}
	}
	
	private String getExtensionOfFile() {
		return Arrays.stream(file.getName().split("\\.")).reduce((a,b) -> b).orElse(null);
	}
}
