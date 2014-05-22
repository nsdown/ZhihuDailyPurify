#声明
---
以下所有 API 均由 知乎 提供，本人（Izzy Leung）采取非正常手段获取并共享之或有侵犯知乎权益的嫌疑。若被告知需停止共享与使用，本人会及时删除此回答。  
也望您暸解情况，并遵守知乎协议。 

---

###API 说明
知乎日报的消息以 JSON 的格式输出

网址中 api/ 后的数字代表 API 版本，数字过高或过低均会得到错误信息
较老的接口（启动界面图像获取，最新消息，过往消息）中将数字2替换为1.2获得效果相同，替换为 1.1 获得的是老版本 API 输出的 JSON 格式（替换为更低如1.0或更高如1.3将会获得错误消息）

1. 启动界面图像获取：http://news-at.zhihu.com/api/3/start-image/1080*1776 
start-image/ 后的数字为图像的分辨率，测试了少数分辨率，发现接受如下几种格式：320*432，480*728，720*1184，1080*1776。若格式为这些之外的，获取的图像为空


2. 最新消息：http://news-at.zhihu.com/api/3/news/latest  
说明：
a) JSON串分为如下部分：date-日期，stories-当日新闻，top_stories-界面顶部 ViewPager 显示内容
b) 在 stories 中每一个元素中，包括以下属性：title-标题，images-图像地址（官方的 API 使用的是数组形式，但是目前暂时未见到使用多张图片的情形），share_url-在线查看内容的地址，ga_prefix-作用未知，type-作用未知，id-url- share_url 中最后的数字（应该是内容的 id）


3. 离线下载：http://news-at.zhihu.com/api/3/news/3892357  
说明：
a) 使用在最新消息中获得的 id，拼接在 “http://news-at.zhihu.com/api/3/news/” 后，得到对应的消息内容。可通过此地址获得对应新闻的 JSON 内容，通过 JSOUP 解析 body 部分获得文章内容
b) 其中获得的图片同『最新消息』获得的图片大小不同。这里获得的是在文章浏览界面中使用的大图。imagesource 是图片的内容提供方，为了避免被起诉非法使用图片，在显示图片时最好附上版权信息。
c) 其中的 css 和 javascript 是供手机端的 WebView(UIWebView) 使用的 <- 知乎日报的文章浏览界面是利用 WebView(UIWebView) 实现


4. 过往消息：http://news.at.zhihu.com/api/3/news/before/20131119  
说明：
a) 若果需要查询 11 月 18 日的消息，before/ 后的数字应为 20131119
b) 知乎日报的生日为 2013 年 5 月 19 日，故 before/ 后数字小于此的只会接受到空消息
c) 输入的今日之后的日期仍然获得今日内容，但是格式不同于最新消息的 JSON 格式


5. 热门消息：http://news-at.zhihu.com/api/3/news/hot  
说明：  
a) 大体同前面介绍的 API 类似，唯一需要注意的是：欲获得图片的属性，使用的不再是 image 而是 thumbnail


6. 软件推广：  
    Android: http://news-at.zhihu.com/api/3/promotion/android  
    iOS: http://news-at.zhihu.com/api/3/promotion/ios


7. 栏目总览：http://news-at.zhihu.com/api/3/sections  
说明：
a) 同样，注意使用 thumbnail 获取图像的地址


8. 栏目具体消息查看：http://news-at.zhihu.com/api/3/section/1 （URL 最后的数字参考『栏目总览』中的 id 属性）
往前：http://news-at.zhihu.com/api/2/section/3/before/1384124400 （在 URL 的最后加上一个时间戳，下一个时间戳详见 JSON 数据末端）
