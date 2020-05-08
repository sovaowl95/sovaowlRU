let testLogin;

document.addEventListener('DOMContentLoaded', function () {
    let regForm = document.getElementById("regForm");

    regForm.getElementsByTagName('input')[0].oninput = function () {
        let login = regForm.getElementsByTagName('input')[0].value;
        if (!(login.length >= 6 && login.length <= 45 && /^[a-zA-z][a-zA-Z0-9\-_]{5,}$/.test(login))) {
            document.getElementById('reg_failed_login1').style.display = "block";
            regForm.getElementsByTagName('input')[0].style.border = ".5px solid red";
            testLogin = false;
        } else {
            document.getElementById('reg_failed_login1').style.display = "none";
            regForm.getElementsByTagName('input')[0].style.border = "none";
            testLogin = true;
        }
    };
});

function doReg() {
    let form = document.getElementById('regForm');
    let login = form.getElementsByTagName('input')[0].value;
    let gender = form.getElementsByClassName('gender');
    gender = gender[0].getElementsByTagName('input')[0].checked;

    let rules = form.getElementsByClassName('gender innerGender');
    rules = rules[0].getElementsByTagName('input')[0].checked;

    event = new Event("input");
    form.getElementsByTagName('input')[0].dispatchEvent(event);

    if (testLogin) {
        let request = new XMLHttpRequest();
        request.open("POST", link, true);
        request.addEventListener("load", function () {
            if (request.status === 200) {
                window.location.replace("/");
            } else if (request.status === 400) {
                let text = request.responseText;
                text = JSON.parse(text);
                text = text.message;
                if (text === "Bad Login" || text === "Login already in use") {
                    document.getElementById('reg_failed_login2').style.display = "block";
                    form.getElementsByTagName('input')[0].style.border = ".5px solid red";
                } else {
                    document.getElementById('reg_failed_login2').style.display = "none";
                    form.getElementsByTagName('input')[0].style.border = "none";
                }
                if (text === "Bad Gender") {
                    document.getElementById('reg_failed_gender2').style.display = "block";
                    form.getElementsByClassName('gender')[0].style.border = ".5px solid red";
                } else {
                    document.getElementById('reg_failed_gender2').style.display = "none";
                    form.getElementsByClassName('gender')[0].style.border = "none";
                }
                if (text === "You must accept rules") {
                    document.getElementById('reg_failed_rules2').style.display = "block";
                    form.getElementsByClassName('gender')[1].style.border = ".5px solid red";
                } else {
                    document.getElementById('reg_failed_rules2').style.display = "none";
                    form.getElementsByClassName('gender')[1].style.border = "none";
                }
                if (text === "Email already in use") {
                    try {
                        document.getElementById('emailInputField').style.border = ".5px solid red";
                    } catch (e) {
                        document.getElementById('reg_failed_login2').style.display = "block";
                    }
                }
                wrongDoReg(form);
            } else {
                wrongDoReg(form);
            }
        });
        let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
        request.setRequestHeader("X-CSRF-Token", csrfToken);
        let secTokenState = document.getElementsByName('secTokenState')[0].getAttribute('content');
        request.setRequestHeader("secTokenState", secTokenState);
        let json;
        if (document.getElementById('emailInputField') !== null) {
            let email = document.getElementById('emailInputField').value;
            json = {'login': login, 'gender': gender, 'rules': rules, 'email': email};
        } else {
            json = {'login': login, 'gender': gender, 'rules': rules};
        }
        let myJson = JSON.stringify(json);
        request.send(myJson);
    } else {
        event = new Event("input");
        wrongDoReg(form);
    }
}

function wrongDoReg(form) {
    form.getElementsByClassName('button')[0].style.borderTop = "1px solid red";
    form.getElementsByClassName('button')[0].style.borderBottom = "1px solid red";
}