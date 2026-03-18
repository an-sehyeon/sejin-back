package com.sejin.platform.domains.file.areas.upload.uploadorder.parser;

import com.sejin.platform.domains.file.areas.upload.uploadorder.validator.UploadOrderValidator;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// 업로드한 엑셀 파일을 읽어서 한 줄씩 ParsedNhRow 객체로 바꿔주는 클래스

// 이 클래스가 하는 일
// 1. 엑셀 파일을 엶
// 2. 업로드 대상 시트를 찾음
// 3. 헤더가 맞는지 검사함
// 4. 데이터 줄을 한 줄씩 읽음
// 5. 각 줄을 ParsedNhRow로 만들어서 리스트로 반환함
@Component
@RequiredArgsConstructor
public class ExcelNhOrderFileParser {

    // 헤더가 있는 줄 번호
    // 엑셀 화면에서는 3번째 줄이 헤더
    private static final int HEADER_ROW_INDEX = 2;

    // 실제 데이터가 시작되는 줄 번호
    // 엑셀 화면에서는 5번째 줄부터 데이터가 시작 됨
    private static final int FIRST_DATA_ROW_INDEX = 4;

    // 파일 구조와 헤더가 올바른지 검사하는 검증 객체
    private final UploadOrderValidator uploadOrderValidator;

    // 엑셀 셀 값을 저장하기 전에 공백 제거, 전화번호 정리 같은 정규화를 담당하는 객체
    private final RowValueNormalizer rowValueNormalizer;

    // 업로드한 엑셀 파일 전체를 읽는 메서드
    // 결과는 ParsedNhRow 목록으로 반환함.
    public List<ParsedNhRow> parse(MultipartFile file) throws IOException {
        List<ParsedNhRow> result = new ArrayList<>();

        // Workbook은 엑셀 파일 전체를 뜻함.
        // 엑셀 파일을 열어서 사용한 뒤, 다 끝나면 자동으로 닫히게 하기 위해 try(...) 형태로 작성함.
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {

            // 업로드 대상 시트를 찾음
            Sheet targetSheet = findTargetSheet(workbook);

            if (targetSheet == null) {
                throw new IllegalArgumentException("업로드 대상 시트를 찾을 수 없습니다.");
            }

            // 헤더 줄을 가져와서 양식이 맞는지 검사함.
            Row headerRow = targetSheet.getRow(HEADER_ROW_INDEX);
            uploadOrderValidator.validateHeader(headerRow);

            // 실제 데이터 줄을 하나씩 읽음
            for (int rowIndex = FIRST_DATA_ROW_INDEX; rowIndex <= targetSheet.getLastRowNum(); rowIndex++) {
                Row row = targetSheet.getRow(rowIndex);

                // 완전히 비어있는 줄은 건너뜀
                if (isEmptyRow(row)) {
                    continue;
                }

                ParsedNhRow parsedNhRow = parseRow(row);
                result.add(parsedNhRow);
            }
        }

        return result;
    }

    // 업로드 대상 시트를 찾는 메서드
    // 시트명에 "숫자 + 년 + 전체" 형식이 있으면 업로드 대상 시트로 판단함.
    private Sheet findTargetSheet(Workbook workbook) {
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String sheetName = sheet.getSheetName();

            if (isTargetSheetName(sheetName)) {
                return sheet;
            }
        }

