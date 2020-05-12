/**CHANGE WEBSITE*/
function changeStreamName(parent) {
    changeAnyNode(parent);
}

function changeStreamGame(parent) {
    changeAnyNode(parent);
}

function changeChatDailyInfo(parent) {
    changeAnyNode(parent);
}

function changeAnyNode(parent) {
    let childNodes = parent.children;
    childNodes[0].style.display = "none";
    childNodes[1].style.display = "block";
    childNodes[2].style.display = "none";
    childNodes[3].style.display = "block";
}

function respStatus200(parent) {
    let childNodes = parent.children;
    childNodes[0].style.display = "block";
    childNodes[1].style.display = "none";
    childNodes[2].innerText = childNodes[1].value;
    childNodes[2].style.display = "block";
    childNodes[3].style.display = "none";
}

function doDeleteStream() {
    let request = new XMLHttpRequest();
    let link = "/delete";
    let streamName = document.getElementsByName('streamName')[0].getAttribute('content');

    request.open("POST", '/' + streamName + link, true);
    request.addEventListener("load", function () {
        if (request.status === 200) {
            window.location = '/';
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    request.send();
}

/**CHANGE DISCORD*/
function changeDiscordNotification(parent) {
    changeAnyNode(parent);
}

function confirmChangeDiscordNotification(parent) {
    let request = new XMLHttpRequest();
    let link = "/stream/settings/changeDiscordNotification";
    request.open("POST", link, true);


    request.addEventListener("load", function () {
        if (request.status === 200) {
            document.getElementById('discordNotificationInput').style.border = "1px solid lightgreen";
        } else {
            document.getElementById('discordNotificationInput').style.border = "1px solid red";
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    let value = document.getElementById('discordNotificationInput').value;
    let json = {'changeDiscordNotification': value};
    request.send(JSON.stringify(json));
}

function confirmChangeVKNotification() {
    let vkInput1 = document.getElementById('vkNotificationInput-1');
    let vkInput2 = document.getElementById('vkNotificationInput-2');
    let vkInput3 = document.getElementById('vkNotificationInput-3');
    let vkInput4 = document.getElementById('vkNotificationInput-4');
    let vkInput5 = document.getElementById('vkNotificationInput-5');

    let request = new XMLHttpRequest();
    let link = "/vk/settings";
    request.open("POST", link, true);
    request.addEventListener("load", function () {
        if (request.status === 200) {
            vkInput1.style.border = "1px solid lightgreen";
            vkInput2.style.border = "1px solid lightgreen";
            vkInput3.style.border = "1px solid lightgreen";
            vkInput4.style.border = "1px solid lightgreen";
            vkInput5.style.border = "1px solid lightgreen";
        } else {
            vkInput1.style.border = "1px solid red";
            vkInput2.style.border = "1px solid red";
            vkInput3.style.border = "1px solid red";
            vkInput4.style.border = "1px solid red";
            vkInput5.style.border = "1px solid red";
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    let json = {
        'group': vkInput1.value,
        'key': vkInput2.value,
        'response': vkInput3.value,
        'secret': vkInput4.value,
        'access_token': vkInput5.value,
    };
    request.send(JSON.stringify(json));
}

function changeDiscordText(parent) {
    changeAnyNode(parent);
}

function confirmChangeDiscordText(parent) {
    let request = new XMLHttpRequest();
    let link = "/stream/settings/changeDiscordText";
    request.open("POST", link, true);
    request.addEventListener("load", function () {
        if (request.status === 200) {
            respStatus200(parent);
        } else {
            document.getElementById('streamGameInput').style.border = "1px solid red";
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    let value = document.getElementById('discordTextInput').value;
    let json = {'discordText': value};
    request.send(JSON.stringify(json));
}

function linkDiscordServer() {
    window.location = 'https://discordapp.com/api/oauth2/authorize' +
        '?' +
        'client_id=' + discordClientId +
        '&' +
        'permissions=8' +
        '&' +
        'redirect_uri=https%3A%2F%2Fsovaowl.ru%2Fdiscord%2Fsuccess' +
        '&' +
        'response_type=code' +
        '&' +
        'scope=bot%20identify';
}

function unlinkDiscordServer() {
    let request = new XMLHttpRequest();
    let link = "/discord/remove";
    request.open("POST", link, true);
    request.addEventListener("load", function () {
        if (request.status === 200) {
            document.location.reload();
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    request.send();
}


function linkVKServer() {
    let request = new XMLHttpRequest();
    let link = "/vk/add";
    request.open("POST", link, true);
    request.addEventListener("load", function () {
        if (request.status === 200) {
            document.location.reload();
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    request.send();
}

function getVKKey() {
    let url = "https://oauth.vk.com/authorize" +
        "?" +
        "client_id=" + vkClientId +
        "&" +
        "display=page" +
        "&" +
        "response_type=token" +
        "&" +
        "redirect_uri=https://oauth.vk.com/blank.html" +
        "&" +
        "scope=wall,offline"
    ;
    let token = document.getElementsByName("secTokenState")[0].getAttribute('content');
    url = url + "&state=" + token;
    window.open(url, '_blank');
}

function unlinkVKServer() {
    let request = new XMLHttpRequest();
    let link = "/vk/remove";
    request.open("POST", link, true);
    request.addEventListener("load", function () {
        if (request.status === 200) {
            document.location.reload();
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    request.send();
}


/**CONFIRM*/
function confirmChangeStreamName(parent) {
    let request = new XMLHttpRequest();
    let link = "/stream/settings/changeStreamName";
    request.open("POST", link, true);
    request.addEventListener("load", function () {
        if (request.status === 200) {
            respStatus200(parent);
        } else {
            document.getElementById('streamNameInput').style.border = "1px solid red";
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    let value = document.getElementById('streamNameInput').value;
    let json = {'streamName': value};
    request.send(JSON.stringify(json));
}

function confirmChangeStreamGame(parent) {
    let request = new XMLHttpRequest();
    let link = "/stream/settings/changeStreamGame";
    request.open("POST", link, true);
    request.addEventListener("load", function () {
        if (request.status === 200) {
            respStatus200(parent);
        } else {
            document.getElementById('streamGameInput').style.border = "1px solid red";
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    let value = document.getElementById('streamGameInput').value;
    let json = {'streamGame': value};
    request.send(JSON.stringify(json));
}

function confirmChangeChatDailyInfo(parent) {
    let request = new XMLHttpRequest();
    let link = "/stream/settings/changeChatDailyInfo";
    request.open("POST", link, true);


    request.addEventListener("load", function () {
        if (request.status === 200) {
            respStatus200(parent);
        } else {
            document.getElementById('streamChatDailyInfoInput').style.border = "1px solid red";
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    let value = document.getElementById('streamChatDailyInfoInput').value;
    let json = {'chatDailyInfo': value};
    request.send(JSON.stringify(json));
}


/** SPAMMER */
function createSpammer() {
    let request = new XMLHttpRequest();
    let link = "/stream/settings/spam/create";
    request.open("POST", link, true);
    request.addEventListener("load", function () {
        if (request.status === 200) {
            document.location.reload();
        } else {
            // document.getElementById('streamChatDailyInfoInput').style.border = "1px solid red";
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    request.send();
}

function deleteSpammer(el) {
    let request = new XMLHttpRequest();
    let link = "/stream/settings/spam/delete";
    request.open("POST", link, true);
    request.addEventListener("load", function () {
        if (request.status === 200) {
            document.location.reload();
        } else {
            // document.getElementById('streamChatDailyInfoInput').style.border = "1px solid red";
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    let json = {'id': el};
    request.send(JSON.stringify(json));
}

function startSpammer(el) {
    let request = new XMLHttpRequest();
    let link = "/stream/settings/spam/start";
    request.open("POST", link + '/' + el, true);

    request.addEventListener("load", function () {
        if (request.status === 200) {
            document.location.reload();
        } else {
            // document.getElementById('streamChatDailyInfoInput').style.border = "1px solid red";
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    request.send();
}

function stopSpammer(el) {
    let request = new XMLHttpRequest();
    let link = "/stream/settings/spam/stop";
    request.open("POST", link + '/' + el, true);

    request.addEventListener("load", function () {
        if (request.status === 200) {
            document.location.reload();
        } else {
            // document.getElementById('streamChatDailyInfoInput').style.border = "1px solid red";
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    request.send();
}

function changeSpammerTime(el, id) {
    let request = new XMLHttpRequest();
    let link = "/stream/settings/spam/edit/time";
    request.open("POST", link, true);

    request.addEventListener("load", function () {
        if (request.status === 200) {
            document.location.reload();
        } else {
            // document.getElementById('streamChatDailyInfoInput').style.border = "1px solid red";
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    let time = el.parentNode.getElementsByTagName('input')[0].value;
    let json = {'id': id, 'time': time};
    request.send(JSON.stringify(json));
}

function changeSpammerDelay(el, id) {
    let request = new XMLHttpRequest();
    let link = "/stream/settings/spam/edit/delay";
    request.open("POST", link, true);

    request.addEventListener("load", function () {
        if (request.status === 200) {
            document.location.reload();
        } else {
            // document.getElementById('streamChatDailyInfoInput').style.border = "1px solid red";
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    let time = el.parentNode.getElementsByTagName('input')[0].value;
    let json = {'id': id, 'time': time};
    request.send(JSON.stringify(json));
}

function changeSpammerText(el, id) {
    let request = new XMLHttpRequest();
    let link = "/stream/settings/spam/edit/text";
    request.open("POST", link, true);

    request.addEventListener("load", function () {
        if (request.status === 200) {
            document.location.reload();
        } else {
            // document.getElementById('streamChatDailyInfoInput').style.border = "1px solid red";
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    let text = el.parentNode.getElementsByTagName('input')[0].value;
    let json = {'id': id, 'text': text};
    request.send(JSON.stringify(json));
}


function show(num) {
    let arr = document.getElementsByClassName('content');
    let row = document.getElementsByClassName('row')[0].getElementsByTagName('a');
    for (let i = 0; i < arr.length; i++) {
        if (i + 1 === num) {
            arr[i].style.display = 'block';
            row[i].style.borderBottom = '3px solid white';
        } else {
            arr[i].style.display = 'none';
            row[i].style.border = 'none';
        }
    }
}