function show(num) {
    arr = document.getElementsByClassName('content');
    row = document.getElementsByClassName('row')[0].getElementsByTagName('a');
    for (let i = 0; i < arr.length; i++) {
        if (i + 1 === num) {
            arr[i].style.display = 'block';
            row[i].style.borderBottom = '3px solid white';
        } else {
            arr[i].style.display = 'none';
            row[i].style.border = 'none';
        }
    }
}