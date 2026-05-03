# 文章功能完整CRUD实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现文章完整的增删改查功能，包括后端分页接口和前端管理页面、详情页面。

**Architecture:** 后端新增 ArticleController + ArticleService/Impl + PageResult，使用 MyBatis-Plus 分页插件；前端修改 ArticleManage.vue 绑定编辑/删除逻辑并改用 Base64 封面图，新增 ArticleDetail.vue 详情页。

**Tech Stack:** Spring Boot 3.5.14, MyBatis-Plus 3.5.15, MySQL 8.0, Vue 3.3.4, Element Plus 2.4.1

---

## 文件清单

| 操作 | 文件路径 | 职责 |
|------|----------|------|
| 新建 | `big-event-back/demo/src/main/java/com/bigevent/lab/common/PageResult.java` | 分页结果泛型封装 |
| 新建 | `big-event-back/demo/src/main/java/com/bigevent/lab/service/ArticleService.java` | 文章业务接口 |
| 新建 | `big-event-back/demo/src/main/java/com/bigevent/lab/service/impl/ArticleServiceImpl.java` | 文章业务实现 |
| 新建 | `big-event-back/demo/src/main/java/com/bigevent/lab/controller/ArticleController.java` | 文章 REST 接口 |
| 修改 | `big-event-back/demo/src/main/java/com/bigevent/lab/config/WebConfig.java` | 添加 MyBatis-Plus 分页插件配置 |
| 修改 | `big-event-back/demo/sql/init.sql` | 修改 cover_img 字段为 LONGTEXT |
| 修改 | `big-event-front/src/api/article.js` | 新增更新、删除、详情 API 函数 |
| 修改 | `big-event-front/src/views/article/ArticleManage.vue` | 绑定编辑/删除、Base64 封面图、编辑模式 |
| 新建 | `big-event-front/src/views/article/ArticleDetail.vue` | 文章详情页 |
| 修改 | `big-event-front/src/router/index.js` | 添加详情页路由 |

---

### Task 1: 数据库迁移 — 修改 cover_img 字段类型

**Files:**
- Modify: `big-event-back/demo/sql/init.sql`

- [ ] **Step 1: 修改 init.sql 中 article 表的 cover_img 字段**

将第 52 行 `cover_img VARCHAR(255) DEFAULT ''` 改为 `cover_img LONGTEXT DEFAULT ''`

```sql
-- 原第52行:
`cover_img`   VARCHAR(255)    DEFAULT ''               COMMENT '封面图URL',
-- 改为:
`cover_img`   LONGTEXT                                 COMMENT '封面图Base64数据',
```

- [ ] **Step 2: 添加 ALTER TABLE 迁移注释**

在文件头部（USE experiment_db; 之后）添加迁移说明注释：

```sql
-- 迁移: 如果数据库已存在，执行以下 SQL 更新字段类型
-- ALTER TABLE article MODIFY COLUMN cover_img LONGTEXT COMMENT '封面图Base64数据';
```

- [ ] **Step 3: Commit**

```bash
git add big-event-back/demo/sql/init.sql
git commit -m "db: change article.cover_img to LONGTEXT for Base64 storage"
```

---

### Task 2: 后端基础 — PageResult 分页封装类

**Files:**
- Create: `big-event-back/demo/src/main/java/com/bigevent/lab/common/PageResult.java`

- [ ] **Step 1: 创建 PageResult.java**

```java
package com.bigevent.lab.common;

import lombok.Data;

import java.util.List;

/**
 * 分页结果封装
 */
@Data
public class PageResult<T> {

    private Long total;
    private List<T> items;

    public PageResult() {
    }

    public PageResult(Long total, List<T> items) {
        this.total = total;
        this.items = items;
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add big-event-back/demo/src/main/java/com/bigevent/lab/common/PageResult.java
git commit -m "feat: add PageResult generic pagination wrapper"
```

---

### Task 3: 后端基础 — 注册 MyBatis-Plus 分页插件

**Files:**
- Modify: `big-event-back/demo/src/main/java/com/bigevent/lab/config/WebConfig.java`

- [ ] **Step 1: 在 WebConfig 中添加分页插件 Bean**

在 WebConfig.java 文件末尾（`addResourceHandlers` 方法之后）添加：

```java
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;

// ... existing code ...

    /**
     * MyBatis-Plus 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        interceptor.addInnerInterceptor(paginationInterceptor);
        return interceptor;
    }
```

