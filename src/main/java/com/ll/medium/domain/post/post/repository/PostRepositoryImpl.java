package com.ll.medium.domain.post.post.repository;

import com.ll.medium.domain.member.member.entity.Member;
import com.ll.medium.domain.post.post.entity.Post;
import com.querydsl.core.BooleanBuilder;
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

import java.util.ArrayList;
import java.util.List;

import static com.ll.medium.domain.post.post.entity.QPost.post;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    public Page<Post> search(boolean published, List<String> kwTypes, String kw, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

       builder.and(post.published.eq(published));

        if (!kw.isBlank()) {
            // 기존의 조건을 리스트에 담습니다.
            List<BooleanExpression> conditions = new ArrayList<>();

            if (kwTypes.contains("authorUsername")) {
                conditions.add(post.author.username.eq(kw));
            }

            if (kwTypes.contains("title")) {
                conditions.add(post.title.containsIgnoreCase(kw));
            }

            if (kwTypes.contains("body")) {
                conditions.add(post.detailBody.val.containsIgnoreCase(kw));
            }

            // 조건 리스트를 or 조건으로 결합합니다.
            BooleanExpression combinedCondition = conditions.stream()
                    .reduce(BooleanExpression::or)
                    .orElse(null);

            // 최종적으로 생성된 조건을 쿼리에 적용합니다.
            if (combinedCondition != null) {
                builder.and(combinedCondition);
            }
        }

        JPAQuery<Post> postsQuery = jpaQueryFactory
                .select(post)
                .from(post)
                .where(builder);

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
                .where(builder);

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
