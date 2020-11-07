$( document ).ajaxSend(function(event, xhr) {
    xhr.setRequestHeader(security.csrf.header, security.csrf.value);
});

$( document ).ajaxSuccess(function(event, xhr, objects, data) {
    const links = data["_links"];
    if (links["up"]) {
        sentiment.up = function() {
            sentiment._up(links["up"]["href"]);
        }
        $("#up-arrow").addClass("enabled")[0].onclick = sentiment.up;
    } else {
        delete sentiment.up;
        delete $("#up-arrow").removeClass("enabled").onclick;
    }
    if (links["down"]) {
        sentiment.down = function() {
            sentiment._down(links["down"]["href"]);
        }
        $("#down-arrow").addClass("enabled")[0].onclick = sentiment.down;
    } else {
        delete sentiment.down;
        delete $("#down-arrow").removeClass("enabled")[0].onclick;
    }
    security.success(xhr);
});

$( document ).ajaxComplete(function(event, xhr) {
    if (xhr.status === 401 || xhr.status === 403) {
        security.authenticate();
    }
});

const sentiment = {
    root: "/sentiment",
    read: function () {
        return $.ajax(sentiment.root,
            {
                method: 'GET',
                contentType: 'application/json',
                success: function(data, text, xhr) {
                    $("#sentiment").html(data.sentiment);
                }
            });
    },
    _up: function (url) {
        return $.ajax(url,
            {
                method: 'POST',
                contentType: 'application/json',
                success: function(data, text, xhr) {
                    $("#sentiment").html(data.sentiment);
                }
            });
    },
    _down: function (url) {
        return $.ajax(url,
            {
                method: 'POST',
                contentType: 'application/json',
                success: function(data, text, xhr) {
                    $("#sentiment").html(data.sentiment);
                }
            });
    }
};

const security = {
    authenticate: function() {
        location.reload();
    },
    csrf: {
        header: "x-csrf-token"
    },
    success: function(xhr) {
        const headers = utils.headers(xhr.getAllResponseHeaders());
        security.csrf.value = headers[security.csrf.header];
    }
};

const utils = {
    headers: function(h) {
        const arr = h.trim().split(/[\r\n]+/);

        // Create a map of header names to values
        const map = {};
        arr.forEach(line => {
            const parts = line.split(': ');
            const header = parts.shift();
            const value = parts.join(': ');
            map[header] = value;
        });

        return map;
    }
};


$(function() {
    sentiment.read();
});