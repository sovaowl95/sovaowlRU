function printMessage(message, dst) {
    let id = message.id;
    if (document.getElementById('message' + id) !== null) {
        return;
    }

    if (message.type === DONATION) {
        printDonationMessage(message);
        return;
    }

    let nick = message.nick;
    let style = message.style;
    let icons = message.icons;
    let text = message.text;
    let premiumText = message.premText;
    let twitchSmilesInfo = message.twitchSmilesInfo;
    let webSiteSmilesInfo = message.webSiteSmilesInfo;
    let ggSmilesInfo = message.ggSmilesInfo;
    let image = message.image;

    let lvl = message.level;
    let highlightedMessage = message.highlighted;

    let authorModerator = message.moderator;
    let globalAdmin = message.globalAdmin;
    let premiumUser = message.premiumUser;

    text = replaceAllSmiles(text, twitchSmilesInfo, webSiteSmilesInfo, ggSmilesInfo);

    let messageDiv = document.createElement('div');
    if (findNick(text, userName)) {
        messageDiv.className = 'messageForU';
    } else {
        messageDiv.className = 'message';
    }

    if (highlightedMessage) {
        messageDiv.classList.add('highlightedMessage');
    }
    messageDiv.id = 'message' + id;

    let divIcons = document.createElement('div');
    divIcons.className = 'icons';

    if (authorModerator) {
        let img = document.createElement('img');
        img.setAttribute('src', '/img/mod.png');
        divIcons.appendChild(img);
    }

    if (globalAdmin) {
        let img = document.createElement('img');
        img.setAttribute('src', '/img/admin.png');
        divIcons.appendChild(img);
    }

    if (premiumUser) {
        let img = document.createElement('img');
        img.setAttribute('src', '/img/premium.png');
        divIcons.appendChild(img);
    }

    if (icons !== undefined && icons.length !== 0) {
        for (let i = 0; i < icons.split(" ").length; i++) {
            let img = document.createElement('img');
            img.setAttribute('src', '/img/' + icons.split(" ")[i]);
            divIcons.appendChild(img);
        }
    }

    let divLogin = document.createElement('div');
    divLogin.innerHTML = nick;
    divLogin.className = 'nick ' + style;
    divLogin.addEventListener("click", clickOnNickListener);

    let divTime = solveTime(message);

    let divText = document.createElement('div');
    if (premiumText === true) {
        divText.setAttribute("class", 'textMessage ' + style);
    } else {
        divText.className = 'textMessage';
    }

    divText.innerHTML = text;

    let tempImgWithShtora;
    let tempImgWithShtoraInsertBefore;
    let addCEOpen = function (ev) {
        ev.stopPropagation();
        let shtora = document.createElement('div');
        shtora.id = 'shtora';
        shtora.className = 'shtora';
        let parentNode = document.getElementsByTagName('body')[0];
        parentNode.after(shtora);
        let img = ev.target;
        img.classList.add('popUpFullScreen');
        img.removeEventListener('click', addCEOpen);
        img.addEventListener('click', addCEClose);
        shtora.addEventListener('click', addCEClose);
        tempImgWithShtora = img;
        tempImgWithShtoraInsertBefore = img.nextElementSibling;
        shtora.after(tempImgWithShtora);
    };
    let addCEClose = function (ev) {
        ev.stopPropagation();
        document.getElementById('shtora').remove();
        if (tempImgWithShtoraInsertBefore === null) {
            divText.appendChild(tempImgWithShtora);
        } else {
            divText.insertBefore(tempImgWithShtora, tempImgWithShtoraInsertBefore);
        }

        tempImgWithShtora.classList.remove('popUpFullScreen');
        tempImgWithShtora.removeEventListener('click', addCEClose);
        tempImgWithShtora.addEventListener('click', addCEOpen);
    };

    if (divText.getElementsByTagName('img').length > 0) {
        let elementsByTagName = divText.getElementsByTagName('img');
        for (let i = 0; i < elementsByTagName.length; i++) {
            if (elementsByTagName[i].classList.contains("copySmile"))
                continue;
            if (!elementsByTagName[i].classList.contains("smile")) {
                elementsByTagName[i].addEventListener('click', addCEOpen);
            } else {
                elementsByTagName[i].style.height = smileSize;
            }

            if (!elementsByTagName[i].classList.contains("imageFromUser"))
                solveToolTip(elementsByTagName, i);
        }
    }

    if (meModerator) {
        let modShortCutEl = createElForModeratorHelpBar('deleteMessageShortCut(this)', 'garbage');
        modShortCutEl.setAttribute('class', 'miniIconDelete');
        messageDiv.appendChild(modShortCutEl);

        let modShortCutEl2 = createElForModeratorHelpBar('timeoutMessageShortCut(this)', 'timeout');
        modShortCutEl2.setAttribute('class', 'miniIconDelete');
        messageDiv.appendChild(modShortCutEl2);

        let modShortCutEl3 = createElForModeratorHelpBar('banMessageShortCut(this)', 'ban');
        modShortCutEl3.setAttribute('class', 'miniIconDelete');
        messageDiv.appendChild(modShortCutEl3);
    }


    let delimiter = document.createElement('div');
    delimiter.innerHTML = "&nbsp;";
    delimiter.classList.add("delimiter");

    messageDiv.appendChild(divTime);
    messageDiv.appendChild(divIcons);
    if (lvl !== undefined && lvl !== null && lvl !== 0) {
        let levelDiv = document.createElement('div');
        levelDiv.classList.add('levelDiv');
        let background;
        if (lvl <= 10) {
            background = "#cd7f32";
        } else if (lvl >= 11 && lvl <= 20) {
            background = "silver";
        } else if (lvl >= 21 && lvl <= 50) {
            background = "gold";
        } else if (lvl >= 51 && lvl <= 200) {
            background = "#e5e4e2";
            levelDiv.style.color = "#000000";
        } else if (lvl >= 201) {
            background = "url('/textures/diamond.jpg')";
            levelDiv.style.color = "rgb(251, 255, 4)";
            levelDiv.style.fontWeight = "700";
            levelDiv.classList.add('diamondShadow');
        }
        levelDiv.style.background = background;
        levelDiv.style.backgroundSize = "100% 100%";
        levelDiv.innerHTML = lvl;
        messageDiv.appendChild(levelDiv);
    }

    messageDiv.appendChild(divLogin);
    if (msId !== null) {
        let fromChannel = document.createElement('div');
        fromChannel.innerHTML = '(' + nick + ')';
        fromChannel.className = 'nick';
        messageDiv.appendChild(fromChannel);
    }
    messageDiv.appendChild(delimiter);
    messageDiv.appendChild(divText);

    if (!scrollEnable) newMessage = true;


    /**MODERATOR BLOCK BY CTRL*/
    if (meModerator) {
        let moderatorBlock = document.createElement('div');
        moderatorBlock.setAttribute('class', 'moderatorHelpBar');
        moderatorBlock.appendChild(createElForModeratorHelpBar('deleteMessage(this)', 'garbage'));
        moderatorBlock.appendChild(createElForModeratorHelpBar('timeoutMessage(this)', 'timeout'));
        moderatorBlock.appendChild(createElForModeratorHelpBar('banMessage(this)', 'ban'));
        if (authorModerator) {
            moderatorBlock.appendChild(createElForModeratorHelpBar('unmodMessage(this)', 'unmod'));
        } else {
            moderatorBlock.appendChild(createElForModeratorHelpBar('modMessage(this)', 'mod'));
        }
        moderatorBlock.appendChild(createElForModeratorHelpBar('purgeMessage(this)', 'purge'));
        messageDiv.appendChild(moderatorBlock);
    }

    messageDiv.style.fontSize = fontSize;
    document.getElementById('bodyChatMain').appendChild(messageDiv);
}

