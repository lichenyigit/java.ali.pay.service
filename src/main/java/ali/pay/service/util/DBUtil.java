package ali.pay.service.util;

import ali.pay.service.listener.Listener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;

public class DBUtil {
	private static final Logger logger = LogManager.getLogger();
	public static Connection getConnection(){
		Connection conn = Listener.getDbConn();
		return conn;
	}

}
