package com.sejin.platform.config.mybatis;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
// @MapperScan("com.sejin.platform") // 매퍼 패키지 스캔 범위
public class MyBatisConfig {

	@Bean
	public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {

		// MyBatis가 쓸 SqlSessionFactory를 직접 생성
		SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();

		// 스프링이 만든 DataSource를 Mybatis에 연결
		// DataSource는 DB url, 아이디, 비번 같은 설정으로 이미 만들어져 있는 객체
		factoryBean.setDataSource(dataSource);

		// XML 매퍼 파일을 읽을 위치 지정
		Resource[] mapperXmlFiles = new PathMatchingResourcePatternResolver()
				.getResources("classpath:/mapper/**/*.xml");
		factoryBean.setMapperLocations(mapperXmlFiles);

		// DB 컬럼이 스네이크 표기법이어도 카멜케이스로 자동 매핑되게 함
		org.apache.ibatis.session.Configuration mybatisSetting = new org.apache.ibatis.session.Configuration();
		mybatisSetting.setMapUnderscoreToCamelCase(true);
		factoryBean.setConfiguration(mybatisSetting);

		// SqlSessionFactory를 최종 생성해서 스프링 빈으로 등록
		return factoryBean.getObject();
	}

	@Bean
	public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}
}