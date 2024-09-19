package net.datasa.finders.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // C:/upload/portfolio/ 디렉토리를 /images/portfollio/** 경로로 매핑
        registry.addResourceHandler("/images/portfolio/**")
                .addResourceLocations("file:/C:/upload/portfolio/");
        
        // C:/upload/profile/ 디렉토리를 /images/profile/** 경로로 매핑
        registry.addResourceHandler("/images/profile/**")
        		.addResourceLocations("file:///c:/upload/profile/");
    }
	
    @Autowired
    private UserInfoInterceptor userInfoInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userInfoInterceptor)
        		.addPathPatterns("/**");
    }
}
