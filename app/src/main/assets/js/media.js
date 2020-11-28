setTimeout( optimize(),30 );

function optimize() {
	handleImage();
	handleVideo();
	handleAudio();
	handleFrame();
	handleTable();
	
	hljs.initHighlightingOnLoad();
	MathJax = { tex:{inlineMath: [['\\$', '\\$'], ['\\(', '\\)']]}, svg:{fontCache: 'global'} };
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
		// 为何不用 src, window.btoa(src)，而是 hashCode(src) 作为图片 id 来传递？
		// 不用 src 的理由：这里获得的 src 是经过转义的，而传递到 java 层再传回来的 src 是未经过转义的（特别是中文）。
		// 不用 window.btoa(src) 的理由：src 的字符不能超出 0x00~0xFF 范围（不能有中文或特殊字符），否则报异常。
		image.attr('id', hashCode(originalUrl) );
	});

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
	
	lozad('.img-lozad', {load: function(el) {
		ArticleBridge.readImage(articleId, el.getAttribute('id'), el.getAttribute('original-src'));
	}}).observe();
}
function handleAudio(){
	$('audio').each(function() {
		var audio = $(this);
		audio.addClass("audio-lozad");
		audio.attr("data-src", audio.attr("src"));
		audio.removeAttr("src");

		audio.attr("style", "pointer-events:none;");
		audio.wrap('<div class="audio_wrap"></div>');
		audio.parent().click(function(event) {
			ArticleBridge.openAudio( audio.attr("src") );
			event.preventDefault();
		});
	});
	lozad('audio').observe();
}

// 处理 Video 标签
function handleVideo(){
	$('video').each(function() {
		var video = $(this);
		//video.addClass("video-lozad");
		video.attr("data-src", video.attr("src"));
		video.removeAttr("src");
	})
	lozad('video', {load: function(el) {
		new Plyr(el, PlyrConfig);
		//$(el).wrap('<div class="video_wrap"></div>');
	}}).observe();
}

// 针对 iframe 标签做处理
function handleFrame(){
	$('iframe').each(function() {
		var frame = $(this);
		frame.attr("data-src", frame.attr("src"));
		frame.removeAttr("src");
		addFrameClickEvent(frame);
	});

	$('embed').each(function() {
		addFrameClickEvent($(this));
	});
	
	lozad('iframe', {load: function(el) {
		el.src = el.getAttribute("data-src");
		qqVideoRemoveAd(el);
		bilibiliVideoHighQuality(el);
	}}).observe();
}

function addFrameClickEvent(frame){
	frame.wrap('<div class="iframe_wrap"></div>');
	// 让iframe默认为点击新窗口打开
	frame.attr("style", "pointer-events:none;");
	frame.parent().click(function(event) {
		ArticleBridge.openLink(frame.attr("src"));
		event.preventDefault();
	});

	// 当iframe【加载完毕】后，根据src来判断是否需要关闭新窗口打开
	frame.on('load', function() {
		if( loadOnInner(frame.attr('src')) ){
			frame.attr("style", "pointer-events:auto;");
		}
	});
}
// 见：https://www.ithmz.com/tencent-video-without-advertisement.html
// 示例：https://v.qq.com/x/page/t0922htc5no.html, https://v.qq.com/txp/iframe/player.html?vid=t0922htc5no
function qqVideoRemoveAd(el) {
	if (el.src.indexOf('v.qq.com') == -1 ){
		return;
	}
	var reg=/vid=(\w+)/ig;
	var vids = reg.exec(el.src)[1];
	if (vids == '' ){
		return;
	}
	var infoUrl = 'https://vv.video.qq.com/getinfo?vids='+ vids +'&platform=101001&charge=0&otype=json';
	$.ajax({
		async : true,
		url : infoUrl,
		dataType : "jsonp", // 返回的数据类型，设置为JSONP方式
		success: function(response){
			var vurl = 'https://ugcws.video.gtimg.com/' + response.vl.vi[0].fn + "?vkey=" + response.vl.vi[0].fvkey;
			var videoHtml = '<video controls src="' + vurl + '" poster="https://puui.qpic.cn/qqvideo_ori/0/' + vids + '_496_280/0"></video>';
			var videoNode = parseDom(videoHtml);
			replaceNode(videoNode, el);
		},
		error: function(){
			el.src = el.src.replace('v.qq.com/iframe/player.html', 'v.qq.com/txp/iframe/player.html');
		}
	});
}

function bilibiliVideoHighQuality(el) {
	if (el.src.indexOf('bilibili.com/player.html') != -1 || el.src.indexOf('bilibili.com/blackboard/html5mobileplayer.html') != -1){
		el.src = el.src + "&high_quality=1&danmaku=1";
	}
}


function handleTable(){
	$('table').each(function() {
		$(this).wrap('<div class="table_wrap"></div>');
	});
}



function loadOnInner(url){
	var flags = ["music.163.com/outchain/player","player.bilibili.com/player.html","bilibili.com/blackboard/html5mobileplayer.html","player.youku.com","open.iqiyi.com","letv.com","sohu.com","fpie1.com/#/video","fpie2.com/#/video","share.polyv.net","www.google.com/maps/embed","youtube.com/embed"];
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
function onImageLoadSuccess(imgId, displayUrl) {
	var image = findImageById(imgId);
	image.attr('src', displayUrl);
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
	// 之所以用字符串格式，是因为通过$(this).attr('id')获取到的是字符串格式
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