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

        function foundAudio(element) {
            element.onloadedmetadata = function() {
                window.parent.postMessage({event:'audio',src:this.src, title:document.title, id:window.name},"*")
            }
        }
        function foundVideo(element) {
            element.onloadedmetadata = function() {
                window.parent.postMessage({event:'video', height:this.videoHeight, width:this.videoWidth, src:this.src, title:document.title, id:window.name},"*")
            }

            if (window.location.href.indexOf('bilibili.com') != -1){
                $(".bilibili-player-video-recommend, .bilibili-player-video-suspension, .bilibili-player-video-share-panel, .bilibili-player-video-panel, .bilibili-player-video-subtitle, .bilibili-player-video-bas-danmaku, .bilibili-player-video-adv-danmaku, .bilibili-player-video-toast-wrp, .bilibili-player-video-info-container, .bilibili-player-video-control, .bilibili-player-video-pause-panel, .bilibili-player-video-state, .bilibili-player-video-sendjumpbar, .bilibili-player-context-menu-container, .player-mobile-display").remove();
                $(element).css("display", "block");
                new Plyr(element, PlyrConfig);
            }else if (window.location.href.indexOf('ixigua.com/embed') != -1){
                $(element).css("position","inherit");
                $("style[type], #application-info, .v3-app-layout__header, [class^=xgplayer-], .xgplayer_injectContainer, .xg-menu, .xg-panel-info, .recommend-cover, .show-recommend, .BU-FixedGroup").remove();
                new Plyr(element, PlyrConfig);
            }
        }
    })();
</script>