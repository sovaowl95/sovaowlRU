//todo: ANOTHER API SERVICE
function sendAdminRequest(link, body) {
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

function setAdminText() {
    let info = document.getElementById('daily').value;
    sendAdminRequest('/admin/setDaily', JSON.stringify({'info': info}));
}

function readFeedback() {
    sendAdminRequest("/admin/readNewMessages");
}

/**TWITCH*/
function reloadTwitchSmiles() {
    sendAdminRequest("/admin/twitch/all");
}

function loadSmilesFromTwitch() {
    sendAdminRequest("/admin/twitch/loadFromTwitch");
}

function parseTwitchSmiles() {
    sendAdminRequest("/admin/twitch/parse");

}

function initTwitchSmiles() {
    sendAdminRequest("/admin/twitch/init");
}

/**GG*/
function reloadGGSmiles() {
    sendAdminRequest("/admin/gg/all");
}

function loadSmilesFromGG() {
    sendAdminRequest("/admin/gg/loadFromGG");
}

function parseGGSmiles() {
    sendAdminRequest("/admin/gg/parse");

}

function initGGSmiles() {
    sendAdminRequest("/admin/gg/init");
}


/**YOUTUBE*/
function reloadYTSmiles() {
    sendAdminRequest("/admin/yt/all");
}

function loadSmilesFromYT() {
    sendAdminRequest("/admin/yt/loadFromYT");
}

function parseYTSmiles() {
    sendAdminRequest("/admin/yt/parse");

}

function initYTSmiles() {
    sendAdminRequest("/admin/yt/init");
}


/**CARAVAN*/
function enableCaravan() {
    sendAdminRequest("/admin/caravan/enable");
}

function disableCaravan() {
    sendAdminRequest("/admin/caravan/disable");
}

function nextStepCaravan() {
    sendAdminRequest("/admin/caravan/nextStep");
}

/**SMILES*/
function addNewSmile() {
    sendAdminRequest("/admin/smiles/addNewSmile");
}

function addSmileForEveryone() {
    let body = document.getElementById('addSmileForEveryone').value;
    sendAdminRequest("/admin/smiles/addSmileForEveryone", JSON.stringify({'id': body}));
}

function reloadWebsiteSmiles() {
    sendAdminRequest("/admin/smiles/reloadWebsiteSmiles");
}

/**STYLES*/
function addNewStyle() {
    sendAdminRequest('/admin/styles/addNewStyle');
}

/**PREMIUMS*/
function revalidatePremiums() {
    sendAdminRequest("/admin/premiums/revalidatePremiums");
}

function addPremiumToUser() {
    let body = document.getElementById('addPremiumToUser').value;
    sendAdminRequest("/admin/premiums/addPremiumToUser", JSON.stringify({'id': body}));
}

function giftPremiumForEveryoneForDays() {
    let body = document.getElementById('giftPremiumForEveryoneForDays').value;
    sendAdminRequest("/admin/premiums/giftPremiumForEveryoneForDays", JSON.stringify({'days': body}));
}

/**ICONS*/
function addNewIconToUser() {
    let body = document.getElementById('addNewIconToUser').value;
    sendAdminRequest("/admin/premiums/addNewIconToUser", JSON.stringify({'id': body}));
}

/**ACHIEVEMENTS*/
function addAchievementToUser() {
    let body = document.getElementById('addAchievementToUser').value;
    let name = document.getElementById('addAchievementToUserName').value;
    sendAdminRequest("/admin/achievements/addAchievementToUser", JSON.stringify({'id': body, 'name': name}));
}


/**USER MAP*/
function saveUserMap() {
    sendAdminRequest("/admin/userHandler/saveUserMap");
}

function clearUserMap() {
    sendAdminRequest("/admin/userHandler/clearUserMap");
}

/**NEWS*/
function addNews() {
    sendAdminRequest("/admin/news/createNews");
}

function addSubNews() {
    let body = document.getElementById('addSubNews').value;
    sendAdminRequest("/admin/news/createNewsSub", JSON.stringify({'id': body}));
}

function addTextToSub() {
    let body = document.getElementById('addTextToSub').value;
    sendAdminRequest("/admin/news/addTextToSub", JSON.stringify({'id': body}));
}
