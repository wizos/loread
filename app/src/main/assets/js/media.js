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
var IMAGE_HOLDER_LOADING_URL = 'file:///android_asset/image/image_holder_loading.png';

var loaded = false;


var articleId = $('article').attr('id');
//ImageBridge.log("==============================================media文件被执行"  + articleId );
// 在这里调用是因为在第一次打开ArticleActivity时，渲染WebView的内容比较慢，此时去调用setupImage不会执行。
// 所以等WebView加载到这个文件时，尝试再去执行tryInitJs，当此页面等于当前展示的文章时，就执行setupImage。
// 不能直接在这里就初始化了，因为在viewpager中预加载而生成webview的时候，这里的懒加载就被触发了
setTimeout( ImageBridge.tryInitJs(articleId),100 );


function setupImage() {
//    ImageBridge.log("============================================== 准备执行 setupImage" );
	if(loaded){
		return;
	}
//    ImageBridge.log("============================================== 开始执行 setupImage" );
	var index = 0;
	articleId = document.getElementsByTagName('article')[0].id;
//	ImageBridge.log("===加载图片"  + articleId );

	$('img').each(function() {
		var image = $(this);
		var originalUrl = image.attr('original-src');
		var url = image.attr('src');
		image.attr('index', index ++);
		image.unveil(200, function() {
		 	var index = parseInt($(this).attr('index'));
        	ImageBridge.loadImage(articleId,index, url, originalUrl);
        });
	});


	$('img').click(function(event) {
		var image = $(this);
		var url = image.attr('src');
		var originalUrl = image.attr('original-src');
		// 此时去下载图片
		if (url == IMAGE_HOLDER_CLICK_TO_LOAD_URL || url == IMAGE_HOLDER_LOAD_FAILED_URL) {
			image.attr('src', IMAGE_HOLDER_LOADING_URL);
			var i = parseInt($(this).attr('index'));
			ImageBridge.downImage(articleId,i, originalUrl);
			event.preventDefault();
			event.stopPropagation();
			return;
		}
		// 由于此时正在加载中所以不处理
		if (url == IMAGE_HOLDER_LOADING_URL) {
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
//		ImageBridge.log("点击了图片" + url + "  序号：" + index );
		// 中间原本是 JSON.stringify(urls) 这里只会把已经保存了的图片文件传递过去
		ImageBridge.openImage(articleId, url , index);
		// 阻止元素发生默认的行为（例如，当点击提交按钮时阻止对表单的提交）。
		event.preventDefault();
		 // 该方法将停止事件的传播，阻止它被分派到其他 Document 节点。在事件传播的任何阶段都可以调用它。注意，虽然该方法不能阻止同一个 Document 节点上的其他事件句柄被调用，但是它可以阻止把事件分派到其他节点。
		event.stopPropagation();
	});

	handleIframe();
	handleEmbed();
	loaded = true;
}


// 针对 iframe 标签做处理
function handleIframe(){
	// 初始化 iframe 的点击事件
    var iframes = document.getElementsByTagName('iframe');
    for (var i = 0, l = iframes.length; i < l; i++) {
        var iframe = iframes[i];
        if( hasAudio(iframe.src) ){
            continue;
        }
        wrapFrame( iframe );
    };
}

// 针对 embed 标签做处理
function handleEmbed(){
	// 初始化 embed 的点击事件
    var embeds = document.getElementsByTagName('embed');
    for (var i = 0, l = embeds.length; i < l; i++) {
        var embed = embeds[i];
        wrapFrame( embed );
    };
}


function wrapFrame( frame ){
        a = document.createElement('a');
	    a.setAttribute('href', "javascript:void(0)");
	    (function( src ){
	        a.onclick = function () {
	             ImageBridge.openLink( src );
            }
         })( frame.src )
	    d = document.createElement('div');
	    d.style.width = '100%';
	    d.style.height = frame.offsetHeight + 'px';
	    d.style.top = frame.offsetTop + 'px';
	    d.style.left = frame.offsetLeft + 'px';
	    d.style.position = 'absolute';
	    d.style.zIndex='2147483647';
	    d.style.overflow='hidden';
	    d.innerHTML = "<button id='video-button'>点击查看</button>";
	    d.style.lineHeight = frame.offsetHeight + 'px';
	    d.style.textAlign = 'center';
	    a.appendChild(d);
	    frame.offsetParent.appendChild(a);

}
/*
	    d.style.width = frame.offsetWidth + 'px';
	    d.style.opacity = '0';
	    d.style.filter = 'alpha(opacity=0)';
	    d.style.background = 'black';
*/


function hasAudio(url){
    if ( url.indexOf("music.163.com/outchain/player") != -1 ) {
        return true;
    }
    return false;
}



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
