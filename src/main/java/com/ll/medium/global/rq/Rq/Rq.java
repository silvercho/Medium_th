package com.ll.medium.global.rq.Rq;

import com.ll.medium.domain.member.member.entity.Member;
import com.ll.medium.global.rsData.RsData.RsData;
import com.ll.medium.global.security.SecurityUser;
import com.ll.medium.standard.util.Ut.Ut;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Optional;

@Component
@RequestScope
@RequiredArgsConstructor
public class Rq {
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final EntityManager entityManager;
    private Member member;

    public String redirect(String url, String msg) {
        String[] urlBits = url.split("#", 2);
        url = urlBits[0];

        StringBuilder sb = new StringBuilder();

        sb.append("redirect:");

        url = Ut.url.deleteQueryParam(url, "msg");

        sb.append(url);

        if (Ut.str.hasLength(msg)) {
            msg = Ut.url.encode(msg);

            if (url.contains("?")) {
                sb.append("&msg=");
            } else {
                sb.append("?msg=");
            }

            sb.append(msg);
        }

        if (urlBits.length == 2) {
            sb.append("#");
            sb.append(urlBits[1]);
        }

        return sb.toString();
    }

    public String historyBack(String msg) {
        request.setAttribute("failMsg", msg);

        return "global/js";
    }

    public String redirectOrBack(RsData<?> rs, String path) {
        if (rs.isFail()) return historyBack(rs.getMsg());

        return redirect(path, rs.getMsg());
    }

    public SecurityUser getUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .filter(it -> it instanceof SecurityUser)
                .map(it -> (SecurityUser) it)
                .orElse(null);
    }

    public boolean isLogin() {
        return getUser() != null;
    }

    public boolean isLogout() {
        return !isLogin();
    }

    public boolean isAdmin() {
        if (isLogout()) return false;

        return getUser()
                .getAuthorities()
                .stream()
                .anyMatch(it -> it.getAuthority().equals("ROLE_ADMIN"));
    }

    public void setAttribute(String key, Object value) {
        request.setAttribute(key, value);
    }

    public String getCurrentQueryStringWithoutParam(String paramName) {
        String queryString = request.getQueryString();

        if (queryString == null) {
            return "";
        }

        queryString = Ut.url.deleteQueryParam(queryString, paramName);

        return queryString;
    }

    public Member getMember() {
        if (isLogout()) return null;

        if (member == null) {
            member = entityManager.getReference(Member.class, getUser().getId());
        }

        return member;
    }

    public String getEncodedCurrentUrl() {
        return Ut.url.encode(getCurrentUrl());
    }

    private String getCurrentUrl() {
        String url = request.getRequestURI();
        String queryString = request.getQueryString();

        if (queryString != null) {
            url += "?" + queryString;
        }

        return url;
    }
}