- [ ] **Step 2: 编译验证**

```bash
cd big-event-back/demo && mvn compile -q
```

Expected: BUILD SUCCESS, no errors.

- [ ] **Step 3: Commit**

```bash
git add big-event-back/demo/src/main/java/com/bigevent/lab/config/WebConfig.java
git commit -m "feat: register MyBatis-Plus pagination interceptor"
```

---

### Task 4: 后端业务 — ArticleService 接口

**Files:**
- Create: `big-event-back/demo/src/main/java/com/bigevent/lab/service/ArticleService.java`

- [ ] **Step 1: 创建 ArticleService.java**

```java
package com.bigevent.lab.service;

import com.bigevent.lab.common.PageResult;
import com.bigevent.lab.entity.Article;

/**
 * 文章Service接口
 */
public interface ArticleService {

    /**
     * 分页查询当前用户的文章列表
     */
    PageResult<Article> pageList(Integer pageNum, Integer pageSize, Integer categoryId, String state, Integer userId);

    /**
     * 新增文章
     */
    void add(Article article, Integer userId);

    /**
     * 更新文章（校验归属权）
     */
    void update(Article article, Integer userId);

    /**
     * 根据ID查询文章详情（校验归属权）
     */
    Article getById(Integer id, Integer userId);

    /**
     * 删除文章（校验归属权）
     */
    void deleteById(Integer id, Integer userId);
}
```

- [ ] **Step 2: Commit**

```bash
git add big-event-back/demo/src/main/java/com/bigevent/lab/service/ArticleService.java
git commit -m "feat: add ArticleService interface"
```

---

### Task 5: 后端业务 — ArticleServiceImpl 实现

**Files:**
- Create: `big-event-back/demo/src/main/java/com/bigevent/lab/service/impl/ArticleServiceImpl.java`

- [ ] **Step 1: 创建 ArticleServiceImpl.java**

```java
package com.bigevent.lab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bigevent.lab.common.PageResult;
import com.bigevent.lab.entity.Article;
import com.bigevent.lab.mapper.ArticleMapper;
import com.bigevent.lab.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 文章Service实现类
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public PageResult<Article> pageList(Integer pageNum, Integer pageSize, Integer categoryId, String state, Integer userId) {
        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getCreateUser, userId);
        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }
        if (state != null && !state.isEmpty()) {
            wrapper.eq(Article::getState, state);
        }
        wrapper.orderByDesc(Article::getCreateTime);
        Page<Article> result = articleMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    @Override
    public void add(Article article, Integer userId) {
        article.setCreateUser(userId);
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        articleMapper.insert(article);
    }

    @Override
    public void update(Article article, Integer userId) {
        Article existing = articleMapper.selectById(article.getId());
        if (existing == null) {
            throw new RuntimeException("文章不存在");
        }
        if (!existing.getCreateUser().equals(userId)) {
            throw new RuntimeException("无权操作该文章");
        }

        Article updateArticle = new Article();
        updateArticle.setId(article.getId());
        updateArticle.setTitle(article.getTitle());
        updateArticle.setContent(article.getContent());
        updateArticle.setCoverImg(article.getCoverImg());
        updateArticle.setState(article.getState());
        updateArticle.setCategoryId(article.getCategoryId());
        updateArticle.setUpdateTime(LocalDateTime.now());
        articleMapper.updateById(updateArticle);
    }

    @Override
    public Article getById(Integer id, Integer userId) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }
        if (!article.getCreateUser().equals(userId)) {
            throw new RuntimeException("无权查看该文章");
        }
        return article;
    }

    @Override
    public void deleteById(Integer id, Integer userId) {
        Article existing = articleMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("文章不存在");
        }
        if (!existing.getCreateUser().equals(userId)) {
            throw new RuntimeException("无权操作该文章");
        }
        articleMapper.deleteById(id);
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
cd big-event-back/demo && mvn compile -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add big-event-back/demo/src/main/java/com/bigevent/lab/service/impl/ArticleServiceImpl.java
git commit -m "feat: implement ArticleService with CRUD and ownership validation"
```

---

### Task 6: 后端接口 — ArticleController

**Files:**
- Create: `big-event-back/demo/src/main/java/com/bigevent/lab/controller/ArticleController.java`

- [ ] **Step 1: 创建 ArticleController.java**

