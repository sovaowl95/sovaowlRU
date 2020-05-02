function smiles() {
    let modal = createModal();
    let body = document.getElementById('bodyChat');
    if (body === null)
        body = document.getElementsByTagName('body')[0];
    body.insertBefore(modal, body.firstChild);
}

function createModal() {
    let temp = document.getElementById("smileInputModal");
    if (temp !== null) temp.remove();

    let modal = document.createElement('div');
    modal.id = "smileInputModal";

    let select = document.createElement('select');
    let option1 = document.createElement('option');
    option1.innerHTML = "Twitch";
    let option2 = document.createElement('option');
    option2.innerHTML = "gg";
    select.append(option1, option2);

    let input = document.createElement('input');
    input.value = 'Kappa';

    let text = document.createElement('div');
    text.id = 'addSmileText';


    let close = document.createElement('button')
    close.innerText = closeAddSmile;
    close.setAttribute('onclick', 'closeModal()');
    close.classList.add('button');

    let button = document.createElement('button');
    button.innerText = addSmile;
    button.setAttribute('onclick', 'sendR()');
    button.classList.add('button');

    modal.append(select, input, text, button, close);
    return modal;
}

function closeModal() {
    try {
        document.getElementById("smileInputModal").remove();
    } catch (e) {
    }
}

function sendR() {
    let code = document.getElementById('smileInputModal').getElementsByTagName('input')[0].value;
    let service = document.getElementById('smileInputModal').getElementsByTagName('select')[0].value;

    let body = JSON.stringify({'code': code, 'service': service});
    console.log(body);

    mySendRequest("/settings/smiles/add/", body, function (request) {
        console.log(request.responseText);
        if (request.status === 200) {
            if (request.responseText === '') {
                document.getElementById('addSmileText').innerText = addSmileAlreadyHave
            } else {
                try {
                    document.getElementById("smileInputModal").remove();
                } catch (e) {
                }

                let response = JSON.parse(request.responseText);

                let wrap = document.createElement('div');
                wrap.classList.add("tooltip", "COMMON", "bodyChatSmilesStyle")

                let image = document.createElement("img");
                image.classList.add("smile", "smileFromWebsite", "savedSmileFromWebsite");
                image.alt = response.smileName;

                if (response.service === 'twitch') {
                    image.src = "https://static-cdn.jtvnw.net/emoticons/v1/" + response.smileCode + "/3.0";
                } else if (response.service === 'gg') {
                    let url = response.smileCode.replace(/%/g, "/");
                    image.src = 'https:' + url + '>';
                }

                let span = document.createElement("span");
                span.classList.add("tooltiptext");
                span.innerText = response.smileName;

                addSmileFromWebsiteListener(image);

                wrap.append(image);
                wrap.append(span);

                let element = document.getElementById('addSmilesImgButton');
                let tooltip = element.parentNode;
                let tooltipParent = tooltip.parentNode;

                tooltipParent.insertBefore(wrap, tooltip);
            }
        } else if (request.status === 400) {
            if (request.responseText.indexOf("saved smile not found") !== -1) {
                document.getElementById('addSmileText').innerText = addSmileNotFound;
            }
        } else {
            console.log("err");
        }
    })
}

function changeSmileTab(tab) {
    let smileList1 = document.getElementById('bodyChatSmilesList1');
    let smileList2 = document.getElementById('bodyChatSmilesList2');
    if (tab === "web") {
        smileList1.style.display = "block";
        smileList2.style.display = "none";
    } else if (tab === "save") {
        smileList1.style.display = "none";
        smileList2.style.display = "block";
    } else {
        console.log("unknown tab " + tab);
    }
}