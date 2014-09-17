/**
 *  点击流js封装类
 *
 *	Last modify: 2014-08-18 10:12:02
 *
 *
 *	@newPv:  新增一个pv，记录相关信息
 *
 *	@sendPv：离开页面，获取对应pv的信息send给后台
 *
 *	@sendCv：点击页面上任何cv，获取相关信息send给后台
 *
 *
 **/
 
	function bind(target, type, func) {
        if (target.addEventListener) {// 非ie 和ie9
            target.addEventListener(type, func, false);
        } else if (target.attachEvent) { // ie6到ie8
            target.attachEvent("on" + type, func);
        } else {
            target["on" + type] = func; // ie5
        }
    }

    function unbind(target, type, func) {
        if (target.removeEventListener) {
            target.removeEventListener(type, func, false);
        } else if (target.detachEvent) {
            target.detachEvent("on" + type, func);
        } else {
            target["on" + type] = null;
        }
    }
 
	var clickStream = {
		
		timeout_id : '',
		
		speed: 10000,
		
		status: false,		//超时标识
		
		data_pv: {},   		//pv参数
		
		data_cv: {},		//cv参数
		
		vt: '',				//访问时间
		
		cd: '',				//当前日志ID
		
		gu: '',				//访问url
		
		lastUrl:'',			//来源url
		
		tag_id:'',			//点击tag
		
		item : 0,
		
		data: {
			pv: {},
			cv: {}
		},
		
		startTimeout: function() {
			var _this = this;
			this.timeout_id = setTimeout( function() {
				_this.sendPv();
				_this.status = true;
			}, this.speed );
		},
		
		clear: function() {
			this.data.cv = {};
			this.item = 0;
		},
		
		bindEvents: function() {
			var _this = this;
			bind( document, 'click', function() {
				if(_this.status === true){
					_this.status = false;
					_this.clear();
					_this.data.pv.vt = _this.getDate();
				}
				_this.stopTimeout();
			} );
			
			bind( document, 'mousemove', function() {
				if(_this.status === true){
					_this.status = false;
					_this.clear();
					_this.data.pv.vt = _this.getDate();
				}
				_this.stopTimeout();
			} );
			
			bind( document, 'keyup', function() {
				if(_this.status === true){
					_this.status = false;
					_this.clear();
					_this.data.pv.vt = _this.getDate();
				}
				_this.stopTimeout();
			} );
		},
		
		stopTimeout: function() {
			clearTimeout( this.timeout_id );
			this.startTimeout();
		},
		
		newPv : function(){
			
			var rand 	 	= Math.floor(Math.random()*10000); 
			this.cd  	 	= new Date().getTime()+rand;			//当前日志ID
			this.vt	 	 	= this.getDate();    					//访问时间
			this.gu 	 	= window.location.href;					//访问url
			this.lastUrl 	= document.referrer;					//来源url
			
		},
		
		getPvParams : function(){
		
			this.data.pv = {};
			
			var Reg =new RegExp("to8to");
			if( !Reg.test(this.lastUrl) ){
				this.data.pv.lu 	 	 = this.lastUrl;			//来源url
			}
			this.data.pv.ly 		 = 1;							//PV
			this.data.pv.vt	 	 	 = this.vt;						//访问时间		
			this.data.pv.cd 	 	 = this.cd;						//当前日志ID
			this.data.pv.gu 		 = this.gu;						//访问url
			this.data.pv.ov 	 	 = this.detectOS();				//操作系统版本
		},
		
		getCvParams : function(tag_id){
			
			this.data.cv[this.item] = {};
			var ctime = new Date().getTime();
			var rand  = Math.floor(Math.random()*10000);
			
			var Reg =new RegExp("to8to");
			if( !Reg.test(this.lastUrl) ){
				this.data.cv[this.item].lu 	 	 = this.lastUrl;				//来源url
			}
			
			this.data.cv[this.item].ly 	 		= 2;							//CV
			this.data.cv[this.item].cg    	 	= tag_id;						//点击tag
			this.data.cv[this.item].cd   	 	= ctime + rand;					//当前日志ID
			this.data.cv[this.item].vt   	 	= this.getDate();			//访问时间
			this.data.cv[this.item].gu 	 		= this.gu;						//访问url
			this.data.cv[this.item].ov   	 	= this.detectOS();				//操作系统版本
			this.item++;
		},
		
		sendPv : function(){			
			
			this.data.pv.lt 	 	 = this.getDate();     	//离开时间
			
			var data = JSON.stringify(this.data);
			
			ajax.request({
				url		:	'./clickStreamClient.php',
				method	:	'post',
				data	:	{key: data},
				dataType:	'json',
				encode	:	'UTF-8',
				cache	:	false,
				async	:	true
			});
		},
		
		getDate : function(){
			var date = new Date();

			var yyyy = date.getFullYear();
			var m = date.getMonth() + 1;
			var mm = (m < 10) ? '0' + m : m;
			var d  = date.getDate();
			var dd = (d < 10) ? '0' + d : d;

			var h = date.getHours();
			var hh = (h < 10) ? '0' + h : h;
			var n = date.getMinutes();
			var nn = (n < 10) ? '0' + n : n;
			var s  = date.getSeconds();
			var ss = (s < 10) ? '0' + s : s;
			var mill = date.getMilliseconds();

			return(yyyy + mm + dd + hh + nn + ss + "." + mill);
		},
		/* sendCv : function( tag_id ){
		
			this.tag_id = tag_id;
			
			this.getCvParams();
			
			ajax.request({
				url		:	'./clickStreamClient.php',
				method	:	'post',
				data	:	this.data,
				//data    :   { "a": "{ \"b\": \"123\" }" },
				dataType:	'json',
				encode	:	'UTF-8',
				cache	:	false,
				async	:	true
			});
		}, */
		
		detectOS : function() { //判断操作系统版本
			var sUserAgent = navigator.userAgent; 
			var isWin = (navigator.platform == "Win32") || (navigator.platform == "Windows"); 
			var isMac = (navigator.platform == "Mac68K") || (navigator.platform == "MacPPC") || (navigator.platform == "Macintosh") || (navigator.platform == "MacIntel"); 
			if (isMac){ 
				return "09";
			}
			var isUnix = (navigator.platform == "X11") && !isWin && !isMac; 
			if (isUnix){ 
				return "08";
			}; 
			var isLinux = (String(navigator.platform).indexOf("Linux") > -1); 
			var bIsAndroid = sUserAgent.toLowerCase().match(/android/i) == "android"; 
			if (isLinux) { 
				if(bIsAndroid){ 
					return "07"; 
				}else{ 
					return "06";
				} 
			} 
			if (isWin) { 
				var isWin2K = sUserAgent.indexOf("Windows NT 5.0") > -1 || sUserAgent.indexOf("Windows 2000") > -1; 
				if (isWin2K) return "05"; 
				var isWinXP = sUserAgent.indexOf("Windows NT 5.1") > -1 || sUserAgent.indexOf("Windows XP") > -1; 
				if (isWinXP) return "04"; 
				var isWin2003 = sUserAgent.indexOf("Windows NT 5.2") > -1 || sUserAgent.indexOf("Windows 2003") > -1; 
				if (isWin2003) return "03"; 
				var isWinVista= sUserAgent.indexOf("Windows NT 6.0") > -1 || sUserAgent.indexOf("Windows Vista") > -1; 
				if (isWinVista) return "02"; 
				var isWin7 = sUserAgent.indexOf("Windows NT 6.1") > -1 || sUserAgent.indexOf("Windows 7") > -1; 
				if (isWin7) return "01"; 
			} 
			return "404"; 
		}
	
	}


