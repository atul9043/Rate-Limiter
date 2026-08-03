package atul.bucket.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import atul.bucket.dto.RateLimitResult;
import atul.bucket.service.JwtService;
import atul.bucket.service.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RLFilter extends OncePerRequestFilter{

    @Autowired
    RateLimiterService service;

    @Autowired
    JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

                String clientId;
                boolean authenticated;
                String authHeader = request.getHeader("Authorization");

                if(authHeader != null && authHeader.startsWith("Bearer ")){
                    String token = authHeader.substring(7);
                    try{
                        String user = jwtService.extractusername(token);
                        clientId = "User: "+user;
                        authenticated = true;
                    } catch (Exception e){
                        clientId = "Ip: "+getClientIp(request);
                        authenticated = false;
                    }
                } else {
                    clientId = "User: "+getClientIp(request);
                    authenticated = false;
                }
                RateLimitResult result = service.isAllowed(clientId, authenticated);
                System.out.println("RLFilter is running!");
                response.setHeader("X-RateLimit-Remaining", String.valueOf(result.tokensLeft()));
                
                if(!result.isAllowed()){
                    response.setStatus(429);
                    response.setContentType("application/json");
                    response.getWriter().write("Error loading page, too many requests");
                    return;
                }
                System.out.println("Allowed: " + result.isAllowed() + ", Tokens left: " + result.tokensLeft());
                filterChain.doFilter(request, response);
        
    }

     private String getClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String forwardedFor = request.getHeader("X-Forwarded-For");
        String resolvedIp = (forwardedFor != null && !forwardedFor.isBlank())
                ? forwardedFor.split(",")[0].trim()
                : remoteAddr;

        System.out.println("RemoteAddr: " + remoteAddr + ", X-Forwarded-For: " + forwardedFor + ", Using: " + resolvedIp);

        return resolvedIp;
    }

}
