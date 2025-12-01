package com.example.elicesecondproject.mall.domain.notice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name="notices")
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 제목
    @Column(nullable = false, length = 200)
    private String title;

    // 내용
    @Lob
    @Column(nullable = false)
    private String content;

    // 노출 상태 (DRAFT / PUBLISHED / HIDDEN)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoticeStatus status;

    // 노출 시작 일시 (옵션)
    @Column(name = "exposure_start_at")
    private LocalDateTime exposureStartAt;

    // 노출 종료 일시 (옵션)
    @Column(name = "exposure_end_at")
    private LocalDateTime exposureEndAt;

    // 작성 관리자 ID (members.id, role=ADMIN)
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    // 수정 관리자 ID
    @Column(name = "updated_by")
    private Long updatedBy;

    // 삭제 일시 (soft delete 용도)
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 상단 고정 여부
    @Column(name = "is_pinned", nullable = false)
    private boolean pinned;
}
