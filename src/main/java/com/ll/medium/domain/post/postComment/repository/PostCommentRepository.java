package com.ll.medium.domain.post.postComment.repository;

import com.ll.medium.domain.post.postComment.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    Optional<PostComment> findCommentById(long id);
}
