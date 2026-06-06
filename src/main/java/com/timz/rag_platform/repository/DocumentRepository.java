package com.timz.rag_platform.repository;

import com.timz.rag_platform.model.Document;
import com.timz.rag_platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByUser(User user);
    List<Document> findByType(String type);
    long countByUser(User user);
}