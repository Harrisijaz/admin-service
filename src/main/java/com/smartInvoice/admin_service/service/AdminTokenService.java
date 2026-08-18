package com.smartInvoice.admin_service.service;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.smartInvoice.admin_service.config.AdminProperties;
import com.smartInvoice.admin_service.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;

@Service
public class AdminTokenService {
	private final AdminProperties properties;
	private final Clock clock;

	public AdminTokenService(AdminProperties properties, Clock clock) {
		this.properties = properties;
		this.clock = clock;
	}

	public AdminPrincipal verify(String authorization) {
		String token = bearer(authorization);
		try {
			SignedJWT jwt = SignedJWT.parse(decryptIfNeeded(token));
			if (!JWSAlgorithm.RS256.equals(jwt.getHeader().getAlgorithm())
					|| !properties.getAuth().getJwtKeyId().equals(jwt.getHeader().getKeyID())) {
				throw invalid("Invalid admin token.");
			}
			RSAPublicKey publicKey = parsePublicKey(properties.getAuth().getJwtPublicKey());
			if (!jwt.verify(new RSASSAVerifier(publicKey))) {
				throw invalid("Invalid admin token signature.");
			}
			JWTClaimsSet claims = jwt.getJWTClaimsSet();
			Instant now = Instant.now(clock);
			if (!properties.getAuth().getIssuer().equals(claims.getIssuer())
					|| !"access".equals(claims.getStringClaim("token_type"))
					|| !"ADMIN".equals(claims.getStringClaim("role"))
					|| claims.getExpirationTime() == null
					|| claims.getExpirationTime().toInstant().isBefore(now)) {
				throw invalid("Admin session expired or invalid.");
			}
			if (claims.getIssueTime() != null
					&& claims.getIssueTime().toInstant().plusSeconds(properties.getAuth().getInactivityTimeoutMinutes() * 60L).isBefore(now)) {
				throw invalid("Admin session expired due to inactivity.");
			}
			return new AdminPrincipal(claims.getSubject(), claims.getStringClaim("email"), claims.getJWTID());
		} catch (ApiException ex) {
			throw ex;
		} catch (Exception ex) {
			throw invalid("Invalid admin token.");
		}
	}

	private String bearer(String authorization) {
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "MISSING_ADMIN_TOKEN", "A valid admin session is required.");
		}
		return authorization.substring(7).trim();
	}

	private String decryptIfNeeded(String token) throws Exception {
		if (!properties.getAuth().isTokenEncryptionEnabled()) return token;
		String secret = properties.getAuth().getJweSecret();
		if (secret == null || secret.isBlank()) {
			throw invalid("Admin token decryption secret is not configured.");
		}
		JWEObject jwe = JWEObject.parse(token);
		if (!JWEAlgorithm.DIR.equals(jwe.getHeader().getAlgorithm())
				|| !EncryptionMethod.A256GCM.equals(jwe.getHeader().getEncryptionMethod())) {
			throw invalid("Invalid admin token encryption.");
		}
		jwe.decrypt(new DirectDecrypter(decodeSecret(secret)));
		return jwe.getPayload().toString();
	}

	private byte[] decodeSecret(String secret) {
		try {
			return Base64.getUrlDecoder().decode(secret);
		} catch (IllegalArgumentException ex) {
			return Base64.getDecoder().decode(secret);
		}
	}

	private RSAPublicKey parsePublicKey(String configured) throws Exception {
		if (configured == null || configured.isBlank()) {
			throw invalid("Admin JWT public key is not configured.");
		}
		String pem = configured.replace("-----BEGIN PUBLIC KEY-----", "")
				.replace("-----END PUBLIC KEY-----", "")
				.replace("\\n", "")
				.replace("\n", "")
				.replace("\r", "")
				.replace(" ", "");
		return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(pem)));
	}

	private ApiException invalid(String message) {
		return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_ADMIN_TOKEN", message);
	}
}
