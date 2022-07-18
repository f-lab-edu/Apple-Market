package com.limited.sales.filter;

import com.google.gson.JsonSyntaxException;
import com.limited.sales.config.Constant;
import com.limited.sales.exception.sub.LoginException;
import com.limited.sales.exception.sub.TokenException;
import com.limited.sales.principal.PrincipalDetails;
import com.limited.sales.redis.RedisService;
import com.limited.sales.user.vo.User;
import com.limited.sales.utils.JwtProperties;
import com.limited.sales.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
  private final AuthenticationManager authenticationManager;
  private final RedisService redisService;

  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      final User user = Constant.getGson().fromJson(new InputStreamReader(request.getInputStream()), User.class);

      final UsernamePasswordAuthenticationToken createdToken =
          new UsernamePasswordAuthenticationToken(user.getUserEmail(), user.getUserPassword());

      return authenticationManager.authenticate(createdToken);
    } catch (JsonSyntaxException jsonSyntaxException) {
      throw new JsonSyntaxException("Json 파싱 에러");
    } catch (AuthenticationException authenticationException) {
      throw new LoginException("인증 실패");
    } catch (IOException e) {
      throw new RuntimeException("인증 도중 예외 발생");
    }
  }

  @Override
  protected void successfulAuthentication(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain,
      Authentication authResult) {
    try {
      final PrincipalDetails principalDetails = (PrincipalDetails) authResult.getPrincipal();

      final String accessToken = JwtUtils.createAccessToken(principalDetails.getUser());
      final String refreshToken = JwtUtils.createRefreshToken(principalDetails.getUser());
      final String userEmail = principalDetails.getUser().getUserEmail();

      redisService.setValue(userEmail, refreshToken);
      response.addHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + accessToken);
    } catch (TokenException e) {
      throw new TokenException("토큰 생성 도중 오류가 발생했습니다.");
    } catch (Exception e) {
      throw new RuntimeException("알 수 없는 오류가 발생했습니다.");
    }
  }
}
