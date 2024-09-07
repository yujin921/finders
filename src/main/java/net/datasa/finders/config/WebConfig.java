package net.datasa.finders.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // C:/upload 디렉토리를 /images/** 경로로 매핑
        registry.addResourceHandler("/images/portfolio/**")
                .addResourceLocations("file:/C:/upload/");
    }
}
