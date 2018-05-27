/*
 * 设置图片的默认加载行为
 *
 * native 需要实现的接口有:
 * String readImageCache(String url);
 * boolean isAutoLoadImage();
 * void loadImage(String url);
 * void openImage(String urls, int index);
 *
 * native 可以调用的方法有:
 * void onImageLoadFailed(String url);
 * void onImageLoadSuccess(String url, String localUrl);
 */

var IMAGE_HOLDER_CLICK_TO_LOAD_URL = 'file:///android_asset/image/image_holder_click_to_load.png';
var IMAGE_HOLDER_LOAD_FAILED_URL = 'file:///android_asset/image/image_holder_load_failed.png';
var IMAGE_HOLDER_LOADING_URL = 'file:///android_asset/image/image_holder_loading.gif';

function findImageByUrl(url) {
	return $('img[original-src="' + url + '"]');
}
function onImageLoadNeedClick(url) {
	var image = findImageByUrl(url);
	if (image) {
		image.attr('src', IMAGE_HOLDER_CLICK_TO_LOAD_URL);
	}
}

function onImageLoadFailed(url) {
	var image = findImageByUrl(url);
	if (image) {
		image.attr('src', IMAGE_HOLDER_LOAD_FAILED_URL);
	}
}

function onImageLoadSuccess(url, localUrl) {
	var image = findImageByUrl(url);
	if (image) {
		image.removeClass('image-holder');
		image.attr('src', localUrl);
	}
}

var loaded = false;
var articleId = $('article').attr('id');


//ImageBridge.log( $("html").html() );
//ImageBridge.log("=============================================="  + articleId );
//setTimeout(setupImage(articleId),100);
setTimeout( ImageBridge.tryInitJs(articleId),100 );



function setupImage(articleId) {
	if(loaded){
		return;
	}
	var urls = [];
	var index = 0;

//	ImageBridge.log("======================初始化懒加载脚本======================" + articleId);
	$('img').each(function() {
		var image = $(this);

		// 方法二
		var originalUrl = image.attr('original-src');
		var url = image.attr('src');
		image.attr('index', index ++);
		image.unveil(200, function() {
		 	var i = parseInt($(this).attr('index'));
        	ImageBridge.loadImage(articleId,i, url, originalUrl);
        });
	});


	$('img').click(function(event) {
		var image = $(this);
		var url = image.attr('src');
		var originalUrl = image.attr('original-src');
		if (url == IMAGE_HOLDER_CLICK_TO_LOAD_URL || url == IMAGE_HOLDER_LOAD_FAILED_URL) {
			image.attr('src', IMAGE_HOLDER_LOADING_URL);
			var i = parseInt($(this).attr('index'));
			ImageBridge.downImage(articleId,i, originalUrl);
			event.preventDefault();
			event.stopPropagation();
			return;
		}

		var index = 0;
		$('img').each(function() {
			if (event.target === this) {
				index = parseInt($(this).attr('index'));
			}
		});

		ImageBridge.openImage(articleId, JSON.stringify(urls), index);
		// 阻止元素发生默认的行为（例如，当点击提交按钮时阻止对表单的提交）。
		event.preventDefault();
		 // 该方法将停止事件的传播，阻止它被分派到其他 Document 节点。在事件传播的任何阶段都可以调用它。注意，虽然该方法不能阻止同一个 Document 节点上的其他事件句柄被调用，但是它可以阻止把事件分派到其他节点。
		event.stopPropagation();
	});

	       var iframes = document.getElementsByTagName('iframe');
	       for (var i = 0, l = iframes.length; i < l; i++) {
	              var iframe = iframes[i],
	              a = document.createElement('a');
	              a.setAttribute('href', "javascript:void(0)");
	              ImageBridge.log("初始化iframe" + iframe.src );
	              a.onclick = function(){
	                  ImageBridge.log("点击了iframe框架" + iframe.src );
	                  ImageBridge.openLink(iframe.src);
	              }
	              d = document.createElement('div');
	              d.style.width = iframe.offsetWidth + 'px';
	              d.style.height = iframe.offsetHeight + 'px';
	              d.style.top = iframe.offsetTop + 'px';
	              d.style.left = iframe.offsetLeft + 'px';
	              d.style.position = 'absolute';
	              d.style.opacity = '0';
	              d.style.filter = 'alpha(opacity=0)';
	              d.style.zIndex='2147483647';
	              d.style.background = 'black';
	              a.appendChild(d);
	              iframe.offsetParent.appendChild(a);
	       };

	loaded = true;

}



//      方法一
//		var originalUrl = image.attr('original-src');
////		var originalUrl = image.attr('src');
////		image.attr('original-src', originalUrl);
//
//        // 修改后，拿出以下2行代码
//		urls.push(originalUrl);
//		image.attr('index', index ++);
//
//		var cacheUrl = ImageBridge.readImageCache(articleId, originalUrl);
//		var isAutoLoadImage = ImageBridge.isAutoLoadImage();
//
//		if (cacheUrl) {
//			image.attr('src', cacheUrl);
//		} else if (isAutoLoadImage) {
//			image.addClass('image-holder');
//			image.attr('src', IMAGE_HOLDER_LOADING_URL);
//			// 回调
//			image.unveil(200, function() {
//			    ImageBridge.loadImage(articleId, originalUrl);
//			});
//		} else {
//			image.addClass('image-holder');
//			image.attr('src', IMAGE_HOLDER_CLICK_TO_LOAD_URL)
//		}
