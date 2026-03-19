package com.billermanagement;

import com.billermanagement.persistance.domain.BMConfig;
//import com.billermanagement.services.handler.ResultMappingBean;
import com.billermanagement.util.GlobalHashmap;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
@EnableScheduling
@EnableSwagger2
//@ComponentScan(basePackages = "com.billermanagement")
public class BillerManagementApplication {
	public static void main(String[] args) throws Exception {
//		new SshTunnelStarter().init();
		SpringApplication.run(BillerManagementApplication.class, args);
//		BniEncryption.TestBniEncryption(); // test encrypt decrypt
		//InitDB config = InitDB.getInstance();
		//log.info(a.toString());
	}
        
	@Bean
	public GlobalHashmap getGlobalHashMap(){
            return new GlobalHashmap();
        }

	@Bean
	public BMConfig bmConfig(){
		return new BMConfig();
	}

//	@Bean
//	public ResultMappingBean resultMappingBean(){
//		return new ResultMappingBean();
//	}
}

@Slf4j
class SshTunnelStarter {

	@Value("${ssh.tunnel.url}")
	private String url;

	@Value("${ssh.tunnel.username}")
	private String username;

	@Value("${ssh.tunnel.password}")
	private String password;

	@Value("${ssh.tunnel.port:777}")
	private int port;

	private Session session;

	@PostConstruct
	public void init() {

		JSch jsch = new JSch();
		log.info("JSch started");
		// Get SSH session
		try {
			session = jsch.getSession("devuser", "147.139.169.114", 777);
			session.setPassword("Kokas@jakart4");
			java.util.Properties config = new java.util.Properties();
			// Never automatically add new host keys to the host file
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			// Connect to remote server
			session.connect();
			// Apply the port forwarding
			session.setPortForwardingL(3307, "localhost", 3306);
			log.info("Ssh Tunnel started please turn off if not needed");


		} catch (JSchException e)  {
			log.error(String.valueOf(e));

		}
	}

	@PreDestroy
	public void shutdown() {
		if (session != null && session.isConnected()) {
			session.disconnect();
			log.info("Ssh Tunnel disconnected");

		}
	}
}
