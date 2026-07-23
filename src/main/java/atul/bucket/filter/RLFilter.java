package atul.bucket.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import atul.bucket.dto.RateLimitResult;
import atul.bucket.service.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RLFilter extends OncePerRequestFilter{

    @Autowired
    RateLimiterService service;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

                String clientId = request.getRemoteAddr();
                RateLimitResult result = service.isAllowed(clientId);
                System.out.println("RLFilter is running!");
                response.setHeader("Rate-Limit Remaining", String.valueOf(result.tokensLeft()));
                
                if(!result.isAllowed()){
                    response.setStatus(429);
                    response.setContentType("application/json");
                    response.getWriter().write("Error loading page, too many requests");
                    return;
                }
                System.out.println("Allowed: " + result.isAllowed() + ", Tokens left: " + result.tokensLeft());
                filterChain.doFilter(request, response);
        
    }

}
