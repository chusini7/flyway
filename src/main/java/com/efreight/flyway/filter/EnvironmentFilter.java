package com.efreight.flyway.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.efreight.flyway.config.FlywayConfiguration;
import com.efreight.flyway.entity.EnvironmentConfig;
import com.efreight.flyway.holder.FlywayHolder;
import com.efreight.flyway.wrapper.CustomHttpServletRequestWrapper;
import lombok.SneakyThrows;
import org.flywaydb.core.Flyway;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Libiao
 * @date 2022/10/9
 */
@Order(1)
@WebFilter(filterName = "environmentFilter", urlPatterns = "/flyway/*")
public class EnvironmentFilter implements Filter {

    private static final String env_key = "env";
    private static final String sql = "select * from environment_config";
    private static Map<String, EnvironmentConfig> envMap;
    private static final Map<String, Flyway> flywayCache = new ConcurrentHashMap<>();

    @Resource
    private DataSource dataSource;
    @Resource
    private FlywayConfiguration flywayConfiguration;

    @SneakyThrows
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        List<EnvironmentConfig> configList = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                EnvironmentConfig config = new EnvironmentConfig()
                        .setId(resultSet.getInt("id"))
                        .setEnv(resultSet.getString("env"))
                        .setUrl(resultSet.getString("url"))
                        .setUser(resultSet.getString("user"))
                        .setPassword(resultSet.getString("password"))
                        .setCreateTime(resultSet.getTimestamp("create_time").toLocalDateTime());
                configList.add(config);
            }
        }
        envMap = Collections.unmodifiableMap(configList.stream()
                .collect(Collectors.toMap(EnvironmentConfig::getEnv, x -> x, (o, n) -> n, HashMap::new)));
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        try {
            String env = getEnv(servletRequest);
            if (!StringUtils.hasText(env)) {
                throw new RuntimeException("请指定环境！");
            }
            Flyway flyway;
            if (flywayCache.containsKey(env)) {
                flyway = flywayCache.get(env);
            } else {
                EnvironmentConfig config = envMap.get(env);
                if (Objects.isNull(config)) {
                    throw new RuntimeException("指定环境不存在！");
                }
                flyway = flywayConfiguration.flyway(config);
                flywayCache.put(env, flyway);
            }
            FlywayHolder.setFlyway(flyway);
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            FlywayHolder.remove();
        }
    }

    private static String getEnv(ServletRequest servletRequest) {
        CustomHttpServletRequestWrapper request = (CustomHttpServletRequestWrapper) servletRequest;
        String env = request.getParameter(env_key);
        if (!StringUtils.hasText(env)) {
            String body = request.getBody();
            if (StringUtils.hasText(body)) {
                JSONObject jsonObject = JSON.parseObject(body);
                env = (String) jsonObject.get(env_key);
            }
        }
        return env;
    }

    @Override
    public void destroy() {
        // do nothing
    }
}
