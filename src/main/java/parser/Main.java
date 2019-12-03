package parser;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	private final static Logger logger = LoggerFactory.getLogger(Main.class);
	private final static LogLineParser parser = new ApiCommandsLineParser();
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		if (args.length != 2) {
			logger.error("Usage: <path_to_file | path_to_dir> <tcp_socket>");
			throw new IllegalStateException();
		}
		
		final File file = new File(args[0]);
		final int port = Integer.parseInt(args[1]);
		
		if (file.isFile()) {
			processFile(file, port);
		} else { // it's a directory
			getAllFilesUnderDir(file).forEach(f -> {
				try {
					logger.info("{}", file.getAbsolutePath());
					processFile(f, port);
				} catch (IOException e) {
					logger.error("IOException: {}", e);
				}
			});
		}
	}
	
	public static Stream<File> getAllFilesUnderDir(File dir) {
		try {
			return Files.walk(dir.toPath())
				.filter(Files::isRegularFile)
				.map(path -> path.toFile());
		} catch (IOException e) {
			logger.error("Error while walking the directory", e);
			throw new IllegalStateException(e);
		}
	}
	
	public static void processFile(File file, int port) throws UnknownHostException, IOException {
		final InputDataStream input = new FileStream(file);
		
		final Socket socket = new Socket((String) null, port); // null indicates loopback host
		final DataOutputStream output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
//		final FileOutputStream outputFile = new FileOutputStream(new File("result.txt"));
//		final DataOutputStream output = new DataOutputStream(new BufferedOutputStream(outputFile));

		final LogProcessor processor = new LogProcessor(input, parser, output);
		processor.processLogStream();
		output.flush();
		socket.close();
	}
}
