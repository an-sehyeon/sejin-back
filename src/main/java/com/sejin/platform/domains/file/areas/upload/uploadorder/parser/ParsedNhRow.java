package com.sejin.platform.domains.file.areas.upload.uploadorder.parser;

import lombok.Builder;
import lombok.Getter;

// 엑셀에서 읽은 한 줄 데이터를 담는 클래스
// 아직 DB에 저장하기 전, 잠깐 담아두는 용도로 사용함.
// 엑셀에서 읽은 값을 바로 엔티티에 넣지 않고,
// 먼저 이 객체에 담아두면 파싱 단계와 저장 단계를 나눠서 보기 쉬워짐.

@Getter
@Builder
public class ParsedNhRow {

    // 엑셀에서 몇 번째 줄인지 저장
    private Integer rowNo;

    // 사업년도
    private Integer year;

    // 기관
    private String region;

    // 마을명
    private String village;

    // 신청자명
    private String nameRaw;

    // 주소
    private String address;

    // 도로명 주소
    private String roadAddress;

    // 전화번호
    private String tel;

    // 핸드폰
    private String mobile;

    // 희망농협
    private String nhBranch;

    // 비종 구분
    private String itemType;

    // 공급월
    private String month;

    // 선정 물량(포)
    private Integer qtyBags;

    // 파싱 성공/실패 상태
    private String parseStatus;

    // 오류 메시지
    private String errMsg;
}