function solveToolTip(elementsByTagName, i) {
    if (isPublicChat === true) return;
    let toolTip = document.createElement('div');
    elementsByTagName[i].parentNode.insertBefore(toolTip, elementsByTagName[i]);
    toolTip.appendChild(elementsByTagName[i]);
    toolTip.style.display = "inline-block";
    toolTip.classList.add('tooltip');

    let imgCopy = document.createElement('img');
    imgCopy.setAttribute('src', elementsByTagName[i].getAttribute('src'));
    imgCopy.setAttribute('alt', elementsByTagName[i].getAttribute('alt'));
    imgCopy.setAttribute('class', elementsByTagName[i].getAttribute('class') + ' copySmile');
    imgCopy.setAttribute('style', 'height: 128px');

    let toolTipText = document.createElement('span');
    toolTipText.classList.add('tooltiptext');
    toolTip.appendChild(toolTipText);
    toolTipText.innerHTML = imgCopy.outerHTML + elementsByTagName[i].getAttribute('title');
    toolTipText.innerHTML = toolTipText.innerHTML.replace("/1.0", "/3.0");
}

function createElForModeratorHelpBar(action, imgLink) {
    let el = document.createElement('div');
    el.setAttribute('class', 'moderatorHelpBarWrap');
    el.setAttribute('onclick', action);
    let purgeImg = document.createElement('img');
    purgeImg.setAttribute('src', '/img/' + imgLink + '.png');
    purgeImg.setAttribute('class', 'moderatorHelpBarItem');
    el.appendChild(purgeImg);
    return el;
}

