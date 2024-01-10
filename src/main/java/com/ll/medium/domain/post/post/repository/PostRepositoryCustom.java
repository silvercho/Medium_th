package com.ll.medium.domain.post.post.repository;

import com.ll.medium.domain.member.member.entity.Member;
import com.ll.medium.domain.post.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostRepositoryCustom {

    Page<Post> search(boolean published, List<String> kwTypes, String kw, Pageable pageable);

    Page<Post> search(Member author, Boolean published, String kw, Pageable pageable);
}
