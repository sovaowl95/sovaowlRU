document.addEventListener('DOMContentLoaded', function (evt) {
   document.getElementById("1").style.display='flex';
    document.getElementsByClassName('row')[0].getElementsByTagName('a')[0].style.borderBottom="3px solid white";
});

function show(num) {
    arr = document.getElementsByClassName('profileHolderBodyLine');
    row = document.getElementsByClassName('row')[0].getElementsByTagName('a');
    for (var i = 0; i < arr.length; i++) {
        if(i+1 === num){
            arr[i].style.display='flex';
            row[i].style.borderBottom='3px solid white';
        }else{
            arr[i].style.display='none';
            row[i].style.border='none';
        }
    }
}