```java
package com.bigevent.lab.controller;

import com.bigevent.lab.common.PageResult;
import com.bigevent.lab.common.Result;
import com.bigevent.lab.entity.Article;
import com.bigevent.lab.service.ArticleService;
import com.bigevent.lab.utils.ThreadLocalUtil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 文章控制器
 */
@RestController
@RequestMapping("/article")
@Validated
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    /**
     * GET /article
     * 分页查询当前用户的文章列表
     */
    @GetMapping
    public Result<PageResult<Article>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "5") Integer pageSize,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String state) {
        Integer userId = ThreadLocalUtil.get("userId");
        PageResult<Article> result = articleService.pageList(pageNum, pageSize, categoryId, state, userId);
        return Result.success(result);
    }

    /**
     * POST /article
     * 新增文章
     */
    @PostMapping
    public Result<Void> add(@Validated @RequestBody Article article) {
        Integer userId = ThreadLocalUtil.get("userId");
        articleService.add(article, userId);
        return Result.success();
    }

    /**
     * PUT /article
     * 更新文章
     */
    @PutMapping
    public Result<Void> update(@Validated @RequestBody Article article) {
        Integer userId = ThreadLocalUtil.get("userId");
        articleService.update(article, userId);
        return Result.success();
    }

    /**
     * GET /article/detail
     * 查看文章详情
     */
    @GetMapping("/detail")
    public Result<Article> detail(@RequestParam("id") Integer id) {
        Integer userId = ThreadLocalUtil.get("userId");
        Article article = articleService.getById(id, userId);
        return Result.success(article);
    }

    /**
     * DELETE /article
     * 删除文章
     */
    @DeleteMapping
    public Result<Void> delete(@RequestParam("id") Integer id) {
        Integer userId = ThreadLocalUtil.get("userId");
        articleService.deleteById(id, userId);
        return Result.success();
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
cd big-event-back/demo && mvn compile -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add big-event-back/demo/src/main/java/com/bigevent/lab/controller/ArticleController.java
git commit -m "feat: add ArticleController with 5 REST endpoints"
```

---

### Task 7: 前端 API — 新增文章更新、删除、详情接口函数

**Files:**
- Modify: `big-event-front/src/api/article.js`

- [ ] **Step 1: 在 article.js 末尾追加三个 API 函数**

```javascript
// 文章更新
export const articleUpdateService = (articleData) => {
    return request.put('/article', articleData);
}

// 文章删除
export const articleDeleteService = (id) => {
    return request.delete('/article?id=' + id);
}

// 文章详情
export const articleDetailService = (id) => {
    return request.get('/article/detail', { params: { id } });
}
```

- [ ] **Step 2: Commit**

```bash
git add big-event-front/src/api/article.js
git commit -m "feat: add article update, delete, detail API functions"
```

---

### Task 8: 前端页面 — 完善 ArticleManage.vue 编辑/删除/Base64功能

**Files:**
- Modify: `big-event-front/src/views/article/ArticleManage.vue`

- [ ] **Step 1: 完整替换 ArticleManage.vue 的 script 部分**

新的 script setup 代码：

