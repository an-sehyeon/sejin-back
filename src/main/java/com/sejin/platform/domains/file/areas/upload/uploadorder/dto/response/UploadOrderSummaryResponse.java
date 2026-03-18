package com.sejin.platform.domains.file.areas.upload.uploadorder.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 파일 업로드 결과 전체 요약 응답 DTO
// 컨트롤러에서 업로드 완료 후 사용자에게 반환하는 최종 응답 객체

// 어떤 파일이 저장되었는지
// 총 몇 건인지
// 성공/실패가 몇 건인지
// 미리보기 행 몇 건
@Getter
@Builder
public class UploadOrderSummaryResponse {

    // 저장된 nh_file id
    private Long nhFileId;

    // 업로드 파일명
    private String fileName;

    // 업로드 파일 연도
    private Integer uploadYear;

    // 전체 데이터 행 수
    private Integer totalRows;

    // 성공 행 수
    private Integer successRows;

    // 실패 행 수
    private Integer failRows;

    // 업로드 상태
    private String status;

    // 화면 미리보기용 일부 행 데이터
    private List<UploadOrderRowResponse> previewRows;
}