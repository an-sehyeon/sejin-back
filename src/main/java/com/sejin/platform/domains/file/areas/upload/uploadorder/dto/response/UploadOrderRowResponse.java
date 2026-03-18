package com.sejin.platform.domains.file.areas.upload.uploadorder.dto.response;

import lombok.Builder;
import lombok.Getter;

// 업로드 결과 중 "행 단위 미리보기" 응답 DTO
// 업로드 직후 화면에서 상위 몇 건을 보여줄 때 사용하기 위한 응답 객체
// 저장된 전체 row를 전부 응답으로 내려주면 너무 길어질 수 있음.
// 그래서 미리보기용 최소 정보만 따로 DTO로 분리한 것.
@Getter
@Builder
public class UploadOrderRowResponse {

    // 실제 엑셀 행 번호
    private Integer rowNo;

    // 파싱 상태
    // SUCCESS / ERROR
    private String parseStatus;

    // 오류 메시지
    private String errMsg;

    // 신청자명 원본
    private String nameRaw;

    // 핸드폰
    private String mobile;

    // 비종 구분
    private String itemType;

    // 공급월
    private String month;

    // 선정 물량
    private Integer qtyBags;
}