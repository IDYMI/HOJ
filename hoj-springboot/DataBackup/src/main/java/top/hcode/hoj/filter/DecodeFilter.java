package top.hcode.hoj.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebFilter(urlPatterns = "/*")
public class DecodeFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化操作（如果需要）
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        List<String> white_url = new ArrayList<>();

        // 获取请求的 URI
        String requestURI = httpRequest.getRequestURI();

        // 仅对 POST, PUT 请求进行解密
        if (("POST".equalsIgnoreCase(httpRequest.getMethod()) || "PUT".equalsIgnoreCase(httpRequest.getMethod()))
                && !white_url.contains(requestURI) && !requestURI.startsWith("/api/file")) {
            WrapperedRequest wrappedRequest = new WrapperedRequest(httpRequest);
            chain.doFilter(wrappedRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // 销毁操作（如果需要）
    }
}
