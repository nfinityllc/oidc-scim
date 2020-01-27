package com.fastcode.ldapimport;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class AppConfig {

    @Autowired
    Environment environment;

    private final String URL = "jdbc:postgresql://localhost:5432/nfinityDemo?currentSchema=blog";
    private final String USER = "postgres";
    private final String DRIVER = "org.postgresql.Driver";
    private final String PASSWORD = "fastcode";

    /*
    spring.datasource.url=jdbc:postgresql://localhost:5432/nfinityDemo?currentSchema=blog
    spring.datasource.username=postgres
    spring.datasource.password=fastcode
    spring.datasource.driverClassName=org.postgresql.Driver
     */

    @Bean
    DataSource dataSource() {
        DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
//        driverManagerDataSource.setUrl(environment.getProperty(URL));
//        driverManagerDataSource.setUsername(environment.getProperty(USER));
//        driverManagerDataSource.setPassword(environment.getProperty(PASSWORD));
//        driverManagerDataSource.setDriverClassName(environment.getProperty(DRIVER));

        driverManagerDataSource.setUrl(URL);
        driverManagerDataSource.setUsername(USER);
        driverManagerDataSource.setPassword(PASSWORD);
        driverManagerDataSource.setDriverClassName(DRIVER);
        return driverManagerDataSource;
    }
}
