var queryInput = document.getElementById("query");
var searchButton = document.getElementById("searchBtn");
var statusElement = document.getElementById("status");
var resultElement = document.getElementById("results");
var backgroundImage = document.getElementById("backgroundImage");

function setStatus(message) {
    statusElement.textContent = message;
}

function renderResults(results) {
    resultElement.innerHTML = "";
    resultElement.scrollTop = 0;
    updateBackgroundPosition();

    if (!results || results.length === 0) {
        setStatus("没有找到相关结果。");
        var emptyDiv = document.createElement("div");
        emptyDiv.className = "empty-text";
        emptyDiv.textContent = "没有找到相关结果，请换一个关键词试试。";
        resultElement.appendChild(emptyDiv);
        return;
    }

    setStatus("共找到 " + results.length + " 条结果。");

    for (var i = 0; i < results.length; i++) {
        var item = results[i];
        var itemDiv = document.createElement("div");
        itemDiv.className = "result-item";

        var title = document.createElement("a");
        title.className = "result-title";
        title.href = item.url;
        title.target = "_blank";
        title.textContent = item.title;
        itemDiv.appendChild(title);

        var desc = document.createElement("p");
        desc.className = "result-desc";
        desc.innerHTML = item.desc;
        itemDiv.appendChild(desc);

        var url = document.createElement("a");
        url.className = "result-url";
        url.href = item.url;
        url.target = "_blank";
        url.textContent = item.url;
        itemDiv.appendChild(url);

        resultElement.appendChild(itemDiv);
    }
}

function search() {
    var query = queryInput.value.trim();
    if (query.length === 0) {
        setStatus("请输入搜索关键词。");
        resultElement.innerHTML = "";
        resultElement.scrollTop = 0;
        updateBackgroundPosition();
        return;
    }

    searchButton.disabled = true;
    setStatus("正在搜索...");
    resultElement.innerHTML = "";
    resultElement.scrollTop = 0;
    updateBackgroundPosition();

    $.ajax({
        url: "search",
        method: "GET",
        data: {
            query: query
        },
        success: function (data) {
            searchButton.disabled = false;
            renderResults(data);
        },
        error: function (request) {
            searchButton.disabled = false;
            var message = request.responseText || "请求失败，请检查服务器是否启动。";
            setStatus(message);
        }
    });
}

function updateBackgroundPosition() {
    var scrollRange = resultElement.scrollHeight - resultElement.clientHeight;
    var progress = scrollRange <= 0 ? 0 : resultElement.scrollTop / scrollRange;
    var imageHeight = backgroundImage.getBoundingClientRect().height;
    var moveRange = imageHeight - window.innerHeight;
    var offset = moveRange <= 0 ? 0 : -moveRange * progress;
    backgroundImage.style.transform = "translate3d(0, " + offset + "px, 0)";
}

searchButton.addEventListener("click", search);
queryInput.addEventListener("keydown", function (event) {
    if (event.key === "Enter") {
        search();
    }
});
resultElement.addEventListener("scroll", updateBackgroundPosition);
window.addEventListener("resize", updateBackgroundPosition);
backgroundImage.addEventListener("load", updateBackgroundPosition);