        return null;
    }

    // 시트명이 업로드 대상 형식인지 검사하는 메서드
    // 예:
    // 25년전체
    // 25년 전체
    // 2026년전체
    private boolean isTargetSheetName(String sheetName) {
        if (sheetName == null) {
            return false;
        }

        String normalizedSheetName = sheetName.replace(" ", "").trim();

        return normalizedSheetName.matches("\\d+년전체");
    }

    // 엑셀 한 줄을 읽어서 ParsedNhRow 객체 하나로 만드는 메서드
    private ParsedNhRow parseRow(Row row) {
        // 자바에서는 줄 번호를 0부터 세므로
        // 사용자가 보는 실제 엑셀 줄 번호로 바꾸기 위해 +1 함.
        int excelRowNo = row.getRowNum() + 1;

        Integer year = getIntegerCellValue(row.getCell(0));
        String region = rowValueNormalizer.normalizeText(getStringCellValue(row.getCell(1)));
        String village = rowValueNormalizer.normalizeText(getStringCellValue(row.getCell(2)));
        String nameRaw = rowValueNormalizer.normalizeText(getStringCellValue(row.getCell(3)));
        String address = rowValueNormalizer.normalizeText(getStringCellValue(row.getCell(4)));
        String roadAddress = rowValueNormalizer.normalizeText(getStringCellValue(row.getCell(5)));
        String tel = rowValueNormalizer.normalizePhone(getStringCellValue(row.getCell(6)));
        String mobile = rowValueNormalizer.normalizePhone(getStringCellValue(row.getCell(7)));
        String nhBranch = rowValueNormalizer.normalizeText(getStringCellValue(row.getCell(9)));
        String itemType = rowValueNormalizer.normalizeText(getStringCellValue(row.getCell(10)));
        String month = rowValueNormalizer.normalizeMonth(getStringCellValue(row.getCell(15)));
        Integer qtyBags = getIntegerCellValue(row.getCell(17));

        // 일단 기본값은 성공으로 두고 시작함.
        String parseStatus = "SUCCESS";
        String errMsg = null;

        // 꼭 필요한 값이 비어 있으면 ERROR로 바꾸고,
        // 어떤 값이 문제인지 메시지를 쌓아감.
        if (year == null) {
            parseStatus = "ERROR";
            errMsg = appendError(errMsg, "사업년도 값이 없습니다.");
        }

        if (region == null) {
            parseStatus = "ERROR";
            errMsg = appendError(errMsg, "기관 값이 없습니다.");
        }

        if (nameRaw == null) {
            parseStatus = "ERROR";
            errMsg = appendError(errMsg, "신청자명이 없습니다.");
        }

        if (mobile == null && tel == null) {
            parseStatus = "ERROR";
            errMsg = appendError(errMsg, "전화번호와 핸드폰이 모두 비어 있습니다.");
        }

        if (itemType == null) {
            parseStatus = "ERROR";
            errMsg = appendError(errMsg, "비종 구분 값이 없습니다.");
        }

        if (month == null) {
            parseStatus = "ERROR";
            errMsg = appendError(errMsg, "공급월 값이 없습니다.");
        }

        if (qtyBags == null || qtyBags <= 0) {
            parseStatus = "ERROR";
            errMsg = appendError(errMsg, "선정 물량(포) 값이 올바르지 않습니다.");
        }

        return ParsedNhRow.builder()
                .rowNo(excelRowNo)
                .year(year)
                .region(region)
                .village(village)
                .nameRaw(nameRaw)
                .address(address)
                .roadAddress(roadAddress)
                .tel(tel)
                .mobile(mobile)
                .nhBranch(nhBranch)
                .itemType(itemType)
                .month(month)
                .qtyBags(qtyBags)
                .parseStatus(parseStatus)
                .errMsg(errMsg)
                .build();
    }

    // 현재 줄이 완전히 비어 있는지 확인하는 메서드
    private boolean isEmptyRow(Row row) {
        if (row == null) {
            return true;
        }

        for (int i = 0; i <= 17; i++) {
            Cell cell = row.getCell(i);

            if (cell == null) {
                continue;
            }

            String value = getStringCellValue(cell);

            if (value != null && !value.trim().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    // 셀 값을 문자열로 읽는 메서드
    // 숫자 셀, 문자 셀 상관없이 엑셀 화면에 보이는 값처럼 읽기 위해 DataFormatter 사용
    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        DataFormatter dataFormatter = new DataFormatter();
        String value = dataFormatter.formatCellValue(cell);

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    // 셀 값을 숫자로 바꾸는 메서드
    // 예: "1,000", "3월" 같은 값도 일부 문자 제거 후 숫자로 변환 시도
    private Integer getIntegerCellValue(Cell cell) {
        String value = getStringCellValue(cell);

        if (value == null) {
            return null;
        }

        try {
            String onlyNumber = value.replace(",", "")
                    .replace("월", "")
                    .trim();

            return Integer.parseInt(onlyNumber);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // 오류 메시지를 이어 붙이는 메서드
    // 예:
    // "기관 값이 없습니다. / 신청자명이 없습니다."
    private String appendError(String current, String next) {
        if (current == null) {
            return next;
        }

        return current + " / " + next;
    }
}