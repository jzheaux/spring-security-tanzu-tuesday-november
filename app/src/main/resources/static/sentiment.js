$( document ).ajaxSend((event, xhr) => {
    if (security.csrf.value) {
        xhr.setRequestHeader(security.csrf.header, security.csrf.value);
    }
});

$( document ).ajaxSuccess((event, xhr, objects, data) => {
    sentiment.up = () => sentiment._up(sentiment.root + "/up");
    $("#up-arrow").addClass("enabled")[0].onclick = sentiment.up;

    sentiment.down = () => sentiment._down(sentiment.root + "/down");
    $("#down-arrow").addClass("enabled")[0].onclick = sentiment.down;

    security.success(xhr);
});

const sentiment = {
    root: "http://localhost:8180/sentiment",
    read: () => $.ajax(sentiment.root,
            {
                method: 'GET',
                xhrFields: { withCredentials: true },
                success: (data) => $("#sentiment").html(data.sentiment)
            }),
    _up: (url) => $.ajax(url,
            {
                method: 'POST',
                xhrFields: { withCredentials: true },
                success: (data) => $("#sentiment").html(data.sentiment)
            }),
    _down: (url) => $.ajax(url,
            {
                method: 'POST',
                xhrFields: { withCredentials: true },
                success: (data) => $("#sentiment").html(data.sentiment)
            })
};

const security = {
    csrf: {
        header: "x-csrf-token"
    },
    success: (xhr) => {
        security.csrf.value = xhr.getResponseHeader(security.csrf.header);
    }
};

$(() => {
    sentiment.read();
});