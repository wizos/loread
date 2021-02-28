$(document).ready(function(e) {
    handleImage();
    handleVideo();
    handleAudio();
    handleFrame();
    handleEmbed();
    handleTable();
    hljs.initHighlightingOnLoad();
    MathJax = {
        tex: {inlineMath: [['\\$', '\\$'], ['\\(', '\\)']]},
        svg: {fontCache: 'global'},
        startup: {
            ready: () =>{
                MathJax.startup.defaultReady();
                MathJax.startup.promise.then(() =>{
                    handleSlidingConflicts();
                });
            }
        }
    };
    handleSlidingConflicts();
    handleEvent();
});

function handleEvent() {
    window.addEventListener('message',function(e) {
        var data = e.data;
        var event = data.event;
        console.log("收到子 iframe 的信息：" + event + "，id: " + data.id);
        if (event === 'audio') {
            console.log(" 音频：" + "src：" + data.src + "; id：" + data.id);
            ArticleBridge.foundAudio(data.src, data.duration);
        } else if (event === 'video') {
            console.log("video 变化：" + "宽:" + data.width + "px; 高:" + data.height + "px; src：" + data.src + "；标题：" + data.title);
            var frame = document.getElementById(data.id);
            $(frame).css("height", (data.height / data.width * frame.clientWidth));
            ArticleBridge.foundVideo(data.src, data.duration);
        }
/*
 else if (event === 'iframe') {
            console.log("iframe 变化" + "width:" + data.width + "; height:" + data.height);
            var frame = document.getElementById(data.id);
            if (data.height > data.width && frame) {
                $(frame).css("height", (data.height / data.width * frame.clientWidth));
            }
        }
*/
    });
}


function handleSlidingConflicts() {
    $("code, pre, .table_wrap, mjx-container").on("scroll", function(e) {
        horizontal = e.currentTarget.scrollLeft;
        if(horizontal > 0){ArticleBridge.requestDisallowInterceptTouchEvent(true);}
    });
}

