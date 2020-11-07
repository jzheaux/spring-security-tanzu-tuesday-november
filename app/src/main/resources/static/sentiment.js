$( document ).ajaxSuccess((event, xhr, objects, data) => {
    sentiment.up = () => sentiment._up(sentiment.root + "/up");
    $("#up-arrow").addClass("enabled")[0].onclick = sentiment.up;

    sentiment.down = () => sentiment._down(sentiment.root + "/down");
    $("#down-arrow").addClass("enabled")[0].onclick = sentiment.down;
});

const sentiment = {
    root: "http://localhost:8180/sentiment",
    read: () => $.ajax(sentiment.root,
            {
                method: 'GET',
                success: (data) => $("#sentiment").html(data.sentiment)
            }),
    _up: (url) => $.ajax(url,
            {
                method: 'POST',
                success: (data) => $("#sentiment").html(data.sentiment)
            }),
    _down: (url) => $.ajax(url,
            {
                method: 'POST',
                success: (data) => $("#sentiment").html(data.sentiment)
            })
};

$(() => {
    sentiment.read();
});