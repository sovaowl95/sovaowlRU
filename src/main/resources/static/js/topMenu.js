let topProfileShow = false;

document.addEventListener('DOMContentLoaded', function () {
    try {
        document.getElementById("profileDropDownHeader").addEventListener("click", toggleProfileTop);
    } catch (e) {

    }
});

function toggleProfileTop(ev) {
    ev.stopPropagation();
    let target = document.getElementById('profileDropDown');
    if (topProfileShow === false) {
        topProfileShow = true;
        target.style.display = "flex";
        document.getElementsByTagName("body")[0].addEventListener("click", bodyListenHeader);
    } else {
        topProfileShow = false;
        target.style.display = "none";
        document.getElementsByTagName("body")[0].removeEventListener("click", bodyListenHeader);
    }
}

function bodyListenHeader(ev) {
    var elem = document.getElementById('profileDropDown');
    if (topProfileShow && !elem.contains(ev.target)) {
        toggleProfileTop(ev);
        document.getElementsByTagName("body")[0].removeEventListener('click', this);
    }
}

function doLogout() {
    let req = new XMLHttpRequest();
    req.open("POST", "/logout", true);
    req.addEventListener("load", function (ev) {
        if (req.status === 200) {
            window.location.replace("/");
        } else {
            console.log("ELSE");
            //todo:
            //document.getElementsByClassName('button')[0].style.border = ".5px solid red";
        }
    });
    let json = {'session': getCookie('session')};
    let myJson = JSON.stringify(json);
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    req.setRequestHeader("X-CSRF-Token", csrfToken);
    req.send(myJson);
}

function getCookie(name) {
    let matches = document.cookie.match(new RegExp(
        "(?:^|; )" + name.replace(/([.$?*|{}()\[\]\\\/+^])/g, '\\$1') + "=([^;]*)"
    ));
    return matches ? decodeURIComponent(matches[1]) : undefined;
}

document.addEventListener('DOMContentLoaded', function () {
    startCloseAdminInfo();
});


let timeA = 5000;
let tempTimeA = 0;
let timeoutTimeA = 25;
let dailyScrollA;

function startCloseAdminInfo() {
    dailyScrollA = setInterval(function (e) {
        tempTimeA += timeoutTimeA;
        let number = tempTimeA / timeA;
        if (number > 1) {
            try {
                bodyChatTopCloseA();
                clearInterval(dailyScrollA);
            } catch (e) {
                clearInterval(dailyScrollA);
            }
        } else {
            try {
                document.getElementById('bodyChatTopTextBeforeA').style.width = number * 100 + "%";
            } catch (e) {
                bodyChatTopCloseA();
                clearInterval(dailyScrollA);
            }
        }
    }, timeoutTimeA);
}

function bodyChatTopCloseA() {
    try {
        document.getElementById('bodyChatTopTextBeforeA').remove();
    } catch (ignore) {
    }
    try {
        document.getElementById('topHeaderTimeline').remove();
        return true;
    } catch (e) {
        return false;
    }
}

let menuOpened = false;

function toggleMenu() {
    if (menuOpened) {
        closeMenu();
    } else {
        openMenu();
    }
}

function openMenu() {
    document.getElementById('header_1').style.display = 'flex';
    menuOpened = true;
}

function closeMenu() {
    document.getElementById('header_1').style.display = 'none';
    menuOpened = false;
}

let wasWidth = -1;
window.onresize = function (event) {
    let width = window.innerWidth;
    let widthCss = 1200;

    if (wasWidth === -1) {
        wasWidth = width;
    }

    if (width >= wasWidth) {
        if (width > widthCss) {
            openMenu();
        }
    } else {
        if (width <= widthCss && wasWidth >= widthCss) {
            closeMenu();
        }
    }
    wasWidth = width;
};