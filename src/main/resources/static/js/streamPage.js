let scrollBody;
let scrollBodyStreamersSelect;

document.addEventListener('DOMContentLoaded', function () {
    scrollBody = OverlayScrollbars(document.getElementById("bodyPlayer"), {className: "os-theme-light "});
    // scrollBodyStreamersSelect = OverlayScrollbars(document.getElementById("bodyPlayerStreamersSelect"), {className: "os-theme-light "});
});

function openOptions() {
    window.location = "/stream/settings";
}

function notifyThemAll() {
    mySendRequest("/notifyThemAll", null, function (request) {
        if (request.status === 200) {
            document.getElementById('bodyPlayerTopLeft')
                .getElementsByTagName('div')[0].style.border = "1px solid lightgreen";
        }
    });
}

function showTwitchPlayer(el, id) {
    showPlayer(el, "playerTwitch", id);
}

function showGGPlayer(el, id) {
    showPlayer(el, "playerGG", id);
}

function showYTPlayer(el, id) {
    showPlayer(el, "playerYT", id);
}


function showPlayer(el, name, id) {
    name = name + '_' + id;

    let parentNode = el.parentNode.parentNode.parentNode;
    let arr = parentNode.getElementsByTagName('iframe');
    for (let i = 0; i < arr.length; i++) {
        if (arr[i].id === name) {
            arr[i].style.display = "block";
        } else {
            arr[i].style.display = "none";
        }
    }
    delStartSelector();
}

function delStartSelector() {
    let elementById = document.getElementById('playerSelectorSecond');
    if (elementById !== null) {
        elementById.remove();
        document.getElementById('playerSelector').style.display = "flex";
    }
}

function changeStreamStatus(status, element) {
    mySendRequest("/stream/settings/changeStatus", JSON.stringify({'status': status}),
        function (request) {
            if (request.status === 200) {
                if (status === false) {
                    element.setAttribute("onclick", "changeStreamStatus(true,this)");
                    element.innerText = statusOffline;
                } else {
                    element.setAttribute("onclick", "changeStreamStatus(false,this)");
                    element.innerText = statusOnline;
                }
            } else {
                element.style.border = "1px solid red";
            }
        }
    );
}