function sendMessage(type, message, secondType) {
    if (message.indexOf("<br>") !== -1) {
        message = message
            .replace(new RegExp("&nbsp;", 'g'), " ")
            .replace(new RegExp("<br>", 'g'), " ");
    }

    if (message.indexOf("!") === 0) {
        type = "command";
    } else if (message.indexOf("/") === 0) {
        type = MOD_ACTION;
    }

    let str = {
        type: type,
        text: message
    };

    if (secondType !== undefined) {
        str.secondType = secondType;
    }

    client.publish({
        destination: destinationSend,
        body: JSON.stringify(str)
    });
}

function deleteMessageShortCut(el) {
    let id = el.parentNode.id.replace(MESSAGE, "");
    sendMessage(MOD_ACTION, "/clear " + id, "id");
}

function timeoutMessageShortCut(el) {
    let id = el.parentNode.id.replace(MESSAGE, "");
    sendMessage(MOD_ACTION, "/t " + id, "id");
}

function banMessageShortCut(el) {
    let id = el.parentNode.id.replace(MESSAGE, "");
    sendMessage(MOD_ACTION, "/b " + id, "id");
}

function deleteMessage(el) {
    let id = el.parentNode.parentNode.id.replace(MESSAGE, "");
    sendMessage(MOD_ACTION, "/clear " + id, "id");
}

function timeoutMessage(el) {
    let id = el.parentNode.parentNode.id.replace(MESSAGE, "");
    sendMessage(MOD_ACTION, "/timeout " + id, "id");
}

function banMessage(el) {
    let id = el.parentNode.parentNode.id.replace(MESSAGE, "");
    sendMessage(MOD_ACTION, "/ban " + id, "id");
}

function modMessage(el) {
    let id = el.parentNode.parentNode.id.replace(MESSAGE, "");
    sendMessage(MOD_ACTION, "/mod " + id, "id");
}

function unmodMessage(el) {
    let id = el.parentNode.parentNode.id.replace(MESSAGE, "");
    sendMessage(MOD_ACTION, "/unmod " + id, "id");
}

function purgeMessage(el) {
    let id = el.parentNode.parentNode.id.replace(MESSAGE, "");
    sendMessage(MOD_ACTION, "/purge " + id, "id");
}

function moderatorActions(text) {
    if (text.info.indexOf("ERROR") === 0) {
        printErrMessage('errorInModeratorActions', text.info);
    } else if (text.info.indexOf("banUserByMessageId ") === 0) {
        let split = splitWithTail(text.info, " ", 3);
        let reason = "";
        if (split.length === 4) reason = split[3];
        banByIdHtmlEdit(split[1], split[2], reason, bannedWord);
    } else if (text.info.indexOf("unBanUserByMessageId ") === 0) {
        let split = splitWithTail(text.info, " ", 3);
        let reason = "";
        if (split.length === 4) reason = split[3];
        unBanByIdHtmlEdit(split[1], split[2], reason, unBannedWord);
    } else if (text.info.indexOf("timeoutUserByMessageId ") === 0) {
        let split = splitWithTail(text.info, " ", 5);
        printSystemMessage('timeoutUserByMessageId', split[2] + " " + timeoutWord + " " + split[4] + " " + forWordNa + " " + split[3] + " " + timeUnitSec + " " + forWordZa + " " + split[5]);
        //timeoutByIdHtmlEdit(split[1], split[2], split[5], timeoutWord);
    } else if (text.info.indexOf("unTimeoutUserByMessageId ") === 0) {
        let split = splitWithTail(text.info, " ", 3);
        let reason = "";
        if (split.length === 4) reason = split[3];
        unTimeoutByIdHtmlEdit(split[1], split[2], reason, unTimeoutWord);
    } else if (text.info.indexOf("clearUserByMessageId ") === 0) {
        let split = splitWithTail(text.info, " ", 3);
        let reason = "";
        if (split.length === 4) reason = split[3];
        clearByIdHtmlEdit(split[1], split[2], reason, clearWord);
    } else if (text.info.indexOf("modUserByMessageId ") === 0) {
        let split = text.info.split(' ');
        printSystemMessage('modUserByMessageId', split[2] + " " + addedInModerList + " " + split[3]);
    } else if (text.info.indexOf("unmodUserByMessageId ") === 0) {
        let split = text.info.split(' ');
        printSystemMessage('unmodUserByMessageId', split[2] + " " + removedFromModerList + " " + split[3]);
    } else if (text.info.indexOf("purgeUserByMessageId ") === 0) {
        let split = splitWithTail(text.info, " ", 3);
        let who = split[1];
        let whom = split[2];
        let ids = split[3];
        let message = ids.split(" ");
        for (let i = 0; i < message.length; i++) {
            removeMessageById('message' + message[i]);
        }
        printSystemMessage('purgeUserByMessageId', who + " " + clearWord + " " + whom);
    } else {
        console.log(text);
    }
}

