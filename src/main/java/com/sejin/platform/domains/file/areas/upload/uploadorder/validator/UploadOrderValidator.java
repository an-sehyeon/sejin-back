package com.sejin.platform.domains.file.areas.upload.uploadorder.validator;

import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

// 업로드 파일 자체를 검증하는 클래스
// 파일이 비어 있지 않은지
// xlsx 파일인지
// 헤더 구조가 우리가 기대한 양식과 맞는지

// 잘못된 파일을 서비스 로직 깊숙이 보내기 전에 초반에 빠르게 걸러내기 위함.
@Component
public class UploadOrderValidator {

    // 실제 업로드 파일에서 확인한 헤더 순서
    // 이번 파일은 줄바꿈 포함 텍스트가 있으므로 그대로 맞춰서 검사함.
    private static final String[] EXPECTED_HEADERS = {
            "사업\n년도",
            "기관",
            "마을명",
            "신청자명",
            "주소",
            "도로명\n주소",
            "전화번호",
            "핸드폰",
            "신청\n면적",
            "희망농협",
            "비종\n구분",
            "등급",
            "희망제품",
            "규격",
            "희망업체",
            "공급월",
            "경영체\n등록\n여부",
            "선정\n물량\n(포)"
    };

    // 파일 기본 검증
    // null 이거나 비어 있으면 업로드 자체가 잘못된 것
    // 확장자가 xlsx 가 아니면 현재 파서가 처리할 수 없음.
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드 파일이 비어 있습니다.");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("xlsx 파일만 업로드할 수 있습니다.");
        }
    }

    // 헤더 검증
    // 실제 시트의 헤더 행이 우리가 기대한 양식과 같은지 확인함.
    // 컬럼 순서가 달라지면 잘못된 값이 다른 컬럼에 들어갈 수 있어서 초반에 반드시 막아야 함.
    public void validateHeader(Row headerRow) {
        if (headerRow == null) {
            throw new IllegalArgumentException("헤더 행을 찾을 수 없습니다.");
        }

        for (int i = 0; i < EXPECTED_HEADERS.length; i++) {
            String actualValue = headerRow.getCell(i) == null
                    ? null
                    : headerRow.getCell(i).getStringCellValue();

            String expectedValue = EXPECTED_HEADERS[i];

            if (actualValue == null || !expectedValue.equals(actualValue.trim())) {
                throw new IllegalArgumentException("엑셀 헤더 형식이 다릅니다. 업로드 양식을 다시 확인해주세요.");
            }
        }
    }
}