# 云图库 API 接口文档

## 一、通用约定

- 基础路径：`/api`（上下文路径），端口 `8123`

### 1. 统一响应 BaseResponse<T>

所有接口统一返回 `BaseResponse<T>`：

```json
{
  "code": 0,        // 状态码（0 成功，非 0 失败）
  "data": {},       // 业务数据（可为 null）
  "message": "ok"   // 提示信息
}
```

```java
public class BaseResponse<T> {
    private int code;       // 状态码
    private T data;         // 数据
    private String message; // 提示信息
}
```

### 2. 错误码

| code | 说明 |
| --- | --- |
| 0 | 成功 |
| 40000 | 请求参数错误 |
| 40100 | 未登录 |
| 40101 | 无权限 |
| 40300 | 禁止访问 |
| 40400 | 请求数据不存在 |
| 50000 | 系统内部异常 |
| 50001 | 操作失败 |

### 3. 统一异常处理

由 `GlobalExceptionHandler` 全局捕获异常，统一转成 `BaseResponse`：

| 异常 | 处理结果 |
| --- | --- |
| `BusinessException`（业务异常） | 返回业务错误码与 message |
| 参数校验异常 | 返回 40000 |
| Sa-Token 未登录 / 无权限 | 返回 40100 / 40101 |
| 其他 `Exception` | 兜底返回 50000 |

- 业务异常类：`BusinessException`（继承 RuntimeException，携带 code + message）
- 统一返回工具：`ResultUtils.success(data)` / `ResultUtils.error(code, message)`

### 4. 鉴权方式

- 注册 / 登录接口无需登录；
- 其余接口需**用户登录**（默认 `StpUtil`，登录态存 Redis）；
- 系统角色分三级：`user` / `admin` / `super_admin`，其中 `super_admin` 继承 `admin` 全部权限，并可管理其他用户的角色；
- 空间内图片/成员操作需**空间权限**（`@SaSpaceCheckPermission`）。

### 5. 权限码

`picture:view` / `picture:upload` / `picture:edit` / `picture:delete` / `spaceUser:manage`

## 二、用户模块

### 1. 注册

- `POST /api/user/register`
- 权限：无

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| userAccount | string | 账号 |
| userPassword | string | 密码 |
| checkPassword | string | 确认密码 |

### 2. 登录

- `POST /api/user/login`
- 权限：无

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| userAccount | string | 账号 |
| userPassword | string | 密码 |

响应 `data`：用户脱敏信息 + token。

### 3. 获取当前登录用户

- `GET /api/user/get/login`
- 权限：登录

响应 `data`：当前用户脱敏信息（不返回密码等敏感字段）。

### 4. 退出登录

- `POST /api/user/logout`
- 权限：登录

### 5. 修改用户角色（仅高级管理员）

- `PUT /api/user/role`
- 权限：登录 + 高级管理员（`super_admin`）

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| targetUserId | long | 目标用户 ID |
| userRole | string | 目标角色：user / admin / super_admin |

说明：仅 `super_admin` 可调用；不能修改自己的角色；不能移除系统中最后一个 `super_admin`。

## 三、图片模块

### 1. 上传图片

- `POST /api/picture/upload`
- 权限：登录 + `@SaSpaceCheckPermission(picture:upload)`

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| file | multipart/file | 本地文件（与 fileUrl 二选一） |
| fileUrl | string | 网络图片 URL（与 file 二选一） |
| spaceId | long | 所属空间 ID（空 = 公共图库） |
| picName | string | 图片名称（可选，不传则用原始文件名） |
| category | string | 图片分类（可选，用于分类分析） |
| tags | string | 图片标签（可选，JSON 数组字符串，用于标签分析） |

说明：上传后由 COS 存储、CI 完成图片处理，并把图片对象加入 CI 图库（entityId 保存图片 ID）。

### 2. 分页查询图片

- `GET /api/picture/list/page/vo`
- 权限：登录（公共图库可看，空间图片按权限）

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| current | int | 当前页 |
| pageSize | int | 每页大小 |
| spaceId | long | 空间 ID（空 = 公共图库） |
| searchText | string | 名称关键词 |
| picColor | string | 主色调（精确匹配，相似度搜索见「按颜色搜索」接口） |

### 3. 图片详情

- `GET /api/picture/get/{id}`
- 权限：登录 + `picture:view`

### 4. 编辑图片

- `POST /api/picture/edit`
- 权限：登录 + `@SaSpaceCheckPermission(picture:edit)`

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| id | long | 图片 ID |
| name | string | 名称 |
| category | string | 图片分类（可选，用于分类分析） |
| tags | string | 图片标签（可选，JSON 数组字符串，用于标签分析） |
| spaceId | long | 目标空间 |

### 5. 删除图片

- `POST /api/picture/delete`
- 权限：登录 + `@SaSpaceCheckPermission(picture:delete)`

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| id | long | 图片 ID |

说明：同时删除 COS 文件与 CI 图库对象（出库）。