function removeMessageById(id) {
    let message = document.getElementById(id);
    if (message === null) return;
    removeAllBannedTagsFromMessage(message);

    message.getElementsByClassName('nick')[0].style.textDecoration = "line-through";

    let textMessageBan = document.createElement('div');
    textMessageBan.setAttribute('class', 'textMessageBanned');
    textMessageBan.setAttribute('onclick', 'showDeletedMessage(this)');
    textMessageBan.innerText = messageDeleted;

    let textMessage = message.getElementsByClassName('textMessage')[0];
    textMessage.after(textMessageBan);
    textMessage.style.display = "none";
}

function showDeletedMessage(el) {
    let message = el.parentNode;
    removeAllBannedTagsFromMessage(message);
    message.getElementsByClassName('textMessage')[0].style.display = 'inline';

    let textMessageBan = document.createElement('div');
    textMessageBan.setAttribute('class', 'textMessageBanned');
    textMessageBan.setAttribute('onclick', 'removeMessageById("' + message.id + '")');
    textMessageBan.innerText = messageDeletedHide;

    let textMessage = message.getElementsByClassName('textMessage')[0];
    textMessage.after(textMessageBan);

    //message.getElementsByClassName('textMessageBanned')[0].innerHTML = messageDeletedHide;
    // el.setAttribute('onclick', 'removeMessageById("' + message.id + '")');
}

function removeAllBannedTagsFromMessage(message) {
    let arr = message.getElementsByClassName('textMessageBanned');
    for (let i = 0; i < arr.length; i++) {
        arr[i].remove();
    }
}

function banByIdHtmlEdit(id, by, reason, word) {
    let message = document.getElementById('message' + id);
    if (message === null) {
        printSystemMessage('moderatorBan', by + " " + word + " " + "?" + " " + reason);
        return;
    }
    let textMessage = message.getElementsByClassName('textMessage')[0];
    if (textMessage === null) {
        printSystemMessage('moderatorBan', by + " " + word + " " + nickName + " " + reason);
        return;
    }
    let nickName = message.getElementsByClassName('nick')[0].innerText;
    if (nickName === null) {
        printSystemMessage('moderatorBan', by + " " + word + " " + nickName + " " + reason);
        return;
    }

    if (message.getElementsByClassName('textMessageBanned')[0] !== undefined) {
        printSystemMessage('moderatorBan', by + " " + word + " " + nickName + " " + reason);
        return;
    }

    removeMessageById('message' + id);

    printSystemMessage('moderatorBan', by + " " + word + " " + nickName + " " + reason);
}

function unBanByIdHtmlEdit(id, by, reason, word) {
    let message = document.getElementById('message' + id);
    if (message === null) {
        printSystemMessage('moderatorBan', by + " " + word + " " + "?" + " " + reason);
        return;
    }
    let textMessage = message.getElementsByClassName('textMessage')[0];
    if (textMessage === null) {
        printSystemMessage('moderatorBan', by + " " + word + " " + nickName + " " + reason);
        return;
    }
    let nickName = message.getElementsByClassName('nick')[0].innerText;
    if (nickName === null) {
        printSystemMessage('moderatorBan', by + " " + word + " " + nickName + " " + reason);
        return;
    }

    message.getElementsByClassName('nick')[0].style.textDecoration = "";

    let textMessageBan = document.getElementsByClassName('textMessageBanned')[0];
    if (textMessageBan == null) {
        printSystemMessage('moderatorBan', by + " " + word + " " + nickName + " " + reason);
        return;
    }
    textMessageBan.remove();

    textMessage.style.display = "inline";

    printSystemMessage('moderatorBan', by + " " + word + " " + nickName + " " + reason);
}

