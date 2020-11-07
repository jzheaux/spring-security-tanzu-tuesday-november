$( document ).ajaxSend((event, xhr) => {
    if (security.csrf.value) {
        xhr.setRequestHeader(security.csrf.header, security.csrf.value);
    }
    if (security.accessToken) {
        xhr.setRequestHeader("Authorization", "Bearer " + security.accessToken);
    }
});

$( document ).ajaxSuccess((event, xhr, objects, data) => {
    sentiment.up = () => sentiment._up(sentiment.root + "/up");
    $("#up-arrow").addClass("enabled")[0].onclick = sentiment.up;

    sentiment.down = () => sentiment._down(sentiment.root + "/down");
    $("#down-arrow").addClass("enabled")[0].onclick = sentiment.down;

    security.success(xhr);
});

$( document ).ajaxComplete((event, xhr) => {
    if (xhr.status === 401 || xhr.status === 403) {
        return security.authorize();
    }
});

const sentiment = {
    root: "http://localhost:8180/sentiment",
    read: () => $.get(sentiment.root, (data) => $("#sentiment").html(data.sentiment)),
    _up: (url) => $.post(url, (data) => $("#sentiment").html(data.sentiment)),
    _down: (url) => $.post(url, (data) => $("#sentiment").html(data.sentiment))
};

const security = {
    authorize: () => {
        const url = "http://idp:8280/oauth2/authorize?response_type=token&client_id=sentiment-client";
        location.href = url;
    },
    csrf: {
        header: "x-csrf-token"
    },
    success: (xhr) => {
        security.csrf.value = xhr.getResponseHeader(security.csrf.header);
    }
};

$(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const accessToken = urlParams.get("access_token");
    if (accessToken) {
        security.accessToken = accessToken;
    }
    sentiment.read();
});