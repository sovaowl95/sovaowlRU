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
    let reg = new XMLHttpRequest();
    reg.open("POST", "/notifyThemAll", true);
    reg.addEventListener("load", function () {
        if (reg.status === 200) {
            document.getElementById('bodyPlayerTopLeft').getElementsByTagName('div')[0].style.border = "1px solid lightgreen";
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    reg.setRequestHeader("X-CSRF-Token", csrfToken);
    reg.send();
}

function showTwitchPlayer(el) {
    showPlayer(el, "playerTwitch");
}

function showGGPlayer(el) {
    showPlayer(el, "playerGG");
}

function showYTPlayer(el) {
    showPlayer(el, "playerYT");
}


function showPlayer(el, name) {
    let parentNode = el.parentNode.parentNode.parentNode;
    let arr = parentNode.getElementsByTagName('iframe');
    for (let i = 0; i < arr.length; i++) {
        if (arr[i].id === name) {
            arr[i].style.display = "block";
        } else {
            arr[i].style.display = "none";
        }
    }
}

function delStartSelector() {
    document.getElementById('playerSelectorSecond').remove();
    document.getElementById('playerSelector').style.display = "flex";
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
        });
}