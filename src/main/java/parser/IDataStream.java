package parser;

import java.io.Closeable;
import java.util.Optional;

public interface IDataStream extends Closeable{
	boolean openStream();
	Optional<String> getLine();
}
