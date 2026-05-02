<script setup>
import { ref } from 'vue'

const resetForm = ref({
    old_pwd: '',
    new_pwd: '',
    re_pwd: ''
})

const rules = {
    old_pwd: [
        { required: true, message: '请输入旧密码', trigger: 'blur' },
        { min: 5, max: 16, message: '密码长度必须在5~16位之间', trigger: 'blur' }
    ],
    new_pwd: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 5, max: 16, message: '密码长度必须在5~16位之间', trigger: 'blur' }
    ],
    re_pwd: [
        { required: true, message: '请再次输入新密码', trigger: 'blur' },
        { min: 5, max: 16, message: '密码长度必须在5~16位之间', trigger: 'blur' }
    ]
}

import { userPasswordUpdateService } from '@/api/user.js'
import { ElMessage } from 'element-plus'

const formRef = ref(null)
const resetPassword = async () => {
    if (!formRef.value) return
    await formRef.value.validate(async (valid) => {
        if (!valid) return
        if (resetForm.value.new_pwd !== resetForm.value.re_pwd) {
            ElMessage.error('两次输入的密码不一致')
            return
        }
        await userPasswordUpdateService(resetForm.value)
        ElMessage.success('密码修改成功')
    })
}
</script>

<template>
    <el-card class="page-container">
        <template #header>
            <div class="header">
                <span>重置密码</span>
            </div>
        </template>
        <el-row>
            <el-col :span="12">
                <el-form ref="formRef" :model="resetForm" :rules="rules" label-width="100px" size="large">
                    <el-form-item label="旧密码" prop="old_pwd">
                        <el-input v-model="resetForm.old_pwd" type="password" show-password placeholder="请输入旧密码"></el-input>
                    </el-form-item>
                    <el-form-item label="新密码" prop="new_pwd">
                        <el-input v-model="resetForm.new_pwd" type="password" show-password placeholder="请输入新密码"></el-input>
                    </el-form-item>
                    <el-form-item label="确认密码" prop="re_pwd">
                        <el-input v-model="resetForm.re_pwd" type="password" show-password placeholder="请再次输入新密码"></el-input>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="resetPassword">提交修改</el-button>
                    </el-form-item>
                </el-form>
            </el-col>
        </el-row>
    </el-card>
</template>
