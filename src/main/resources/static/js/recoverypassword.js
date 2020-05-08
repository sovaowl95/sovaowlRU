function doRecovery() {
    let form = document.getElementById('regForm');
    let email = form.getElementsByTagName('input')[0].value;
    let request = new XMLHttpRequest();
    request.open("POST", "/recoverypassword", true);
    request.addEventListener("load", function () {
        if (request.status === 200) {
            document.getElementById('email_send_success').style.display = "block";
            document.getElementById('buttonRecovery').style.border = "1px solid lightgreen";
            document.getElementById('buttonRecovery').style.borderLeft = "0";
            document.getElementById('buttonRecovery').style.borderRight = "0";
            document.getElementById('email_send_wrong').style.display = "none";
            let elementById = document.getElementById('buttonRecovery');
            elementById.setAttribute('onclick', '');

            document.getElementsByClassName('button')[0].style.display = "none";
            document.getElementsByClassName('button')[1].style.display = "block";
        } else {
            document.getElementById('email_send_success').style.display = "none";
            document.getElementById('buttonRecovery').style.border = "1px solid red";
            document.getElementById('buttonRecovery').style.borderLeft = "0";
            document.getElementById('buttonRecovery').style.borderRight = "0";
            document.getElementById('email_send_wrong').style.display = "block";
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    let json = {'email': email};
    let myJson = JSON.stringify(json);
    request.send(myJson);
}