function getCaravanImg(clazz) {
    return "<img style='max-width: 100%' " + "alt='caravan' " +
        "class='caravanImageClassForInteractive imageFromUser " + clazz + "Border' " +
        "src='/caravan/caravan.jpg' " +
        "onclick='joinRobbery()'>";
}

function getImgPrice() {
    return "<img style='width: 16px' src='/img/money-bag.png' alt='price'>";
}

function getImgExp() {
    return "<img style='width: 16px' src='/img/rank.png' alt='exp'>";
}

function getImgPremium() {
    return "<img src='/img/premium.png' class='premiumImg' alt='premium'>";
}

function joinRobbery() {
    sendMessage("command", "!rob");
}

function getTranslatedRarity(message) {
    let rWord;
    let rarity = message.rarity;
    if (rarity === "ANCIENT") {
        return ANCIENT;
    } else if (rarity === "LEGENDARY") {
        return LEGENDARY;
    } else if (rarity === "EPIC") {
        return EPIC;
    } else if (rarity === "RARE") {
        return RARE;
    } else if (rarity === "COMMON") {
        return COMMON;
    }
}

function getTranslatedItemName(name) {
    switch (name.toLowerCase()) {
        case "AdultMagazine".toLowerCase():
            return caravanItemNameAdultMagazine;
        case "AlcoholMashine".toLowerCase():
            return caravanItemNameAlcoholMashine;
        case "BugOfPotatoes".toLowerCase():
            return caravanItemNameBugOfPotatoes;
        case "Cake".toLowerCase():
            return caravanItemNameCake;
        case "Cookie".toLowerCase():
            return caravanItemNameCookie;
        case "Dildo".toLowerCase():
            return caravanItemNameDildo;
        case "GoldBar".toLowerCase():
            return caravanItemNameGoldBar;
        case "GoodTeammates".toLowerCase():
            return caravanItemNameGoodTeammates;
        case "GreenShield".toLowerCase():
            return caravanItemNameGreenShield;
        case "KeyFromTheHeartOfAnOwl".toLowerCase():
            return caravanItemNameKeyFromTheHeartOfAnOwl;
        case "Kitten".toLowerCase():
            return caravanItemNameKitten;
        case "Medkit".toLowerCase():
            return caravanItemNameMedkit;
        case "RubberWoman".toLowerCase():
            return caravanItemNameRubberWoman;
        case "Socks".toLowerCase():
            return caravanItemNameSocks;
        case "Trash".toLowerCase():
            return caravanItemNameTrash;
    }
}

function printCaravanJoinMessage() {
    let bodyChatMain = document.getElementById('bodyChatMain');
    let div = document.createElement('div');
    div.innerHTML = caravanJoin;
    div.className = "caravanMessage caravanJoin";
    div.style.fontSize = fontSize;
    bodyChatMain.appendChild(div);
}


function printCaravanReward(message) {
    message = JSON.parse(message);
    let bodyChatMain = document.getElementById('bodyChatMain');
    let div = document.createElement('div');
    div.className = "caravanMessage";

    let duplicate = message.duplicate;
    if (duplicate !== true) duplicate = false;

    let exp;
    if (message.exp !== 0) {
        exp = " " + getImgExp() + message.exp;
    } else {
        exp = "";
    }

    let money = duplicate ? getImgPrice() + message.price : "";

    let rewardName;
    if (message.type === "smile") {
        rewardName = caravanItemNameSmile + "<img src='/smiles/" + message.link + "' class='smile' alt='reward'>";
        if (duplicate) rewardName = rewardName + " (" + caravanItemNameDuplicate + ")";
    } else if (message.type === "style") {
        rewardName = "<span class='" + message.name + "'>" + message.name + " " + caravanItemNameStyle + "</span>";
        if (duplicate) rewardName = rewardName + " (" + caravanItemNameDuplicate + ")";
    } else if (message.type === "item") {
        rewardName = getTranslatedItemName(message.name);
        money = getImgPrice() + message.price;
    } else if (message.type === "premium") {
        rewardName = getImgPremium() + caravanItemNamePremium;
        money = exp = "";
    }

    let premiumImg = "";
    if (message.premiumUser === true) {
        premiumImg = getImgPremium();
    }

    div.innerHTML = prepareCaravanRewardInnerHTML(message, premiumImg, rewardName, money, exp);

    div.style.fontSize = fontSize;
    bodyChatMain.appendChild(div);
}

function prepareCaravanRewardInnerHTML(message, premiumImg, rewardName, money, exp) {
    return "<span  class='bodyChatTitle " + message.rarity + "'>" +
        "<div style='display: inline-block'>" + premiumImg + message.nickname + "</div>" +
        " " + caravanReceived + ": " + rewardName + " " +
        "<div style='display: inline-block'>" + money + exp + "</div>" +
        "</span>";
}


function printCaravanStartMessage(message) {
    message = JSON.parse(message);
    let bodyChatMain = document.getElementById('bodyChatMain');
    let div = document.createElement('div');
    div.className = "caravanMessage caravanStart";
    let rarityTranslated = getTranslatedRarity(message);

    div.innerHTML = prepareCaravanStartInnerHTML(message, rarityTranslated);

    div.style.fontSize = fontSize;
    bodyChatMain.appendChild(div);
}


function prepareCaravanStartInnerHTML(message, rarityTranslated) {
    return caravanStart + " "
        + message.time + " " + timeUnitMin + ". " + robCommand
        + getCaravanImg(message.rarity)
        + "<span style='cursor: pointer' onclick='joinRobbery()' "
        + "class='caravanTitleClassForInteractive bodyChatTitle " + message.rarity + "'>"
        + rarityTranslated
        + "</span>"
        + " " + caravanPriceToJoin + ": " + getImgPrice() + message.price + " ";
}


function printCaravanEndMessage(clazz, message) {
    let bodyChatMain = document.getElementById('bodyChatMain');
    let div = document.createElement('div');
    div.className = "caravanMessage " + clazz;
    div.innerHTML = message;
    div.style.fontSize = fontSize;
    bodyChatMain.appendChild(div);
    muteAllCaravanImagesAndTitles();
}

function muteAllCaravanImagesAndTitles() {
    muteAllCaravanImages();
    muteAllCaravanTitles();
}

function muteAllCaravanImages() {
    let images = document.getElementsByClassName('caravanImageClassForInteractive');
    for (let i = 0; i < images.length; i++) {
        images[i].onclick = "";
        images[i].classList.add('grayCaravan');
    }
}

function muteAllCaravanTitles() {
    let title = document.getElementsByClassName('caravanTitleClassForInteractive');
    for (let i = 0; i < title.length; i++) {
        title[i].onclick = "";
        title[i].classList.add('grayCaravanTitle');
    }
}
