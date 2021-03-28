<script type="text/javascript">
(function() {
    console.log('开始监听 iframe 的高度变化');
    var recordHeight = 0;
    var pageHeightObserver = new MutationObserver(function(mutations) {
        var docHeight = window.getComputedStyle(document.body).getPropertyValue('height');
        if (docHeight === recordHeight) {
            return;
        }
        recordHeight = docHeight;
        var docWidth = window.getComputedStyle(document.body).getPropertyValue('width');
        window.parent.postMessage({event:'iframe', height:docHeight, width:docWidth, id:window.name},"*");
    });
    pageHeightObserver.observe(document.body, {childList: true,attributes: true,characterData: true,subtree: true});


    console.log('开始嗅探 iframe 内的多媒体');
    var videos = document.getElementsByTagName("video");
    for (var i = 0; i < videos.length; i++) {
        foundVideo(videos[i])
    }
    var audios = document.getElementsByTagName("audio");
    for (var i = 0; i < audios.length; i++) {
        foundAudio(audios[i])
    }
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
                    }
                }
            }
        })
    });
    mediaObserver.observe(document.body, {attributes: false,childList: true,subtree: true});


    var myCss = document.createElement("style");
	myCss.innerHTML = `
.loread__controls {
	padding: 0;
    margin: 0;
    right: 0;
    top: 10%;
    position: fixed;
    border-bottom-left-radius: inherit;
    border-bottom-right-radius: inherit;
    color: #fff;
    transition: opacity .4s ease-in-out,transform .4s ease-in-out;
    z-index: 2201;
    align-items: center;
    justify-content: flex-end;
    text-align: center;
}

.loread__controls > * {
    display: block;
}

.loread__controls * {
    box-sizing: inherit;
}

.loread__controls .loread__controls__item {
    margin-left: calc(10px / 4);
    margin-left: calc(var(--plyr-control-spacing,10px)/ 4);
}
.loread__menu {
    display: flex;
    position: relative;
}

.loread__controls button{
    touch-action: manipulation;
    font: inherit;
    line-height: inherit;
    width: auto;
}

.loread__control {
    background: 0 0;
    border: 0;
    border-radius: 3px;
    border-radius: var(--plyr-control-radius,3px);
    color: inherit;
    cursor: pointer;
    flex-shrink: 0;
    overflow: visible;
    padding: calc(10px * .7);
    padding: calc(var(--plyr-control-spacing,10px) * .7);
    position: relative;
    transition: all .3s ease;
}
.loread__menu__container {
    animation: plyr-popup .2s ease;
    background: rgba(255,255,255,.9);
    background: var(--plyr-menu-background,rgba(255,255,255,.9));
    border-radius: 4px;
    box-shadow: 0 1px 2px rgba(0,0,0,.15);
    box-shadow: var(--plyr-menu-shadow,0 1px 2px rgba(0,0,0,.15));
    color: #4a5464;
    color: var(--plyr-menu-color,#4a5464);
    font-size: 15px;
    font-size: var(--plyr-font-size-base,15px);
    position: absolute;
    right: 40px;
    bottom: 0%;
    text-align: left;
    white-space: nowrap;
    z-index: 3;
}
.loread__menu__container .loread__control[role=menuitemradio] {
    padding-left: calc(10px * .7);
    padding-left: calc(var(--plyr-control-spacing,10px) * .7);
}
.loread__menu__container .loread__control {
    align-items: center;
    color: #4a5464;
    color: var(--plyr-menu-color,#4a5464);
    font-size: 13px;
    font-size: var(--plyr-font-size-menu,var(--plyr-font-size-small,13px));
    padding-bottom: calc(calc(10px * .7)/ 1.5);
    padding-bottom: calc(calc(var(--plyr-control-spacing,10px) * .7)/ 1.5);
    padding-left: calc(calc(10px * .7) * 1.5);
    padding-left: calc(calc(var(--plyr-control-spacing,10px) * .7) * 1.5);
    padding-right: calc(calc(10px * .7) * 1.5);
    padding-right: calc(calc(var(--plyr-control-spacing,10px) * .7) * 1.5);
    -webkit-user-select: none;
    -ms-user-select: none;
    user-select: none;
}

.plyr--fullscreen-enabled [data-plyr=fullscreen], .plyr--pip-supported [data-plyr=pip] {
    display: inline-block;
}
.plyr [data-plyr=fullscreen], .plyr [data-plyr=pip] {
    display: none;
}
button[data-plyr=speed]{
	min-width: 30px;
}
button{
   outline:none;
}
.loread__control svg {
    display: block;
    fill: currentColor;
    height: 18px;
    height: var(--plyr-control-icon-size,18px);
    pointer-events: none;
    width: 18px;
    width: var(--plyr-control-icon-size,18px);
}
.loread__control.loread__control--pressed .icon--not-pressed, .loread__control.loread__control--pressed .label--not-pressed, .loread__control:not(.loread__control--pressed) .icon--pressed, .loread__control:not(.loread__control--pressed) .label--pressed {
    display: none;
}

.loread__control.loread__tab-focus, .loread__control:hover, .loread__control[aria-expanded=true] {
    background: #00b3ff;
    background: var(--plyr-video-control-background-hover,var(--plyr-color-main,var(--plyr-color-main,#00b3ff)));
    color: #fff;
    color: var(--plyr-video-control-color-hover,#fff);
}
	`;
	document.head.appendChild(myCss);


        function foundAudio(element) {
            element.onloadedmetadata = function() {
                window.parent.postMessage({event:'audio',src:this.src, title:document.title, id:window.name},"*")
            }
        }
        function foundVideo(element) {
            element.onloadedmetadata = function() {
                window.parent.postMessage({event:'video', height:this.videoHeight, width:this.videoWidth, src:this.src, title:document.title, id:window.name},"*")
            }


            if (window.location.href.indexOf('player.youku.com/embed') != -1){
                $(".ykplayer").css("position","inherit");
                $("#youku-playerBox").attr("style","");
                controls(element);
            }else{
                controls(element);
            }
        }





    function controls(videoEl) {
	    console.log("插入控制器");
	    var id = "controls__" + hashCode(videoEl.src);
	    if (document.querySelector("#" + id)) return;
	    var controlsDiv = document.createElement("div");
	    controlsDiv.id = id;
	    controlsDiv.classList.add("loread__controls");
	    controlsDiv.innerHTML = `
	    <div class="loread__controls__item loread__menu">
			<button aria-haspopup="true" aria-expanded="false" type="button" class="loread__control" data-plyr="settings">
				<svg aria-hidden="true" focusable="false">
					<svg id="plyr-settings" viewBox="0 0 18 18"><path d="M16.135 7.784a2 2 0 01-1.23-2.969c.322-.536.225-.998-.094-1.316l-.31-.31c-.318-.318-.78-.415-1.316-.094a2 2 0 01-2.969-1.23C10.065 1.258 9.669 1 9.219 1h-.438c-.45 0-.845.258-.997.865a2 2 0 01-2.969 1.23c-.536-.322-.999-.225-1.317.093l-.31.31c-.318.318-.415.781-.093 1.317a2 2 0 01-1.23 2.969C1.26 7.935 1 8.33 1 8.781v.438c0 .45.258.845.865.997a2 2 0 011.23 2.969c-.322.536-.225.998.094 1.316l.31.31c.319.319.782.415 1.316.094a2 2 0 012.969 1.23c.151.607.547.865.997.865h.438c.45 0 .845-.258.997-.865a2 2 0 012.969-1.23c.535.321.997.225 1.316-.094l.31-.31c.318-.318.415-.781.094-1.316a2 2 0 011.23-2.969c.607-.151.865-.547.865-.997v-.438c0-.451-.26-.846-.865-.997zM9 12a3 3 0 110-6 3 3 0 010 6z"></path></svg>
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
			<svg aria-hidden="true" focusable="false">
				<svg id="plyr-pip" viewBox="0 0 18 18"><path d="M13.293 3.293L7.022 9.564l1.414 1.414 6.271-6.271L17 7V1h-6z"></path><path d="M13 15H3V5h5V3H2a1 1 0 00-1 1v12a1 1 0 001 1h12a1 1 0 001-1v-6h-2v5z"></path></svg>
			</svg>
		</button>
		<button class="loread__controls__item loread__control" type="button" data-plyr="fullscreen">
			<svg class="icon--pressed" aria-hidden="true" focusable="false">
				<svg id="plyr-exit-fullscreen" viewBox="0 0 18 18"><path d="M1 12h3.6l-4 4L2 17.4l4-4V17h2v-7H1zM16 .6l-4 4V1h-2v7h7V6h-3.6l4-4z"></path></svg>
			</svg>
			<svg class="icon--not-pressed" aria-hidden="true" focusable="false">
				<svg id="plyr-enter-fullscreen" viewBox="0 0 18 18"><path d="M10 3h3.6l-4 4L11 8.4l4-4V8h2V1h-7zM7 9.6l-4 4V10H1v7h7v-2H4.4l4-4z"></path></svg>
			</svg>
		</button>`;
        document.body.appendChild(controlsDiv);
        var pos = videoEl.getBoundingClientRect();
        
        $("#"+ id + " > [data-plyr=pip]" ).click(function(event) {
	        console.log("新窗口打开：" + videoEl.src);
            ArticleBridge.openLink(window.location.href);
        });
        
        $("#"+ id + " > [data-plyr=fullscreen]" ).click(function(event) {
		    if(videoEl.videoHeight > videoEl.videoWidth){
			  ArticleBridge.postVideoPortrait(true);
		    }else{
		      ArticleBridge.postVideoPortrait(false);
		    }
	        if($(this).hasClass("loread__control--pressed")){
		        $(this).removeClass("loread__control--pressed");
		        if (document.exitFullscreen) {
		          document.exitFullscreen();
		        } else if (document.webkitCancelFullScreen) {
		          document.webkitCancelFullScreen();
		        } else if (document.mozCancelFullScreen) {
		          document.mozCancelFullScreen();
		        } else if (document.msExitFullscreen) {
		          document.msExitFullscreen();
		        }
		        console.log("请求全屏");
	        }else{
		        $(this).addClass("loread__control--pressed");
		        if (document.body.requestFullscreen) {
		          document.body.requestFullscreen();
		        } else if (document.body.webkitRequestFullScreen) {
		          document.body.webkitRequestFullScreen();
		        } else if (document.body.mozRequestFullScreen) {
		          document.body.mozRequestFullScreen();
		        } else if (document.body.msRequestFullscreen) {
		          // IE11
		          document.body.msRequestFullscreen();
		        }
		        console.log("取消全屏");
	        }
        });
        $("#"+ id + " [data-plyr=settings]" ).click(function(event) {
	        var menu = $("#"+ id + " .loread__menu__container");
	        var attr = menu.attr("hidden");
	        if(typeof attr !== typeof undefined && attr !== false){
		        console.log("隐藏倍速设置菜单");
		        menu.prop("hidden",false);
	        }else{
		        console.log("展示倍速设置菜单");
		        menu.prop("hidden",true);
	        }
        });

        $("#"+ id + " [data-plyr=speed]" ).click(function(event) {
	        var value = $(this).attr("value");
	        videoEl.playbackRate = value;
		    console.log("设置倍速为：" + value);
		    var menu = $("#"+ id + " .loread__menu__container");
		    menu.prop("hidden",true);
        });
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

})();
</script>