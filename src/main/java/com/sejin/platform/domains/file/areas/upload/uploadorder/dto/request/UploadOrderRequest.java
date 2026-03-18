package com.sejin.platform.domains.file.areas.upload.uploadorder.dto.request;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

// 업로드 요청 데이터를 담는 DTO
// 컨트롤러에서 multipart/form-data 요청을 받을 때 사용하는 객체라고 보면 됨.
// 지금은 최소 범위 기준으로
// 1. 업로드 사용자 id
// 2. 실제 업로드 파일
// 이 두 값만 받도록 구성함.
@Getter
public class UploadOrderRequest {

    // 업로드한 사용자 id
    // 현재는 테스트/초기 개발 단계라 request에서 직접 받고,
    // 나중에 인증 붙으면 로그인 사용자 정보로 대체 가능
    private Long uploadedId;

    // 실제 업로드할 엑셀 파일
    private MultipartFile file;
}