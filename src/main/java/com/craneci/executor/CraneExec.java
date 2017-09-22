package com.craneci.executor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.locks.Lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class CraneExec {

	public static void main(String[] args) {
		SpringApplication.run(CraneExec.class, args);
	}
	
	@Bean
	public HazelcastInstance hazelcastInstance() {
		Config config = new Config();
        return Hazelcast.newHazelcastInstance(config);
	}
	
	@Autowired
	HazelcastInstance hz;
	
	@Scheduled(fixedDelay=1)
	public void take() throws Exception {
		
		IQueue<String> q = hz.getQueue("command");
		String command = q.take();
		execute(null, command);
		
	}
	
	@Async
	public void execute(String channel, String... command) {
		
		IQueue<String> q = hz.getQueue("message");
		
		try {
			
			ProcessBuilder pb = new ProcessBuilder(command);
			Process p = pb.start();

			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			
			while ((line = br.readLine()) != null) {				
				q.put(line);				
			}
			
			br.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}
