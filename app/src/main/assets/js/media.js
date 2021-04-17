$(document).ready(function(e) {
    handleImage();
    handleVideo();
    handleAudio();
    handleFrame();
    handleEmbed();
    handleTable();
    $("section, img").removeAttr("style");

    hljs.highlightAll();
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
});



function handleSlidingConflicts() {
    $("code, pre, .table_wrap, mjx-container").on("scroll", function(e) {
        horizontal = e.currentTarget.scrollLeft;
        if(horizontal > 0){LoreadBridge.requestDisallowInterceptTouchEvent(true);}
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
        image.removeAttr("style");
    });

    $('img').click(function(event) {
        var image = $(this);
        var displayUrl = image.attr('src');
        var originalUrl = image.attr('original-src');
        // 此时去下载图片
        if (displayUrl == IMAGE_HOLDER_CLICK_TO_LOAD_URL) {
            image.attr('src', IMAGE_HOLDER_LOADING_URL);
            LoreadBridge.downImage(articleId, image.attr('id'), originalUrl, false);
        } else if (displayUrl == IMAGE_HOLDER_LOAD_FAILED_URL) {
            image.attr('src', IMAGE_HOLDER_LOADING_URL);
            LoreadBridge.downImage(articleId, image.attr('id'), originalUrl, false);
        } else if (displayUrl == IMAGE_HOLDER_IMAGE_ERROR_URL) {
            image.attr('src', IMAGE_HOLDER_LOADING_URL);
            LoreadBridge.downImage(articleId, image.attr('id'), originalUrl, true);
        } else if (displayUrl != IMAGE_HOLDER_LOADING_URL) { // 由于此时正在加载中所以不处理
            if (image.parent()[0].tagName.toLowerCase() === "a" && image.parent().attr('href').toLowerCase() !== image.attr('original-src').toLowerCase()) {
                LoreadBridge.openImageOrLink(articleId, displayUrl, image.parent().attr('href'));
            } else {
                LoreadBridge.openImage(articleId, displayUrl);
            }
        }
        // 阻止元素发生默认的行为（例如点击提交按钮时阻止对表单的提交）
        event.preventDefault();
        // 停止事件传播，阻止它被分派到其他节点。
        event.stopPropagation();
    });

    lozad('.img-lozad', {
        load: function(el) {
            LoreadBridge.readImage(articleId, el.getAttribute('id'), el.getAttribute('original-src'));
        },
        rootMargin: '540px 0px'
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
            LoreadBridge.openAudio(audio.attr("src"));
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
        var url = LoreadBridge.rewrite(frame.attr("src"));
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
            frame.removeAttr("style");
            frame.css("pointer-events", "none");
            frame.parent().click(function(event) {
                if (!loadOnInner(frame.attr('src'))) {
                    LoreadBridge.openLink(frame.attr("src"));
                    event.preventDefault();
                }
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
            LoreadBridge.openLink(frame.attr("src"));
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
            new Plyr(videoNode, PlyrConfig);
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
    var flags = ["anchor.fm", "music.163.com/outchain/player"];
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