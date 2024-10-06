package net.datasa.finders.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * 시큐리티 환경설정
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    //로그인 없이 접근 가능 경로
    private static final String[] PUBLIC_URLS = {
            "/"                     //root
    		, "/header"
    		, "/nav"
    		, "/footer"
    		, "/find/**"
            , "/images/**"          //이미지 경로
            , "/css/**"             //CSS파일들
            , "/js/**"              //JavaSCript 파일들
            , "/member/join"        //회원가입
            , "/member/idCheck"		//중복체크
            , "/board/view"
            , "/board/list"
            , "/board/latestProjects"  // 이 줄을 추가
            , "/member/findId"
            , "/member/findPw"
            , "/member/findIdResult"
            , "/member/resetPw"
            , "/member/verifyUser"
            , "/member/resetPassword"
            , "/support/guide"
            , "/unifiedreview/**"
            , "/guestportfolio/content"
            , "/portfolio/content"
    };
    
    // 프리랜서 회원 접근 가능
    private static final String[] FREELANCER_URLS = {
            "/member/freelancer/**"
    };
    
    // 고객(기업) 회원 접근 가능
    private static final String[] CLIENT_URLS = {
            "/member/client/**"
    };

    //관리자 접근 가능
    private static final String[] ADMIN_URLS = {
            "/member/admin/**"
    };

    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    	
        http
            .authorizeHttpRequests(author -> author
                .requestMatchers(PUBLIC_URLS).permitAll()
                .requestMatchers(FREELANCER_URLS).hasAnyRole("FREELANCER", "ADMIN") //관리자는 모두 접근 가능
                .requestMatchers(CLIENT_URLS).hasAnyRole("CLIENT", "ADMIN") //관리자는 모두 접근 가능
                .requestMatchers(ADMIN_URLS).hasAnyRole("ADMIN")
                .requestMatchers("/board/write").hasRole("CLIENT") // 클라이언트만 접근 가능
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .accessDeniedHandler(customAccessDeniedHandler())
            )
            .httpBasic(Customizer.withDefaults())
            .formLogin(formLogin -> formLogin
                    .loginPage("/member/loginForm")
                    .usernameParameter("id")
                    .passwordParameter("password")
                    .loginProcessingUrl("/member/login")
                    .successHandler(customSuccessHandler())  // Use custom success handler
                    .permitAll()
            )
            .logout(logout -> logout
                    .logoutUrl("/member/logout")
                    .logoutSuccessUrl("/")
            );

        http
            .cors(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable)
            ;

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }

    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }
}
