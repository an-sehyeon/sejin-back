package com.sejin.platform.domains.file.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

//업로드한 엑셀의 "행 1개"를 저장하는 엔티티
//즉, nh_file 이 파일 단위라면 nh_row 는 데이터 행 단위라고 보면 됨.
//1. 엑셀 원본 행 데이터를 그대로 보관할 수 있음.
//2. 파싱 성공 / 실패 여부를 행 단위로 저장할 수 있음.
//3. 오류 행만 다시 수정하거나 재처리하는 기능으로 확장 가능함.
//4. 나중에 실제 주문(order)으로 변환하기 전 중간 저장소 역할을 함.
@Getter
@Entity
@Table(name = "nh_row")

//JPA용 기본 생성자
//protected 로 막아서 외부에서 의미 없는 기본 생성 사용을 방지함.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NhRow {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 // 어떤 업로드 파일에 속한 행인지 연결하는 부모 참조
 // nh_file 1건 : nh_row 여러 건 구조
 // fetch = LAZY
 // nhRow만 조회할 때 항상 nhFile까지 즉시 가져올 필요는 없으므로 지연 로딩 사용
 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "nh_file_id", nullable = false)
 private NhFile nhFile;

 // 실제 엑셀 행 번호
 // 예: 엑셀에서 5번째 줄 데이터라면 5 저장
 // 나중에 "몇 번째 줄이 오류인지" 사용자에게 알려줄 때 필요함.
 @Column(name = "row_no", nullable = false)
 private Integer rowNo;

 // 사업년도
 @Column(name = "year", nullable = false)
 private Integer year;

 // (읍/면/동) 값
 // 엑셀 원본 그대로 저장
 @Column(name = "region", length = 100)
 private String region;

 // (읍/면/동) 값 정정본
 // 업로드 직후에는 null
 // 사용자가 오류 수정 화면에서 수정하면 이 컬럼에 저장하는 방향으로 확장 가능
 @Column(name = "region_fixed", length = 100)
 private String regionFixed;

 // 마을명 원본
 @Column(name = "village", length = 100)
 private String village;

 // 마을명 정정본
 @Column(name = "village_fixed", length = 100)
 private String villageFixed;

 // 신청자명 원본
 @Column(name = "name_raw", length = 100)
 private String nameRaw;

 // 신청자명 정정본
 @Column(name = "name_fixed", length = 100)
 private String nameFixed;

 // 지번 주소 또는 일반 주소
 @Column(name = "address", length = 255)
 private String address;

 // 주소 정정본
 @Column(name = "address_fixed", length = 255)
 private String addressFixed;

 // 도로명 주소 원본
 @Column(name = "road_address", length = 255)
 private String roadAddress;

 // 도로명 주소 정정본
 @Column(name = "road_address_fixed", length = 255)
 private String roadAddressFixed;

 // 집전화
 @Column(name = "tel", length = 20)
 private String tel;

 // 핸드폰 원본
 @Column(name = "mobile", length = 20)
 private String mobile;

 // 핸드폰 정정본
 @Column(name = "mobile_fixed", length = 20)
 private String mobileFixed;

 // 희망농협
 @Column(name = "nh_branch", length = 100)
 private String nhBranch;

 // 비종 구분 원본
 @Column(name = "item_type", length = 50)
 private String itemType;

 // 비종 구분 정정본
 @Column(name = "item_type_fixed", length = 50)
 private String itemTypeFixed;

 // 공급월 원본
 @Column(name = "month", length = 20)
 private String month;

 // 공급월 정정본
 @Column(name = "month_fixed", length = 20)
 private String monthFixed;

 // 선정 물량(포) 원본
 @Column(name = "qty_bags")
 private Integer qtyBags;

 // 선정 물량(포) 정정본
 @Column(name = "qty_bags_fixed")
 private Integer qtyBagsFixed;

 // 파싱 상태
 // SUCCESS : 기본 검증 통과
 // ERROR   : 필수값 누락 등 오류 존재
 @Column(name = "parse_status", nullable = false, length = 20)
 private String parseStatus;

 // 수정 상태
 // 업로드 직후에는 NONE
 // 이후 수정 기능이 붙으면 CORRECTED 같은 값으로 바뀔 수 있음.
 @Column(name = "correction_status", nullable = false, length = 20)
 private String correctionStatus;

 // 이후 주문 테이블과 연결될 주문 id
 // 업로드 직후에는 아직 주문으로 변환되지 않았으므로 null
 @Column(name = "order_id")
 private Long orderId;

 // 파싱 오류 메시지
 // 예: "신청자명이 없습니다. / 공급월 값이 없습니다."
 @Column(name = "err_msg", length = 255)
 private String errMsg;

 // 수정 비고
 @Column(name = "remark_fixed", length = 255)
 private String remarkFixed;

 // 누가 수정했는지 사용자 id
 @Column(name = "corrected_by")
 private Long correctedBy;

 // 언제 수정했는지 시각
 @Column(name = "corrected_at")
 private LocalDateTime correctedAt;

 // 누가 재처리했는지 사용자 id
 @Column(name = "reprocessed_by")
 private Long reprocessedBy;

 // 언제 재처리했는지 시각
 @Column(name = "reprocessed_at")
 private LocalDateTime reprocessedAt;

 // 재처리 실패 메시지
 @Column(name = "reprocess_err_msg", length = 255)
 private String reprocessErrMsg;

 // 생성 시각
 @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
 private LocalDateTime createdAt;

 // 수정 시각
 @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
 private LocalDateTime updatedAt;

 // Builder 생성자
 // 행 저장 시 넣을 값이 많기 때문에 builder 패턴으로 명확하게 작성하는 편이 안전함.
 @Builder
 public NhRow(NhFile nhFile,
              Integer rowNo,
              Integer year,
              String region,
              String regionFixed,
              String village,
              String villageFixed,
              String nameRaw,
              String nameFixed,
              String address,
              String addressFixed,
              String roadAddress,
              String roadAddressFixed,
              String tel,
              String mobile,
              String mobileFixed,
              String nhBranch,
              String itemType,
              String itemTypeFixed,
              String month,
              String monthFixed,
              Integer qtyBags,
              Integer qtyBagsFixed,
              String parseStatus,
              String correctionStatus,
              Long orderId,
              String errMsg,
              String remarkFixed,
              Long correctedBy,
              LocalDateTime correctedAt,
              Long reprocessedBy,
              LocalDateTime reprocessedAt,
              String reprocessErrMsg) {
     this.nhFile = nhFile;
     this.rowNo = rowNo;
     this.year = year;
     this.region = region;
     this.regionFixed = regionFixed;
     this.village = village;
     this.villageFixed = villageFixed;
     this.nameRaw = nameRaw;
     this.nameFixed = nameFixed;
     this.address = address;
     this.addressFixed = addressFixed;
     this.roadAddress = roadAddress;
     this.roadAddressFixed = roadAddressFixed;
     this.tel = tel;
     this.mobile = mobile;
     this.mobileFixed = mobileFixed;
     this.nhBranch = nhBranch;
     this.itemType = itemType;
     this.itemTypeFixed = itemTypeFixed;
     this.month = month;
     this.monthFixed = monthFixed;
     this.qtyBags = qtyBags;
     this.qtyBagsFixed = qtyBagsFixed;
     this.parseStatus = parseStatus;
     this.correctionStatus = correctionStatus;
     this.orderId = orderId;
     this.errMsg = errMsg;
     this.remarkFixed = remarkFixed;
     this.correctedBy = correctedBy;
     this.correctedAt = correctedAt;
     this.reprocessedBy = reprocessedBy;
     this.reprocessedAt = reprocessedAt;
     this.reprocessErrMsg = reprocessErrMsg;
 }

}
