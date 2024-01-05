package com.ll.medium.domain.post.postComment.controller;

import com.ll.medium.domain.post.post.entity.Post;
import com.ll.medium.domain.post.post.service.PostService;
import com.ll.medium.domain.post.postComment.entity.PostComment;
import com.ll.medium.global.exceptions.GlobalException;
import com.ll.medium.global.rq.Rq.Rq;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/post/{id}/comment")
@RequiredArgsConstructor
public class PostCommentController {
    private final PostService postService;
    private final Rq rq;

    @Getter
    @Setter
    public static class WriteForm {
        @NotBlank
        private String body;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/write")
    public String write(
            @PathVariable long id,
            @Valid WriteForm form
    ) {
        Post post = postService.findById(id).orElseThrow(() -> new GlobalException("404-1", "해당 글이 존재하지 않습니다."));

        PostComment postComment = postService.writeComment(rq.getMember(), post, form.getBody());

        return rq.redirect("/post/" + id + "#postComment-" + postComment.getId(), "댓글이 작성되었습니다.");
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{commentId}/modify")
    public String showModify(
            @PathVariable long id,
            @PathVariable long commentId
    ) {
        Post post = postService.findById(id).orElseThrow(() -> new GlobalException("404-1", "해당 글이 존재하지 않습니다."));
        PostComment postComment = postService.findCommentById(commentId).orElseThrow(() -> new GlobalException("404-1", "해당 댓글이 존재하지 않습니다."));

        if (!postService.canModifyComment(rq.getMember(), postComment))
            throw new GlobalException("403-1", "해당 댓글을 수정할 권한이 없습니다.");

        rq.setAttribute("post", post);
        rq.setAttribute("postComment", postComment);

        return "domain/post/postComment/modify";
    }

    @Getter
    @Setter
    public static class ModifyForm {
        @NotBlank
        private String body;
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{commentId}/modify")
    public String modify(
            @PathVariable long id,
            @PathVariable long commentId,
            @Valid ModifyForm form
    ) {
        PostComment postComment = postService.findCommentById(commentId).orElseThrow(() -> new GlobalException("404-1", "해당 댓글이 존재하지 않습니다."));

        if (!postService.canModifyComment(rq.getMember(), postComment))
            throw new GlobalException("403-1", "해당 댓글을 수정할 권한이 없습니다.");

        postService.modifyComment(postComment, form.getBody());

        return rq.redirect("/post/" + id + "#postComment-" + postComment.getId(), "댓글이 수정되었습니다.");
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{commentId}/delete")
    public String delete(
            @PathVariable long id,
            @PathVariable long commentId
    ) {
        PostComment postComment = postService.findCommentById(commentId).orElseThrow(() -> new GlobalException("404-1", "해당 댓글이 존재하지 않습니다."));

        if (!postService.canDeleteComment(rq.getMember(), postComment))
            throw new GlobalException("403-1", "해당 댓글을 수정할 권한이 없습니다.");

        postService.deleteComment(postComment);

        return rq.redirect("/post/" + id, commentId + "번 댓글이 삭제되었습니다.");
    }
}
