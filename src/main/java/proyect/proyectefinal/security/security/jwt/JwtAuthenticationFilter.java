package proyect.proyectefinal.security.security.jwt;



import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import proyect.proyectefinal.repository.SesionActivaRepository;
import proyect.proyectefinal.security.entity.SesionActiva;
import proyect.proyectefinal.security.impl.UserDetailsServiceImpl;
import proyect.proyectefinal.security.service.JwtService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  // Se ejecutará en cada petición de la API Rest (por heredar de OncePerRequestFilter)
  // y comprobará que sea válido el token utilizando el provider
 @Autowired
    SesionActivaRepository sesionActivaRepository;
  private final JwtService jwtService;
  private final UserDetailsServiceImpl userDetailsService;

  public JwtAuthenticationFilter(JwtService jwtService, UserDetailsServiceImpl userDetailsService) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
  }

@Override
protected void doFilterInternal(HttpServletRequest req,
                                HttpServletResponse res,
                                FilterChain filterChain)
        throws ServletException, IOException {

    String authHeader = req.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(req, res);
        return;
    }

    String jwt = authHeader.substring(7);
    try {
        String nickname = jwtService.getNicknameUsuarioFromToken(jwt);
        if (nickname != null &&
            SecurityContextHolder.getContext().getAuthentication() == null &&
            jwtService.isTokenValid(jwt, userDetailsService.loadUserByUsername(nickname))) {

            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                    userDetailsService.loadUserByUsername(nickname),
                    null,
                    userDetailsService.loadUserByUsername(nickname).getAuthorities());

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
        filterChain.doFilter(req, res);

    } catch (ExpiredJwtException e) {
        // Token caducado → 401
        logger.warn("JWT expirado: " + e.getMessage());
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json");
        res.getWriter().write("{\"error\":\"Token expirado\"}");
    } catch (JwtException | IllegalArgumentException e) {
        // Cualquier otro fallo de parseo → 400
        logger.warn("JWT inválido: " + e.getMessage());
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.setContentType("application/json");
        res.getWriter().write("{\"error\":\"Token inválido\"}");
    }
}


}
