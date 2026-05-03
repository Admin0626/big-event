# 文章功能完整CRUD设计文档

**日期**: 2026-05-03
**项目**: big-event-lab
**状态**: 待实现

## 概述

为"大事件"文章管理系统实现完整的文章 CRUD 功能，包括后端的分页查询、新增、编辑、删除、详情接口，以及前端的文章列表管理、编辑、删除和详情查看页面。封面图采用 Base64 编码直接存储在数据库 `cover_img` 字段中。

## 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Spring Boot 3.5.14, MyBatis-Plus 3.5.15, MySQL 8.0, JWT |
| 前端 | Vue 3.3.4, Element Plus 2.4.1, Pinia, Vue Router 4.2.5, Quill Editor |

## 现有代码分析

### 已存在
- `Article.java` 实体类：包含 id, title, content, coverImg, state, categoryId, createUser, createTime, updateTime 字段
- `ArticleMapper.java`：继承 BaseMapper，已具备基础 CRUD 能力
- `article` 数据库表：已在 init.sql 中定义
- 前端 `ArticleManage.vue`：包含搜索表单、表格、分页组件、添加文章抽屉（Quill 编辑器），但编辑和删除按钮未绑定逻辑
- 前端 `article.js`：已定义 `articleListService`、`articleAddService`

### 需要新建
- 后端：`ArticleController.java`、`ArticleService.java`、`ArticleServiceImpl.java`、`PageResult.java`
- 前端：`ArticleDetail.vue`
- 前端 API：`articleUpdateService`、`articleDeleteService`、`articleDetailService`

### 需要修改
- 前端 `ArticleManage.vue`：绑定编辑、删除逻辑，封面图改为 Base64 本地转换
- 前端 `router/index.js`：添加详情页路由

## 后端设计

### 新增类

#### PageResult.java（分页结果封装）
- 位置：`com.bigevent.lab.common.PageResult`
- 字段：`total`（Long）、`items`（List<T>）
- 泛型类，用于包装分页查询结果

#### ArticleService.java（接口）
```
PageResult<Article> pageList(Integer pageNum, Integer pageSize, Integer categoryId, String state, Integer userId)
void add(Article article, Integer userId)
void update(Article article, Integer userId)
void deleteById(Integer id, Integer userId)
Article getById(Integer id, Integer userId)
```

#### ArticleServiceImpl.java（实现）
- `pageList`：使用 MyBatis-Plus 的 `Page<Article>` 分页插件，构建 `LambdaQueryWrapper` 按 categoryId、state、createUser 条件过滤，按 create_time 倒序
- `add`：设置 createUser、createTime、updateTime，插入数据库
- `update`：先校验文章存在且归属权一致，再执行更新（不修改 createUser 和 createTime）
- `deleteById`：先校验文章存在且归属权一致，再物理删除
- `getById`：查询文章详情，校验归属权

#### ArticleController.java
| 方法 | 路径 | 参数 | 说明 |
|------|------|------|------|
| GET | `/article` | pageNum, pageSize, categoryId(可选), state(可选) | 分页查询当前用户文章列表 |
| POST | `/article` | @RequestBody Article | 新增文章 |
| PUT | `/article` | @RequestBody Article | 编辑文章 |
| DELETE | `/article` | id（query） | 删除文章 |
| GET | `/article/detail` | id（query） | 查看文章详情 |

所有接口从 ThreadLocal 获取 userId，所有写操作校验归属权。

### 分页配置

需要在 Spring Boot 配置类中注册 MyBatis-Plus 分页插件 `PaginationInnerInterceptor`。检查 `WebConfig.java` 是否已配置，如未配置则需添加 `@Bean MybatisPlusInterceptor`。

### 校验规则

- `title`：必填，@NotBlank，长度 1-100
- `content`：必填，@NotBlank，长度 1-50000
- `state`：必填，值限定为"草稿"或"已发布"
- `category_id`：必填，需为正整数
- `cover_img`：可选，若提供则存储为 Base64 字符串（LONGTEXT）

## 前端设计

### 新增文件

#### ArticleDetail.vue（文章详情页）
- 路由路径：`/article/detail`
- 布局：标题（大字）→ 元信息行（分类、状态、发布时间）→ 封面图（居中）→ 正文（v-html 渲染 HTML 内容）→ 返回按钮 + 编辑按钮 + 删除按钮
- 加载时通过 `articleDetailService` 获取数据
- 删除操作需 ElMessageBox.confirm 确认，确认后调用 articleDeleteService 并 router.push('/article/manage') 返回列表
- 编辑操作使用 router.push({ path: '/article/manage', query: { editId: article.id } }) 跳转回管理页，ArticleManage.vue 在 onMounted 中检测 editId 参数，若存在则自动加载文章数据并打开编辑抽屉

### 修改文件

#### article.js（新增 API 函数）
```javascript
// 文章更新
export const articleUpdateService = (articleData) => request.put('/article', articleData)

// 文章删除
export const articleDeleteService = (id) => request.delete('/article?id=' + id)

// 文章详情
export const articleDetailService = (id) => request.get('/article/detail', { params: { id } })
```

#### ArticleManage.vue（完善交互逻辑）
1. **编辑功能**：
   - 新增 `isEdit` 状态变量区分新增/编辑模式
   - 编辑按钮点击：将行数据填充到 articleModel，打开抽屉
   - 提交时根据 isEdit 调用 articleAddService 或 articleUpdateService
   - 抽屉标题动态显示"添加文章"或"编辑文章"

2. **删除功能**：
   - 删除按钮点击：ElMessageBox.confirm 弹出确认
   - 确认后调用 articleDeleteService，成功后 ElMessage.success 提示并刷新列表

3. **封面图改为 Base64**：
   - 移除 el-upload 组件，改为原生 `<input type="file">` + 预览图
   - 监听 change 事件，使用 FileReader.readAsDataURL() 将文件转为 Base64 字符串
   - 将 Base64 字符串赋值给 articleModel.coverImg

4. **分页组件**：
   - 已有 `<el-pagination>` 组件，无需新增
   - 确保 articleList 调用时使用 pageNum、pageSize 参数

#### router/index.js
- 导入 ArticleDetailVue
- 在 LayoutVue children 中添加路由：`{ path: '/article/detail', component: ArticleDetailVue }`

## 错误处理

### 后端
- 文章不存在：throw RuntimeException("文章不存在")
- 无权限操作：throw RuntimeException("无权操作该文章")
- 全局异常处理器 GlobalExceptionHandler 统一捕获，返回 Result.error(msg)

### 前端
- 表单校验失败：Element Plus 表单 validate 拦截
- 接口报错：axios 响应拦截器 + ElMessage.error 展示
- 删除取消：不显示任何提示
- 详情页文章不存在：ElMessage.warning 提示并 router.push 回列表页

## 数据流图

```
新增: 前端表单 → POST /article → Service 设置 userId → insert → 返回列表刷新
编辑: 前端表单 → PUT /article → Service 校验归属 → updateById → 返回列表刷新
删除: 前端确认 → DELETE /article?id → Service 校验归属 → deleteById → 刷新列表
详情: 前端请求 → GET /article/detail?id → Service 校验归属 → selectById → 渲染页面
列表: 前端请求 → GET /article?params → Service Page查询 → 返回 PageResult → 渲染表格
```

## 数据库

`article` 表需要修改 `cover_img` 字段：当前类型为 VARCHAR(255)，需改为 LONGTEXT 以存储完整的 Base64 图片字符串。提供 ALTER TABLE 迁移 SQL 同步更新 init.sql 和已存在的数据库。
