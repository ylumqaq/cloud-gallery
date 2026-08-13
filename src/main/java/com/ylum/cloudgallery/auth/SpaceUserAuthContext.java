package com.ylum.cloudgallery.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 空间权限上下文工具。
 *
 * <p>用于从当前请求中解析 {@code spaceId}。由于 {@code spaceId} 可能以
 * 请求参数（query / form）或请求体（@RequestBody）形式传递，本工具仅负责
 * 从请求参数中解析，请求体场景由 {@link SpaceUserAuthAspect} 结合方法参数解析。</p>
 */
public class SpaceUserAuthContext {

    /** 请求参数名：空间 ID */
    private static final String PARAM_SPACE_ID = "spaceId";

    private SpaceUserAuthContext() {
    }

    /**
     * 从当前请求的 query / form 参数中解析 spaceId。
     *
     * @return 空间 ID，解析不到时返回 null
     */
    public static Long getSpaceId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        String spaceId = request.getParameter(PARAM_SPACE_ID);
        if (spaceId == null || spaceId.isBlank()) {
            return null;
        }
        return Long.valueOf(spaceId);
    }
}