```vue
<script setup>
import { Edit, Delete } from '@element-plus/icons-vue'
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
    articleCategoryListService,
    articleListService,
    articleAddService,
    articleUpdateService,
    articleDeleteService
} from '@/api/article.js'

const route = useRoute()

// 文章分类数据
const categorys = ref([])

// 搜索条件
const categoryId = ref('')
const state = ref('')

// 文章列表
const articles = ref([])

// 分页
const pageNum = ref(1)
const total = ref(0)
const pageSize = ref(5)

const onSizeChange = (size) => {
    pageSize.value = size
    articleList()
}

const onCurrentChange = (num) => {
    pageNum.value = num
    articleList()
}

// 加载分类列表
const articleCategoryList = async () => {
    let result = await articleCategoryListService()
    categorys.value = result.data
}

// 加载文章列表
const articleList = async () => {
    let params = {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        categoryId: categoryId.value || null,
        state: state.value || null
    }
    let result = await articleListService(params)
    total.value = result.data.total
    articles.value = result.data.items

    // 扩展分类名称
    for (let i = 0; i < articles.value.length; i++) {
        let article = articles.value[i]
        for (let j = 0; j < categorys.value.length; j++) {
            if (article.categoryId === categorys.value[j].id) {
                article.categoryName = categorys.value[j].categoryName
                break
            }
        }
    }
}

// 抽屉控制
const visibleDrawer = ref(false)
const isEdit = ref(false)
const articleModel = ref({
    id: '',
    title: '',
    categoryId: '',
    coverImg: '',
    content: '',
    state: ''
})

// 封面图 Base64 转换
const handleCoverChange = (e) => {
    const file = e.target.files[0]
    if (!file) return
    const reader = new FileReader()
    reader.onload = (event) => {
        articleModel.value.coverImg = event.target.result
    }
    reader.readAsDataURL(file)
}

// 提交文章（新增或编辑）
const submitArticle = async (clickState) => {
    articleModel.value.state = clickState

    let result
    if (isEdit.value) {
        result = await articleUpdateService(articleModel.value)
        ElMessage.success(result.msg ? result.msg : '修改成功')
    } else {
        result = await articleAddService(articleModel.value)
        ElMessage.success(result.msg ? result.msg : '添加成功')
    }

    visibleDrawer.value = false
    resetArticleModel()
    articleList()
}

// 重置表单
const resetArticleModel = () => {
    articleModel.value = {
        id: '',
        title: '',
        categoryId: '',
        coverImg: '',
        content: '',
        state: ''
    }
    isEdit.value = false
}

// 打开添加抽屉
const handleAdd = () => {
    resetArticleModel()
    visibleDrawer.value = true
}

// 打开编辑抽屉
const handleEdit = (row) => {
    resetArticleModel()
    isEdit.value = true
    articleModel.value = {
        id: row.id,
        title: row.title,
        categoryId: row.categoryId,
        coverImg: row.coverImg,
        content: row.content,
        state: row.state
    }
    visibleDrawer.value = true
}

// 删除文章
const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm('确定要删除这篇文章吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })
        let result = await articleDeleteService(row.id)
        ElMessage.success(result.msg ? result.msg : '删除成功')
        articleList()
    } catch {
        // 用户取消，不做处理
    }
}

// 页面初始化
articleCategoryList()
articleList()

// 检查路由参数是否带有 editId，自动进入编辑模式
const loadArticleForEdit = async (articleId) => {
    let result = await articleListService({ pageNum: 1, pageSize: 100, categoryId: null, state: null })
    total.value = result.data.total
    articles.value = result.data.items

    for (let i = 0; i < articles.value.length; i++) {
        let article = articles.value[i]
        for (let j = 0; j < categorys.value.length; j++) {
            if (article.categoryId === categorys.value[j].id) {
                article.categoryName = categorys.value[j].categoryName
                break
            }
        }
    }

    let target = articles.value.find(a => a.id == articleId)
    if (target) {
        handleEdit(target)
    }
}

onMounted(() => {
    if (route.query.editId) {
        loadArticleForEdit(route.query.editId)
    }
})
</script>
```

- [ ] **Step 2: 替换 template 部分的操作列按钮，绑定点击事件**

将操作列（第 207-212 行）替换为：

```vue
<el-table-column label="操作" width="100">
    <template #default="{ row }">
        <el-button :icon="Edit" circle plain type="primary" @click="handleEdit(row)"></el-button>
        <el-button :icon="Delete" circle plain type="danger" @click="handleDelete(row)"></el-button>
    </template>
    <template #empty>
        <el-empty description="没有数据" />
    </template>
</el-table-column>
```

- [ ] **Step 3: 替换添加按钮，绑定 handleAdd**

将第 177 行替换为：

```vue
<el-button type="primary" @click="handleAdd">添加文章</el-button>
```

- [ ] **Step 4: 替换抽屉，支持动态标题和 Base64 封面**

将第 223-268 行的抽屉部分替换为：

