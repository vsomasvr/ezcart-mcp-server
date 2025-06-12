package com.example.ezcart.mcp.server;

import com.example.ezcart.mcp.server.service.EzcartService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EzcartMcpServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EzcartMcpServerApplication.class, args);
	}

    @Bean
    public ToolCallbackProvider ezcartPlatformTools(EzcartService ezcartService) {
        return MethodToolCallbackProvider.builder().toolObjects(ezcartService).build();
    }
}
