//变js代码发送ajax
//1.先要获取url上的参数
var kv = window.location.search;//获取url上的key-value
var v = kv.split("=")[1];
//将+++变成
v = v.replace(/\+/g," ");
v = v.replace(/\%2B/g,"+");

v = decodeURI(v);//url解码
if (v == null || v == '') {
    //没有条件,返回首页
    window.location.href = "/index.html";
}
//将条件放入到关键字输入框上
$("#inputSeach").val(v);
//根据条件发送ajax
//query();
pageQuery(1, 10);//调用分页查询方法

//不分页
function query() {
    $.post('/s.action', {'keywords': v}, function (data) {
        //前端解析json 遇到数组就循环 遇到对象就点属性  [{title:'',id:'',content:''},{},{}]
        var html = "";
        $(data).each(function () {//循环列表中的元素  this也就是新闻对象
            var docurl = this.docurl;
            //把显示的url变短
            docurl = docurl.substring(0, 15);
            html += "<div class=\"item\">\n" +
                "\t\t\t\t<div class=\"title\"><a href=\"" + this.docurl + "\">" + this.title + "</a></div>\n" +
                "\t\t\t\t<div class=\"contentInfo_src\">\n" +
                "\t\t\t\t\t<a href=\"#\"><img src=\"./img/item.jpeg\" alt=\"\" class=\"imgSrc\" width=\"121px\" height=\"75px\"></a>\n" +
                "\t\t\t\t\t<div class=\"infoBox\">\n" +
                "\t\t\t\t\t\t<p class=\"describe\">\n" +
                "\t\t\t\t\t\t\t" + this.content + "\n" +
                "\t\t\t\t\t\t</p>\n" +
                "\t\t\t\t\t\t<p><a class=\"showurl\" href=\"" + this.docurl + "\">" + docurl + " " + this.time + "</a> <span class=\"lab\">" + this.editor + " - " + this.source + "</span></p>\n" +
                "\t\t\t\t\t</div>\n" +
                "\t\t\t\t</div>\n" +
                "\t\t\t</div>"
        })
        $(".itemList").html(html);

    }, 'json')
}

//分页查询
function pageQuery(page, pageSize) {
    //json对象中属性的名称必须和pojo对应
    //当发送ajax之前先获取到查询条件
    var dateStart = $("[name=dateStart]").val();
    var dateEnd = $("[name=dateEnd]").val();
    var source = $("#source").val();
    var editor = $("#editor").val();
    var params = {
        'keywords': v,
        'startDate': dateStart,
        'endDate': dateEnd,
        'source': source,
        'editor': editor,
        'pageBean.page': page,
        'pageBean.pageSize': pageSize
    };
    // $.post('/ps.action', params, function (data) {
    $.post('http://www.toutiao.com/toutiao_lua', params, function (data) {
        var html = "";
        $(data.pageBean.newsList).each(function () {
            var docurl = this.docurl;
            //把显示的url变短
            docurl = docurl.substring(0, 15);
            html += "<div class=\"item\">\n" +
                "\t\t\t\t<div class=\"title\"><a href=\"" + this.docurl + "\">" + this.title + "</a></div>\n" +
                "\t\t\t\t<div class=\"contentInfo_src\">\n" +
                "\t\t\t\t\t<a href=\"#\"><img src=\"./img/item.jpeg\" alt=\"\" class=\"imgSrc\" width=\"121px\" height=\"75px\"></a>\n" +
                "\t\t\t\t\t<div class=\"infoBox\">\n" +
                "\t\t\t\t\t\t<p class=\"describe\">\n" +
                "\t\t\t\t\t\t\t" + this.content + "\n" +
                "\t\t\t\t\t\t</p>\n" +
                "\t\t\t\t\t\t<p><a class=\"showurl\" href=\"" + this.docurl + "\">" + docurl + " " + this.time + "</a> <span class=\"lab\">" + this.editor + " - " + this.source + "</span></p>\n" +
                "\t\t\t\t\t</div>\n" +
                "\t\t\t\t</div>\n" +
                "\t\t\t</div>"
        })
        $(".itemList").html(html);

        //生成分页工具条
        var pageNum = data.pageBean.pageNum;//总页数
        var start = 1;
        var end = pageNum;
        if (pageNum > 7) {
            if (page <= 4) {//显示前7页
                end = 7;
            } else if (page >= pageNum - 3) {//显示后7页
                start = pageNum - 6;
            } else {
                start = page - 3;
                end = page + 3;
            }
        }
        var pageHtml = "<ul>";
        if(page!=1){
            pageHtml +="<li><a href=\"javascript:void(0)\" onclick='pageQuery("+parseInt(page-1)+","+pageSize+")'>< 上一页</a></li>"
        }
        for (var i = start; i <= end; i++) {
            if(page==i){//如果当前页码是当前页的话就是激活的样式
                pageHtml +="<li class=\"on\">"+i+"</li>";//已经是第3页，就不会再点击查询第三页了
            }else {
                pageHtml +="<li><a href='javascript:void(0)' onclick='pageQuery("+i+","+pageSize+")'>"+i+"</a></li>";
            }
        }
        if(page!=pageNum){
            pageHtml+="<li><a href='javascript:void(0)' onclick='pageQuery("+parseInt(page+1)+","+pageSize+")'>下一页 ></a></li>"
        }
        pageHtml += "</ul>";
        $(".pageList").html(pageHtml);

    }, 'json')
}

function queryTopKey(num) {
    $.post('/top.action',{'num':num},function (data) {
        var html = "";
        $(data).each(function () {
            html += "<div class=\"item\" onclick='getTopKey(this)'><span>"+this.topkey+"</span><span style='float: right;color:red'>"+this.score+"</span></div>";
        })
        $(".recommend").html(html);
    },'json')
}
function getTopKey(obj) {
    window.location.href="list.html?keyword="+$(obj).children(":first").text();
}
///歇了一些东西