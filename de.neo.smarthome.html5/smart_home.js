
var mEndpoint = '';
var mToken = '';
var mMediaCenter = '';
var mPath = '';
var mPlaylist = '';

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
		var east = document.getElementById('east');
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

function getPlaying(callback){
	if (mMediaCenter != null && mMediaCenter != '' && mEndpoint != null && mEndpoint != ''){
		var url = mEndpoint + '/mediaserver/list?token=' + mToken;
		var req = new XMLHttpRequest();  
		req.open("GET", url);
		req.addEventListener('load', function(event) {
			var handled = false;
			if (req.status >= 200 && req.status < 300) {
				var response = JSON.parse(req.responseText);
				if (response !== undefined && !response.error) {
					if (response.length > 0 && response[0] !== undefined){
						for (var i = 0; i < response.length; i++) {
							var m = response[i];
							if (m.id == mMediaCenter) {
								callback(m.current_playing);
								handled = true;
							}
						}
					}
				} else {
					// Ignore error
				}
			} else if (req.status == 404 || req.status == 500) {
				// Ignore error
			}
			if (!handled)
				callback(null);
		});	
		req.timeout = 4000; // 4 seconds timeout
		req.open("GET", url);
		req.send();  
	}
}

function refreshPlayer(){
	getPlaying(function(playing){
		var content = document.getElementById('player_content');
		var console = document.getElementById('player_console');
		var text = "<table>";
		if (playing != null){
			if (playing.artist != null)
				text = '<tr><td>' + playing.artist + '</td></tr>';
			if (playing.title != null)
				text += '<tr><td>' + playing.title + '</td></tr>';
			if (playing.artist == null && playing.title == null)
				text += '<tr><td>' + playing.file + '</td></tr>';
		} else {
			text += '<tr><td>Nothing played</td></tr>';
		}
		text += '</table>';
		content.innerHTML = text;
	});
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

function play(file){
	if (mEndpoint != null && mEndpoint != ''){
		var request = new XMLHttpRequest();
		mFile = file;
		if (mPath != '' && mPath != null)
			file = mPath + '<->' + file; 
		var url = mEndpoint + '/mediaserver/play_file?token=' + mToken + '&id=' + mMediaCenter + '&player=mplayer&file=' + file;
		request.open("GET", url);
		request.addEventListener('load', function(event) {
			if (checkResult(request)) {
				showMessage(mFile, 'Start playing file');
			}
		});
		request.send();
	}
}

function extendPls(pls, file){
	hideDialog('select_pls');
	if (mEndpoint != null && mEndpoint != '' && mMediaCenter != null && mMediaCenter != ''){
		var request = new XMLHttpRequest();
		var url = mEndpoint + '/mediaserver/playlist_extend?token=' + mToken + '&id=' + mMediaCenter + '&playlist=' + pls + '&item=' + file;
		request.open("GET", url);
		request.addEventListener('load', function(event) {
			if (checkResult(request)) {
				showMessage('Playlist extended', 'Playlist <b>' + pls + '</b> was extended.');	
			}
		});
		request.send();
	}
}

function addFileToPls(file){
	mFile = file;
	if (mPath != null && mPath != '')
		mFile = mPath + '<->' + file;
	if (mEndpoint != null && mEndpoint != '' && mMediaCenter != null && mMediaCenter != ''){
		var request = new XMLHttpRequest();
		var url = mEndpoint + '/mediaserver/playlists?token=' + mToken + '&id=' + mMediaCenter;
		request.open("GET", url);
		request.addEventListener('load', function(event) {
			if (checkResult(request)) {
				var pls = JSON.parse(request.responseText);
				var content = "";
				var root = document.getElementById('select_pls_content');
				pls.sort(function(a, b){return a.name.localeCompare(b.name);});
				for (var i = 0; i < pls.length; i++) {
					var p = pls[i];
					content += '<div onclick="extendPls(\'' + p.name + '\', \'' + mFile + '\')" class="file link">' + p.name + "</div>";
				}
				root.innerHTML = content;
			} else {
				root.innerHTML = 'No playlists';
			}
			showDialog('select_pls');
		});
		request.send();
	}
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
					content = '<div onclick="directoryClick(\'<->\')" class="file dir link"><table width="100%"><tr>';
					content += '<td><img src="img/arrow.png" height="16px" class="link"></td><td width="100%">' + mPath.replace(/<->/g, ' | ') + '</td>';
					content += '</tr></table></div>';
				}
				files.sort(function(a, b){if (a.filetype == b.filetype) { return a.name.localeCompare(b.name);} return a.filetype.localeCompare(b.filetype);});
				for (var i = 0; i < files.length; i++) {
					var f = files[i];
					if (f.filetype == "Directory") {
						content += '<div class="file dir">';
					} else {
						content += '<div class="file">';
					}
					content += '<table width="100%"><tr>';
					if (f.filetype == "Directory") {
						content += '<td width="90%" class="link" onclick="directoryClick(\'' + f.name + '\')" >' + f.name + '</td>';
					} else {
						content += '<td width="90%" class="link" onclick="fileClick(\'' + f.name + '\')" >' + f.name + '</td>';
					}
					content += '<td align="right" width="100px">';
					content += '<img src="img/play.png" height="32px"/ class="link" onclick="play(\'' + f.name + '\')">';
					content += '<img src="img/pls.png" height="32px"/ class="link" onclick="addFileToPls(\'' + f.name + '\')">';
					content += '</td></tr></table></div>';
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

function showMessage(title, msg){
	var doc_title = document.getElementById("msg_title");
	var doc_msg = document.getElementById("msg_content");
	doc_title.innerHTML = title;
	doc_msg.innerHTML = msg;
	showDialog('message');
}

function plsClick(pls){
	if (mEndpoint != null && mEndpoint != '' && mMediaCenter != null && mMediaCenter != ''){
		var request = new XMLHttpRequest();
		var url = mEndpoint + '/mediaserver/play_playlist?token=' + mToken + '&id=' + mMediaCenter + '&playlist=' + pls + '&player=mplayer';
		request.open("GET", url);
		mPlaylist = pls;
		request.addEventListener('load', function(event) {
			if (request.status >= 200 && request.status < 300) {
				var pls = JSON.parse(request.responseText);
				showMessage(mPlaylist, 'Play playlist ' + mPlaylist);
			}
		});
		request.send();
	}
}

function refreshPlaylist(){
	var root = document.getElementById('east');
	if (mEndpoint != null && mEndpoint != '' && mMediaCenter != null && mMediaCenter != ''){
		var request = new XMLHttpRequest();
		var url = mEndpoint + '/mediaserver/playlists?token=' + mToken + '&id=' + mMediaCenter;
		request.open("GET", url);
		request.addEventListener('load', function(event) {
			if (request.status >= 200 && request.status < 300) {
				var pls = JSON.parse(request.responseText);
				var content = "";
				pls.sort(function(a, b){return a.name.localeCompare(b.name);});
				for (var i = 0; i < pls.length; i++) {
					var p = pls[i];
					content += '<div onclick="plsClick(\'' + p.name + '\')" class="file link">' + p.name + "</div>";
				}
				root.innerHTML = content;
			} else {
				root.innerHTML = 'No playlists';
			}
			align();
		});
		request.send();
	} else {
		root.innerHTML = 'No playlists';
	}
}

function checkResult(request){
	if (request.status >= 200 && request.status < 300) {
		var pls = JSON.parse(request.responseText);
		if (pls != null && pls.success != null && pls.success == false){
			if (pls.error != null)
			showMessage("Error", pls.error.message);
			return false;
		}
		return true;
	} else {
		return false;
	}
}

function playerAction(action, parameter){
	if (mEndpoint != null && mEndpoint != '' && mMediaCenter != null && mMediaCenter != ''){
		var request = new XMLHttpRequest();
		var url = mEndpoint + '/mediaserver/' + action + '?token=' + mToken + '&id=' + mMediaCenter + '&player=mplayer';
		if (typeof parameter !== 'undefined') {
			url = url + '&' + parameter;
		}
		request.open("GET", url);
		request.addEventListener('load', function(event) {
			if (checkResult(request)) {
				refreshPlayer();
			}
		});
		request.send();
	}
}

function vol_up(){
	getPlaying(function(playing){
		if (playing != null){
			playerAction('volume', 'volume=' + (playing.volume + 5));
		}
	});
}

function vol_down(){
	getPlaying(function(playing){
		if (playing != null){
			playerAction('volume', 'volume=' + (playing.volume - 5));
		}
	});
}
