package ru.sovaowltv.aop.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
@Slf4j
public class RequestRejectedExceptionAOP extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = null;
        try {
            request = (HttpServletRequest) servletRequest;
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (RequestRejectedException | IllegalArgumentException e) {
            handleException((HttpServletResponse) servletResponse, request, e);
        }
    }

    private void handleException(HttpServletResponse servletResponse, HttpServletRequest request, Exception e) throws IOException {
        log.debug("request.getRemoteHost() = " + request.getRemoteHost(), e);
        log.debug("request.getRequestURL() = " + request.getRequestURL(), e);
        log.debug("request.getRequestURI() = " + request.getRequestURI(), e);
        servletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
