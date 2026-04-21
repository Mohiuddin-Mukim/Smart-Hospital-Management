package com.tmukimi.hospital_management.services;

import com.tmukimi.hospital_management.entities.Notice;
import java.util.List;

public interface NoticeService {
    void saveNotice(String content);
    List<Notice> getAllActiveNotices();
    void deleteNotice(Long id);
    void toggleNoticeStatus(Long id); // নোটিশ অন/অফ করার জন্য
}