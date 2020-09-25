
// ArticleBridge.log("触发脚本" );
// 在这里调用是因为在第一次打开ArticleActivity时渲染WebView内容比较慢，此时在ArticleActivity中调用optimize不会执行。
// 不直接执行optimize是因为在viewpager中预加载而生成webview的时候，这里的懒加载就被触发了，3个webview首屏的图片就都被触发下载了
setTimeout( optimize(),30 );


function optimize() {
	handleImage();
	setTimeout( removeQQVideoAd(),30000 );
	handleVideo();
	handleIFrame();
	handleEmbed();
	handleAudio();
	handleTable();

	const otherObserver = lozad(document.querySelectorAll('.lozad'));
	otherObserver.observe();
}

//设置图片的默认加载行为
function handleImage() {
	var articleId = $('article').attr('id');

	$('img').each(function() {
		var image = $(this);
		var originalUrl = image.attr('original-src');
		if( originalUrl == null || originalUrl == "" || originalUrl == undefined ){
			return true;
		}
		// 为何用 hashCode 作为图片 id 来传递，而不是 src, window.btoa(url)？
		// 1、这里获得的src是经过转义的，而传递到java层再传回来的src是未经过转义的（特别是中文）。
		// 2、window.btoa(url) 中 url 的字符不能超出 0x00~0xFF 范围（不能有中文或特殊字符），否则报异常。
		image.attr('id', hashCode(originalUrl) );
	});
	const imgObserver = lozad('img', {
		load: function(el) {
			el.src = ArticleBridge.readImage(articleId, el.getAttribute('id'), el.getAttribute('original-src'));
		}
	});
	imgObserver.observe();

	$('img').click(function(event) {
		var image = $(this);
		var displayUrl = image.attr('src');
		var originalUrl = image.attr('original-src');
		// 此时去下载图片
		if (displayUrl == IMAGE_HOLDER_CLICK_TO_LOAD_URL) {
			image.attr('src', IMAGE_HOLDER_LOADING_URL);
			ArticleBridge.downImage(articleId, image.attr('id'), originalUrl, false);
		}else if (displayUrl == IMAGE_HOLDER_LOAD_FAILED_URL){
			image.attr('src', IMAGE_HOLDER_LOADING_URL);
			ArticleBridge.downImage(articleId, image.attr('id'), originalUrl, false);
		}else if (displayUrl == IMAGE_HOLDER_IMAGE_ERROR_URL){
			image.attr('src', IMAGE_HOLDER_LOADING_URL);
			ArticleBridge.downImage(articleId, image.attr('id'), originalUrl, true);
		}else if (displayUrl != IMAGE_HOLDER_LOADING_URL){ // 由于此时正在加载中所以不处理
			ArticleBridge.openImage(articleId, displayUrl);
		}
		// 阻止元素发生默认的行为（例如点击提交按钮时阻止对表单的提交）
		event.preventDefault();
		// 停止事件传播，阻止它被分派到其他 Document 节点。在事件传播的任何阶段都可以调用它。
		// 注意，虽然该方法不能阻止同一个 Document 节点上的其他事件句柄被调用，但是它可以阻止把事件分派到其他节点。
		event.stopPropagation();
	});
}

// 将老的QQ视频链接换成新的
function handleQQVideoUrl() {
	var list = document.querySelectorAll('iframe[src^="http://v.qq.com/iframe/player.html"],iframe[src^="https://v.qq.com/iframe/player.html"]');
	for (var i = 0,len = list.length; i < len; i++) {
		list[i].src = list[i].src.replace('v.qq.com/iframe/player.html', 'v.qq.com/txp/iframe/player.html');
	}
}
// 见：https://www.ithmz.com/tencent-video-without-advertisement.html
// 示例：https://v.qq.com/x/page/t0922htc5no.html, https://v.qq.com/txp/iframe/player.html?vid=t0922htc5no
function removeQQVideoAd() {
	var list = document.querySelectorAll('iframe[src*="v.qq.com/iframe/player.html"],iframe[src*="v.qq.com/txp/iframe/player.html"]');
	var reg=/vid=(\w+)/ig;
	var reg2=/^QZOutputJson=(.*?);$/ig;
	try{
		for (var i = 0,len = list.length; i < len; i++) {
			var vids = reg.exec(list[i].src)[1];
			var infoUrl = 'https://vv.video.qq.com/getinfo?vids='+ vids +'&platform=101001&charge=0&otype=json';
			var infoStr = ArticleBridge.get(infoUrl);
			var tempObj = JSON.parse(reg2.exec(infoStr)[1]);
			var vurl = 'https://ugcws.video.gtimg.com/' + tempObj.vl.vi[0].fn + "?vkey=" + tempObj.vl.vi[0].fvkey;
			var video = '<video style="width: 100%;" controls src="' + vurl + '" poster="https://puui.qpic.cn/qqvideo_ori/0/' + vids + '_496_280/0"></video>';
			replaceNode(parseDom(video),list[i]);
		}
	}catch(e){
		handleQQVideoUrl();
	}
}


