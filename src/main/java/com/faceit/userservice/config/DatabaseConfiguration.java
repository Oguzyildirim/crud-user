package com.faceit.userservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;

@Configuration
@EnableJpaRepositories("com.faceit.userservice.repository")
@EnableTransactionManagement
public class DatabaseConfiguration {

    private final Logger log = LoggerFactory.getLogger(DatabaseConfiguration.class);

    private final Environment env;

    public DatabaseConfiguration(Environment env) {
        this.env = env;
    }

    /**
     * Open the TCP port for the H2 database, so it is available remotely.
     *
     * @return the H2 database TCP server.
     * @throws SQLException if the server failed to start.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @Profile(ProfileConstants.SPRING_PROFILE_DEVELOPMENT)
    public Object h2TCPServer() throws SQLException {
        String port = getValidPortForH2();
        log.debug("H2 database is available on port {}", port);
        return createServer(port);
    }

    private String getValidPortForH2() {
        int port = Integer.parseInt(env.getProperty("server.port"));
        if (port < 10000) {
            port = 10000 + port;
        } else {
            if (port < 63536) {
                port = port + 2000;
            } else {
                port = port - 2000;
            }
        }
        return String.valueOf(port);
    }

    public static Object createServer(String port) throws SQLException {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class<?> serverClass = Class.forName("org.h2.tools.Server", true, loader);
            Method createServer = serverClass.getMethod("createTcpServer", String[].class);
            return createServer.invoke((Object)null, (Object) new String[]{"-tcp", "-tcpAllowOthers", "-tcpPort", port});
        } catch (LinkageError | ClassNotFoundException var4) {
            throw new RuntimeException("Failed to load and initialize org.h2.tools.Server", var4);
        } catch (NoSuchMethodException | SecurityException var5) {
            throw new RuntimeException("Failed to get method org.h2.tools.Server.createTcpServer()", var5);
        } catch (IllegalArgumentException | IllegalAccessException var6) {
            throw new RuntimeException("Failed to invoke org.h2.tools.Server.createTcpServer()", var6);
        } catch (InvocationTargetException var7) {
            Throwable t = var7.getTargetException();
            if (t instanceof SQLException) {
                throw (SQLException)t;
            } else {
                throw new RuntimeException("Unchecked exception in org.h2.tools.Server.createTcpServer()", t);
            }
        }
    }

    public static void initH2Console(ServletContext servletContext) {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class<?> servletClass = Class.forName("org.h2.server.web.WebServlet", true, loader);
            Servlet servlet = (Servlet)servletClass.getDeclaredConstructor().newInstance();
            ServletRegistration.Dynamic h2ConsoleServlet = servletContext.addServlet("H2Console", servlet);
            h2ConsoleServlet.addMapping(new String[]{"/h2-console/*"});
            h2ConsoleServlet.setInitParameter("-properties", "src/main/resources/");
            h2ConsoleServlet.setLoadOnStartup(1);
        } catch (LinkageError | NoSuchMethodException | InvocationTargetException | ClassNotFoundException var5) {
            throw new RuntimeException("Failed to load and initialize org.h2.server.web.WebServlet", var5);
        } catch (InstantiationException | IllegalAccessException var6) {
            throw new RuntimeException("Failed to instantiate org.h2.server.web.WebServlet", var6);
        }
    }
}
