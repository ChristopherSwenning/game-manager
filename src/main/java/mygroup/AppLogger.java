package mygroup;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 */
public final class AppLogger {
    private static final Logger INSTANCE = Logger.getLogger("mygroup");
  
    static {
      INSTANCE.setUseParentHandlers(false);
      INSTANCE.setLevel(Level.INFO);
      ConsoleHandler ch = new ConsoleHandler();
      ch.setLevel(Level.INFO);
      INSTANCE.addHandler(ch);
      
    }
    
    private AppLogger() {}
  
    public static Logger get() {
      return INSTANCE;
    }
  }
  