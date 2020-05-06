function solveTime(message) {
    let divTime = document.createElement('div');
    if (message.time !== undefined && message.time !== null && message.time.date !== undefined) {
        let year = message.time.date.year;
        let month = message.time.date.month < 10 ? (0 + "" + message.time.date.month) : (message.time.date.month);
        let day = message.time.date.day < 10 ? (0 + "" + message.time.date.day) : (message.time.date.day);

        let hour = message.time.time.hour < 10 ? (0 + "" + message.time.time.hour) : message.time.time.hour;
        let minute = message.time.time.minute < 10 ? (0 + "" + message.time.time.minute) : message.time.time.minute;
        let sec = message.time.time.second < 10 ? (0 + "" + message.time.time.second) : message.time.time.second;
        let nano = message.time.time.nano;

        message.time = year + '-' + month + '-' + day + 'T' + hour + ':' + minute + ':' + sec + '.' + nano;
    }
    let date = new Date(message.time);
    date = addHours(date);

    let hours = date.getHours();
    let minutes = date.getMinutes();
    let seconds = date.getSeconds();
    if (hours < 10) hours = "0" + hours;
    if (minutes < 10) minutes = "0" + minutes;
    if (seconds < 10) seconds = "0" + seconds;

    divTime.innerHTML = hours + ":" + minutes;
    divTime.classList.add('time');

    if (showMessageTime) {
        divTime.style.display = "inline";
    } else {
        divTime.style.display = "none";
    }

    return divTime;
}

function addHours(date) {
    let offset = new Date().getTimezoneOffset();
    //-240

    if (offset !== 0) {
        offset = (offset / 60);
        offset = -offset;
        date.setTime(date.getTime() + (offset * 60 * 60 * 1000));
    }
    return date;
}