### 6. 批量抓取上传

- `POST /api/picture/upload/batch`
- 权限：登录

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| searchText | string | 抓取关键词 |
| count | int | 抓取数量 |
| spaceId | long | 目标空间 |

### 7. 以图搜图

- `POST /api/picture/search/by/picture`
- 权限：登录 + `picture:view`

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| file | multipart/file | 查询图片（或 fileUrl） |
| spaceId | long | 限定搜索的空间（可选） |
| topK | int | 返回最相似数量，默认 20 |

说明：查询图临时上传到 COS → CI 图库检索相似图片 ID → 回 MySQL 查详情。

### 8. 按颜色搜索

- `GET /api/picture/search/color`
- 权限：登录（公共图库可搜，空间图片按权限）

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| picColor | string | 目标主色调（十六进制，如 `0xff0000`） |
| spaceId | long | 空间 ID（空 = 公共图库） |
| topN | int | 返回相近图片数量，默认 20 |

说明：将目标主色调转为 RGB，与各图片主色调计算欧氏距离，按距离升序返回相近图片。

## 四、空间模块

### 1. 创建空间

- `POST /api/space/add`
- 权限：登录

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| spaceName | string | 空间名称 |
| spaceType | int | 0 私有 / 1 团队 |

### 2. 编辑空间

- `POST /api/space/edit`
- 权限：登录（创建者或管理员）

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| id | long | 空间 ID |
| spaceName | string | 新名称 |

### 3. 删除空间

- `POST /api/space/delete`
- 权限：登录（创建者或管理员）

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| id | long | 空间 ID |

### 4. 空间详情

- `GET /api/space/get/{id}`
- 权限：登录

### 5. 我的空间列表

- `GET /api/space/list`
- 权限：登录

## 五、空间成员模块

### 1. 添加成员

- `POST /api/spaceUser/add`
- 权限：登录 + `@SaSpaceCheckPermission(spaceUser:manage)`

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| spaceId | long | 空间 ID |
| userId | long | 被添加用户 ID |
| spaceRole | string | viewer / editor / admin |

### 2. 修改成员角色

- `POST /api/spaceUser/edit`
- 权限：登录 + `@SaSpaceCheckPermission(spaceUser:manage)`

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| spaceId | long | 空间 ID |
| userId | long | 成员用户 ID |
| spaceRole | string | 新角色 |

### 3. 移除成员

- `POST /api/spaceUser/delete`
- 权限：登录 + `@SaSpaceCheckPermission(spaceUser:manage)`

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| spaceId | long | 空间 ID |
| userId | long | 成员用户 ID |

### 4. 成员列表

- `GET /api/spaceUser/list`
- 权限：登录 + `picture:view`

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| spaceId | long | 空间 ID |

### 5. 获取当前用户在某空间的权限

- `GET /api/spaceUser/get`
- 权限：登录

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| spaceId | long | 空间 ID |

响应 `data`：当前用户在该空间的角色与权限码列表。

## 六、空间分析模块

> 说明：除排行外，各接口均需登录且拥有空间查看权限（`picture:view`）。
> `spaceId` 为空表示统计公共图库。

### 1. 空间使用分析

- `GET /api/space/analyze/usage`
- 权限：登录（空间成员）

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| spaceId | long | 空间 ID（空 = 公共图库） |

响应 `data`：`usedCount` 已用数量、`usedSize` 已用总大小、`maxCount` 数量上限、`maxSize` 大小上限、`countUsageRatio` 数量使用率、`sizeUsageRatio` 大小使用率。

### 2. 分类分析

- `GET /api/space/analyze/category`
- 权限：登录（空间成员）

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| spaceId | long | 空间 ID（空 = 公共图库） |

响应 `data`：按图片分类分组的列表（`category` 分类、`count` 数量、`totalSize` 总大小）。

### 3. 标签分析

- `GET /api/space/analyze/tag`
- 权限：登录（空间成员）

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| spaceId | long | 空间 ID（空 = 公共图库） |

响应 `data`：按图片标签统计的列表（`tag` 标签名、`count` 数量、`totalSize` 总大小）。

### 4. 大小分析

- `GET /api/space/analyze/size`
- 权限：登录（空间成员）

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| spaceId | long | 空间 ID（空 = 公共图库） |

响应 `data`：按大小区间分组的列表（`sizeRange` 区间、`count` 数量），区间为 `<100KB` / `100KB-500KB` / `500KB-1MB` / `>1MB` / `未知`。

### 5. 空间用量排行

- `GET /api/space/analyze/rank`
- 权限：登录（系统管理员）

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| topN | int | 返回前 N 个空间，默认 10 |

响应 `data`：按图片总大小降序的列表（`spaceId` 空间 ID、`count` 图片数量、`totalSize` 总大小）。

## 七、其他

### 1. 缓存测试

- `GET /api/cache/test`
- 权限：无（演示 Caffeine + Redis 两级缓存）
