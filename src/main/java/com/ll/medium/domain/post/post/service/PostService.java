package com.ll.medium.domain.post.post.service;

import com.ll.medium.domain.base.genFile.entity.GenFile;
import com.ll.medium.domain.base.genFile.service.GenFileService;
import com.ll.medium.domain.member.member.entity.Member;
import com.ll.medium.domain.post.post.entity.Post;
import com.ll.medium.domain.post.post.entity.PostDetail;
import com.ll.medium.domain.post.post.repository.PostDetailRepository;
import com.ll.medium.domain.post.post.repository.PostRepository;
import com.ll.medium.domain.post.postComment.entity.PostComment;
import com.ll.medium.domain.post.postComment.repository.PostCommentRepository;
import com.ll.medium.domain.post.postLike.entity.PostLike;
import com.ll.medium.domain.post.postLike.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;
    private final PostDetailRepository postDetailRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCommentRepository postCommentRepository;    private final GenFileService genFileService;

    @Transactional
    public Post write(Member author, String title, String body, boolean published) {
        return write(author, title, body, published, 0);
    }
    @Transactional
    public Post write(Member author, String title, String body, boolean published, int minMembershipLevel){
        Post post = Post.builder()
                .author(author)
                .title(title)
                .published(published)
                .minMembershipLevel(minMembershipLevel)
                .build();

        postRepository.save(post);

        saveBody(post, body);

        return post;
    }

    public List<Post> findTop30ByPublishedOrderByIdDesc(boolean published) {
        return postRepository.findTop30ByPublishedOrderByIdDesc(published);
    }

    public Optional<Post> findById(long id) {
        return postRepository.findById(id);
    }

    public Page<Post> search(String kw, Pageable pageable) {
        return postRepository.search(true, kw, pageable);
    }

    public Page<Post> search(Member author, Boolean published, String kw, Pageable pageable) {
        return postRepository.search(author, published, kw, pageable);
    }

    public boolean canRead(Member actor, Post post){
        if (actor == null) {
            // 비회원인 경우 공개된 글만 볼 수 있다.
            if (post.isPublished() && post.getMinMembershipLevel() == 0) return true;
        } else {
            // 작성자인 경우 무조건 보기 가능
            if (post.getAuthor().equals(actor)) return true;

            // 관리자인 경우 무조건 보기 가능
            if (actor.isAdmin()) return true;

            // 그 외에는 공개되지 않은 글을 절대 볼 수 없다.
            if (!post.isPublished()) return false;

            // 공개되어 있다면 멤버십 레벨을 체크한다.
            if (actor.getMembershipLevel() >= post.getMinMembershipLevel()) return true;
        }

        return false;
    }

    public boolean canLike(Member actor, Post post) {
        if (actor == null) return false;
        if (!canRead(actor,post)) return false;

        return !post.hasLike(actor);
    }

    public boolean canCancelLike(Member actor, Post post) {
        if (actor == null) return false;

        return post.hasLike(actor);
    }

    public boolean canModify(Member actor, Post post) {
        if (actor == null) return false;

        return actor.equals(post.getAuthor());
    }

    public boolean canDelete(Member actor, Post post) {
        if (actor == null) return false;

        if (actor.isAdmin()) return true;

        return actor.equals(post.getAuthor());
    }

    @Transactional
    public void edit(Post post, String title, String body, boolean published, int minMembershipLevel) {
        post.setTitle(title);
        post.setPublished(published);
        post.setMinMembershipLevel(minMembershipLevel);

        saveBody(post, body);
    }

    private void saveBody(Post post, String body) {
        PostDetail detailBody = findDetail(post, "common__body");
        detailBody.setVal(body);
        post.setDetailBody(detailBody);
    }

    private PostDetail findDetail(Post post, String name) {
        Optional<PostDetail> opDetailBody = postDetailRepository.findByPostAndName(post, name);

        PostDetail detailBody = opDetailBody.orElseGet(() -> postDetailRepository.save(
                PostDetail.builder()
                        .post(post)
                        .name(name)
                        .build()
        ));

        return detailBody;
    }

    private List<GenFile> findGenFiles(Post post) {
        return genFileService.findByRelId(post.getModelName(), post.getId());
    }
    @Transactional
    public void delete(Post post) {
        findGenFiles(post).forEach(genFileService::remove);
        postDetailRepository.deleteByPost(post);
        postRepository.delete(post);
    }

    @Transactional
    public void increaseHit(Post post) {
        post.increaseHit();
    }

    @Transactional
    public void like(Member actor, Post post) {
        post.addLike(actor);
    }

    @Transactional
    public void cancelLike(Member actor, Post post) {
        post.deleteLike(actor);
    }

    @Transactional
    public PostComment writeComment(Member actor, Post post, String body) {
        return post.writeComment(actor, body);
    }

    public boolean canModifyComment(Member actor, PostComment comment) {
        if (actor == null) return false;

        return actor.equals(comment.getAuthor());
    }

    public boolean canDeleteComment(Member actor, PostComment comment) {
        if (actor == null) return false;

        if (actor.isAdmin()) return true;

        return actor.equals(comment.getAuthor());
    }

    public Optional<PostComment> findCommentById(long id) {
        return postCommentRepository.findCommentById(id);
    }

    @Transactional
    public void modifyComment(PostComment postComment, String body) {
        postComment.setBody(body);
    }

    @Transactional
    public void deleteComment(PostComment postComment) {
        postCommentRepository.delete(postComment);
    }

    public Post findTempOrMake(Member author) {
        return postRepository.findByAuthorAndPublishedAndTitle(author, false, "임시글")
                .orElseGet(() -> write(author, "임시글", "", false));
    }

    public List<PostLike> findLikesByPostInAndMember(List<Post> posts, Member member){
        return postLikeRepository.findByPostInAndMember(posts, member);
    }
}
