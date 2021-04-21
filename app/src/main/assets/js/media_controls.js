LoreadBridge.log('开始嵌入控制器脚本');
$(document).ready(function(e) {
    LoreadBridge.log('监听是否动态增加 iframe, video，audio');
    const mediaObserver = new MutationObserver(function(records) {
        records.forEach((record, index, records) => {
            if (record.type === 'childList' && record.addedNodes.length != 0) {
                var nodes = record.addedNodes;
                for (var i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    if (node.nodeName === 'VIDEO') {
                        foundVideo(node);
                    }else if(node.nodeName === 'AUDIO'){
                        foundAudio(node);
                    }else if(node.nodeName === 'IFRAME'){
                        foundFrame(node);
                    }
                }
            }
        })
    });
    mediaObserver.observe(document.body, {attributes: false,childList: true,subtree: true});

    LoreadBridge.log('嗅探多媒体');
    var iframes = document.getElementsByTagName("iframe");
    for (var i = 0; i < iframes.length; i++) {
        foundFrame(iframes[i])
    }
    var videos = document.getElementsByTagName("video");
    for (var i = 0; i < videos.length; i++) {
        foundVideo(videos[i])
    }
    var audios = document.getElementsByTagName("audio");
    for (var i = 0; i < audios.length; i++) {
        foundAudio(audios[i])
    }

    function getScrollTop(videoElement) {
        if (!videoElement.window) {
            videoElement.window = $(window);
        }
        return videoElement.window.scrollTop();
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

    function foundFrame(element){
        if(loadOnInner(url)){
            return;
        }
        var frame = $(element);
        frame.wrap('<div></div>');
        frame.removeAttr("style");
        frame.css("pointer-events", "none");
        frame.parent().click(function(event) {
            window.location.href = element.src;
        });
    }


    function foundAudio(element) {
        element.onloadedmetadata = function() {
            //window.parent.postMessage({event:'audio',src:this.src, title:document.title, id:window.name},"*")
        }
    }

    function foundVideo(element) {
        element.oncanplay = function() {
            //window.parent.postMessage({event:'video', height:this.videoHeight, width:this.videoWidth, src:this.src, title:document.title, id:window.name},"*");
            controls(element);
        }
        element.onplaying = function() {
            controls(element);
        }

        if (window.location.href.indexOf('player.youku.com/embed') != -1){
            var meta = document.createElement('meta');
            meta.setAttribute('name', 'viewport');
            meta.setAttribute('content', 'width=device-width, initial-scale=1.0, user-scalable=no');
            document.head.appendChild(meta);
            $(".ykplayer").css("position","inherit");
            $("#youku-playerBox").attr("style","");
            $(document.body).css("background","black");
        }
    }

    function controls(videoEl) {
        LoreadBridge.log("生成控制器");
        var id = "controls__" + hashCode(videoEl.src);
        if (document.querySelector("#" + id)) return;
        var controlsDiv = document.createElement("div");
        controlsDiv.id = id;
        controlsDiv.classList.add("loread__controls");
        controlsDiv.innerHTML = `
        <div class="loread__controls__item loread__menu">
            <button aria-haspopup="true" aria-expanded="false" type="button" class="loread__control" data-plyr="settings">
                <svg class="loread__icon" aria-hidden="true" focusable="false">
                    <use xlink:href="#plyr-settings"></use>
                </svg>
            </button>
            <div class="loread__menu__container" role="menu" hidden>
                <button data-plyr="speed" type="button" role="menuitemradio" class="loread__control" aria-checked="false" value="0.75"><span>0.75×</span>
                </button>
                <button data-plyr="speed" type="button" role="menuitemradio" class="loread__control" aria-checked="false" value="1"><span>1x</span>
                </button>
                <button data-plyr="speed" type="button" role="menuitemradio" class="loread__control" aria-checked="false" value="1.5"><span>1.5×</span>
                </button>
                <button data-plyr="speed" type="button" role="menuitemradio" class="loread__control" aria-checked="false" value="1.75"><span>1.75×</span>
                </button>
                <button data-plyr="speed" type="button" role="menuitemradio" class="loread__control" aria-checked="false" value="2"><span>2×</span>
                </button>
            </div>
        </div>
        <button class="loread__controls__item loread__control" type="button" data-plyr="pip">
            <svg class="loread__icon" aria-hidden="true" focusable="false">
                <use xlink:href="#plyr-pip"></use>
            </svg>
        </button>
        <button class="loread__controls__item loread__control" type="button" data-plyr="fullscreen">
            <svg class="loread__icon icon--pressed" aria-hidden="true" focusable="false">
                <use xlink:href="#plyr-exit-fullscreen"></use>
            </svg>
            <svg class="loread__icon icon--not-pressed" aria-hidden="true" focusable="false">
                <use xlink:href="#plyr-enter-fullscreen"></use>
            </svg>
        </button>`;
        document.body.appendChild(controlsDiv);


        var scroll_top = getScrollTop(videoEl);
        var pos = videoEl.getBoundingClientRect();
        var top = pos.top + scroll_top;
        var label_top = top + (videoEl.offsetHeight - controlsDiv.offsetHeight)/2;
        var label_left = pos.left + videoEl.offsetWidth - controlsDiv.offsetWidth;
        $(controlsDiv).css({ top:label_top, left:label_left} );
        //LoreadBridge.log("高度：", top, pos.right , videoEl.scrollLeft );
        //LoreadBridge.log("坐标：", label_top, label_left, videoEl.src );
        //LoreadBridge.log("左边：" + pos.width + " ," + controlsDiv.offsetWidth);


        const videoObserver = new ResizeObserver(entries => {
            for (let entry of entries) {
                position(videoEl, controlsDiv);
            }
        });
        videoObserver.observe(videoEl);
        videoObserver.observe(document.body);


        $("#"+ id + " > [data-plyr=pip]" ).click(function(event) {
            LoreadBridge.log("新窗口打开：" + videoEl.src);
            window.location.href = videoEl.src;
            //LoreadBridge.openLink(window.location.href);
        });

        $("#"+ id + " > [data-plyr=fullscreen]" ).click(function(event) {
            if(videoEl.videoHeight > videoEl.videoWidth){
                LoreadBridge.postVideoPortrait(true);
            }else{
                LoreadBridge.postVideoPortrait(false);
            }
            var el = getEl(videoEl);

            if($(this).hasClass("loread__control--pressed")){
                $(this).removeClass("loread__control--pressed");
                if (document.exitFullscreen) {
                    document.exitFullscreen();
                }
                LoreadBridge.log("请求全屏");
            }else{
                $(this).addClass("loread__control--pressed");
                if (el.requestFullscreen) {
                    el.requestFullscreen();
                }
                LoreadBridge.log("取消全屏");
            }
        });
        $("#"+ id + " [data-plyr=settings]" ).click(function(event) {
            var menu = $("#"+ id + " .loread__menu__container");
            var attr = menu.attr("hidden");
            if(typeof attr !== typeof undefined && attr !== false){
                LoreadBridge.log("隐藏倍速设置菜单");
                menu.prop("hidden",false);
            }else{
                LoreadBridge.log("展示倍速设置菜单");
                menu.prop("hidden",true);
            }
        });

        $("#"+ id + " [data-plyr=speed]" ).click(function(event) {
            var value = $(this).attr("value");
            videoEl.playbackRate = value;
            var menu = $("#"+ id + " .loread__menu__container");
            menu.prop("hidden",true);
            LoreadBridge.log("设置倍速为：" + value);
        });
    }

    function position(videoEl, controlsDiv) {
            var scroll_top = getScrollTop(videoEl);
            var pos = videoEl.getBoundingClientRect();
            var top = pos.top + scroll_top;
            var label_top = top + (videoEl.offsetHeight - controlsDiv.offsetHeight)/2;
            var label_left = pos.left + videoEl.offsetWidth - controlsDiv.offsetWidth;
            $(controlsDiv).css({ top:label_top, left:label_left} );
    }

    function getEl(videoEl) {
        if(videoEl.offsetHeight < document.body.offsetHeight - 100){
            return videoEl;
        }
        return document.body;
    }

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

    function isEmpty(str) {
        return (str == null || str == undefined || str == "");
    }
});