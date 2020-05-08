let spinDoing;

let slot1Interval;
let slot2Interval;
let slot3Interval;

let spinRes1;
let spinRes2;
let spinRes3;

function closeSlotHtml() {
    try {
        let slotHtml = document.getElementById('slotHTML');
        slotHtml.remove();
        let bodyChatMain = document.getElementById('bodyChatMain');
        bodyChatMain.appendChild(main);
    } catch (e) {
    }
}

function createSlotHtml(betValue) {
    try {
        let slotHtml = document.getElementById('slotHTML');
        slotHtml.remove();
        let bodyChatMain = document.getElementById('bodyChatMain');
        bodyChatMain.appendChild(main);
    } catch (e) {
        let main = document.createElement("main");
        main.id = "slotHTML";

        let section1 = document.createElement('section');
        let section2 = document.createElement('section');
        let section3 = document.createElement('section');

        section1.id = "status";
        section2.id = "Slots";
        section3.id = "Gira";

        section3.innerText = slotSpin;

        section3.setAttribute('onclick', 'doServerSpin()');

        main.appendChild(section1);
        main.appendChild(section2);
        main.appendChild(section3);

        let div1 = document.createElement('div');
        let div2 = document.createElement('div');
        let div3 = document.createElement('div');

        let div1Inner = document.createElement('div');
        let div2Inner = document.createElement('div');
        let div3Inner = document.createElement('div');

        div1Inner.innerText = 1;
        div2Inner.innerText = 1;
        div3Inner.innerText = 1;

        div1Inner.classList.add("innerSpinNum");
        div2Inner.classList.add("innerSpinNum");
        div3Inner.classList.add("innerSpinNum");

        div1.appendChild(div1Inner);
        div2.appendChild(div2Inner);
        div3.appendChild(div3Inner);

        div1.id = "slot1";
        div2.id = "slot2";
        div3.id = "slot3";

        section2.appendChild(div1);
        section2.appendChild(div2);
        section2.appendChild(div3);

        let bodyChatMain = document.getElementById('bodyChatMain');
        bodyChatMain.appendChild(main);

        let innerDivBet = document.createElement('input');
        innerDivBet.type = "number";

        if (betValue !== undefined) {
            innerDivBet.value = betValue;
        } else {
            innerDivBet.value = "100";
        }
        innerDivBet.style.marginLeft = "30px";
        innerDivBet.style.fontFamily = "'Sancreek', sans-serif";
        section1.appendChild(innerDivBet);

        let closeMark = document.createElement('div');
        closeMark.innerHTML = '✖';
        closeMark.style.display = "inline-block";
        closeMark.style.color = "red";
        closeMark.style.fontSize = "xx-large";
        closeMark.style.verticalAlign = "bottom";
        closeMark.style.marginLeft = "5px";
        closeMark.setAttribute('onclick', 'closeSlotHtml()');
        section1.appendChild(closeMark);
    }
}

function starSpinning() {
    let gira = document.getElementById('Gira');
    if (gira == null) {
        createSlotHtml();
        gira = document.getElementById('Gira');
    }
    gira.innerText = slotSpinning;
    gira.setAttribute('onclick', '');

    if (spinDoing) {
        return null;
    }
    spinDoing = true;

    clearInterval(slot1Interval);
    clearInterval(slot2Interval);
    clearInterval(slot3Interval);
    spinRes1 = -1;
    spinRes2 = -1;
    spinRes3 = -1;

    slot1Interval = setInterval(function () {
        spin("slot1Interval", "slot1", "spinRes1");
    }, randomInt(25, 75));

    slot2Interval = setInterval(function () {
        spin("slot2Interval", "slot2", "spinRes2");
    }, randomInt(25, 75));

    slot3Interval = setInterval(function () {
        spin("slot3Interval", "slot3", "spinRes3");
    }, randomInt(25, 75));
}


function spin(slotInterval, elementId, spinRes) {
    let slotTile = document.getElementById(elementId);
    let el = slotTile.getElementsByTagName('div')[0];
    let num = parseInt(el.innerText);
    if (getSpinRes(spinRes) === -1) {
        incSlot(num, el);
    } else {
        if (num === getSpinRes(spinRes)) {
            if (slotInterval.indexOf("slot3Interval") === 0) {
                let gira = document.getElementById('Gira');
                gira.innerText = slotSpin;
                gira.setAttribute('onclick', 'doServerSpin()');
            }
            clearSlotInterval(slotInterval);
            return null;
        } else {
            incSlot(num, el);
        }
    }
}

function incSlot(num, el) {
    if (num === 7) {
        el.innerText = 1;
    } else {
        el.innerText = num + 1;
    }
}

