<script setup>
import { Edit, Delete, View, Plus } from '@element-plus/icons-vue'
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
    articleCategoryListService,
    articleListService,
    articleAddService,
    articleUpdateService,
    articleDeleteService,
    articleDetailService
} from '@/api/article.js'

const route = useRoute()
const router = useRouter()

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
    id: null,
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

    try {
        let result
        if (isEdit.value) {
            result = await articleUpdateService(articleModel.value)
        } else {
            result = await articleAddService(articleModel.value)
        }
        ElMessage.success(result.msg ? result.msg : (isEdit.value ? '修改成功' : '添加成功'))
        visibleDrawer.value = false
        resetArticleModel()
        articleList()
    } catch (error) {
        ElMessage.error(error.response?.data?.msg || '操作失败')
    }
}

// 重置表单
const resetArticleModel = () => {
    articleModel.value = {
        id: null,
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

const handleView = (row) => {
    router.push({ path: '/article/detail', query: { id: row.id } })
}

// 页面初始化
articleCategoryList()
articleList()

// 检查路由参数是否带有 editId，自动进入编辑模式
const loadArticleForEdit = async (articleId) => {
    try {
        let result = await articleDetailService(articleId)
        let target = result.data

        // 扩展分类名称
        for (let j = 0; j < categorys.value.length; j++) {
            if (target.categoryId === categorys.value[j].id) {
                target.categoryName = categorys.value[j].categoryName
                break
            }
        }

        handleEdit(target)
    } catch {
        ElMessage.warning('文章不存在或无权限编辑')
    }
}

onMounted(() => {
    if (route.query.editId) {
        loadArticleForEdit(route.query.editId)
    }
})
</script>
<template>
    <el-card class="page-container">
        <template #header>
            <div class="header">
                <span>文章管理</span>
                <div class="extra">
                    <el-button type="primary" @click="handleAdd">添加文章</el-button>
                </div>
            </div>
        </template>
        <!-- 搜索表单 -->
        <el-form inline>
            <el-form-item label="文章分类：">
                <el-select placeholder="请选择" v-model="categoryId">
                    <el-option v-for="c in categorys" :key="c.id" :label="c.categoryName" :value="c.id">
                    </el-option>
                </el-select>
            </el-form-item>

            <el-form-item label="发布状态：">
                <el-select placeholder="请选择" v-model="state">
                    <el-option label="已发布" value="已发布"></el-option>
                    <el-option label="草稿" value="草稿"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="articleList">搜索</el-button>
                <el-button @click="categoryId = ''; state = ''">重置</el-button>
            </el-form-item>
        </el-form>
        <!-- 文章列表 -->
        <el-table :data="articles" style="width: 100%">
            <el-table-column label="文章标题" width="400" prop="title"></el-table-column>
            <el-table-column label="分类" prop="categoryName"></el-table-column>
            <el-table-column label="发表时间" prop="createTime"> </el-table-column>
            <el-table-column label="状态" prop="state"></el-table-column>
            <el-table-column label="操作" width="100">
<template #default="{ row }">
    <el-button :icon="View" circle plain type="success" @click="handleView(row)"></el-button>
    <el-button :icon="Edit" circle plain type="primary" @click="handleEdit(row)"></el-button>
    <el-button :icon="Delete" circle plain type="danger" @click="handleDelete(row)"></el-button>
</template>
            </el-table-column>
            <template #empty>
                <el-empty description="没有数据" />
            </template>
        </el-table>
        <!-- 分页条 -->
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :page-sizes="[3, 5, 10, 15]"
            layout="jumper, total, sizes, prev, pager, next" background :total="total" @size-change="onSizeChange"
            @current-change="onCurrentChange" style="margin-top: 20px; justify-content: flex-end" />

        <!-- 抽屉 -->
        <el-drawer v-model="visibleDrawer" :title="isEdit ? '编辑文章' : '添加文章'" direction="rtl" size="50%">
            <!-- 添加文章表单 -->
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

/* 抽屉样式 */
.avatar-uploader {
    :deep() {
        .avatar {
            width: 178px;
            height: 178px;
            display: block;
        }

        .el-upload {
            border: 1px dashed var(--el-border-color);
            border-radius: 6px;
            cursor: pointer;
            position: relative;
            overflow: hidden;
            transition: var(--el-transition-duration-fast);
        }

        .el-upload:hover {
            border-color: var(--el-color-primary);
        }

        .el-icon.avatar-uploader-icon {
            font-size: 28px;
            color: #8c939d;
            width: 178px;
            height: 178px;
            text-align: center;
        }
    }
}

.editor {
    width: 100%;

    :deep(.ql-editor) {
        min-height: 200px;
    }
}
</style>