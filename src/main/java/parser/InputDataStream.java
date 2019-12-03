package parser;

import java.io.Closeable;
import java.util.Optional;

public interface InputDataStream extends Closeable{
	boolean openStream();
	Optional<String> getLine();
}