function clearSlotInterval(str) {
    if (str === "slot1Interval") {
        clearInterval(slot1Interval);
    }
    if (str === "slot2Interval") {
        clearInterval(slot2Interval);
    }
    if (str === "slot3Interval") {
        clearInterval(slot3Interval);
    }
}

function getSpinRes(str) {
    if (str === "spinRes1") {
        return spinRes1;
    }
    if (str === "spinRes2") {
        return spinRes2;
    }
    if (str === "spinRes3") {
        return spinRes3;
    }
}

function doServerSpin() {
    let val = document.getElementById('status').firstChild.value;
    sendMessage("message", "!slot " + val);
}

function randomInt(min, max) {
    return Math.floor((Math.random() * (max - min + 1)) + min);
}

function printSlotMessage(clazz, message, messageId) {
    if (messageId === undefined || messageId === 0) {
        printSlotMessagePart2(clazz, message)
    } else {
        let interval = setInterval(function () {
            let el = document.getElementById('message' + messageId);
            if (el !== null) {
                printSlotMessagePart2(clazz, message);
                clearInterval(interval);
            }
        }, 100);
    }
}

function printSlotMessagePart2(clazz, message) {
    let jObj = JSON.parse(message);

    setTimeout(function () {
        spinRes1 = parseInt(jObj.el1);
    }, 1000);
    setTimeout(function () {
        spinRes2 = parseInt(jObj.el2);
    }, 2000);
    setTimeout(function () {
        spinRes3 = parseInt(jObj.el3);
        spinDoing = false;
        let type = jObj.type;
        let nickName = jObj.nickname;
        let coins = jObj.coins;

        let bodyChatMain = document.getElementById('bodyChatMain');
        let div = document.createElement('div');
        div.className = "commandMessage " + clazz;

        let premImg = "";
        if (jObj.premium === true) {
            premImg = getImgPremium();
        }

        let bet = "";
        let typeSave = type;
        let additionalReward = "";

        let typeClass;
        if (type === "win2") {
            type = slotWin;
            typeClass = "goodMessage";
            bet = slotBet + " " + jObj.bet + " " + coinsKeyword;
        } else if (type === "win10") {
            type = slotWin;
            typeClass = "goodMessage";
            bet = slotBet + " " + jObj.bet + " " + coinsKeyword;
        } else if (type === "ace") {
            type = slotACE;
            typeClass = "goodMessage";
            bet = slotBet + " " + jObj.bet + " " + coinsKeyword;

            let smiles = jObj.smiles;
            let styles = jObj.styles;

            for (let i = 0; i < smiles.length; i++) {
                smiles[i] = JSON.parse(smiles[i]);
                let duplicate = smiles[i].duplicate;
                if (duplicate !== true) duplicate = false;
                let rewardName = caravanItemNameSmile +
                    "<img src='/smiles/" + smiles[i].link + "' class='smile' alt='smile'>";
                if (duplicate) rewardName = rewardName + " (" + caravanItemNameDuplicate + ")";

                additionalReward = additionalReward + " " + rewardName;
                coins = parseInt(coins) + parseInt(smiles[i].price);
            }

            for (let i = 0; i < styles.length; i++) {
                styles[i] = JSON.parse(styles[i]);
                let duplicate = styles[i].duplicate;
                if (duplicate !== true) duplicate = false;
                let rewardName = "<span class='" + styles[i].name + "'>"
                    + styles[i].name + " " + caravanItemNameStyle
                    + "</span>";
                if (duplicate) rewardName = rewardName + " (" + caravanItemNameDuplicate + ")";

                additionalReward = additionalReward + " " + rewardName;
                coins = parseInt(coins) + parseInt(styles[i].price);
            }
        } else if (type === "lose") {
            type = slotLose;
            typeClass = "errMessage";
        }

        type = "<span class='" + typeClass + "' " +
            "style=\"font-family:'Sancreek'\"" +
            "'>" + type + "</span>";
        coins = "<span class='" + typeClass + "' " +
            "style=\"font-family:'Sancreek'\"" +
            ">" + coins + "</span>";

        let innerHTML;
        innerHTML = premImg + nickName + " " + slotStartSlotAndGet + " " + bet + " ";
        innerHTML = innerHTML + type + " " + getImgPrice() + coins + " ";
        if (typeSave === "ace") {
            innerHTML = innerHTML + additionalReward + " ";
        }
        innerHTML = innerHTML + "(" + "<span style='font-family:\"Sancreek\"; color: white'>" + jObj.el1 + " " + jObj.el2 + " " + jObj.el3 + " " + "</span>" + ")";
        div.innerHTML = innerHTML;
        div.style.fontSize = fontSize;
        div.style.fontFamily = "Sancreek";
        bodyChatMain.appendChild(div);
    }, 3000);
}