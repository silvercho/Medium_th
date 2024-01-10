package com.ll.medium.domain.home.home.controller;

import com.ll.medium.domain.post.post.entity.Post;
import com.ll.medium.domain.post.post.service.PostService;
import com.ll.medium.global.rq.Rq.Rq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final Rq rq;
    private final PostService postService;

    @GetMapping("/")
    public String showMain() {
        List<Post> posts = postService.findTop30ByPublishedOrderByIdDesc(true);

        if (rq.isLogin()) {
            postService.loadLikeMapOnRequestScope(posts, rq.getMember());
    }

        rq.attr("posts", posts);

        return "domain/home/home/main";
    }
}
