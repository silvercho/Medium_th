package com.ll.medium.domain.home.home.controller;

import com.ll.medium.domain.post.post.entity.Post;
import com.ll.medium.domain.post.post.service.PostService;
import com.ll.medium.domain.post.postLike.entity.PostLike;
import com.ll.medium.global.rq.Rq.Rq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final Rq rq;
    private final PostService postService;

    @GetMapping("/")
    public String showMain() {
        List<Post> posts = postService.findTop30ByPublishedOrderByIdDesc(true);

        if (rq.isLogin()) {
            List<PostLike> likes = postService.findLikesByPostInAndMember(posts, rq.getMember());

            Map<Post, PostLike> likeMap = likes
                    .stream()
                    .collect(
                            HashMap::new,
                            (map, like) -> map.put(like.getPost(), like),
                            HashMap::putAll
                    );

            System.out.println(likeMap);
        }

        rq.setAttribute("posts", posts);

        return "domain/home/home/main";
    }
}
