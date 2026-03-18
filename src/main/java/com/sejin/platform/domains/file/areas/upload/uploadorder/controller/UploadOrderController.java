package com.sejin.platform.domains.file.areas.upload.uploadorder.controller;

import com.sejin.platform.domains.file.areas.upload.uploadorder.dto.response.UploadOrderSummaryResponse;
import com.sejin.platform.domains.file.areas.upload.uploadorder.service.UploadOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// 엑셀 업로드 요청을 받는 컨트롤러
//
// 역할
// 1. 클라이언트 요청 받기
// 2. 요청 파라미터 꺼내기
// 3. 서비스 호출
// 4. 결과 응답 반환

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/file/upload-orders")
public class UploadOrderController {

    private final UploadOrderService uploadOrderService;

    // multipart/form-data 방식으로 파일 업로드 받는 API
    // uploadedId : 현재는 테스트용으로 request parameter로 받음
    // file       : 실제 엑셀 파일

    // consumes = multipart/form-data 를 지정한 이유
    // 파일 업로드 요청 형식을 명확하게 제한하기 위함.
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadOrderSummaryResponse> upload(
            @RequestParam("uploadedId") Long uploadedId,
            @RequestPart("file") MultipartFile file
    ) {
        UploadOrderSummaryResponse response = uploadOrderService.upload(uploadedId, file);
        return ResponseEntity.ok(response);
    }
}