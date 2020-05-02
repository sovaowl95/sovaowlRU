function sendRequest(link, functionResp, variables, body) {
    let request = new XMLHttpRequest();
    request.open("POST", link, true);
    request.addEventListener("load", function () {
        functionResp(request, variables);
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    if (body === undefined)
        request.send();
    else
        request.send(body);
}

/**
 * CHANGE STYLE
 * */
function changeStyle(obj) {
    sendRequest("/settings/style/" + obj.innerText, function (request) {
        if (request.status === 200) {
            clearUnderlineStyles();
            obj.firstElementChild.style.borderBottom = "1px solid";
        }
    })
}

function clearUnderlineStyles() {
    let el = document.getElementsByClassName('oneOfStyle');
    Array.from(el).forEach((el) => {
        el.style.borderBottom = "";
    });
}

/**
 * PREMIUM
 * */
function premText(el) {
    let val = el.checked;
    sendRequest("/settings/style/premtext/" + val, function (request) {
        if (request.status !== 200) {
            el.getElementsByTagName('input')[0] = !val;
        }
    })
}

/**
 * SHOW TIME
 * */
function showMessageTimeFunc(el) {
    let was = showMessageTime;
    showMessageTime = el.checked;
    if (isGuest === true) {
        showMessageTimeChangeHtml();
    } else if (was !== showMessageTime) {
        sendRequest("/settings/showTime/" + showMessageTime, function (request) {
            if (request.status === 200) {
                showMessageTimeChangeHtml();
            }
        });
    }
}

function showMessageTimeChangeHtml() {
    let message = document.getElementsByClassName('message');
    for (let i = 0; i < message.length; i++) {
        let time = message[i].getElementsByClassName('time')[0];
        if (showMessageTime) time.style.display = "inline";
        else time.style.display = "none";
    }
    let el = document.getElementsByClassName('bodyChatOptionsShowMessageTimeChoose')[0];
    if (showMessageTime) {
        el.firstElementChild.firstElementChild.style.borderBottom = "1px solid white";
    } else {
        el.lastElementChild.firstElementChild.style.borderBottom = "1px solid white";
    }
}

/**
 * SMILES
 * */
function changeSmilesSize() {
    let value = document.getElementById('smileSizeValue').value;
    if (isGuest === true) {
        changeSmilesSizeHTML(value);
        return;
    }
    sendRequest("/settings/style/smileSize/" + value, function (request) {
        if (request.status === 200) {
            console.log("req 200");
            changeSmilesSizeHTML(value);
        } else {
            console.log("req");
        }
    })
}

function restoreSmilesSize() {
    let value = 28;
    if (isGuest === true) {
        changeSmilesSizeHTML(value);
        return;
    }
    sendRequest("/settings/style/smileSize/" + value, function (request) {
        if (request.status === 200 || isGuest === true) {
            changeSmilesSizeHTML(value);
        }
    });
}

function changeSmilesSizeHTML(value) {
    let arr = document.getElementsByClassName('smile');
    for (let i = 0; i < arr.length; i++) {
        arr[i].style.height = value;
        document.getElementById('smileSizeValue').value = value;
        smileSize = value;
    }
}

/**
 * TEXT SIZE
 * */
function changeTextSize() {
    let size = document.getElementById('textSizeValue').value;
    if (isGuest === true) {
        setTextSize(size);
        return;
    }
    sendTextSizeReq("/settings/style/textSize/" + size, size);
}

function restoreTextSize() {
    let size = 16;
    if (isGuest === true) {
        setTextSize(size);
        return;
    }
    sendTextSizeReq("/settings/style/textSize/" + size, size);
}

function sendTextSizeReq(link, value) {
    sendRequest(link, function (request) {
        if (request.status === 200) {
            setTextSize(value);
        }
    })
}

function setTextSize(value) {
    fontSize = value;
    document.getElementById('textSizeValue').value = value;
    const childNodes = document.getElementById('bodyChatMain').childNodes;
    for (let i = 0; i < childNodes.length; i++) {
        childNodes[i].style.fontSize = value + "px";
    }
}

/**
 * DOM LOAD
 * */
document.addEventListener('DOMContentLoaded', function () {
    document.getElementById('smileSizeValue').oninput = function () {
        let elementById = document.getElementById('smileSizeValue');
        if (elementById.value > 0 && elementById.value < 150) {
            document.getElementById('smileSizeValueTest').style.height = elementById.value;
            scrollOptions.scroll({el: elementById});
        }
    }
});