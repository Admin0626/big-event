# Postman/Apifox 完整测试流程指南

## 前置准备
1. 确保MySQL已运行，执行 `sql/init.sql` 创建数据库和表
2. 修改 `application.yml` 中的数据库用户名和密码
3. 启动项目：`mvn spring-boot:run`
4. 服务地址：`http://localhost:8080`

---

## 测试流程

### 1. 用户注册
- **方法**: POST
- **URL**: `http://localhost:8080/user/register`
- **Headers**: `Content-Type: application/x-www-form-urlencoded`
- **Body** (x-www-form-urlencoded):
  | Key | Value |
  |---|---|
  | username | testuser |
  | password | 123456 |
- **预期响应**: `{"code":0,"msg":"操作成功","data":null}`

### 2. 用户登录
- **方法**: POST
- **URL**: `http://localhost:8080/user/login`
- **Headers**: `Content-Type: application/x-www-form-urlencoded`
- **Body** (x-www-form-urlencoded):
  | Key | Value |
  |---|---|
  | username | testuser |
  | password | 123456 |
- **预期响应**: `{"code":0,"msg":"操作成功","data":"eyJhbGci..."}`（Token字符串）
- **操作**: 复制 `data` 中的Token值，后续接口使用

### 3. 获取用户信息
- **方法**: GET
- **URL**: `http://localhost:8080/user/userInfo`
- **Headers**: `Authorization: <上一步获取的Token>`
- **预期响应**: `{"code":0,"msg":"操作成功","data":{"id":1,"username":"testuser",...}}`

### 4. 更新用户信息
- **方法**: PUT
- **URL**: `http://localhost:8080/user/update`
- **Headers**: 
  - `Authorization: <Token>`
  - `Content-Type: application/json`
- **Body** (raw JSON):
  ```json
  {
    "nickname": "测试用户",
    "email": "test@example.com"
  }
  ```
- **预期响应**: `{"code":0,"msg":"操作成功","data":null}`

### 5. 更新头像
- **方法**: PATCH
- **URL**: `http://localhost:8080/user/updateAvatar?avatar_url=https://example.com/avatar.jpg`
- **Headers**: `Authorization: <Token>`
- **预期响应**: `{"code":0,"msg":"操作成功","data":null}`

### 6. 修改密码
- **方法**: PATCH
- **URL**: `http://localhost:8080/user/updatePwd`
- **Headers**: 
  - `Authorization: <Token>`
  - `Content-Type: application/json`
- **Body** (raw JSON):
  ```json
  {
    "old_pwd": "123456",
    "new_pwd": "654321",
    "re_pwd": "654321"
  }
  ```
- **预期响应**: `{"code":0,"msg":"操作成功","data":null}`

---

### 7. 查询分类列表
- **方法**: GET
- **URL**: `http://localhost:8080/category`
- **Headers**: `Authorization: <Token>`
- **预期响应**: `{"code":0,"msg":"操作成功","data":[]}`

### 8. 新增分类
- **方法**: POST
- **URL**: `http://localhost:8080/category`
- **Headers**: 
  - `Authorization: <Token>`
  - `Content-Type: application/json`
- **Body** (raw JSON):
  ```json
  {
    "name": "技术文章",
    "alias": "tech"
  }
  ```
- **预期响应**: `{"code":0,"msg":"操作成功","data":null}`

### 9. 查询分类详情
- **方法**: GET
- **URL**: `http://localhost:8080/category/detail?id=1`
- **Headers**: `Authorization: <Token>`
- **预期响应**: `{"code":0,"msg":"操作成功","data":{"id":1,"name":"技术文章","alias":"tech",...}}`

### 10. 更新分类
- **方法**: PUT
- **URL**: `http://localhost:8080/category`
- **Headers**: 
  - `Authorization: <Token>`
  - `Content-Type: application/json`
- **Body** (raw JSON):
  ```json
  {
    "id": 1,
    "name": "Java技术",
    "alias": "java"
  }
  ```
- **预期响应**: `{"code":0,"msg":"操作成功","data":null}`

### 11. 删除分类
- **方法**: DELETE
- **URL**: `http://localhost:8080/category?id=1`
- **Headers**: `Authorization: <Token>`
- **预期响应**: `{"code":0,"msg":"操作成功","data":null}`

---

## 异常场景测试

| 场景 | 预期 |
|---|---|
| 未携带Token访问受保护接口 | HTTP 401 |
| 携带过期Token | HTTP 401 |
| 注册重复用户名 | `{"code":1,"msg":"用户名已被占用"}` |
| 登录错误密码 | `{"code":1,"msg":"密码错误"}` |
| 参数校验失败（用户名<5位） | `{"code":1,"msg":"用户名长度必须在5~16位之间"}` |
| 修改密码两次不一致 | `{"code":1,"msg":"两次输入的密码不一致"}` |
| 删除他人分类 | `{"code":1,"msg":"无权操作该分类"}` |