```vue
<el-drawer v-model="visibleDrawer" :title="isEdit ? '编辑文章' : '添加文章'" direction="rtl" size="50%">
    <el-form :model="articleModel" label-width="100px">
        <el-form-item label="文章标题">
            <el-input v-model="articleModel.title" placeholder="请输入标题"></el-input>
        </el-form-item>
        <el-form-item label="文章分类">
            <el-select placeholder="请选择" v-model="articleModel.categoryId">
                <el-option v-for="c in categorys" :key="c.id" :label="c.categoryName" :value="c.id">
                </el-option>
            </el-select>
        </el-form-item>
        <el-form-item label="文章封面">
            <div class="avatar-uploader">
                <img v-if="articleModel.coverImg" :src="articleModel.coverImg" class="avatar" />
                <label v-else class="el-upload avatar-uploader-icon" for="cover-input">
                    <el-icon><Plus /></el-icon>
                </label>
                <input id="cover-input" type="file" accept="image/*" style="display: none" @change="handleCoverChange" />
            </div>
        </el-form-item>
        <el-form-item label="文章内容">
            <div class="editor">
                <quill-editor theme="snow" v-model:content="articleModel.content" contentType="html">
                </quill-editor>
            </div>
        </el-form-item>
        <el-form-item>
            <el-button type="primary" @click="submitArticle('已发布')">发布</el-button>
            <el-button type="info" @click="submitArticle('草稿')">草稿</el-button>
        </el-form-item>
    </el-form>
</el-drawer>
```

- [ ] **Step 5: 添加 Plus icon 导入（如果尚未导入）**

确保第 2 行附近有：

```js
import { Plus } from '@element-plus/icons-vue'
```

- [ ] **Step 6: 修改 pageSize 默认值为 5**

将第 77 行改为：

```js
const pageSize = ref(5)
```

- [ ] **Step 7: Commit**

```bash
git add big-event-front/src/views/article/ArticleManage.vue
git commit -m "feat: wire up article edit, delete, Base64 cover, and edit-mode drawer"
```

---

### Task 9: 前端页面 — 创建 ArticleDetail.vue 详情页

**Files:**
- Create: `big-event-front/src/views/article/ArticleDetail.vue`

- [ ] **Step 1: 创建 ArticleDetail.vue**

```vue
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Edit, Delete } from '@element-plus/icons-vue'
import { articleDetailService, articleDeleteService } from '@/api/article.js'
import { articleCategoryListService } from '@/api/article.js'

const route = useRoute()
const router = useRouter()

const article = ref({})
const categoryName = ref('')

const loadArticle = async () => {
    const id = route.query.id
    if (!id) {
        ElMessage.warning('文章ID不存在')
        router.push('/article/manage')
        return
    }

    try {
        let result = await articleDetailService(id)
        article.value = result.data

        // 加载分类名称
        let catResult = await articleCategoryListService()
        for (let cat of catResult.data) {
            if (cat.id === article.value.categoryId) {
                categoryName.value = cat.categoryName
                break
            }
        }
    } catch (error) {
        ElMessage.warning('文章不存在或无权限查看')
        router.push('/article/manage')
    }
}

const handleEdit = () => {
    router.push({ path: '/article/manage', query: { editId: article.value.id } })
}

const handleDelete = async () => {
    try {
        await ElMessageBox.confirm('确定要删除这篇文章吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })
        let result = await articleDeleteService(article.value.id)
        ElMessage.success(result.msg ? result.msg : '删除成功')
        router.push('/article/manage')
    } catch {
        // 用户取消
    }
}

const goBack = () => {
    router.push('/article/manage')
}

onMounted(() => {
    loadArticle()
})
</script>

<template>
    <el-card class="page-container">
        <template #header>
            <div class="header">
                <el-button type="primary" :icon="ArrowLeft" plain @click="goBack">返回</el-button>
                <div class="actions">
                    <el-button type="primary" :icon="Edit" @click="handleEdit">编辑</el-button>
                    <el-button type="danger" :icon="Delete" @click="handleDelete">删除</el-button>
                </div>
            </div>
        </template>

        <div class="article-detail">
            <h1 class="title">{{ article.title }}</h1>

            <div class="meta">
                <span>分类: {{ categoryName }}</span>
                <span>状态: {{ article.state }}</span>
                <span>发布时间: {{ article.createTime }}</span>
            </div>

            <div class="cover" v-if="article.coverImg">
                <img :src="article.coverImg" alt="封面图" />
            </div>

            <div class="content" v-html="article.content"></div>
        </div>
    </el-card>
</template>

<style lang="scss" scoped>
.page-container {
    min-height: 100%;
    box-sizing: border-box;

    .header {
        display: flex;
        align-items: center;
        justify-content: space-between;
    }
}

.article-detail {
    max-width: 900px;
    margin: 0 auto;
    padding: 20px;

    .title {
        font-size: 28px;
        font-weight: bold;
        margin-bottom: 16px;
        text-align: center;
    }

    .meta {
        display: flex;
        justify-content: center;
        gap: 24px;
        color: #999;
        font-size: 14px;
        margin-bottom: 24px;
        padding-bottom: 16px;
        border-bottom: 1px solid #eee;
    }

    .cover {
        text-align: center;
        margin-bottom: 24px;

        img {
            max-width: 100%;
            max-height: 400px;
            border-radius: 8px;
        }
    }

    .content {
        line-height: 1.8;
        font-size: 16px;

        :deep(img) {
            max-width: 100%;
        }
    }
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add big-event-front/src/views/article/ArticleDetail.vue
git commit -m "feat: add article detail view page"
```

