function saveCommand(el) {
    let commandBlock = el.parentNode;
    let id = commandBlock.getElementsByTagName('div')[0].innerText;
    let enabled = commandBlock.getElementsByTagName('label')[0].getElementsByTagName('input')[0].checked;
    let keyWord = commandBlock.getElementsByTagName('label')[1].getElementsByTagName('input')[0].value;
    let alias = commandBlock.getElementsByTagName('label')[2].getElementsByTagName('input')[0].value;
    let action = commandBlock.getElementsByTagName('label')[3].getElementsByTagName('input')[0].value;
    let forPublicShown = commandBlock.getElementsByTagName('label')[4].getElementsByTagName('input')[0].checked;
    let needArgs = commandBlock.getElementsByTagName('label')[5].getElementsByTagName('input')[0].checked;
    let argsCount = commandBlock.getElementsByTagName('label')[6].getElementsByTagName('input')[0].value;
    let cooldown = commandBlock.getElementsByTagName('label')[7].getElementsByTagName('input')[0].value;
    let cost = commandBlock.getElementsByTagName('label')[8].getElementsByTagName('input')[0].value;

    let jsonBody = JSON.stringify({
        'id': id,
        'keyWord': keyWord,
        'alias': alias,
        'action': action,
        'forPublicShown': forPublicShown,
        'needArgs': needArgs,
        'argsCount': argsCount,
        'enabled': enabled,
        'cooldown': cooldown,
        'cost': cost
    });
    sendRequestCommand("/stream/commands/edit", jsonBody);
}

function deleteCommand(el) {
    let commandBlock = el.parentNode;
    let id = commandBlock.getElementsByTagName('div')[0].innerText;
    sendRequestCommand("/stream/commands/delete", JSON.stringify({'id': id}));
}

function createNewCommand() {
    sendRequestCommand("/stream/commands/create");
}

function testDonation() {
    sendRequestCommand("/stream/settings/sendTestDonation")
}


function sendRequestCommand(link, body) {
    let request = new XMLHttpRequest();
    request.open("POST", link, true);
    request.addEventListener("load", function () {
        if (request.status === 200) {
            alert("COMPLETE -> " + link);
        } else {
            alert("ERR");
            console.log(request.response);
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    if (body === null) {
        request.send();
    } else {
        request.send(body);
    }
}