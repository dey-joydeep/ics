var KeyCode = Object.freeze({
	"ENTER" : 13,
	"BSP" : 8,
	"DEL" : 46
});
var ChatAppenderCaller = Object.freeze({
	"USER_SELECT" : 1,
	"SEND_BTN" : 2,
	"RECONNECT" : 3,
	"NEW_MESSAGE" : 4
});

var CommType = Object.freeze({
	"MSG" : "MSG",
	"ACK" : "ACK",
	"NOT" : "NOT",
	"ERR" : "ERR",
	"TYPE" : "TYPE"
});
var ReceiverType = Object.freeze({
	"SINGLE" : "SINGLE",
	"BROADCAST" : "BROADCAST",
	"GROUP" : "GROUP",
});
var AnswerType = Object.freeze({
	"REPLY" : "REPLY",
	"FORWARD_SINGLE" : "FORWARD_SINGLE",
	"FORWARD_MULTIPLE" : "FORWARD_MULTIPLE"
});

var UserStatus = Object.freeze({
	"ONLINE" : "ONLINE",
	"OFFLINE" : "OFFLINE"
});

var ErrorLevel = Object.freeze({
	"LOW" : "LOW",
	"HIGH" : "HIGH"
});

var AcknowledgeType = Object.freeze({
	"SENT" : "SENT",
	"DELIVERED" : "DELIVERED",
	"READ" : "READ"
});

var StatusIconText = Object.freeze({
	"SENT" : "check_circle_outline", // done
	"DELIVERED" : "check_circle", // done_all
});
