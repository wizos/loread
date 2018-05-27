/*
 * 图片懒加载, 用于 image.js;
 * 对 Unveil 进行了一定的修改
 *
 * jQuery Unveil
 * A very lightweight jQuery plugin to lazy load images
 * http://luis-almeida.github.com/unveil
 *
 * Licensed under the MIT license.
 * Copyright 2013 Luís Almeida
 * https://github.com/luis-almeida
 */

;(function($) {
	$.fn.unveil = function(threshold, callback) {
		var $w = $(window),
			th = threshold || 0,
			//保存当前所有未显示的图片列表；后面会更新
			images = this,
			loaded;

		//对所有的图片绑定unveil事件,当图片触发unveil事件的时候，读取图片的original-src属性进行src赋值显示
		this.one('unveil', function() {
			var source = this.getAttribute('original-src');
			if (source) {
				if (typeof callback === 'function') {
					callback.call(this, source);
				}
			}
		});

		// 此函数用来读取当前屏幕内的所有图片,并且触发元素的unveil事件,之后将这些图片从全局中去除掉
		function unveil() {
			var inview = images.filter(function() {
				var $e = $(this);
				if ($e.css('display') == 'hidden') {
					return;
				}

				var wt = $w.scrollTop(),
					wb = wt + $w.height(),
					et = $e.offset().top,
					eb = et + $e.height();

				return eb >= wt - th && et <= wb + th;
			});

			loaded = inview.trigger('unveil');
			images = images.not(loaded);
		}

		//针对.unveil元素的绑定scroll/resize/lookup事件，一旦触发此事件则执行unveil函数
		$w.on('scroll.unveil resize.unveil lookup.unveil', unveil);
		//页面初始化的时候执行一次unveil
		unveil();
		return this;
	};
})(window.Zepto);