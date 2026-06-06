package com.timz.rag_platform.repository;

import com.timz.rag_platform.model.Question;
import com.timz.rag_platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByUserOrderByCreatedAtDesc(User user);
    long countByUser(User user);
}