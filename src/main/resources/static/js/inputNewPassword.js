function inputNewPassword() {
    let password1 = document.getElementById('pass1').value;
    let password2 = document.getElementById('pass2').value;

    if (password1 !== password2) {
        let elementById = document.getElementById('reg_failed_login1');
        elementById.style.display = 'block';
        return;
    } else {
        let elementById = document.getElementById('reg_failed_login1');
        elementById.style.display = 'none';
    }

    let request = new XMLHttpRequest();
    let url = "/profile/recover/newPassword";
    request.open("POST", url, true);
    request.addEventListener("load", function () {
        if (request.status === 200) {
            document.getElementById('success').style.display = "block";
            colorInGreen();
            document.getElementsByClassName('button')[0].style.display = "none";
            document.getElementsByClassName('button')[1].style.display = "block";
        } else {
            colorInRed();
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);

    let passToken = document.getElementsByName('passToken')[0].getAttribute('content');
    let json = {'passToken': passToken, 'password': password1};
    let js = JSON.stringify(json);
    request.send(js);
}

function colorInRed() {
    let elementsByClassName = document.getElementsByClassName('button')[0];
    elementsByClassName.style.borderBottom = '1px solid red';
    elementsByClassName.style.borderTop = '1px solid red';
}

function colorInGreen() {
    let elementsByClassName = document.getElementsByClassName('button')[0];
    elementsByClassName.style.borderBottom = '1px solid lightgreen';
    elementsByClassName.style.borderTop = '1px solid lightgreen';
    elementsByClassName.setAttribute('onclick', '');
}