package com.sejin.platform.domains.file.repository;

import com.sejin.platform.domains.file.entity.NhRow;
import org.springframework.data.jpa.repository.JpaRepository;

// nh_row 테이블 접근용 repository
// 업로드한 행 데이터를 저장하거나 조회할 때 사용함.
public interface NhRowRepository extends JpaRepository<NhRow, Long> {
}