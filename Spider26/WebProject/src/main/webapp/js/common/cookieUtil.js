var cookieUtil = {
	//设置cookie
	setCookie : function(c_name, value, expiredays) {
		var exdate = new Date();
		exdate.setDate(exdate.getDate() + expiredays);
		document.cookie = c_name
				+ "="
				+ escape(value)
				+ ((expiredays == null) ? "" : ";expires="
						+ exdate.toGMTString());
	},
	//检查cookie
	checkCookie : function checkCookie(key) {
		var vaule = getCookie(key);
		if (vaule != null && vaule != "") {
			
		} else {
			setCookie(key,"");
		}
	},
	//获取cookie
	getCookie : function(c_name) {
		if (document.cookie.length > 0) {
			c_start = document.cookie.indexOf(c_name + "=");
			if (c_start != -1) {
				c_start = c_start + c_name.length + 1;
				c_end = document.cookie.indexOf(";", c_start);
				if (c_end == -1)
					c_end = document.cookie.length;
				return unescape(document.cookie.substring(c_start, c_end));
			}
		}
		return "";
	}
};