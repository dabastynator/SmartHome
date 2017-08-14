
var mEndpoint = '';
var mToken = '';
var mMediaCenter = '';
var mPath = '';

function initialize() {
	readSetting();

	align();
	window.addEventListener('resize', align);

	loop();
	refreshControlCenter();
	refreshFiles();
	refreshPlaylist();
	
	// Setup refresh loop for 5 seconds
	setInterval(loop, 1000 * 5);

	if (mEndpoint == null || mEndpoint == '')
		showDialog('settings');
}

function readSetting(){
	mEndpoint = window.localStorage.getItem("endpoint");
	mToken = window.localStorage.getItem("token");
	mMediaCenter = window.localStorage.getItem("mediacenter");
	mPath = window.localStorage.getItem("path");
	if (mPath == null)
		mPath = '';
}

function initSettings(){
	var endpoint = document.getElementById('setting_endpoint');
	var token = document.getElementById('setting_token');
	if (mEndpoint != null)
		endpoint.value = mEndpoint;
	else
		endpoint.value = '';
	if (mToken != null)
		token.value = mToken;
	else
		token.value = '';
}

function saveSettings(){
	mEndpoint = document.getElementById('setting_endpoint').value;
	mToken = document.getElementById('setting_token').value;
	window.localStorage.setItem("endpoint", mEndpoint);
	window.localStorage.setItem("token", mToken);
	refreshControlCenter();
	refreshFiles();
	refreshPlaylist();
}

function loop() {
	refreshPlayer();
	refreshSwitches();
}

function refreshSwitches(){
	if (mEndpoint != null && mEndpoint != ''){
		var root = document.getElementById('west');
		var request = new XMLHttpRequest();
		var url = mEndpoint + '/switch/list?token=' + mToken;
		request.open("GET", url);
		request.addEventListener('load', function(event) {
			if (request.status >= 200 && request.status < 300) {
				var switches = JSON.parse(request.responseText);
				switches.sort(function(a, b){return a.name.localeCompare(b.name)});
				var content = "";
				for (var i = 0; i < switches.length; i++) {
					var s = switches[i];
					if (s.state == "ON") {
						content += '<div onclick="switchClick(\'' + s.id + '\', \'OFF\')" class="switch on">';
					} else {
						content += '<div onclick="switchClick(\'' + s.id + '\', \'ON\')" class="switch off">';
					}
					content += s.name + "</div>";
					if (i % 2 == 1)
						content += "<br/>";
				}
				root.innerHTML = content;
			} else {
				root.innerHTML = 'No switches';
			}
			align();
		});
		request.send();
	}
}

function switchClick(id, state){
	var request = new XMLHttpRequest();
	var url = mEndpoint + '/switch/set?token=' + mToken + '&id=' + id + '&state=' + state;
	request.open("GET", url);
	request.send();
	request.addEventListener('load', function(event) {
		refreshSwitches();
	});
}

function mediaClick(id){
	mMediaCenter = id;
	window.localStorage.setItem("mediacenter", mMediaCenter);
	mPath = '';
	refreshControlCenter();
	refreshFiles();
	refreshPlaylist();
}

function refreshPlayer(){
	var root = document.getElementById('south');
	if (mMediaCenter != null && mMediaCenter != '' && mEndpoint != null && mEndpoint != ''){
		root.innerHTML = 'TODO';
	} else {
		root.innerHTML = 'No player';
	}
}

function refreshControlCenter(){
	var root = document.getElementById('controlcenter');
	if (mEndpoint != null && mEndpoint != ''){
		var request = new XMLHttpRequest();
		var url = mEndpoint + '/mediaserver/list?token=' + mToken;
		request.open("GET", url);
		request.addEventListener('load', function(event) {
			if (request.status >= 200 && request.status < 300) {
				var media = JSON.parse(request.responseText);
				media.sort(function(a, b){return a.name.localeCompare(b.name)});
				var content = "";
				for (var i = 0; i < media.length; i++) {
					var m = media[i];
					if (m.id == mMediaCenter) {
						content += '<div onclick="mediaClick(\'' + m.id + '\')" class="switch on">';
					} else {
						content += '<div onclick="mediaClick(\'' + m.id + '\')" class="switch off">';
					}
					content += m.name + "</div>";
					if (i % 2 == 5)
						content += "<br/>";
				}
				root.innerHTML = content;
			} else {
				root.innerHTML = 'No mediaserver';
			}
			align();
		});
		request.send();
	} else {
		root.innerHTML = 'No mediaserver';
	}
}

function directoryClick(dir){
	if (dir == '<->'){
		if (mPath.lastIndexOf('<->') >= 1)
			mPath = mPath.substring(0, mPath.lastIndexOf('<->'));
		else
			mPath = '';
	}else if (mPath == '')
		mPath = dir;
	else
		mPath = mPath + '<->' + dir;
	refreshFiles();
}

function refreshFiles(){
	var root = document.getElementById('center');
	if (mEndpoint != null && mEndpoint != '' && mMediaCenter != null && mMediaCenter != ''){
		var request = new XMLHttpRequest();
		var url = mEndpoint + '/mediaserver/files?token=' + mToken + '&id=' + mMediaCenter + '&path=' + mPath;
		request.open("GET", url);
		request.addEventListener('load', function(event) {
			if (request.status >= 200 && request.status < 300) {
				var files = JSON.parse(request.responseText);
				var content = "";
				if (mPath != ''){
					content = '<div onclick="directoryClick(\'<->\')" class="file dir"><img src="img/arrow.png" height="16px">' + mPath.replace(/<->/g, ' | ') + '</div>';
				}
				files.sort(function(a, b){if (a.filetype == b.filetype) { return a.name.localeCompare(b.name);} return a.filetype.localeCompare(b.filetype);});
				for (var i = 0; i < files.length; i++) {
					var f = files[i];
					if (f.filetype == "Directory") {
						content += '<div onclick="directoryClick(\'' + f.name + '\')" class="file dir">';
					} else {
						content += '<div onclick="fileClick(\'' + f.name + '\')" class="file">';
					}
					content += f.name + "</div>";
				}
				root.innerHTML = content;
			} else {
				root.innerHTML = 'No files';
			}
			align();
		});
		request.send();
	} else {
		root.innerHTML = 'No files';
	}
}

function refreshPlaylist(){

}

