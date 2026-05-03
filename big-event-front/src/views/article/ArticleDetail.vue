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
