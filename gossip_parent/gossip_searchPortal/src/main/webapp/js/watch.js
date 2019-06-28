!(function(i) {
  var o = new Date().getTime(),
    c = new Date().getTime(),
    u = '__scount',
    s = '__pcount'
  function g(t) {
    return 0 < document.cookie.length &&
      ((c_start = document.cookie.indexOf(t + '=')), -1 != c_start)
      ? ((c_start = c_start + t.length + 1),
        (c_end = document.cookie.indexOf(';', c_start)),
        -1 == c_end && (c_end = document.cookie.length),
        unescape(document.cookie.substring(c_start, c_end)))
      : ''
  }
  function l(t) {
    var e = new RegExp('(^|&)' + t + '=([^&]*)(&|$)', 'i'),
      a = window.location.search.substr(1).match(e)
    if (null != a) return unescape(a[2])
  }
  function r(t, e) {
    var a = f()
    a.clstag = t.clstag
    //var n = '//' + location.hostname + '/watch'  //将地址指定nginx所在ip的位置
	//最终把日志发送到哪里
	var n = '//192.168.72.103/kafka_lua'
    i.ajax({
      url: n,
      type: 'POST',
      data: a,
      dataType: 'json',
      success: function(t) {},
      error: function(t) {},
      complete: function(t) {
        e()
      }
    })
  }
  function v() {
    var t = ''
    return (
      -1 != navigator.userAgent.indexOf('MSIE')
        ? (t = 'IE')
        : -1 != navigator.userAgent.indexOf('Firefox') && (t = 'FIREFOX'),
      t
    )
  }
  function d(t) {
    return void 0 !== t ? t : ''
  }
  function t() {
    return (
      d(navigator.javaEnabled()) +
      '|' +
      d(screen.colorDepth) +
      '|' +
      d(screen.width + ',' + screen.height) +
      '|' +
      d(
        (function() {
          var t = ''
          switch (v()) {
            case 'IE':
              t = window.navigator.systemLanguage
              break
            case 'FIREFOX':
            default:
              t = window.navigator.language
          }
          return t
        })()
      ) +
      '|' +
      d(
        (function() {
          var t = ''
          switch (v()) {
            case 'IE':
              t = document.charset
              break
            case 'FIREFOX':
            default:
              t = document.characterSet
          }
          return t
        })()
      ) +
      '|' +
      d(window.location.title) +
      '|' +
      d(location.hostname) +
      '|' +
      d(
        ((i = n = 0),
        navigator.plugins &&
          0 < navigator.plugins.length &&
          (a = navigator.plugins['Shockwave Flash']) &&
          ((n = 1), (i = a.description.split(' '))),
        (r = {f: n, v: i}).f ? r.v : '')
      ) +
      '|' +
      d(navigator.platform) +
      '|' +
      d(v()) +
      '|' +
      d(
        ((t = navigator.appName),
        (e = navigator.appVersion),
        t + ',' + parseFloat(e))
      ) +
      '|' +
      d(o) +
      '|' +
      d(c) +
      '|' +
      d(new Date().getTime()) +
      '|' +
      d(g(u)) +
      '|' +
      d(g(s)) +
      '|' +
      d(l('utm_source')) +
      '|' +
      d(l('utm_campaign')) +
      '|' +
      d(l('utm_medium')) +
      '|' +
      d(l('utm_term')) +
      '|' +
      d(
        window.performance.timing.domContentLoadedEventEnd -
          window.performance.timing.navigationStart
      ) +
      '|' +
      d(new Date().getTime()) +
      '|' +
      d(void 0)
	  
    )
    var t, e, a, n, i, r
	alert(a+"---");
  }
  function f() {
    return {
      sessionid: d(g('sessionid')),
      pin: d(g('pin')),
      url: d(encodeURIComponent(window.location.href)),
      v: t(),
      jaq: (function() {
        if (void 0 !== jaq) return jaq
      })()
    }
  }
  i('a').click(function(t) {
    var e =
        void 0 !== t.target.attributes.clstag
          ? t.target.attributes.clstag.value
          : '',
      a =
        void 0 !== t.target.attributes.href
          ? t.target.attributes.href.value
          : '',
      n =
        void 0 !== t.target.attributes.target
          ? t.target.attributes.target.value
          : ''
    '' !== e &&
      '' === n &&
      (t.preventDefault(),
      r({clstag: e}, function() {
        '' !== a && (location.href = a)
      }))
	  alert(a);
  }),
    i('input[type=button]').click(function(t) {
      var e =
        void 0 !== t.target.attributes.clstag
          ? t.target.attributes.clstag.value
          : ''
      '' !== e && r({clstag: e}, function() {})
    }),
    i('button').click(function(t) {
      var e =
        void 0 !== t.target.attributes.clstag
          ? t.target.attributes.clstag.value
          : ''
      '' !== e && r({clstag: e}, function() {})
    }),
    i(document).click(function(t) {
      var e =
          void 0 !== t.target.attributes.clstag
            ? t.target.attributes.clstag.value
            : '',
        a =
          void 0 !== t.target.attributes.href
            ? t.target.attributes.href.value
            : '',
        n =
          void 0 !== t.target.attributes.target
            ? t.target.attributes.target.value
            : '',
        i = void 0 !== t.target.type ? t.target.type.toLowerCase() : ''
      '' !== e &&
        (('' !== a && '' === n) ||
          ('button' !== i && 'submit' !== i && r({clstag: e}, function() {})))
    }),
    i('#output').text(JSON.stringify(f()))
})(jQuery || $)
