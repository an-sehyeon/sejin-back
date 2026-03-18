package com.sejin.platform.domains.file.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

//업로드한 엑셀 파일 1건 자체를 저장하는 엔티티
//사용자가 파일을 한 번 업로드할 때마다 nh_file 테이블에 1건이 저장됨.
//즉, 이 테이블은 "파일 단위" 이력 관리용이라고 보면 됨.
//1. 어떤 사용자가 어떤 파일을 올렸는지 추적할 수 있음.
//2. 전체 행 수 / 성공 수 / 실패 수를 파일 기준으로 관리할 수 있음.
//3. 나중에 업로드 이력 목록 화면을 만들 때 기준 데이터가 됨.
//4. nh_row 여러 건이 이 파일 1건에 연결되는 부모 역할을 함.

@Getter
@Entity
@Table(name = "nh_file")

//JPA는 기본 생성자가 필요함.
//access = PROTECTED 로 두는 이유는
//외부에서 의미 없이 new NhFile() 하지 못하게 막고,
//JPA 내부 동작에서만 기본 생성자를 사용하게 하기 위함.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NhFile {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
    // 업로드한 사용자 id
    // 지금은 컨트롤러에서 request 값으로 받아서 저장하는 구조로 잡았고,
    // 나중에 로그인 기능이 붙으면 현재 로그인 사용자 id를 넣으면 됨.
    @Column(name = "uploaded_id", nullable = false)
    private Long uploadedId;

    // 업로드한 원본 파일명
    // 예: b2025상품.xlsx
    // 나중에 업로드 이력 화면에서 어떤 파일이었는지 보여줄 때 사용함.
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    // 업로드 파일이 어떤 연도 데이터인지 저장
    // 예: 2025
    // 파일명에서 먼저 추출하거나, 나중에는 화면에서 입력받는 방식으로 바꿀 수도 있음.
    @Column(name = "upload_year", nullable = false)
    private Integer uploadYear;

    // 실제 데이터 행으로 인식한 총 행 수
    // 엑셀 전체 줄 수가 아니라, 파싱 대상 데이터 행 수를 의미함.
    @Column(name = "total_rows", nullable = false)
    private Integer totalRows;

    // 정상 파싱된 행 수
    @Column(name = "success_rows", nullable = false)
    private Integer successRows;

    // 오류가 있는 행 수
    @Column(name = "fail_rows", nullable = false)
    private Integer failRows;

    // 업로드 상태
    // 현재 기준
    // UPLOADED : 파일만 업로드된 상태
    // PARSED   : 행 파싱과 저장까지 끝난 상태
    // FAILED   : 파싱 도중 예외가 발생한 상태
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    // 삭제 여부
    // 실제 삭제 대신 논리 삭제 방식으로 운영할 수 있게 둔 컬럼
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    // 생성 시각
    // DDL에서 DEFAULT CURRENT_TIMESTAMP 같은 방식으로 넣는 경우가 많아서
    // insertable = false, updatable = false로 두어 애플리케이션이 직접 값을 넣지 않게 함.
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // 수정 시각
    // DB가 자동으로 관리하는 컬럼일 때 같은 방식으로 둠.
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    // Builder를 사용하는 이유
    // 생성자 파라미터가 많아질 때 순서 실수를 줄이고,
    // 어떤 값을 넣는지 코드에서 바로 읽히게 하기 위함.
    @Builder
    public NhFile(Long uploadedId,
                  String name,
                  Integer uploadYear,
                  Integer totalRows,
                  Integer successRows,
                  Integer failRows,
                  String status,
                  Boolean isDeleted) 
    {
        this.uploadedId = uploadedId;
        this.name = name;
        this.uploadYear = uploadYear;
        this.totalRows = totalRows;
        this.successRows = successRows;
        this.failRows = failRows;
        this.status = status;
        this.isDeleted = isDeleted;
    }

    // 업로드 파일 처리 완료 시 호출하는 메서드
    // 전체 행 수, 성공 수, 실패 수를 반영하고 상태를 PARSED 로 바꿈.
    public void complete(int totalRows, int successRows, int failRows) {
        this.totalRows = totalRows;
        this.successRows = successRows;
        this.failRows = failRows;
        this.status = "PARSED";
    }

    // 업로드 처리 중 예외가 발생했을 때 호출하는 메서드
    // 상태만 FAILED 로 바꿔서 어떤 파일에서 실패했는지 추적 가능하게 함.
    public void fail() {
        this.status = "FAILED";
    }

}
