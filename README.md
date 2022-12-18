# WinChatAndroid

> 部分layout来自[ChatYuk](https://github.com/alfianyusufabdullah/ChatYuk)，图标来自[阿里巴巴矢量图标库-一码当先红包](https://www.iconfont.cn/illustrations/detail?cid=36441)

> 配套服务端[WinChatServer](https://github.com/wa-kakalala/WinChatServer)

简易安卓聊天客户端实现

## 目前实现的界面

### 用户管理

<table>
	<tr>
		<td align="center"><img src="https://user-images.githubusercontent.com/41315874/202401108-932b771a-1508-4957-8408-378cc53904b2.png"></td>
		<td align="center"><img src="https://user-images.githubusercontent.com/41315874/202401129-5cf557ed-4dd7-47cc-aa31-1ea60970a7a1.png"></td>
	</tr>
    <tr>
		<td align="center">登陆</td>
		<td align="center">注册</td>
	</tr>
</table>

### 聊天列表行

![line chat](https://user-images.githubusercontent.com/41315874/208007355-4126540a-852a-43fc-bd17-585cb15590ed.png)

<table>
	<tr>
		<td align="center"><img src="https://user-images.githubusercontent.com/41315874/208019682-e759b620-6cf1-425f-a80d-b0c45eca9b7f.jpg"></td>
		<td align="center"><img src="https://user-images.githubusercontent.com/41315874/208229884-77a39145-661d-404b-8aa6-03b23597c8b8.png"></td>
	</tr>
    <tr>
		<td align="center">深色模式</td>
		<td align="center">浅色模式</td>
	</tr>
</table>

### 聊天

<table>
	<tr>
		<td align="center"><img src="https://user-images.githubusercontent.com/41315874/208019470-6305915a-c945-41d5-94f7-c395428f122e.jpg"></td>
		<td align="center"><img src="https://user-images.githubusercontent.com/41315874/208019480-ad12c68b-b8b9-4e30-9798-eae275ad10dc.png"></td>
	</tr>
    <tr>
		<td align="center">深色模式</td>
		<td align="center">浅色模式</td>
	</tr>
</table>

### 导航流程图

![nav_graph](https://user-images.githubusercontent.com/41315874/208229916-23c4979e-b586-4d1a-92a4-5af7023ee2e9.png)

## 目前实现的功能

登陆、发送文本消息

## 协议介绍

> 本软件基于自创的一套简单的基于UDP的协议，现介绍如下

### 总的封包格式

<table>
	<tr>
		<td align="center"><img src="https://user-images.githubusercontent.com/41315874/202403800-23ca4d43-aa6e-48f7-90fa-e61736ad0a2c.jpg"></td>
		<td align="center"><img src="https://user-images.githubusercontent.com/41315874/202403824-3d98ce28-668d-48bc-b989-21f15bbb5f81.png"></td>
	</tr>
    <tr>
		<td align="center">草稿</td>
		<td align="center">文档</td>
	</tr>
</table>

### TYPE_LOGIN

<table>
	<tr>
		<td align="center"><img src="https://user-images.githubusercontent.com/41315874/202404113-e2341cec-8edd-4077-8934-052efbba5e91.jpg"></td>
		<td align="center"><img src="https://user-images.githubusercontent.com/41315874/202404142-3f6ad6e9-1cd1-4e85-916a-3d51a2b03ce3.png"></td>
	</tr>
    <tr>
		<td align="center">草稿</td>
		<td align="center">文档</td>
	</tr>
</table>

### TYPE_MSG_TXT

<table>
	<tr>
		<td align="center"><img src="https://user-images.githubusercontent.com/41315874/202404342-b450d00d-ea25-4f98-8d52-244e11e8984f.jpg"></td>
		<td align="center"><img src="https://user-images.githubusercontent.com/41315874/202404357-ddbe0ff3-2d3c-4053-8062-2990854eb1de.png"></td>
	</tr>
    <tr>
		<td align="center">草稿</td>
		<td align="center">文档</td>
	</tr>
</table>

### TYPE_MSG_BIN/ACK

<table>
	<tr>
		<td align="center"><img src="https://user-images.githubusercontent.com/41315874/202405983-1ecf06cf-863e-45ed-850e-ada6e973ff96.jpg"></td>
		<td align="center"><img src="https://user-images.githubusercontent.com/41315874/202404630-c30c51c6-7609-401e-a1d6-f2b44bc40a54.png"></td>
	</tr>
    <tr>
		<td align="center">草稿</td>
		<td align="center">文档</td>
	</tr>
</table>

### TYPE_GRP_JOIN/QUIT

<table>
	<tr>
		<td align="center"><img src="https://user-images.githubusercontent.com/41315874/202404791-bf23d5fc-e5ad-4a82-ab56-04f1dcb0a8a8.jpg"></td>
		<td align="center"><img src="https://user-images.githubusercontent.com/41315874/202404877-3e3b8646-be01-40c2-ab50-460b388fcb1e.png"></td>
	</tr>
    <tr>
		<td align="center">草稿</td>
		<td align="center">文档</td>
	</tr>
</table>

### TYPE_BIN_GET

<table>
	<tr>
		<td align="center"><img src="https://user-images.githubusercontent.com/41315874/202405044-7d7ef466-6157-4672-aa23-7277a67cbc82.jpg"></td>
		<td align="center"><img src="https://user-images.githubusercontent.com/41315874/202405409-789ff618-2192-4c79-9b94-57919de349fb.png"></td>
	</tr>
    <tr>
		<td align="center">草稿</td>
		<td align="center">文档</td>
	</tr>
</table>

### TYPE_GRP_LST

<table>
	<tr>
		<td align="center"><img src="https://user-images.githubusercontent.com/41315874/208307219-4874a6ab-f8b8-4afc-aeb2-0abf67a76a82.jpg"></td>
		<td align="center"><img src="https://user-images.githubusercontent.com/41315874/202405609-439caa75-942a-49e7-ac85-7d2b1da5f3e4.png"></td>
	</tr>
    <tr>
		<td align="center">草稿</td>
		<td align="center">文档</td>
	</tr>
</table>