// 针对 iframe 标签做处理
function handleIFrame(){
	$('iframe').each(function() {
		var frame = $(this);
		frame.removeAttr("sandbox");// sandbox 会限制 iframe 的各种能力
		frame.attr("frameborder", "0");
		frame.attr("allowfullscreen", "");
		frame.attr("scrolling", "no");
		frame.addClass("lozad");
		frame.attr("data-src", frame.attr("src").replace(/(width|height)=\d+/ig, "").replace(/(&(amp;)*){2,}/ig, "&"));
		frame.removeAttr("src");

		// 让iframe默认为点击新窗口打开
		frame.attr("style", "pointer-events:none;");
		frame.wrap('<figure class="iframe_wrap"></figure>');
		frame.parent().click(function(event) {
			ArticleBridge.openLink(frame.attr("src"));
			event.preventDefault();
		});
		// 当iframe加载完毕后，根据src来判断是否需要关闭新窗口打开
		frame.on('load', function() {
			if( loadOnInner(frame.attr('src')) ){
				$(this).attr("style", "pointer-events:auto;");
			}
		});
	});
}
function handleEmbed(){
	$('embed').each(function() {
		var frame = $(this);
		frame.attr("autostart","1");

		frame.addClass("lozad");
		frame.attr("data-src", frame.attr("src").replace(/(width|height)=\d+/ig, "").replace(/(&(amp;)*){2,}/ig, "&"));
		frame.removeAttr("src");

		frame.attr("style", "pointer-events:none;");
		frame.wrap('<figure class="embed_wrap"></figure>');
		frame.parent().click(function(event) {
			ArticleBridge.openLink(frame.attr("src"));
			event.preventDefault();
		});
		// 当iframe加载完毕后，根据src来判断是否需要关闭新窗口打开
		frame.on('load', function() {
			if( loadOnInner(frame.attr('src')) ){
				$(this).attr("style", "pointer-events:auto;");
			}
		});
	});
}
function handleVideo(){
	$('video').each(function() {
		var video = $(this);
		video.attr("controls", "true");
		video.attr("width", "100%");
		video.attr("height", "auto");
		video.attr("preload", "metadata");

		video.addClass("lozad");
		video.attr("data-src", video.attr("src"));
		video.removeAttr("src");

		video.wrap('<div class="video_wrap"></div>');
	});
}
function handleAudio(){
	$('audio').each(function() {
		var audio = $(this);
		audio.attr("controls", "true");
		audio.attr("width", "100%")

		audio.addClass("lozad");
		audio.attr("data-src", audio.attr("src"));
		audio.removeAttr("src");

		audio.attr("style", "pointer-events:none;");
		audio.wrap('<div class="audio_wrap"></div>');
		audio.parent().click(function(event) {
			ArticleBridge.openAudio( audio.attr("src") );
			event.preventDefault();
		});
	});
}
function handleTable(){
	$('table').each(function() {
		$(this).wrap('<div class="table_wrap"></div>');
	});
}

function loadOnInner(url){
	var flags = ["music.163.com/outchain/player","player.bilibili.com/player.html","bilibili.com/blackboard/html5mobileplayer.html","player.youku.com","youtube.com/embed","open.iqiyi.com","v.qq.com","letv.com","sohu.com","fpie1.com/#/video","fpie2.com/#/video","www.google.com/maps/embed"];
	for (var i = 0; i < flags.length; i++) {
		if (url.indexOf(flags[i]) != -1 ){
			return true;
		}
	}
	return false;
}

function findImageById(imgId) {
	return $('img[id="' + imgId + '"]');
}
function onImageLoadNeedClick(imgId) {
	var image = findImageById(imgId);
	if (image) {
		image.attr('src', IMAGE_HOLDER_CLICK_TO_LOAD_URL);
	}

}
function onImageLoading(imgId) {
	var image = findImageById(imgId);
	if (image) {
		image.attr('src', IMAGE_HOLDER_LOADING_URL);
	}
}
function onImageLoadFailed(imgId) {
	var image = findImageById(imgId);
	if (image) {
		image.attr('src', IMAGE_HOLDER_LOAD_FAILED_URL);
	}
}
function onImageError(imgId) {
	var image = findImageById(imgId);
	if (image) {
		image.attr('src', IMAGE_HOLDER_IMAGE_ERROR_URL);
	}
}
function onImageLoadSuccess(imgId, displayUrl) {
	var image = findImageById(imgId);
	image.attr('src', displayUrl);
}

//产生一个hash值，只有数字，规则和java的hashcode规则相同
function hashCode(str){
	var h = 0;
	var len = str.length;
	for(var i = 0; i < len; i++){
		var tmp=str.charCodeAt(i);
		h = 31 * h + tmp;
		if(h>0x7fffffff || h<0x80000000){
			h=h & 0xffffffff;
		}
	}
	// 之所以用字符串格式，是因为通过$(this).attr('id')获取到的是字符串格式。
	return (h).toString();
};

function replaceNode(newNode, oldNode){
	oldNode.parentNode.insertBefore(newNode,oldNode);
	newNode.parentNode.removeChild(oldNode);
}

function parseDom(str) {
	var objE = document.createElement("div");
	objE.innerHTML = str;
	return objE.childNodes[0];
};
