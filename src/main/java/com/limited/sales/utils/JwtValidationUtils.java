package com.limited.sales.utils;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

import static com.auth0.jwt.JWT.require;

@Validated
public final class JwtValidationUtils {

  public static boolean isAccessTokenValid(
      @NotEmpty(message = "토큰이 존재하지 않습니다.") final String token) {
    try {
      final String prefixToken = JwtUtils.replaceTokenPrefix(token);
      require(Algorithm.HMAC512(JwtProperties.ACCESS_SECRET)).build().verify(prefixToken);
      return true;
    } catch (JWTVerificationException e) {
      return false;
    } catch (NullPointerException e) {
      return false;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("억세스 토큰 검증 도중 오류 발생");
    }
  }

  public static boolean isRefreshTokenValid(
      @NotEmpty(message = "토큰이 존재하지 않습니다.") final String token) {
    try {
      require(Algorithm.HMAC512(JwtProperties.REFRESH_SECRET)).build().verify(token);
      return true;
    } catch (JWTVerificationException e) {
      return false;
    } catch (NullPointerException e) {
      return false;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("리프레쉬 토큰 검증 도중 오류 발생");
    }
  }

  public static boolean hasValidJwtTokenNull(
      @NotEmpty(message = "토큰이 존재하지 않습니다.") final String token) {
    if (token == null) {
      return true;
    }

    return !token.startsWith(JwtProperties.TOKEN_PREFIX);
  }
}
