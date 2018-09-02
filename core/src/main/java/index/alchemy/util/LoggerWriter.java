package index.alchemy.util;

import java.io.IOException;
import java.io.Writer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class LoggerWriter extends Writer {
    
    public final Logger logger;
    public final Level level;
    public final StringBuffer buffer = new StringBuffer();
    
    public LoggerWriter(Logger logger, Level level) {
        this.logger = logger;
        this.level = level;
    }
    
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        buffer.append(cbuf, off, len);
    }
    
    @Override
    public synchronized void flush() throws IOException {
        logger.log(level, buffer.toString());
        buffer.replace(0, buffer.length(), "");
    }
    
    @Override
    public void close() throws IOException {
        flush();
    }
    
}