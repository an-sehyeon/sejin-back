package com.sejin.platform.domains.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sejin.platform.domains.file.entity.NhFile;

//nh_file 테이블 접근용 repository
//기본적인 단건 저장, 단건 조회, 목록 조회 같은 CRUD 기능을 바로 사용할 수 있음.
//JpaRepository<엔티티 타입, 기본키 타입>
//여기서는 NhFile 엔티티를 대상으로 하고, 기본키는 Long 타입임.
public interface NhFileRepository extends JpaRepository<NhFile, Long> {
	
}