package com.e203.meeting.repository;

import com.e203.meeting.entity.Meeting;
import com.e203.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Integer> {

    List<Meeting> findByproject(Project project);
}
