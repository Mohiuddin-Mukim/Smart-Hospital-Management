package com.tmukimi.hospital_management.services;

import com.tmukimi.hospital_management.entities.Notice;
import com.tmukimi.hospital_management.repositories.NoticeRepository;
import com.tmukimi.hospital_management.services.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;

    @Override
    public void saveNotice(String content) {
        // ডাটাবেস থেকে প্রথম নোটিশটি খুঁজি (ID: 1)
        Notice notice = noticeRepository.findById(1L).orElse(new Notice());

        // যদি নতুন হয় তবে আইডি ১ সেট করে দিই (যাতে সবসময় একটিই রো থাকে)
        if (notice.getId() == null) {
            notice.setId(1L);
        }

        notice.setContent(content);
        notice.setActive(true);
        noticeRepository.save(notice);
    }

    @Override
    public List<Notice> getAllActiveNotices() {
        return noticeRepository.findByIsActiveTrue();
    }

    @Override
    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
    }

    @Override
    public void toggleNoticeStatus(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notice not found"));
        notice.setActive(!notice.isActive());
        noticeRepository.save(notice);
    }
}