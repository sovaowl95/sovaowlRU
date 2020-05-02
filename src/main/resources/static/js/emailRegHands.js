function doVerify() {
    let form = document.getElementById("regForm");
    let email = form.getElementsByTagName('input')[0];
    let code = form.getElementsByTagName('input')[1];
    if (email === undefined || email === null || email.value.length === 0 || code === undefined || code === null || code.value.length === 0) {
        return;
    }
    let request = new XMLHttpRequest();
    let url = "/profile/email/verificationByHands/";
    url = url + email.value + "/" + code.value;
    request.open("POST", url, true);
    request.addEventListener("load", function () {
        if (request.status === 200) {
            window.location = "/profile/email/verification/success";
        } else {
            document.getElementsByClassName('button')[0].style.borderBottom = "1px solid red";
            document.getElementsByClassName('button')[0].style.borderTop = "1px solid red";
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    request.send();
}

function sendEmail() {
    let form = document.getElementById("regForm");
    let email = form.getElementsByTagName('input')[0];
    if (email === undefined || email === null || email.value.length === 0) {
        email.focus();
        return;
    }
    let request = new XMLHttpRequest();
    let url = "/profile/sendEmailAgain/";
    url = url + email.value;
    request.open("POST", url, true);
    request.addEventListener("load", function () {
        if (request.status === 200) {
            document.getElementsByClassName('button')[1].style.borderBottom = "1px solid lightgreen";
            document.getElementsByClassName('button')[1].style.borderTop = "1px solid lightgreen";
            //document.getElementsByClassName('button')[1].setAttribute("onclick", "");
        } else {
            document.getElementsByClassName('button')[1].style.borderBottom = "1px solid red";
            document.getElementsByClassName('button')[1].style.borderTop = "1px solid red";
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    request.send();
}

function changeEmail() {
    let form = document.getElementById("regForm");
    let email = form.getElementsByTagName('input')[0];
    if (email === undefined || email === null || email.value.length === 0) {
        email.focus();
        return;
    }
    let request = new XMLHttpRequest();
    let url = "/profile/changeEmail/";
    url = url + email.value;
    request.open("POST", url, true);
    request.addEventListener("load", function () {
        if (request.status === 200) {
            document.getElementsByClassName('button')[2].style.borderBottom = "1px solid lightgreen";
            document.getElementsByClassName('button')[2].style.borderTop = "1px solid lightgreen";
            //document.getElementsByClassName('button')[2].setAttribute("onclick", "");
        } else {
            document.getElementsByClassName('button')[2].style.borderBottom = "1px solid red";
            document.getElementsByClassName('button')[2].style.borderTop = "1px solid red";
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    request.send();
}