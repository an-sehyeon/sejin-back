package com.sejin.platform.domains.file.areas.upload.uploadorder.serviceimpl;

import com.sejin.platform.domains.file.areas.upload.uploadorder.dto.response.UploadOrderRowResponse;
import com.sejin.platform.domains.file.areas.upload.uploadorder.dto.response.UploadOrderSummaryResponse;
import com.sejin.platform.domains.file.areas.upload.uploadorder.parser.ExcelNhOrderFileParser;
import com.sejin.platform.domains.file.areas.upload.uploadorder.parser.ParsedNhRow;
import com.sejin.platform.domains.file.areas.upload.uploadorder.service.UploadOrderService;
import com.sejin.platform.domains.file.areas.upload.uploadorder.validator.UploadOrderValidator;
import com.sejin.platform.domains.file.entity.NhFile;
import com.sejin.platform.domains.file.entity.NhRow;
import com.sejin.platform.domains.file.repository.NhFileRepository;
import com.sejin.platform.domains.file.repository.NhRowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// 업로드 기능 실제 구현 클래스
//
// 이 클래스가 하는 일
// 1. 파일 기본 검증
// 2. nh_file 저장
// 3. 엑셀 파싱
// 4. nh_row 여러 건 저장
// 5. 성공/실패 건수 집계
// 6. 최종 응답 반환
//
// 즉, 업로드의 전체 흐름을 조립하는 핵심 서비스 클래스라고 보면 됨.
@Service

// 생성자 주입용 lombok 어노테이션
// final 필드들을 파라미터로 받는 생성자를 자동 생성해줌.
@RequiredArgsConstructor

// 업로드 처리 전체를 하나의 트랜잭션으로 묶음.
// 중간에 예외가 나면 저장 작업을 롤백해서 데이터 정합성을 맞추기 쉬움.
@Transactional
public class UploadOrderServiceImpl implements UploadOrderService {

    private final UploadOrderValidator uploadOrderValidator;
    private final ExcelNhOrderFileParser excelNhOrderFileParser;
    private final NhFileRepository nhFileRepository;
    private final NhRowRepository nhRowRepository;

    @Override
    public UploadOrderSummaryResponse upload(Long uploadedId, MultipartFile file) {
        // 업로드 가능한 파일인지 가장 먼저 체크
        uploadOrderValidator.validateFile(file);

        // 파일 이력 1건 먼저 저장
        // 왜 먼저 저장하냐면
        // 이후 파싱 중 예외가 나더라도 어떤 파일이 들어왔는지 기록이 남도록 하기 위함.
        NhFile nhFile = NhFile.builder()
                .uploadedId(uploadedId)
                .name(file.getOriginalFilename())
                .uploadYear(extractUploadYear(file.getOriginalFilename()))
                .totalRows(0)
                .successRows(0)
                .failRows(0)
                .status("UPLOADED")
                .isDeleted(false)
                .build();

        nhFileRepository.save(nhFile);

        try {
            // 엑셀 파싱
            List<ParsedNhRow> parsedRows = excelNhOrderFileParser.parse(file);

            int successCount = 0;
            int failCount = 0;

            // 응답에서 미리보기로 보여줄 일부 행 목록
            List<UploadOrderRowResponse> previewRows = new ArrayList<>();

            for (ParsedNhRow parsedRow : parsedRows) {
                // 성공/실패 건수 집계
                if ("SUCCESS".equals(parsedRow.getParseStatus())) {
                    successCount++;
                } else {
                    failCount++;
                }

                // ParsedNhRow -> NhRow 엔티티 변환
                // fixed 컬럼들은 아직 수정 전 단계이므로 null
                // correctionStatus 는 업로드 직후라 NONE
                NhRow nhRow = NhRow.builder()
                        .nhFile(nhFile)
                        .rowNo(parsedRow.getRowNo())
                        .year(parsedRow.getYear())
                        .region(parsedRow.getRegion())
                        .regionFixed(null)
                        .village(parsedRow.getVillage())
                        .villageFixed(null)
                        .nameRaw(parsedRow.getNameRaw())
                        .nameFixed(null)
                        .address(parsedRow.getAddress())
                        .addressFixed(null)
                        .roadAddress(parsedRow.getRoadAddress())
                        .roadAddressFixed(null)
                        .tel(parsedRow.getTel())
                        .mobile(parsedRow.getMobile())
                        .mobileFixed(null)
                        .nhBranch(parsedRow.getNhBranch())
                        .itemType(parsedRow.getItemType())
                        .itemTypeFixed(null)
                        .month(parsedRow.getMonth())
                        .monthFixed(null)
                        .qtyBags(parsedRow.getQtyBags())
                        .qtyBagsFixed(null)
                        .parseStatus(parsedRow.getParseStatus())
                        .correctionStatus("NONE")
                        .orderId(null)
                        .errMsg(parsedRow.getErrMsg())
                        .remarkFixed(null)
                        .correctedBy(null)
                        .correctedAt(null)
                        .reprocessedBy(null)
                        .reprocessedAt(null)
                        .reprocessErrMsg(null)
                        .build();

                nhRowRepository.save(nhRow);

                // 응답이 너무 길어지는 걸 막기 위해
                // 앞에서 최대 10건까지만 미리보기로 담음.
                if (previewRows.size() < 10) {
                    previewRows.add(UploadOrderRowResponse.builder()
                            .rowNo(nhRow.getRowNo())
                            .parseStatus(nhRow.getParseStatus())
                            .errMsg(nhRow.getErrMsg())
                            .nameRaw(nhRow.getNameRaw())
                            .mobile(nhRow.getMobile())
                            .itemType(nhRow.getItemType())
                            .month(nhRow.getMonth())
                            .qtyBags(nhRow.getQtyBags())
                            .build());
                }
            }

            // 파일 단위 집계 정보 마무리
            nhFile.complete(parsedRows.size(), successCount, failCount);

            // 최종 응답 반환
            return UploadOrderSummaryResponse.builder()
                    .nhFileId(nhFile.getId())
                    .fileName(nhFile.getName())
                    .uploadYear(nhFile.getUploadYear())
                    .totalRows(parsedRows.size())
                    .successRows(successCount)
                    .failRows(failCount)
                    .status(nhFile.getStatus())
                    .previewRows(previewRows)
                    .build();

        } catch (IOException e) {
            // 엑셀 파일 자체를 읽는 중 오류 발생 시
            nhFile.fail();
            throw new RuntimeException("엑셀 파일을 읽는 중 오류가 발생했습니다.", e);
        } catch (RuntimeException e) {
            // 기타 비즈니스 예외 발생 시도 상태를 FAILED로 바꿔 추적 가능하게 함.
            nhFile.fail();
            throw e;
        }
    }

    // 파일명 안에서 연도 추출
    // 예: b2025상품.xlsx -> 2025
    //
    // 현재는 간단하게 "4자리 숫자"를 찾아서 연도로 사용
    // 나중에 업로드 화면에서 연도를 직접 받는 구조로 바꾸면 이 메서드는 없어질 수도 있음.
    private Integer extractUploadYear(String fileName) {
        if (fileName == null) {
            return 0;
        }

        String onlyDigits = fileName.replaceAll("[^0-9]", " ").trim();

        if (onlyDigits.isEmpty()) {
            return 0;
        }

        String[] parts = onlyDigits.split("\\s+");

        for (String part : parts) {
            if (part.length() == 4) {
                try {
                    return Integer.parseInt(part);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return 0;
    }
}