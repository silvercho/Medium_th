package com.ll.medium.domain.post.post.controller;

import com.ll.medium.domain.post.post.entity.Post;
import com.ll.medium.domain.post.post.service.PostService;
import com.ll.medium.global.exceptions.GlobalException;
import com.ll.medium.global.rq.Rq.Rq;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final Rq rq;

    @GetMapping("/{id}")
    public String showDetail(@PathVariable long id) {
        Post post = postService.findById(id).orElseThrow(() -> new GlobalException("404-1", "해당 글이 존재하지 않습니다."));

        postService.increaseHit(post);

        rq.setAttribute("post", post);

        return "domain/post/post/detail";
    }

    @GetMapping("/list")
    public String showList(
            @RequestParam(defaultValue = "") String kw,
            @RequestParam(defaultValue = "1") int page
    ) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("id"));
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(sorts));

        Page<Post> postPage = postService.search(kw, pageable);
        rq.setAttribute("postPage", postPage);
        rq.setAttribute("page", page);

        return "domain/post/post/list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/myList")
    public String showMyList(
            @RequestParam(defaultValue = "") String kw,
            @RequestParam(defaultValue = "1") int page
    ) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("id"));
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(sorts));

        Page<Post> postPage = postService.search(rq.getMember(), null, kw, pageable);
        rq.setAttribute("postPage", postPage);
        rq.setAttribute("page", page);

        return "domain/post/post/myList";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/makeTemp")
    public String makeTemp() {
        Post post = postService.findTempOrMake(rq.getMember());

        return rq.redirect("/post/%d/edit".formatted(post.getId()), post.getId() + "번 임시글이 생성되었습니다.");
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/edit")
    public String showEdit(@PathVariable long id, Model model) {
        Post post = postService.findById(id).orElseThrow(() -> new GlobalException("404-1", "해당 글이 존재하지 않습니다."));

        if (!postService.canModify(rq.getMember(), post)) throw new GlobalException("403-1", "권한이 없습니다.");

        model.addAttribute("post", post);

        return "domain/post/post/edit";
    }

    @Getter
    @Setter
    public static class EditForm {
        @NotBlank
        private String title;
        @NotBlank
        private String body;
        private boolean published;
        private int minMembershipLevel;
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}/edit")
    public String edit(@PathVariable long id, @Valid PostController.EditForm form) {
        Post post = postService.findById(id).orElseThrow(() -> new GlobalException("404-1", "해당 글이 존재하지 않습니다."));

        if (!postService.canModify(rq.getMember(), post)) throw new GlobalException("403-1", "권한이 없습니다.");

        postService.edit(
                post,
                form.getTitle(),
                form.getBody(),
                form.isPublished(),
                form.getMinMembershipLevel()
        );

        return rq.redirect("/post/" + post.getId(), post.getId() + "번 글이 수정되었습니다.");
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}/delete")
    public String delete(
            @PathVariable long id,
            String redirectUrl
    ) {
        if (redirectUrl == null) redirectUrl = "/post/list";

        Post post = postService.findById(id).orElseThrow(() -> new GlobalException("404-1", "해당 글이 존재하지 않습니다."));

        if (!postService.canDelete(rq.getMember(), post)) throw new GlobalException("403-1", "권한이 없습니다.");

        postService.delete(post);

        return rq.redirect(redirectUrl, post.getId() + "번 글이 삭제되었습니다.");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/like")
    public String like(
            @PathVariable long id,
            String redirectUrl
    ) {
        if (redirectUrl == null) redirectUrl = "/post/" + id;

        Post post = postService.findById(id).orElseThrow(() -> new GlobalException("404-1", "해당 글이 존재하지 않습니다."));

        if (!postService.canLike(rq.getMember(), post)) throw new GlobalException("403-1", "권한이 없습니다.");

        postService.like(rq.getMember(), post);

        return rq.redirect(redirectUrl, post.getId() + "번 글을 추천하였습니다.");
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}/cancelLike")
    public String cancelLike(
            @PathVariable long id,
            String redirectUrl
    ) {
        if (redirectUrl == null) redirectUrl = "/post/" + id;

        Post post = postService.findById(id).orElseThrow(() -> new GlobalException("404-1", "해당 글이 존재하지 않습니다."));

        if (!postService.canCancelLike(rq.getMember(), post)) throw new GlobalException("403-1", "권한이 없습니다.");

        postService.cancelLike(rq.getMember(), post);

        return rq.redirect(redirectUrl, post.getId() + "번 글을 추천취소하였습니다.");
    }
}