function timeoutByIdHtmlEdit(id, by, reason, word) {
    banByIdHtmlEdit(id, by, reason, word);
}

function unTimeoutByIdHtmlEdit(id, by, reason, word) {
    unBanByIdHtmlEdit(id, by, reason, word);
}

function clearByIdHtmlEdit(id, by, reason, word) {
    banByIdHtmlEdit(id, by, reason, word);
}


//todo: ANOTHER API SERVICE
function replaceAllSmiles(text, twitchSmilesInfo, webSiteSmileInfo, ggSmilesInfo) {
    let smilesList = [];
    if ((twitchSmilesInfo !== undefined && twitchSmilesInfo !== null && twitchSmilesInfo.length > 0)
        || (webSiteSmileInfo !== undefined && webSiteSmileInfo !== null && webSiteSmileInfo.length > 0)
        || (ggSmilesInfo !== undefined && ggSmilesInfo !== null && ggSmilesInfo.length > 0)) {

        try {
            let arr = twitchSmilesInfo.split("/");
            for (let i = 0; i < arr.length; i++) {
                let id = arr[i].split(":", 2)[0];
                let parts = arr[i].split(":", 2)[1].split(",");
                for (let j = 0; j < parts.length; j++) {
                    smilesList.push({
                        'id': id,
                        'from': parts[j].split("-")[0],
                        'to': parts[j].split("-")[1],
                        'source': 'twitch'
                    });
                }
            }
        } catch (e) {
        }

        try {
            let arr = webSiteSmileInfo.split("/");
            for (let i = 0; i < arr.length; i++) {
                let id = arr[i].split(":", 2)[0];
                let parts = arr[i].split(":", 2)[1].split(",");
                for (let j = 0; j < parts.length; j++) {
                    smilesList.push({
                        'id': id,
                        'from': parts[j].split("-")[0],
                        'to': parts[j].split("-")[1],
                        'source': 'website'
                    });
                }
            }
        } catch (e) {
        }

        try {
            let arr = ggSmilesInfo.split("/");
            for (let i = 0; i < arr.length; i++) {
                let id = arr[i].split(":", 2)[0];
                let parts = arr[i].split(":", 2)[1].split(",");
                for (let j = 0; j < parts.length; j++) {
                    smilesList.push({
                        'id': id,
                        'from': parts[j].split("-")[0],
                        'to': parts[j].split("-")[1],
                        'source': 'gg'
                    });
                }
            }
        } catch (e) {
        }

        smilesList.sort((a, b) => parseInt(a.from) < parseInt(b.from)
            ? 1
            : (parseInt(b.from) < parseInt(a.from) ? -1 : 0));

        for (let i = 0; i < smilesList.length; i++) {
            text = subStringReplace(text, smilesList[i].id, smilesList[i].from, smilesList[i].to, smilesList[i].source);
        }
    }

    return text;
}

function subStringReplace(text, replace, from, to, source) {
    let s1 = text.substring(0, from);
    let s2;
    if (source === "twitch") {
        s2 = "<img src='https://static-cdn.jtvnw.net/emoticons/v1/" + replace + "/1.0' " +
            "alt='" + text.substring(from, parseInt(to) + 1) + "' " +
            "title='" + text.substring(from, parseInt(to) + 1) + "' " +
            "class='smile'" +
            ">";
    } else if (source === "website") {
        s2 = "<img src='/smiles/" + replace + "' " +
            "alt='" + text.substring(from, parseInt(to) + 1) + "' " +
            "title='" + text.substring(from, parseInt(to) + 1) + "' " +
            "class='smile'" +
            ">";
    } else if (source === "gg") {
        replace = replace.replace(/%/g, "/");
        s2 = "<img src='https:" + replace + "' " +
            "alt='" + text.substring(from, parseInt(to) + 1) + "' " +
            "title='" + text.substring(from, parseInt(to) + 1) + "' " +
            "class='smile'" +
            ">";
    }
    let s3 = text.substring(parseInt(to) + 1, text.length);
    return s1 + s2 + s3;
}


function splitWithTail(str, delim, count) {
    let parts = str.split(delim);
    let tail = parts.slice(count).join(delim);
    let result = parts.slice(0, count);
    result.push(tail);
    return result;
}