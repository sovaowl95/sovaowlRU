let client = null;

let scrollSmiles1;
let scrollSmiles2;
let scrollOptions;
let scrollDonation;
let scrollInput;
let scrollChat;
let textArea;

let destinationSubArray = [];
let destinationSend;

let meModerator = false;
let connected = false;

let chatSessionId;

function bodyChatTopOpen() {
    let elClose = document.getElementById('bodyChatTopClose');
    let elOpen = document.getElementById('bodyChatTopOpen');
    let elText = document.getElementById('bodyChatTopText');
    elClose.style.display = "block";
    elOpen.style.display = "none";
    elText.style.display = "block";
}

function bodyChatTopClose() {
    try {
        document.getElementById('bodyChatTopTextBefore').remove();
    } catch (ignore) {
    }
    try {
        let elClose = document.getElementById('bodyChatTopClose');
        let elOpen = document.getElementById('bodyChatTopOpen');
        let elText = document.getElementById('bodyChatTopText');
        elClose.style.display = "none";
        elOpen.style.display = "block";
        elText.style.display = "none";
        return true;
    } catch (e) {
        return false;
    }
}

function connect() {
    client = new StompJs.Client({
        connectHeaders: {
            login: isGuest ? "guest" : userName,
        },
        debug: function (str) {
            console.log(str);
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000
    });

    client.webSocketFactory = function () {
        return new SockJS("/chat");
    };

    client.onConnect = function () {
        destinationSubArray = [];

        connected = true;
        let split = client.webSocket._transport.url.split("/");
        chatSessionId = split[split.length - 2];

        if (typeof streamsLogins !== 'undefined' && streamsLogins !== null) {
            for (let i = 0; i < streamsLogins.length; i++) {
                destinationSubArray.push('/topic/' + streamsLogins[i]);
            }
            destinationSubArray.push('/topic/ms/' + msId);
            destinationSend = '/app/ms/' + msId;
        } else {
            destinationSubArray.push('/topic/' + streamerName);
            destinationSend = '/app/' + streamerName;
        }

        for (let i = 0; i < destinationSubArray.length; i++) {
            let destinationSub = destinationSubArray[i];

            client.subscribe(destinationSub, function (message) {
                let dst = destinationSub.replace('/topic/', '');
                try {
                    let parse = JSON.parse(message.body);
                    console.log(parse);
                    if (parse.type === MESSAGE) {
                        printMessage(parse, dst);
                    } else if (parse.type === INFO_BAN) {
                        printSystemMessage('infoFromServer', parse.info);
                    } else if (parse.type === INFO_TIMEOUT) {
                        printSystemMessage('infoFromServer', parse.info);
                    } else if (parse.type === MOD_ACTION) {
                        moderatorActions(parse);
                    } else if (parse.type === DONATION) {
                        printDonationMessage(parse);
                    } else if (parse.type === RANK_UP) {
                        printLvlUpMessage(parse.info);
                    } else if (parse.type === SPAM) {
                        printSpamInfoMessage('systemSpamInfo', parse);
                    } else if (parse.type === CLEAR_ALL) {
                        printClearAllMessage(CLEAR_ALL, parse.info);
                    } else if (parse.type === COMMAND_ANSWER_OK) {
                        printCommandMessage(COMMAND_ANSWER_OK, parse.info, parse.streamId);
                    }
                    /**
                     * SLOT
                     */
                    else if (parse.type === SLOT_RES) {
                        printSlotMessage(SLOT_RES, parse.info, parse.streamId);
                    }
                    /**
                     * CARAVAN
                     * */
                    else if (parse.type === CARAVAN_START) {
                        printCaravanStartMessage(parse.info);
                    } else if (parse.type === CARAVAN_END) {
                        printCaravanEndMessage(CARAVAN_END, caravanEnd, parse.info);
                    } else if (parse.type === CARAVAN_REWARD) {
                        printCaravanReward(parse.info);
                    } else {
                        printSystemMessage('ERROR');
                        console.log("ERR");
                        console.log(parse);
                    }
                } catch (e) {
                    printSystemMessage('ERROR', e);
                    console.log(e);
                }
            });
            client.subscribe(destinationSub + '-user' + chatSessionId, function (message) {
                try {
                    let parse = JSON.parse(message.body);
                    console.log(parse);
                    if (parse.type === "history") {
                        let parse2 = JSON.parse(parse.info);
                        if (!isGuest) meModerator = parse2[0].info === "true";
                        else meModerator = false;
                        // parse2.splice(0, 1);

                        parse2.sort((a, b) => parseInt(a.id) < parseInt(b.id)
                            ? 1
                            : (parseInt(b.id) < parseInt(a.id) ? -1 : 0));

                        if (isGuest) {
                            for (let i = parse2.length - 1; i >= 0; i--) {
                                printMessage(parse2[i]);
                            }
                        } else
                            for (let i = parse2.length - 2; i >= 0; i--) {
                                printMessage(parse2[i]);
                            }
                    } else if (parse.type === MOD_ACTION) {
                        moderatorActions(parse);
                    } else if (parse.type === INFO_BAN) {
                        printSystemMessage('infoFromServer', parse.info);
                    } else if (parse.type === INFO_SPAM) {
                        printSpamMessage('infoFromServer', parse.info);
                    } else if (parse.type === INFO_TIMEOUT) {
                        printSystemMessage('infoFromServer', parse.info);
                    } else if (parse.type === COMMAND_ANSWER_ERROR) {
                        printErrMessage(COMMAND_ANSWER_ERROR, parse.info);
                    } else if (parse.type === COMMAND_ANSWER_OK) {
                        printCommandMessage(COMMAND_ANSWER_OK, parse.info);
                    } else if (parse.type === INFO_PREMIUM_EXPIRED_IN) {
                        printPremiumExpiredMessage(parse.info);
                    } else if (parse.type === INFO_HELP) {
                        printSystemMessage(INFO_HELP, parse.info);
                    }

                    /**
                     * API PRIVATE
                     */
                    else if (parse.type === ACC_REJOIN) {
                        printErrMessage(ACC_REJOIN, parse.info);
                    } else if (parse.type === ACC_REJOIN_OK) {
                        printGoodMessage(ACC_REJOIN_OK, parse.info);
                    } else if (parse.type === ACC_REJOIN_ASK) {
                        printSystemMessage(ACC_REJOIN_ASK, parse.info);
                    } else if (parse.type === API_MOTIVATION) {
                        printGoodMessage(API_MOTIVATION, parse.info);
                    }

                    /**
                     * SLOT PRIVATE
                     */
                    else if (parse.type === SLOT_NOT_ENOUGH_MONEY) {
                        printErrMessage(SLOT_NOT_ENOUGH_MONEY, slotNotEnoughMoney);
                    } else if (parse.type === SLOT_START) {
                        starSpinning();
                    }
                    /**
                     * CARAVAN PRIVATE
                     * */
                    else if (parse.type === CARAVAN_JOIN) {
                        printCaravanJoinMessage();
                    } else if (parse.type === CARAVAN_JOIN_NOT_ENOUGH_MONEY) {
                        printErrMessage(CARAVAN_JOIN_NOT_ENOUGH_MONEY, caravanErrNotEnoughMoney);
                    } else if (parse.type === CARAVAN_JOIN_ERR_ALREADY_IN_JOIN) {
                        printErrMessage(CARAVAN_JOIN_ERR_ALREADY_IN_JOIN, caravanErrAlreadyInJoin);
                    } else if (parse.type === CARAVAN_JOIN_ERR_STATUS_JOIN) {
                        printErrMessage(CARAVAN_JOIN_ERR_STATUS_JOIN, caravanErrStatusJoin);
                    } else if (parse.type === CARAVAN_JOIN_ERR_STATUS_JOIN_ANON) {
                        printErrMessage(CARAVAN_JOIN_ERR_STATUS_JOIN_ANON, caravanErrStatusJoinAnon);
                    } else if (parse.type === CARAVAN_START) {
                        printCaravanStartMessage(parse.info);
                    } else {
                        console.log("ERR");
                        console.log(parse);
                    }
                } catch (e) {
                    console.log(e);
                    printSystemMessage('ERROR', e);
                }
            });
        }


        setTimeout(function () {
            sendMessage("history", "");
        }, 1000);
        printSocketOpenMessage();
    };

    client.onDisconnect
        = client.onStompError
        = client.onUnhandledFrame
        = client.onUnhandledMessage
        = client.onUnhandledReceipt
        = client.onWebSocketClose
        = client.onWebSocketError
        =
        function (frame) {
            connected = false;
            printSocketCloseMessage();
        };
    client.activate();
}

function replaceTags() {
    if (/(?!<br>)(<([^>]+)>)/ig.test(textArea.innerHTML)) {
        $("#textareaID").html($("#textareaID").text());
        focusEndOfTextArea();
    }
    //todo: автозаполнение никнейма
}

function findNick(str, nick) {
    if (nick === undefined) return false;
    if (str === undefined) return false;
    str = str.replace(new RegExp('&nbsp;', 'g'), ' ');
    return str.split(" ").some(function (value) {
        return value === nick;
    });
}

function clickOnNickListener(ev) {
    if (!findNick(textArea.innerHTML, ev.target.innerHTML)) {
        textArea.innerHTML = textArea.innerHTML + " " + ev.target.innerHTML + "&nbsp;";
        focusEndOfTextArea();
    }
}

function focusEndOfTextArea() {
    textArea.focus();
    let br = $(textArea).find('br').remove();
    if (typeof window.getSelection !== undefined && typeof document.createRange !== undefined) {
        let range = document.createRange();
        range.selectNodeContents(textArea);
        range.collapse(false);
        let sel = window.getSelection();
        sel.removeAllRanges();
        sel.addRange(range);
    } else if (typeof document.body.createTextRange !== undefined) {
        let textRange = document.body.createTextRange();
        textRange.moveToElementText(textArea);
        textRange.collapse(false);
        textRange.select();
    }
    if (br !== undefined) {
        textArea.append(document.createElement('br'));
    }
}

function printSocketOpenMessage() {
    printGoodMessage('socketOpened', connectMessage);
}

function printSocketCloseMessage() {
    printErrMessage('socketClosed', disconnectMessage);
}

function printSystemMessage(clazz, message, isError) {
    let bodyChatMain = document.getElementById('bodyChatMain');

    if (bodyChatMain.lastElementChild === null ||
        !(bodyChatMain.lastElementChild.classList.contains(clazz) && (clazz === 'socketOpened' || clazz === 'socketClosed'))
    ) {
        let div = document.createElement('div');
        let date = new Date();
        let hours = date.getHours();
        let minutes = date.getMinutes();
        let seconds = date.getSeconds();
        if (hours < 10) hours = "0" + hours;
        if (minutes < 10) minutes = "0" + minutes;
        if (seconds < 10) seconds = "0" + seconds;
        if (isError === "true") {
            div.className = "errMessage " + clazz;
            message = message.replace("ERR -> ", "");
        } else if (isError === "false") {
            div.className = "goodMessage " + clazz;
        } else {
            div.className = "systemMessage " + clazz;
        }
        div.innerHTML = hours + ":" + minutes + ":" + seconds + " " + message;
        div.style.fontSize = fontSize;
        bodyChatMain.appendChild(div);
    }
}

function printPremiumExpiredMessage(message) {
    message = JSON.parse(message);
    let bodyChatMain = document.getElementById('bodyChatMain');
    let div = document.createElement('div');
    div.className = "systemMessage premiumExpiredIn";
    let day = message.day;
    let hour = message.hour % 24;
    let min = message.min % 60;
    if (day !== 0) {
        message = day + premiumKeyWordDays;
    } else {
        message = hour + premiumKeyWordHours + " " + min + premiumKeyWordMinutes;
    }
    div.innerHTML = premiumExpiredMessage + " " + message;
    div.style.fontSize = fontSize;
    bodyChatMain.appendChild(div);
}

function printCommandMessage(clazz, message, messageId) {
    if (messageId === undefined) {
        printCommandMessagePart2(clazz, message)
    } else {
        let interval = setInterval(function () {
            let el = document.getElementById('message' + messageId);
            if (el !== null) {
                printCommandMessagePart2(clazz, message);
                clearInterval(interval);
            }
        }, 100);
    }
}


function printCommandMessagePart2(clazz, message) {
    let bodyChatMain = document.getElementById('bodyChatMain');
    let div = document.createElement('div');
    div.className = "commandMessage " + clazz;

    div.innerHTML = message;
    div.style.fontSize = fontSize;
    bodyChatMain.appendChild(div);
}

function printErrMessage(clazz, message) {
    printSystemMessage(clazz, message, "true");
}

function printGoodMessage(clazz, message) {
    printSystemMessage(clazz, message, "false");
}

function printSpamInfoMessage(clazz, message) {
    let bodyChatMain = document.getElementById('bodyChatMain');
    let div = document.createElement('div');
    div.innerHTML = message.text;
    div.className = "systemMessage " + clazz;
    div.style.fontSize = fontSize;
    bodyChatMain.appendChild(div);
}

function printClearAllMessage(clazz, nick) {
    let bodyChatMain = document.getElementById('bodyChatMain');
    let div = document.createElement('div');
    div.innerHTML = chatClearedBy + " " + nick;
    div.className = "systemMessage " + clazz;
    div.style.fontSize = fontSize;
    bodyChatMain.innerHTML = '';
    bodyChatMain.appendChild(div);
}


let timeoutOp;

function printSpamMessage(clazz, message) {
    if (document.getElementById('bodyChatBottomInputTimeout') !== null) {
        try {
            clearInterval(timeoutOp);
        } catch (e) {
        }
        try {
            document.getElementById('bodyChatBottomInputTimeout').remove();
        } catch (e) {
        }

    }

    let bodyChatMain = document.getElementById('bodyChatMain');
    let div = document.createElement('div');
    let date = new Date();
    let hours = date.getHours();
    let minutes = date.getMinutes();
    let seconds = date.getSeconds();
    if (hours < 10) hours = "0" + hours;
    if (minutes < 10) minutes = "0" + minutes;
    if (seconds < 10) seconds = "0" + seconds;
    div.innerHTML = hours + ":" + minutes + ":" + seconds + " " + message;
    div.className = "systemMessage " + clazz;
    bodyChatMain.appendChild(div);

    let el = document.getElementById('bodyChatBottomInput');
    let divForChatHold = document.createElement('div');
    divForChatHold.id = 'bodyChatBottomInputTimeout';

    let time = div.getElementsByTagName('span')[0];
    divForChatHold.innerHTML = "<div>" + youMutedFromThisChannelFor + "&nbsp;" + "<span class='spanTime2'>" + time.innerText + "</span>" + "&nbsp;" + youTimeoutTimeUnit + "</div>";
    el.appendChild(divForChatHold);
    el.style.fontSize = fontSize;
    document.getElementById('textareaID').setAttribute('contenteditable', 'false');

    timeoutOp = setInterval(function () {
        let el = divForChatHold.getElementsByTagName('div')[0].getElementsByTagName('span')[0];
        let number = parseInt(el.innerHTML);
        if (number <= 0) {
            try {
                document.getElementById('bodyChatBottomInputTimeout').remove();
            } catch (e) {
            }
            clearInterval(timeoutOp);
            document.getElementById('textareaID').setAttribute('contenteditable', 'true');
        } else {
            el.innerText = number - 1;
        }
    }, 1000);
}

function printLvlUpMessage(message) {
    let bodyChatMain = document.getElementById('bodyChatMain');
    let div = document.createElement('div');
    div.innerHTML = message;
    div.className = "systemMessage";
    let parse = JSON.parse(message);
    div.innerHTML = parse.nickName + " " + rankUpJS_THYMELEAF + " " + parse.level + " " + levelJS_THYMELEAF;
    div.style.fontSize = fontSize;
    bodyChatMain.appendChild(div);
}

function printDonationMessage(message) {
    if (document.getElementById('donationMessage' + message.id) !== null) {
        return;
    }
    let json = JSON.parse(message.info);
    let name = json.nick === "null" ? nickName : json.nick;
    let sum = json.sum;
    let currency = json.currency;
    let text = json.text;
    let messageId = message.id;

    message = name + " " + supportedForSum + " " + sum + " " + currency + ". <br>" + text;

    let bodyChatMain = document.getElementById('bodyChatMain');

    let div = document.createElement('div');
    div.className = "donationMessage";
    div.id = 'donationMessage' + messageId;

    let imgEl = document.createElement('img');
    imgEl.setAttribute('class', 'donationMessageBag');
    imgEl.setAttribute('src', '/img/money-bag.png');

    let messageEl = document.createElement('div');
    messageEl.className = 'textMessage';

    // messageEl.style.color = 'black';
    messageEl.innerHTML = message;
    messageEl.style.display = "block";

    div.appendChild(imgEl);
    div.appendChild(messageEl);
    div.style.fontSize = fontSize;

    bodyChatMain.appendChild(div);
}

function sendMessageAndClearTextArea(ev) {
    if (connected && textArea.innerHTML.length > 0) {
        if (textArea.innerHTML.indexOf("/slot") === 0) {
            createSlotHtml();
            textArea.innerHTML = "";
        } else if (textArea.innerHTML.indexOf("/closeSlot") === 0) {
            closeSlotHtml();
            textArea.innerHTML = "";
        } else {
            sendMessage(MESSAGE, textArea.innerHTML);
            textArea.innerHTML = "";
        }
    }
}

function toggleSmiles(ev) {
    let target = document.getElementById('bodyChatSmilesPopUpBlock');
    if (target.style.display === "block") {
        target.style.display = "none";
        document.getElementsByTagName('body')[0].removeEventListener('click', closeAll);
        document.getElementsByTagName('body')[0].removeEventListener('click', bodyListenChat);
    } else {
        closeAll();
        document.getElementsByTagName('body')[0].addEventListener("click", bodyListenChat);
        target.style.display = "block";
    }
}

function toggleOptions(ev) {
    let target = document.getElementById('bodyChatOptionsPopUpBlock');
    if (target.style.display === "block") {
        target.style.display = "none";
        document.getElementsByTagName('body')[0].removeEventListener('click', closeAll);
        document.getElementsByTagName('body')[0].removeEventListener('click', bodyListenChat);
    } else {
        closeAll();
        document.getElementsByTagName('body')[0].addEventListener("click", bodyListenChat);
        target.style.display = "block";
    }
}

function toggleDonations(ev) {
    let target = document.getElementById('bodyChatDonationPopUpBlock');
    if (target.style.display === "block") {
        target.style.display = "none";
        document.getElementsByTagName('body')[0].removeEventListener('click', closeAll);
        document.getElementsByTagName('body')[0].removeEventListener('click', bodyListenChat);
    } else {
        closeAll();
        document.getElementsByTagName('body')[0].addEventListener("click", bodyListenChat);
        target.style.display = "block";
    }
}

let bodyListenChat = function bodyListenChat(ev) {
    let target1 = document.getElementById('bodyChatDonationPopUpBlock');
    let target2 = document.getElementById('bodyChatOptionsPopUpBlock');
    let target3 = document.getElementById('bodyChatSmilesPopUpBlock');

    let target11 = document.getElementById('streamerDonate');
    let target22 = document.getElementById('bodyChatInputOptions');
    let target33 = document.getElementById('bodyChatInputSmiles');
    if (!target1.contains(ev.target) && !target2.contains(ev.target) && !target3.contains(ev.target)
        && !target11.contains(ev.target) && !target22.contains(ev.target) && !target33.contains(ev.target)
    ) {
        closeAll();
        document.getElementsByTagName('body')[0].removeEventListener('click', bodyListenChat);
    }
};

function closeAll() {
    let target1 = document.getElementById('bodyChatDonationPopUpBlock');
    let target2 = document.getElementById('bodyChatOptionsPopUpBlock');
    let target3 = document.getElementById('bodyChatSmilesPopUpBlock');
    target1.style.display = "none";
    target2.style.display = "none";
    target3.style.display = "none";
}


function addSmileFromWebsiteListener(el) {
    let smiles;
    if (el === undefined) {
        smiles = document.getElementsByClassName('smileFromWebsite');
    } else {
        smiles = [];
        smiles.push(el);
    }
    for (let i = 0; i < smiles.length; i++) {
        smiles[i].addEventListener("click", function (ev) {
            let smile = ev.target;
            if (!smile.classList.contains('savedSmileFromWebsite')) {
                smile = smile.getAttribute('src').split('/');
                smile = smile[smile.length - 1].split('.')[0];
            } else {
                smile = smile.getAttribute('alt');
            }
            textArea.innerHTML = textArea.innerHTML + " " + smile + " ";
            ev.preventDefault();
            ev.stopPropagation();
            focusEndOfTextArea();
        });
    }
}


function restoreScroll() {
    scrollEnable = true;
    newMessage = false;
    scrollChat.scroll({y: "100%"});
    offsetChat = scrollChat.scroll().position.y;
    document.getElementById('bodyChatScrollEnableScroll').style.display = "none";
    document.getElementById('bodyChatScrollNewMessages').style.display = "none";
}

let offsetChat = 0;
let scrollEnable = true;
let newMessage = false;
document.addEventListener('DOMContentLoaded', function () {
    connect();

    let smiles = document.getElementsByClassName('smile');
    for (let i = 0; i < smiles.length; i++) {
        smiles[i].style.height = smileSize;
    }

    scrollChat = OverlayScrollbars(document.getElementById("bodyChatWrap"), {className: "os-theme-light"});
    scrollChat.options({
        scrollbars: {
            visibility: chatScrollVisibility,
            autoHide: "move",
            autoHideDelay: 500
        },
        callbacks: {
            onScrollStart: function (eventArgs) {
                offsetChat = scrollChat.scroll().position.y;
            },
            onScrollStop: function (eventArgs) {
                let y = scrollChat.scroll().position.y;
                let max = scrollChat.scroll().max.y;
                if (y < max) {
                    scrollEnable = false;
                    if (newMessage) {
                        document.getElementById('bodyChatScrollEnableScroll').style.display = "none";
                        document.getElementById('bodyChatScrollNewMessages').style.display = "block";
                    } else {
                        document.getElementById('bodyChatScrollEnableScroll').style.display = "block";
                        document.getElementById('bodyChatScrollNewMessages').style.display = "none";
                    }
                } else {
                    scrollEnable = true;
                    try {
                        document.getElementById('bodyChatScrollEnableScroll').style.display = "none";
                        document.getElementById('bodyChatScrollNewMessages').style.display = "none";
                    } catch (e) {
                        console.log(e);
                    }
                }
            },
            onContentSizeChanged: function (eventArgs) {
                if (scrollEnable) {
                    newMessage = false;
                    scrollChat.scroll({y: "100%"});
                } else {
                    document.getElementById('bodyChatScrollEnableScroll').style.display = "none";
                    document.getElementById('bodyChatScrollNewMessages').style.display = "block";
                }
            },

            // onInitialized: function (eventArgs) {
            //     console.log('onInitialized');
            // },
            // onInitializationWithdrawn: function (eventArgs) {
            //     console.log('onInitializationWithdrawn');
            // },
            // onDestroyed: function (eventArgs) {
            //     console.log('onDestroyed');
            // },
            //
            // onOverflowChanged: function (eventArgs) {
            //     console.log('onOverflowChanged');
            // },
            // onOverflowAmountChanged: function (eventArgs) {
            //     console.log('onOverflowAmountChanged');
            // },
            // onDirectionChanged: function (eventArgs) {
            //     console.log('onDirectionChanged');
            // },
            // onHostSizeChanged: function (eventArgs) {
            //     console.log('onHostSizeChanged');
            // },
            // onUpdated: function (eventArgs) {
            //     console.log('onUpdated');
            // }
        }
    });

    scrollSmiles1 = OverlayScrollbars(document.getElementById("bodyChatSmilesList1"), {className: "os-theme-light"});
    scrollSmiles2 = OverlayScrollbars(document.getElementById("bodyChatSmilesList2"), {className: "os-theme-light"});
    scrollOptions = OverlayScrollbars(document.getElementById("bodyChatOptionsList"), {className: "os-theme-light"});
    scrollDonation = OverlayScrollbars(document.getElementsByClassName("wrapPay")[0], {className: "os-theme-light"});
    scrollInput = OverlayScrollbars(document.getElementById("textareaIDWrap"), {className: "os-theme-light"});

    //chat-popout
    try {
        document.getElementById("bodyChatInputSmiles").addEventListener("click", toggleSmiles);
        document.getElementById("bodyChatInputOptions").addEventListener("click", toggleOptions);
        document.getElementById("streamerDonate").addEventListener("click", toggleDonations);

        document.getElementById("bodyChatInputSend").addEventListener("click", sendMessageAndClearTextArea);

        textArea = document.getElementById("textareaID");
        textArea.addEventListener("keydown", function (ev) {
            if ((ev.code === "Enter" || ev.keyCode === 13)) {
                ev.preventDefault();
                sendMessageAndClearTextArea();
            }
        });

        textArea.addEventListener("input", function (ev) {
            setTimeout(function () {
                replaceTags();
            }, 10);
        });

        document.getElementById('streamerName').addEventListener("click", function (ev) {
            clickOnNickListener(ev);
        });

        addSmileFromWebsiteListener();

        /**
         * MODERATOR BAR
         */
        let ctrl = false;
        let moderatorHelpBar = [];
        $(document).mousemove(function (e) {
            if (meModerator) {
                if (ctrl) {
                    h = document.getElementById("bodyChatMain");
                    d = h.querySelectorAll('.message:hover, .messageForU:hover');
                    if (d.length === 1) {
                        if (moderatorHelpBar[0] !== undefined)
                            moderatorHelpBar[0].style = '';
                        c = d[0].getElementsByClassName('moderatorHelpBar')[0];
                        c.style.display = 'flex';
                        moderatorHelpBar[0] = c;
                    } else {
                        if (moderatorHelpBar[0] !== undefined) {
                            moderatorHelpBar[0].style = '';
                            moderatorHelpBar[0] = undefined;
                        }
                    }
                } else {
                    if (moderatorHelpBar[0] !== undefined) {
                        moderatorHelpBar[0].style = '';
                        moderatorHelpBar[0] = undefined;
                    }
                }
            }
        });

        setInterval(function (e) {
            if (ctrl === false && moderatorHelpBar[0] !== undefined) {
                $(document).trigger("mousemove");
            }
        }, 100);

        $(document).keydown(function (e) {
            if (e.ctrlKey === true) {
                ctrl = true;
                $(document).trigger("mousemove");
            }
        });

        $(document).keyup(function (e) {
            if (e.ctrlKey === false) {
                ctrl = false;
            }
        });
    } catch (e) {

    }

    startCloseDailyInfo();
});

let time = 5000;
let tempTime = 0;
let timeoutTime = 25;
let dailyScroll;

function startCloseDailyInfo() {
    dailyScroll = setInterval(function (e) {
        tempTime += timeoutTime;
        let number = tempTime / time;
        if (number > 1) {
            try {
                bodyChatTopClose();
                clearInterval(dailyScroll);
            } catch (e) {
                clearInterval(dailyScroll);
            }
        } else {
            try {
                document.getElementById('bodyChatTopTextBefore').style.width = number * 100 + "%";
            } catch (e) {
                bodyChatTopClose();
                clearInterval(dailyScroll);
            }
        }
    }, timeoutTime);
}

