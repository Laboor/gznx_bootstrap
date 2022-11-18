package com.gznx.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;

@ComponentScan("com.gznx")
@SpringBootApplication
public class BootstrapApplication {
    private static final Logger LOG = LoggerFactory.getLogger(BootstrapApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BootstrapApplication.class);
        Environment env = app.run(args).getEnvironment();
        LOG.info("Bootstrap 启动成功！！");
        LOG.info("地址：\thttp://localhost:{}", env.getProperty("server.port"));
    }

    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/index.html");
            factory.addErrorPages(error404Page);
        };
    }
}
