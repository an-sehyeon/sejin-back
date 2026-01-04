package com.sejin.platform;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootTest
class DbConnectionTest {

    @Autowired
    DataSource dataSource;

    @Test
    void connection_test() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("DB 연결 성공: " + conn.getMetaData().getURL());
        }
    }
}