/**
 * ajax 封装类
 *
 * Last modify: 2011-03-21 16:12:02
 *
 * example：
 * ajax.request({
 *      url         请求URL
 *      method      请求方式(默认：GET)
 *      data        请求参数
 *      dataType    数据格式(默认：text)
 *      encode      请求的编码(默认：UTF-8)
 *      timeout     请求超时时间(默认：0, 不超时)
 *      success     请求成功后执行函数 参数：text、json、 xml数据
 *      failure     请求失败后执行函数 参数：msg, xmlhttp对象, exp
 *      cache       是否缓存(默认：false)
 *      async       是否异步(默认：true)
 * });
 */
	var ajax = (function(){
    
		 /**
		 * 创建XMLHTTPRequest
		 */
		function create() {
			var xmlhttp = null;
			if ( typeof XMLHttpRequest !== 'undefined' ) {
				xmlhttp = new XMLHttpRequest();
			} else {
				var ver_arr = ['Microsoft.XMLHTTP', 'MSXML6.XMLHTTP', 'MSXML5.XMLHTTP', 'MSXML4.XMLHTTP', 'MSXML3.XMLHTTP', 'MSXML2.XMLHTTP', 'MSXML.XMLHTTP'];
				for (var i in ver_arr) {
					try {
						xmlhttp = new ActiveXObject(ver_arr[i]);
						break;
					} catch(e) {
						continue;
					}
				}
			}
			return xmlhttp;
		}
		
		/**
		 * 发送请求
		 */
		function request(obj) {
			function fn(){}
			obj = obj || {};
			var url     = obj.url       || location.toString(),
				method  = obj.method    || 'GET',
				data    = obj.data      || null,
				dataType= obj.dataType  || 'text',
				encode  = obj.encode    || 'UTF-8',
				timeout = obj.timeout   || 0,
				success = obj.success   || fn,
				failure = obj.failure   || fn,
				cache   = obj.cache     || false,
				async   = obj.async !== false;
				method  = method.toUpperCase();
				dataType= dataType.toLowerCase();
			if (data && typeof(data) == 'object') {
				data = _serialize(data);
			}
			xmlhttp = create();
			if (!xmlhttp) {
				alert('Not Support Ajax');
				return;
			}
			if (method == 'GET' && data) {
				url += (url.indexOf('?') == -1 ? '?' : '&') + data;
				data = null;
			}
			var isTimeout = false, timer;
			if (async && timeout > 0) {
				timer = setTimeout(function() {
					xmlhttp.abort();
					isTimeout = true;
				}, timeout);
			}
			xmlhttp.onreadystatechange = function() {
				if (xmlhttp.readyState == 4 && !isTimeout) {
					stateChange(xmlhttp, dataType, success, failure);
					clearTimeout(timer);
				}
			}
			xmlhttp.open(method, url, async);
			// 设置ajax请求头标识
			xmlhttp.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
			if ( method == 'POST' ) {
				xmlhttp.setRequestHeader('Content-type', 'application/x-www-form-urlencoded;charset=' + encode);
			} else {
				if (!cache) {
					xmlhttp.setRequestHeader('If-Modified-Since', '0');
					xmlhttp.setRequestHeader('Cache-Control', 'no-cache');
				}
			}
			xmlhttp.send(data);
		}
		
		/** 
		 * 转换参数
		 */
		function _serialize(data) {
			var row = [];
			for (var k in data) {
				var val = data[k];
				if(val.constructor == Array) {
					for (var i = 0, max = val.length; i < max; i++) {
						row.push(k + '=' + encodeURIComponent(val[i]));
					}
				} else {
					row.push(k + '=' + encodeURIComponent(val));
				}
			}
			return row.join('&');
		}

		/**
		 * 状态变化
		 */
		function stateChange(xmlhttp, dataType, success, failure) {
			var sts = xmlhttp.status, result;
			if (sts == 200) {
				switch (dataType) {
					case 'text':
						result = xmlhttp.responseText;
						break;
					case 'json':
						result = function(str){
							try {
								return JSON.parse(str);
							} catch(e) {
								try {
									return (new Function('return ' + str))();
								} catch (e) {
									try {
										return eval('(' + str + ')');
									} catch(e) {
										failure('Parse json error', xmlhttp, e);
									}
								}
							}
						}(xmlhttp.responseText);
						break;
					case 'xml':
						result = xmlhttp.responseXML;
						break;
				}
				typeof result !== 'undefined' && success(result);
			} else if (sts == 0) {
				failure('Request timeout', xmlhttp);
			} else {
				failure(sts, xmlhttp);
			}
			xmlhttp = null;
		}

		/**
		 * 获得当前时间戳(微秒)
		 */
		function getIntTime() {
			var d = new Date();
			return Date.parse(d);
		}

		/**
		 * 动态创建随机名称的方法
		 *
		 * @param   string      pre     方法前缀
		 * @return  string              方法名称
		 */
		function createFunc(pre) {
			pre                 = pre || 'ajax';
			var func_name       = pre + '_' + getIntTime();
			window[func_name]   = function() {};
			return func_name;
		}

		/**
		 * 动态加载js
		 * 
		 * @param   string      url     链接地址
		 */
		function loadScript(url, callback) {
			callback    = callback  || function() {};
			var func    = createFunc('xcSjbAd');
			window[func]= callback;
			var ahead   = document.head || document.getElementsByTagName( "head" )[0] || document.documentElement;
			var ascript = document.createElement('script');
			if ( url.toLowerCase().indexOf('callback=') == -1 ) {
				url     = url.indexOf('?') >= 0 ? url + '&callback=' + func : url + '?callback=' + func
			}
			ascript.src = url;
			ascript.type= 'text/javascript';
			ahead.appendChild(ascript);
			ascript.onload = ascript.onreadystatechange = function() {
				if( !ascript.readyState || /loaded|complete/.test( ascript.readyState ) ) {
					ahead.removeChild(ascript);
					window[func]  = null;
				}
			}
		}
		/**
		 * 调用
		 */
		return {request: request, loadScript: loadScript}
	})();
	
	clickStream.startTimeout();
	clickStream.bindEvents();