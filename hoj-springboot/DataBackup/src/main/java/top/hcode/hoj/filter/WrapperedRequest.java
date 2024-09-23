package top.hcode.hoj.filter;

import org.apache.commons.lang.StringUtils;

import lombok.extern.slf4j.Slf4j;
import top.hcode.hoj.utils.AESUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * WrapperedRequest 用于包装 HttpServletRequest 并处理加密的请求体。
 * 解密请求体后，将解密后的内容替代原始请求体用于后续处理。
 */

@Slf4j(topic = "hoj")
public class WrapperedRequest extends HttpServletRequestWrapper {

    private String requestBody = ""; // 解密后的请求体
    private final HttpServletRequest req; // 原始请求
    private final Map<String, Object> parameterMap = new HashMap<>(); // 参数Map

    private static final String secretKey = "5A8F3C6B1D9E2F7A4B0C9D6E7F3B8A1C"; // 32字节密钥

    /**
     * 构造函数，解析并解密请求体。
     *
     * @param request 原始的 HttpServletRequest
     * @throws IOException 读取请求体时发生异常
     */
    public WrapperedRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.req = request;

        // 读取请求体并尝试解密
        String bodyString = getBodyString(request);
        if (bodyString != null && !bodyString.isEmpty()) {
            try {
                this.requestBody = AESUtils.decrypt(bodyString, secretKey); // 解密请求体
            } catch (Exception e) {
                log.error("Error processing request body--------------->{}", e.getMessage());
                this.requestBody = bodyString; // 如果解密出错，保留原始请求体
            }
        }
    }

    /**
     * 读取请求体并返回字符串形式。
     *
     * @param request ServletRequest 请求对象
     * @return 请求体的字符串
     */
    public static String getBodyString(ServletRequest request) {
        StringBuilder sb = new StringBuilder();
        try (InputStream inputStream = request.getInputStream();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream, Charset.forName("UTF-8")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 返回一个 BufferedReader，用于读取解密后的请求体。
     *
     * @return BufferedReader 读取解密后的请求体
     * @throws IOException 如果读取失败，抛出异常
     */
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new StringReader(this.requestBody));
    }

    /**
     * 返回 ServletInputStream，用于读取解密后的请求体。
     *
     * @return ServletInputStream 读取解密后的请求体
     * @throws IOException 如果读取失败，抛出异常
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(this.requestBody.getBytes("UTF-8"));
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener listener) {
                // 未使用
            }

            @Override
            public int read() throws IOException {
                return bais.read();
            }
        };
    }

    /**
     * 获取指定参数名的值，如果没有则返回 null。
     * 接收一般变量，例如 text 类型。
     *
     * @param name 参数名
     * @return 参数值
     */
    @Override
    public String getParameter(String name) {
        String result = (String) parameterMap.get(name);
        return StringUtils.isBlank(result) ? null : result;
    }

    /**
     * 获取指定参数名的所有值，返回数组形式。
     * 适用于接收数组类型变量，例如 checkbox 类型。
     *
     * @param name 参数名
     * @return 参数值数组
     */
    @Override
    public String[] getParameterValues(String name) {
        String result = (String) parameterMap.get(name);
        return result == null ? null : result.split(",");
    }

    /**
     * 返回所有参数名的枚举。
     *
     * @return 参数名的枚举
     */
    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameterMap.keySet());
    }

}
