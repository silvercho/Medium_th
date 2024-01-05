package com.ll.medium.domain.post.post.repository;

import com.ll.medium.domain.member.member.entity.Member;
import com.ll.medium.domain.post.post.entity.Post;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;

import static com.ll.medium.domain.post.post.entity.QPost.post;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    public Page<Post> search(boolean published, String kw, Pageable pageable) {

        // 조건 생성
        BooleanExpression condition = post
                .published.eq(published);

        if (kw != null && !kw.isBlank()) {
            condition = condition.and(
                    post.title.containsIgnoreCase(kw)
                            .or(post.detailBody.val.containsIgnoreCase(kw))
            );
        }

        JPAQuery<Post> postsQuery = jpaQueryFactory
                .select(post)
                .from(post)
                .where(condition);

        for (Sort.Order o : pageable.getSort()) {
            PathBuilder pathBuilder = new PathBuilder(post.getType(), post.getMetadata());
            postsQuery.orderBy(new OrderSpecifier(o.isAscending() ? Order.ASC : Order.DESC, pathBuilder.get(o.getProperty())));
        }

        postsQuery
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 전체 개수를 가져오기 위한 쿼리
        JPAQuery<Long> totalQuery = jpaQueryFactory
                .select(post.count())
                .from(post)
                .where(condition);

        return PageableExecutionUtils.getPage(postsQuery.fetch(), pageable, totalQuery::fetchCount);
    }

    @Override
    public Page<Post> search(Member author, Boolean published, String kw, Pageable pageable) {
        // 조건 생성
        BooleanExpression condition = post
                .author.eq(author);

        if (published != null) {
            condition = condition.and(
                    post.published.eq(published)
            );
        }

        if (kw != null && !kw.isBlank()) {
            condition = condition.and(
                    post.title.containsIgnoreCase(kw)
                            .or(post.detailBody.val.containsIgnoreCase(kw))
            );
        }

        JPAQuery<Post> postsQuery = jpaQueryFactory
                .select(post)
                .from(post)
                .where(condition);

        for (Sort.Order o : pageable.getSort()) {
            PathBuilder pathBuilder = new PathBuilder(post.getType(), post.getMetadata());
            postsQuery.orderBy(new OrderSpecifier(o.isAscending() ? Order.ASC : Order.DESC, pathBuilder.get(o.getProperty())));
        }

        postsQuery
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 전체 개수를 가져오기 위한 쿼리
        JPAQuery<Long> totalQuery = jpaQueryFactory
                .select(post.count())
                .from(post)
                .where(condition);

        return PageableExecutionUtils.getPage(postsQuery.fetch(), pageable, totalQuery::fetchCount);
    }
}