//设置图片的默认加载行为
function handleImage() {
    var articleId = $('article').attr('id');

    $('img').each(function() {
        var image = $(this);
        var originalUrl = image.attr('original-src');
        if (isEmpty(originalUrl)) {
            return true;
        }

        image.attr('id', hashCode(originalUrl.replace(/^https*:\/\/.*?\//i, "")));
    });

    $('img').click(function(event) {
        var image = $(this);
        var displayUrl = image.attr('src');
        var originalUrl = image.attr('original-src');
        // 此时去下载图片
        if (displayUrl == IMAGE_HOLDER_CLICK_TO_LOAD_URL) {
            image.attr('src', IMAGE_HOLDER_LOADING_URL);
            ArticleBridge.downImage(articleId, image.attr('id'), originalUrl, false);
        } else if (displayUrl == IMAGE_HOLDER_LOAD_FAILED_URL) {
            image.attr('src', IMAGE_HOLDER_LOADING_URL);
            ArticleBridge.downImage(articleId, image.attr('id'), originalUrl, false);
        } else if (displayUrl == IMAGE_HOLDER_IMAGE_ERROR_URL) {
            image.attr('src', IMAGE_HOLDER_LOADING_URL);
            ArticleBridge.downImage(articleId, image.attr('id'), originalUrl, true);
        } else if (displayUrl != IMAGE_HOLDER_LOADING_URL) { // 由于此时正在加载中所以不处理
            if (image.parent()[0].tagName.toLowerCase() === "a" && image.parent().attr('href').toLowerCase() !== image.attr('original-src').toLowerCase()) {
                ArticleBridge.openImageOrLink(articleId, displayUrl, image.parent().attr('href'));
            } else {
                ArticleBridge.openImage(articleId, displayUrl);
            }
        }
        // 阻止元素发生默认的行为（例如点击提交按钮时阻止对表单的提交）
        event.preventDefault();
        // 停止事件传播，阻止它被分派到其他节点。
        event.stopPropagation();
    });

    lozad('.img-lozad', {
        load: function(el) {
            ArticleBridge.readImage(articleId, el.getAttribute('id'), el.getAttribute('original-src'));
        },
        rootMargin: '1080px 0px'
    }).observe();
}

function handleAudio() {
    $('audio').each(function() {
        var audio = $(this);
        audio.addClass("audio-lozad");
        audio.attr("data-src", audio.attr("src"));
        audio.removeAttr("src");

        audio.attr("style", "pointer-events:none;");
        audio.wrap('<div class="audio_wrap"></div>');
        audio.parent().click(function(event) {
            ArticleBridge.openAudio(audio.attr("src"));
            event.preventDefault();
        });
    });
    lozad('audio').observe();
}

// 处理 Video 标签
function handleVideo() {
    $('video').each(function() {
        var video = $(this);
        video.wrap('<div class="video_wrap"></div>');
        video.attr("data-src", video.attr("src"));
        video.removeAttr("src");
    });
    lozad('video', {
        load: function(el) {
            new Plyr(el, PlyrConfig);
        }
    }).observe();
}

// 处理 iframe 标签
function handleFrame() {
    $('iframe').each(function() {
        var frame = $(this);
        var url = ArticleBridge.rewrite(frame.attr("src"));
        if (isEmpty(url)) {
            url = frame.attr("src");
        }
        frame.attr("data-src", url);
        frame.removeAttr("src");
    });

    lozad('iframe', {
        load: function(el) {
            el.src = el.getAttribute("data-src");
            el.id = hashCode(el.src);
            el.name = el.id;
            var frame = $(el);
            frame.wrap('<div class="iframe_wrap"></div>');

            if(el.src.indexOf('youtube.com') != -1 || el.src.indexOf('youtube.com') != -1){
                frame.wrap('<div class="plyr__video-embed"></div>');
                new Plyr(frame.parent()[0], PlyrConfig);
            }

            frame.removeAttr("style");
            frame.css("pointer-events", "none");
            frame.parent().click(function(event) {
                ArticleBridge.openLink(frame.attr("src"));
                event.preventDefault();
            });

            // 当iframe加载完毕后，根据src来判断是否需要关闭新窗口打开
            frame.on('load',function() {
                if (loadOnInner(frame.attr('src'))) {
                    frame.css("pointer-events", "auto");
                }
            });
            qqVideoRemoveAd(el);
        }
    }).observe();
}

// 处理 embed 标签
function handleEmbed() {
    $('embed').each(function() {
        var frame = $(this);
        frame.wrap('<div class="iframe_wrap"></div>');
        // 让iframe默认为点击新窗口打开
        frame.css("pointer-events", "none");
        frame.parent().click(function(event) {
            ArticleBridge.openLink(frame.attr("src"));
            event.preventDefault();
        });

        //加载完后，根据src判断是否要新窗口打开
        frame.on('load', function() {
            if (loadOnInner(frame.attr('src'))) {
                frame.css("pointer-events", "auto");
            }
        });
    });
}

function handleTable() {
    $('table').each(function() {
        $(this).wrap('<div class="table_wrap"></div>');
    });
}

// 见：https://www.ithmz.com/tencent-video-without-advertisement.html
// 示例：https://v.qq.com/x/page/t0922htc5no.html, https://v.qq.com/txp/iframe/player.html?vid=t0922htc5no
function qqVideoRemoveAd(el) {
    if (el.src.indexOf('v.qq.com') == -1) {
        return;
    }
    var reg = /vid=(\w+)/ig;
    var vids = reg.exec(el.src)[1];
    if (vids == '') {
        return;
    }
    var infoUrl = 'https://vv.video.qq.com/getinfo?vids=' + vids + '&platform=101001&charge=0&otype=json';
    $.ajax({
        async: true,
        url: infoUrl,
        dataType: "jsonp",
        success: function(response) {
            var vurl = 'https://ugcws.video.gtimg.com/' + response.vl.vi[0].fn + "?vkey=" + response.vl.vi[0].fvkey;
            var videoHtml = '<video controls src="' + vurl + '" poster="https://puui.qpic.cn/qqvideo_ori/0/' + vids + '_496_280/0"></video>';
            var videoNode = parseDom(videoHtml);
            replaceNode(videoNode, el);
        },
        error: function() {
            el.src = el.src.replace('v.qq.com/iframe/player.html', 'v.qq.com/txp/iframe/player.html');
        }
    });
}


function loadOnInner(url) {
    if (isEmpty(url)) {
        return false;
    }
    var flags = ["anchor.fm", "ixigua.com","music.163.com/outchain/player", "player.bilibili.com/player.html", "bilibili.com/blackboard/html5mobileplayer.html", "player.youku.com", "open.iqiyi.com", "letv.com", "sohu.com", "fpie1.com/#/video", "fpie2.com/#/video", "share.polyv.net", "www.google.com/maps/embed", "youtube.com/embed", "vimeo.com"];
    for (var i = 0; i < flags.length; i++) {
        if (url.indexOf(flags[i]) != -1) {
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

//产生数字hash值，规则和java的hashcode相同。返回字符串格式，因为通过$(this).attr('id')获取到的是字符串格式
function hashCode(str) {
    var h = 0;
    var len = str.length;
    for (var i = 0; i < len; i++) {
        h = 31 * h + str.charCodeAt(i);
        if (h > 0x7fffffff || h < 0x80000000) {
            h = h & 0xffffffff;
        }
    }
    return (h).toString();
}

function replaceNode(newNode, oldNode) {
    oldNode.parentNode.insertBefore(newNode, oldNode);
    newNode.parentNode.removeChild(oldNode);
}

function parseDom(str) {
    var objE = document.createElement("div");
    objE.innerHTML = str;
    return objE.childNodes[0];
}

function isEmpty(str) {
    return (str == null || str == undefined || str == "");
}