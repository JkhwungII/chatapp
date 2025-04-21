

const sessionEndedDialog = document.getElementById('sessionEndedDialog');
const toPairDialog = document.getElementById('toPairDialog');
const searchingPairDialog = document.getElementById("searchingPairDialog");
const leaveDialog = document.getElementById("leaveDialog");
const queueEmptyDialog = document.getElementById("queueEmptyDialog");



const showSingleDialogBtn = document.getElementById('showSingleDialog');
const pairingBtn = document.getElementById("pairingBtn");
const pairAgainBtn = document.getElementById("pairAgainBtn");
const showLeaveBtn = document.getElementById("showLeaveBtn");
const leaveBtn = document.getElementById("leaveBtn");
const cancelLeaveBtn = document.getElementById("cancelLeaveBtn");

showLeaveBtn.addEventListener('click',()=>{
    leaveDialog.showModal();
})
leaveBtn.addEventListener('click',leave)
pairAgainBtn.addEventListener('click', ()=>{
    waitforParing();
    ws.send("#PAIR");
    console.log("clicked")
});
cancelLeaveBtn.addEventListener('click',()=>{
    leaveDialog.close();
})
queueEmptyDialog.addEventListener('click', (e) => {
    if (e.target === queueEmptyDialog) {
        e.stopPropagation(); 
    }
});
pairingBtn.addEventListener("click",()=>{
    toPairDialog.close();
    waitforParing();
    ws.send("#PAIR");
    console.log("clicked")
})
toPairDialog.addEventListener('cancel', (e) => {
    e.preventDefault();
});

toPairDialog.addEventListener('click', (e) => {
    if (e.target === toPairDialog) {
        e.stopPropagation(); 
    }
});

searchingPairDialog.addEventListener('cancel', (e) => {
    e.preventDefault();
});

searchingPairDialog.addEventListener('click', (e) => {
    if (e.target === searchingPairDialog) {
        e.stopPropagation(); 
    }
});

const LEFT = "left";
const RIGHT = "right";


var flag_load_previous_messages = false;
var url = "ws://" + window.location.host + "/socket";
var ws = new WebSocket(url);

ws.addEventListener("error", (event) => {
    console.log("WebSocket error: ", event);
});
ws.onopen = function(){
    ws.send("#ASK_IF_PAIRED");
}
var chatroom = document.getElementsByClassName("msger-chat")
var text = document.getElementById("msg");
var send = document.getElementById("send")


send.onclick = function (e) {
    if (!flag_load_previous_messages)
        handleMessageEvent();
}

text.onkeydown = function (e) {
    if (e.keyCode === 13 && !flag_load_previous_messages) {
        handleMessageEvent()
    }
};

ws.onmessage = function (event) {

    var response = event.data
    console.log(event.data)
    if (response.startsWith("#")){
        switch(response){
            case "#LEAVE_COMPLETE":
                leaveDialog.close()
                showToast();
                document.getElementById("chatBox").innerHTML = "";
                toPair();
            break;
            case "#QUEUE_EMPTY":
                searchingPairDialog.close()
                showQueueEmptyDialog();
            break;
            case "#PAIRED":
                searchingPairDialog.close()
                event_msg = getEventMessage("配對成功")
                insertMsg(event_msg, chatroom[0]);
            break;
            case "#IS_PAIRED":
                flag_load_previous_messages = true;
                ws.send("#FETCH_PREVIOUS_MESSAGES");
            break;
            case "#NOT_PAIRED":
                toPair();
            break;
        }
    } else if (response.startsWith("@PREVIOUS_MESSAGES")) {
        var m = JSON.parse(event.data.split("::")[1]);
        var msg = "";
        if(m.is_from_you){
            msg = getMessage("你", LEFT,m.message,m.date);
        }else{
            msg = getMessage("陌生人", RIGHT, m.message,m.date);
        }
        insertMsg(msg, chatroom[0]);
    } else if (response.startsWith("@END_OF_MESSAGES")) {
        flag_load_previous_messages = false;
    }else {
        var m = JSON.parse(event.data)
        console.log(m)
        var msg = ""
        if(m.is_from_you){
            msg = getMessage("你", LEFT,m.message,m.date);
        }else{
            msg = getMessage("陌生人", RIGHT, m.message,m.date);
        }
        insertMsg(msg, chatroom[0]);
    }
    
};


function toPair(){
    toPairDialog.showModal()

}

function waitforParing(){
    searchingPairDialog.showModal()

}


function showQueueEmptyDialog(){
    queueEmptyDialog.showModal();
}

function handleMessageEvent() {
    if (text.value !== ""){
        ws.send(text.value);
        text.value = "";
    }
}

function getEventMessage(msg) {
    var msg = `<div class="msg-notify">${msg}</div>`
    return msg;
}

function getMessage(name, side, text, date) {
    const d = new Date(date)
    //   Simple solution for small apps
    var msg = `
    <div class="msg ${side}-msg">
    
      <div class="msg-bubble">
        <div class="msg-info">
          <div class="msg-info-name">${name}</div>
          <div class="msg-info-time">${d.getFullYear()}/${d.getMonth()+1}/${d.getDate()} ${d.getHours()}:${('0'+d.getMinutes()).slice(-2)}</div>
        </div>

        <div class="msg-text">${text}</div>
      </div>
    </div>
  `
    return msg;
}

function insertMsg(msg, domObj) {
    domObj.insertAdjacentHTML("beforeend", msg);
    domObj.scrollTop += 500;
}

function getRandomNum(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}


function showToast() {
    const toast = document.getElementById('toast');
    toast.classList.add('show');
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}
function leave(){
    ws.send("#LEAVE");
    leaveDialog.addEventListener('cancel', (e) => {
        e.preventDefault();
    });

    leaveDialog.addEventListener('click', (e) => {
        if (e.target === searchingPairDialog) {
            e.stopPropagation(); 
        }
    });
}