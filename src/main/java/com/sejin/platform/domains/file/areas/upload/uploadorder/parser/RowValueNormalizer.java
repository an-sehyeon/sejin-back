package com.sejin.platform.domains.file.areas.upload.uploadorder.parser;

import org.springframework.stereotype.Component;

// 엑셀에서 읽은 문자열 값을 정리하는 클래스

// 엑셀 값은 사용자가 직접 입력한 값이라서
// 앞뒤 공백
// 하이픈 포함 전화번호
// "1" / "1월" 같이 형식이 제각각인 값이 들어올 수 있음.
// 그래서 저장 전에 값을 한 번 정리해주는 역할을 따로 분리한 것.
@Component
public class RowValueNormalizer {

    // 일반 문자열 정리
    // 앞뒤 공백 제거 후, 비어 있으면 null 로 바꿔줌.
    // null로 바꾸는 이유는 공백 문자열 "" 보다 null 이 뒤 검증 로직에서 다루기 쉬움.
    public String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed;
    }

    // 전화번호 정리
    // 하이픈(-), 공백 제거
    // 예: 010-1234-5678 -> 01012345678
    public String normalizePhone(String value) {
        String normalized = normalizeText(value);

        if (normalized == null) {
            return null;
        }

        return normalized.replace("-", "")
                .replace(" ", "");
    }

    // 공급월 정리
    // 사용자가 "1", "1월", " 1월 " 같이 넣어도
    // 최종적으로 "1월" 형태로 맞추기 위한 메서드
    public String normalizeMonth(String value) {
        String normalized = normalizeText(value);

        if (normalized == null) {
            return null;
        }

        if (normalized.endsWith("월")) {
            return normalized;
        }

        return normalized + "월";
    }
}