#### app工作流程：
  ##### 1、用户输入用户名和密码，点击启动后启动service
  ##### 2、在service中监听网络变化，当连接到移动网络时，获取当前ip，和缓存sp中对比，不同就上传，上传成功后保存到sp中
  ##### 3、此后每2秒，判断当前网络是移动网络就获取ip对比上传
#### 提交参数及加密方式：
  ##### 参数共6个，username，password，ipv4，ipv6，imsi，refreshTime，6个参数组成json串。
  ##### json串md5加密后取前10位作为请求头参数token，json串经3des加密后作为请求参数param
  
#### apk使用：workflow自动打包出来的是未签名的安装包， 使用360加固助手签名并加固。
  360加固助手使用方法：
     1、主页面点击签名设置打开签名设置页面，选择签名文件，填写密码，添加签名
     2、主页面点击添加应用，选择未签名的安装包，或者直接把未签名的安装包拖进主页面，即可完成加固和签名