---

### Task 10: 前端路由 — 添加详情页路由

**Files:**
- Modify: `big-event-front/src/router/index.js`

- [ ] **Step 1: 导入 ArticleDetailVue 并添加路由**

修改 router/index.js：

```javascript
import { createRouter, createWebHistory } from 'vue-router'

import LoginVue from '@/views/Login.vue'
import LayoutVue from '@/views/Layout.vue'

import ArticleCategoryVue from '@/views/article/ArticleCategory.vue'
import ArticleManageVue from '@/views/article/ArticleManage.vue'
import ArticleDetailVue from '@/views/article/ArticleDetail.vue'
import UserAvatarVue from '@/views/user/UserAvatar.vue'
import UserInfoVue from '@/views/user/UserInfo.vue'
import UserResetPasswordVue from '@/views/user/UserResetPassword.vue'

const routes = [
    { path: '/login', component: LoginVue },
    {
        path: '/', component: LayoutVue, redirect: '/article/manage', children: [
            { path: '/article/category', component: ArticleCategoryVue },
            { path: '/article/manage', component: ArticleManageVue },
            { path: '/article/detail', component: ArticleDetailVue },
            { path: '/user/info', component: UserInfoVue },
            { path: '/user/avatar', component: UserAvatarVue },
            { path: '/user/resetPassword', component: UserResetPasswordVue }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router
```

- [ ] **Step 2: 在 ArticleManage.vue 中添加查看按钮并绑定跳转**

在 ArticleManage.vue 的操作列中，编辑按钮之前添加一个查看按钮：

```vue
<el-button :icon="View" circle plain type="success" @click="handleView(row)"></el-button>
```

在 script 中导入 View 图标并添加 handleView 函数：

```js
import { Edit, Delete, View } from '@element-plus/icons-vue'

const handleView = (row) => {
    router.push({ path: '/article/detail', query: { id: row.id } })
}
```

- [ ] **Step 3: Commit**

```bash
git add big-event-front/src/router/index.js big-event-front/src/views/article/ArticleManage.vue
git commit -m "feat: add article detail route and view button in manage list"
```

---

## 自审检查

### 1. Spec 覆盖检查

| 需求 | 对应 Task | 状态 |
|------|-----------|------|
| PageResult 分页封装 | Task 2 | ✓ |
| MyBatis-Plus 分页插件 | Task 3 | ✓ |
| ArticleService 接口 | Task 4 | ✓ |
| ArticleServiceImpl 实现（CRUD + 归属权校验） | Task 5 | ✓ |
| ArticleController 5 个端点 | Task 6 | ✓ |
| cover_img 改为 LONGTEXT | Task 1 | ✓ |
| 前端 API（更新、删除、详情） | Task 7 | ✓ |
| ArticleManage 编辑/删除/Base64 | Task 8 | ✓ |
| ArticleDetail 详情页 | Task 9 | ✓ |
| 路由配置 + 查看按钮 | Task 10 | ✓ |

### 2. Placeholder 扫描
无 TBD/TODO/placeholder。所有代码步骤包含完整实现代码。

### 3. 类型一致性
- 后端：`PageResult<Article>` 泛型在 Service 和 Controller 中一致
- 前端：API 函数命名与调用一致，`articleModel` 字段在所有组件中一致
- 路由参数：详情页使用 `query.id`，编辑跳转使用 `query.editId`，与组件读取一致

### 4. 注意事项
- Task 10 需要修改两个文件（router/index.js 和 ArticleManage.vue），确保在同一个 commit 中
- Task 8 的 ArticleManage.vue 改动较大，需确保原有功能（搜索、分页、分类加载）不受影响
- Base64 封面图通过原生 `<input type="file">` + FileReader 实现，不再依赖后端的 /upload 接口
