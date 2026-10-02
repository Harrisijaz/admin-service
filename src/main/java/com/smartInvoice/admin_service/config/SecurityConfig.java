package com.smartInvoice.admin_service.config;

import com.smartInvoice.admin_service.service.AdminPrincipal;
import com.smartInvoice.admin_service.service.AdminTokenService;
import com.smartInvoice.admin_service.web.ApiException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
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
	SecurityFilterChain securityFilterChain(HttpSecurity http, AdminTokenService tokenService) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/actuator/health").permitAll()
						.requestMatchers("/api/blog/**").permitAll()
						.requestMatchers("/admin/billing/internal/**").permitAll()
						.anyRequest().hasRole("ADMIN"))
				.addFilterBefore(new AdminTokenFilter(tokenService), UsernamePasswordAuthenticationFilter.class)
				.headers(headers -> headers
						.contentTypeOptions(content -> {})
						.frameOptions(frame -> frame.deny())
						.httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000)));
		return http.build();
	}

	private static class AdminTokenFilter extends OncePerRequestFilter {
		private final AdminTokenService tokenService;

		private AdminTokenFilter(AdminTokenService tokenService) {
			this.tokenService = tokenService;
		}

		@Override
		protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
				throws ServletException, IOException {
			String authorization = request.getHeader("Authorization");
			if (authorization != null && authorization.startsWith("Bearer ")) {
				try {
					AdminPrincipal principal = tokenService.verify(authorization);
					SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
							principal, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
				} catch (ApiException ex) {
					response.setStatus(ex.getStatus().value());
					response.setContentType(MediaType.APPLICATION_JSON_VALUE);
					response.getWriter().write("{\"code\":\"" + ex.getCode() + "\",\"message\":\"" + ex.getMessage() + "\"}");
					return;
				}
			}
			filterChain.doFilter(request, response);
		}
	}
}
