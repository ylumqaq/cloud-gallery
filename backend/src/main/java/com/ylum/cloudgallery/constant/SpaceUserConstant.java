package com.ylum.cloudgallery.constant;

/**
 * 空间成员与权限相关常量。
 */
public interface SpaceUserConstant {

    /** 角色：浏览者 */
    String ROLE_VIEWER = "viewer";

    /** 角色：编辑者 */
    String ROLE_EDITOR = "editor";

    /** 角色：管理员 */
    String ROLE_ADMIN = "admin";

    /** 权限码：查看图片 */
    String PERMISSION_PICTURE_VIEW = "picture:view";

    /** 权限码：上传图片 */
    String PERMISSION_PICTURE_UPLOAD = "picture:upload";

    /** 权限码：编辑图片 */
    String PERMISSION_PICTURE_EDIT = "picture:edit";

    /** 权限码：删除图片 */
    String PERMISSION_PICTURE_DELETE = "picture:delete";

    /** 权限码：管理空间成员 */
    String PERMISSION_SPACE_USER_MANAGE = "spaceUser:manage";
}
