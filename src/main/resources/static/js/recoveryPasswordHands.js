function doRecovery() {
    let url = "/profile/recover/";
    let elementById = document.getElementById("regForm");
    let email = elementById.getElementsByTagName('input')[0];
    let code = elementById.getElementsByTagName('input')[1];
    url = url + email.value + "/" + code.value;
    window.location = url;
}

