package com.sejin.platform.domains.file.areas.upload.uploadorder.service;

import com.sejin.platform.domains.file.areas.upload.uploadorder.dto.response.UploadOrderSummaryResponse;
import org.springframework.web.multipart.MultipartFile;

// 업로드 기능 서비스 인터페이스

// 인터페이스 두는 이유
// 컨트롤러는 구현체가 아니라 기능 자체에만 의존하게 할 수 있음.
// 나중에 구현 방식이 바뀌어도 컨트롤러 수정 범위를 줄일 수 있음.
// 테스트나 확장 시 구조가 더 깔끔해짐.
public interface UploadOrderService {

    // 파일 업로드 전체 흐름 실행
    // uploadedId : 업로드한 사용자 id
    // file       : 업로드 파일
    UploadOrderSummaryResponse upload(Long uploadedId, MultipartFile file);
}