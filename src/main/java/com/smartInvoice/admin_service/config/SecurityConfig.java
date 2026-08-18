package com.smartInvoice.admin_service.config;

import com.smartInvoice.admin_service.service.AdminPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
public class SecurityConfig {
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/actuator/health").permitAll()
						.anyRequest().hasRole("ADMIN"))
				.addFilterBefore(new GatewayAdminHeaderFilter(), UsernamePasswordAuthenticationFilter.class)
				.headers(headers -> headers
						.contentTypeOptions(content -> {})
						.frameOptions(frame -> frame.deny())
						.httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000)));
		return http.build();
	}

	private static class GatewayAdminHeaderFilter extends OncePerRequestFilter {
		@Override
		protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
				throws ServletException, IOException {
			String role = request.getHeader("X-User-Role");
			String userId = request.getHeader("X-User-Id");
			String email = request.getHeader("X-User-Email");
			if ("ADMIN".equals(role) && userId != null && !userId.isBlank()) {
				AdminPrincipal principal = new AdminPrincipal(userId, email == null ? "" : email, "");
				SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
						principal, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
			}
			filterChain.doFilter(request, response);
		}
	}
}
