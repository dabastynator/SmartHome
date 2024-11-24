

class APIHandler {
	configure(endpoint, name, defaultParameter = {})
	{
		this.endpoint = endpoint;
		this.name = name;
		this.defaultParameter = JSON.parse(JSON.stringify(defaultParameter));
	}

	addDefaultParameter(key, value)
	{
		this.defaultParameter[key] = value;
	}

	call(method, callback = null, parameters = {})
	{
		var url = this.endpoint + '/' + this.name + '/' + method;
		parameters = {...this.defaultParameter, ...parameters }
		var xmlHttp = new XMLHttpRequest();
		xmlHttp.onreadystatechange = function() { 
			if (xmlHttp.readyState == 4 && xmlHttp.status == 200)
			{
				var jsonResponse = JSON.parse(xmlHttp.responseText);
				if (callback)
				{
					callback(jsonResponse);
				}				
			}				
		}
		xmlHttp.open("POST", url, true);
		xmlHttp.send(JSON.stringify(parameters));
	}
}

const Separator = "/";

var apiTrigger = new APIHandler();
var apiMediaServer = new APIHandler();
var apiSwitch = new APIHandler();
var apiAction = new APIHandler();
var apiUser = new APIHandler();
var mMediaCenter = '';
var mPath = '';
var mPlaylist = '';
var mUserList;
var mSelectedUser;
var mFiles;

var htmlFiles;
var htmlPls;
var htmlPlsContent;
var htmlSwitches;
var htmlScripts;
var htmlPlayInfo;
var htmlMediaServer;
var htmlUserList;

function initialize() {
	htmlFiles = document.getElementById('center');
	htmlPls = document.getElementById('playlists');
	htmlPlsContent = document.getElementById('playlist_content');
	htmlSwitches = document.getElementById('west');
	htmlScripts = document.getElementById('scripts_content');
	htmlPlayInfo = document.getElementById('player_content');
	htmlMediaServer = document.getElementById('mediaserver');
	htmlUserList = document.getElementById('userlist_content');

	readSetting();
	align();
	window.addEventListener('resize', align);

	var volume_input = document.getElementById('volume_input');
	volume_input.addEventListener('input', setVolume);

	refreshMediaServer();
	refreshFiles();
	refreshPlaylist();
	loop();
	
	// Setup refresh loop for 5 seconds
	setInterval(loop, 1000 * 5);

	if (apiMediaServer.endpoint == null || apiMediaServer.endpoint == '')
	{
		showDialog('settings');
	}
}

function findGetParameter(parameterName)
{
    var result = null,
        tmp = [];
    var items = location.search.substr(1).split("&");
    for (var index = 0; index < items.length; index++) {
        tmp = items[index].split("=");
        if (tmp[0] === parameterName) result = decodeURIComponent(tmp[1]);
    }
    return result;
}

function clearSettings()
{
	window.localStorage.clear();
}

function readSetting()
{
	endpoint = window.localStorage.getItem("endpoint");
	token = window.localStorage.getItem("token");
	if (endpoint == null || endpoint == '')
	{
		endpoint = findGetParameter('endpoint');
		token = findGetParameter('token');
		if (endpoint && token)
		{
			window.localStorage.setItem("endpoint", endpoint);
			window.localStorage.setItem("token", token);
		}
	}	
	mMediaCenter = window.localStorage.getItem("mediacenter");
	mPath = window.localStorage.getItem("path");	
	if (mPath == null)
		mPath = '';
	parameter = {'token' : token};
	apiTrigger.configure(endpoint, 'trigger', parameter)
	apiMediaServer.configure(endpoint, 'mediaserver', parameter)
	apiMediaServer.addDefaultParameter('player', 'mplayer');
	apiMediaServer.addDefaultParameter('id', mMediaCenter);
	apiSwitch.configure(endpoint, 'switch', parameter);
	apiAction.configure(endpoint, 'action', parameter);
	apiUser.configure(endpoint, 'user', parameter);

	document.getElementById('setting_current_user').value = '';
	setIsAdmin(false);
	apiUser.call('current', function(result)
	{
		if (checkResult(result, null, false))
		{
			setIsAdmin(result.role === 'ADMIN');
			document.getElementById('setting_current_user').value = result.name;
		}
	}, {'token': token});
}

function setIsAdmin(isAdmin)
{
	adminVisible = 'none';
	if (isAdmin)
	{
		adminVisible = 'block';
	}
	document.getElementById('btn_scripts').style.display = adminVisible;
	document.getElementById('btn_user').style.display = adminVisible;
}

function initSettings(){
	var endpoint = document.getElementById('setting_endpoint');
	var token = document.getElementById('setting_token');

	endpoint.value = window.localStorage.getItem("endpoint");
	token.value = window.localStorage.getItem("token");
}

function saveSettings(){
	endpoint = document.getElementById('setting_endpoint').value;
	token = document.getElementById('setting_token').value;
	mMediaCenter = '';
	window.localStorage.setItem("endpoint", endpoint);
	window.localStorage.setItem("token", token);
	readSetting();
	refreshMediaServer();
	refreshFiles();
	refreshPlaylist();
}

function loop() {
	refreshPlayer();
	refreshSwitches();
}

function refreshSwitches(){
	apiSwitch.call('list', function(switches)
	{
		if (checkResult(switches, htmlSwitches)){
			switches.sort(function(a, b){return a.name.localeCompare(b.name)});
			var content = "";
			for (var i = 0; i < switches.length; i++) {
				var s = switches[i];
				if (s.state == "ON") {
					content += '<button onclick="switchClick(\'' + s.id + '\', \'OFF\')" class="switch on" style="min-width: 140px">';
				} else {
					content += '<button onclick="switchClick(\'' + s.id + '\', \'ON\')" class="switch off" style="min-width: 140px">';
				}
				content += s.name + "</button>";
			}
			htmlSwitches.innerHTML = content;
			align();
		}		
	});
}

function switchClick(id, state){
	apiSwitch.call('set', refreshSwitches, {'id': id, 'state': state});
}

function mediaClick(id){
	mMediaCenter = id;
	window.localStorage.setItem("mediacenter", mMediaCenter);
	apiMediaServer.addDefaultParameter('id', mMediaCenter);
	mPath = '';
	refreshMediaServer();
	refreshFiles();
	refreshPlaylist();
}

function getPlaying(callback){
	apiMediaServer.call('list', function(result)
	{
		handled = false;
		if (checkResult(result, null, false)) {
			if (result.length > 0 && result[0] !== undefined){
				for (var i = 0; i < result.length; i++) {
					var m = result[i];
					if (m.id == mMediaCenter) {
						callback(m.current_playing);
						handled = true;
					}
				}
			}
		}
		if (!handled)
		{
			callback(null);
		}
	});
}

function refreshPlayer(){
	getPlaying(function(playing){
		var text = '';
		if (playing != null){
			if (playing.artist != null && playing.artist != '')
				text += playing.artist + '<br/>';
			if (playing.title != null && playing.title != '')
				text += playing.title + '<br/>';
			if (playing.artist == null || playing.title == null || playing.artist == '' || playing.title == '')
				text += playing.file + '<br/>';
		} else {
			text += 'Nothing played';
		}
		htmlPlayInfo.innerHTML = text;
	});
}


function refreshMediaServer()
{
	document.title = 'Smart Home Console';
	apiMediaServer.call('list', function(media) 
	{
		if (checkResult(media, htmlMediaServer))
		{
			media.sort(function(a, b){return a.name.localeCompare(b.name)});
			var content = "";
			var title = 'Smart Home Console';
			if (media.length == 1)
			{
				if (mMediaCenter != media[0].id)
				{
					mediaClick(media[0].id);
				}
			}
			for (var i = 0; i < media.length; i++)
			{
				var m = media[i];
				if (m.id == mMediaCenter)
				{
					content += '<button onclick="mediaClick(\'' + m.id + '\')" class="switch on">';
					title = m.name + ' - Smart Home Console';
				} else {
					content += '<button onclick="mediaClick(\'' + m.id + '\')" class="switch off">';
				}
				content += m.name + "</button>";
			}
			if (content == '')
			{
				content = 'No mediaserver';
			}
			htmlMediaServer.innerHTML = content;
			document.title = title;
		}		
	}, {'id': ''});
}

function directoryClick(index){	
	if (index == Separator)
	{
		if (mPath.lastIndexOf(Separator) >= 1)
			mPath = mPath.substring(0, mPath.lastIndexOf(Separator));
		else
			mPath = '';
	}
	else if (mPath == '')
	{
		dir = mFiles[index];
		mPath = dir.name;
	}
	else
	{
		dir = mFiles[index];
		mPath = mPath + Separator + dir.name;
	}
	refreshFiles();
}

function play(index){
	mFile = mFiles[index].name;
	if (mPath != '' && mPath != null)
		file = mPath + Separator + mFile; 
	playPath(file);
}

function playPath(file){
	apiMediaServer.call('play_file', function(result)
	{
		if (checkResult(result)) {
			showMessage(mFile, 'Start playing file');
		}
	}, {'file': file});	
}

function extendPls(pls){
	hideDialog('playlist');
	apiMediaServer.call('playlist_extend', function(result)
	{
		if (checkResult(result)) {
			showMessage('Playlist extended', 'Playlist <b>' + pls + '</b> was extended.');	
		}
	}, {'playlist': pls, 'item': mFile});
}

function addFileToPls(path, file){
	mFile = file;	
	if (path != null && path != '')
	{
		mFile = path + Separator + file;
	}
	apiMediaServer.call('playlists', function(result)
	{
		if (checkResult(result)) {
			var content = "";
			var title = document.getElementById('playlist_title');
			title.innerHTML = "Select playlist";
			result.sort(function(a, b){return a.name.localeCompare(b.name);});
			for (var i = 0; i < result.length; i++) {
				var p = result[i];
				content += '<div onclick="extendPls(\'' + p.name + '\')" class="file link">' + p.name + "</div>";
			}
			htmlPlsContent.innerHTML = content;
			showDialog('playlist');
		}
	});
}

function refreshFiles(){
	apiMediaServer.call('files', function(files)
	{
		if (checkResult(files, htmlFiles)) {
			var content = "";
			if (mPath != ''){
				content = '<button onclick="directoryClick(\'' + Separator + '\')" class="file dir link">';
				content += '<table width="100%"><tr><td>';
				content += '<img src="img/arrow.png" height="16px" class="link"></td><td width="100%">' + mPath.replace(Separator, ' | ') + '</td>';
				content += '</tr></table></button>';
			}
			files.sort(function(a, b){if (a.filetype == b.filetype) { return a.name.localeCompare(b.name);} return a.filetype.localeCompare(b.filetype);});
			mFiles = files;
			for (var i = 0; i < files.length; i++) {
				var f = files[i];
				if (f.filetype == "Directory") {
					content += '<button class="file dir">';
				} else {
					content += '<button class="file">';
				}
				content += '<table width="100%"><tr>';
				if (f.filetype == "Directory") {
					content += '<td class="link" onclick="directoryClick(' + i + ')" >' + f.name + '</td>';
				} else {
					content += '<td class="link" onclick="play(' + i + ')" >' + f.name + '</td>';
				}
				content += '<td align="right" width="70px">';
				content += '<img src="img/play.png" height="32px"/ class="link" onclick="play(' + i + ')">';
				content += '<img src="img/pls.png" height="32px"/ class="link" onclick="addFileToPls(mPath, \'' + f.name + '\')">';
				content += '</td></tr></table></button>';
			}
			htmlFiles.innerHTML = content;
		}
	}, {'path': mPath});
}

function showMessage(title, msg){
	var doc_title = document.getElementById("msg_title");
	var doc_msg = document.getElementById("msg_content");
	doc_title.innerHTML = title;
	doc_msg.innerHTML = msg;
	showDialog('message');
}

function plsClick(pls){
	mPlaylist = pls;
	apiMediaServer.call('play_playlist', function(result)
	{
		if (checkResult(result)) {
			showMessage(mPlaylist, 'Play playlist ' + mPlaylist);
		}
	}, {'playlist': pls});
}

function deletePlsItem(pls, item){
	mPls = pls;
	apiMediaServer.call('playlist_delete_item', function(result)
	{
		if (checkResult(result)) {
			showPlsContent(mPls);
		}
	}, {'playlist': pls, 'item': item});
}

function showPlsContent(pls){
	mPls = pls;
	apiMediaServer.call('playlist_content', function(result)
	{
		if (checkResult(result, htmlPlsContent)) {
			var content = "";			
			var title = document.getElementById('playlist_title');
			title.innerHTML = "Playlist content";
			for (var i = 0; i < result.length; i++) {
				var p = result[i];
				content += '<button class="file"><table width="100%"><tr>';
				content += '<td width="90%" onclick="mFile=\'' + p.name + '\';playPath(\'' + p.path + '\')" class="link">' + p.name + "</td>";
				content += '<td align="right">';
				content += '<img src="img/delete.png" height="32px"/ class="link" onclick="deletePlsItem(\'' + mPls + '\', \'' + p.path + '\')">';
				content += '</td></tr></table></button>';
			}
			htmlPlsContent.innerHTML = content;
			showDialog('playlist');
		}
	}, {'playlist': pls});	
}

function refreshPlaylist(){	
	apiMediaServer.call('playlists', function(result)
	{
		if (checkResult(result, htmlPls)){
			var content = "";
			result.sort(function(a, b){return a.name.localeCompare(b.name);});
			for (var i = 0; i < result.length; i++) {
				var p = result[i];
				content += '<button class="file"><table width="100%"><tr>';
				content += '<td width="90%" onclick="plsClick(\'' + p.name + '\')" class="link">' + p.name + "</td>";
				content += '<td align="right">';
				//content += '<img src="img/play.png" height="32px"/ class="link" onclick="plsClick(\'' + p.name + '\')">';
				content += '<img src="img/pls.png" height="32px"/ class="link" onclick="showPlsContent(\'' + p.name + '\')">';
				content += '</td></tr></table></button>';
			}
			htmlPls.innerHTML = content;
		}		
	});
}

function checkResult(result, errorHtmlElement = null, showErrorMsg = true){
	if (result != null && result.success != null && result.success == false){
		if (result.error != null)
		{
			var message = result.error.message;
			if (message == null && result.error.class != null){
				message = 'Ups, that shouldn\'t happen... ' + result.error.class;
			}
			if (errorHtmlElement){
				errorHtmlElement.innerHTML = message;
			} else if (showErrorMsg) {
				showMessage("Error", result.error.message);
			}
		}		
		return false;
	}
	return true;
}

function playerAction(action, parameter){
	apiMediaServer.call(action, function(result)
	{
		if (checkResult(result)) {
			refreshPlayer();
		}
	}, parameter);
}

function getVolume(){
	getPlaying(function(playing){
		if (playing != null){
			var input = document.getElementById('volume_input');
			input.value = playing.volume;
		}
	});
}

function setVolume(){
	var input = document.getElementById('volume_input');
	playerAction('volume', {'volume': input.value});
}

function doTrigger(trigger){
	mTrigger = trigger;
	apiTrigger.call('dotrigger', function(result)
	{
		if (checkResult(result)) {
			showMessage('Perform trigger', 'Perform <b>' + mTrigger + '</b> with ' + scripts.triggered_events + ' events.' );
		}
	}, {'trigger': trigger});
}

function deleteScript(trigger){
	mTrigger = trigger;
	apiTrigger.call('delete_script', function(result)
	{
		if (checkResult(result)) {
			showScripts();
		}
	}, {'trigger': trigger});
}

/***************
***** User *****
***************/

function showUser()
{
	apiUser.call('list', function(result)
	{
		if (checkResult(result))
		{
			var content = "";
			mUserList = result;
			for (var i = 0; i < result.length; i++)
			{
				var u = result[i];
				content += '<div class="line">' + u.name;
				if (u.role === 'ADMIN')
				{
					content += ' (Admin)';
				}
				content += '<img src="img/edit.png" class="icon" onclick="editUser(\'' + i + '\')"></div>';
			}
			htmlUserList.innerHTML = content;
			showDialog('userlist');
		}
	});
}

function editUser(index)
{
	var title = document.getElementById('edit_user_title');
	document.getElementById('user_passwd').value = '';
	var visibility = 'hidden';
	if (index >= 0)
	{
		mSelectedUser = mUserList[index];
		document.getElementById('user_name').value = mSelectedUser.name;
		document.getElementById('user_role').value = mSelectedUser.role;
		title.innerHTML = 'Edit User ' + mSelectedUser.name;
		visibility = 'visible';
	} else {
		mSelectedUser = null;
		document.getElementById('user_name').value = '';
		title.innerHTML = 'Create new User';
	}
	document.getElementById('btn_user_remove').style.visibility = visibility;
	var user_required = document.getElementsByClassName('user_required');
	for (var i = 0; i < user_required.length; i++) {
		user_required[i].style.visibility = visibility;
	}
	showDialog('user_edit');
	refreshUserAccess();
	refreshUserSessions();
}

function deleteUser()
{
	if (mSelectedUser != null)
	{
		apiUser.call('delete', function(result)
		{
			if (checkResult(result))
			{
				hideDialog('user_edit');
				showUser();
			}
		}, {'user_id': mSelectedUser.id});
	}
}

function refreshUserAccess()
{
	var accessList = document.getElementById('user_access');
	if (mSelectedUser == null)
	{
		accessList.innerHTML = '';
		return;
	} else {
		accessList.innerHTML = '<div style="text-align: center"><img src="img/loading.png" class="rotate" width="128px"/></div>';
	}
	apiUser.call('get_access', function(result)
	{
		var accessList = document.getElementById('user_access');
		if (checkResult(result, accessList))
		{
			var content = "";
			for (var i = 0; i < result.length; i++)
			{
				var access = result[i];
				content += '<div class="line">' + access.name + ' (' + access.id + ')';
				content += '<img src="img/delete.png" class="icon" onclick="deleteUserAccess(\'' + access.id + '\')"></div>';
			}
			if (!content)
			{
				content = '<i>No User Access</i>';
			}
			accessList.innerHTML = content;
		}
	}, {'user_id': mSelectedUser.id});
}

function deleteUserAccess(id)
{
	apiUser.call('remove_access', function(result)
	{
		if (checkResult(result))
		{
			refreshUserAccess();
		}
	}, {'user_id': mSelectedUser.id, 'unit_id': id});
}

function applyUserName()
{
	var newName = document.getElementById('user_name').value;
	var newPasswd = document.getElementById('user_passwd').value;
	var callback = function(result)
		{
			if (checkResult(result)){
				hideDialog('user_edit');
				showUser();
			}
		};
	if (mSelectedUser != null)
	{
		apiUser.call('change_name', callback, {'user_id': mSelectedUser.id, 'new_name': newName});
		if (newPasswd)
		{
			apiUser.call('change_password', null, {'user_id': mSelectedUser.id, 'new_password': newPasswd});
		}
	} else {
		apiUser.call('create', callback, {'user_name': newName, 'password': newPasswd, 'avatar': ''});
	}
}

function addUserAccess()
{
	apiTrigger.call('list_controlunits', function(result)
	{
		if (checkResult(result))
		{
			var element = document.getElementById('accesslist_content');
			var content = '';
			for (var i = 0; i < result.length; i++)
			{
				var unit = result[i];
				content += '<div class="line">' + unit.name + ' (' + unit.id + ')';
				content += '<img src="img/add.png" class="icon" onclick="addUnitAccess(\'' + unit.id + '\')"></div>';
			}
			element.innerHTML = content;
			showDialog('accesslist');
		}
	});
}

function addUnitAccess(unitId)
{
	if (mSelectedUser != null)
	{
		apiUser.call('add_access', function(result)
		{
			if (checkResult(result))
			{
				refreshUserAccess();
			}
		}, {'user_id': mSelectedUser.id, 'unit_id': unitId});
	}
	hideDialog('accesslist');
}

function refreshUserSessions()
{
	var accessList = document.getElementById('user_session');
	if (mSelectedUser == null)
	{
		accessList.innerHTML = '';
		return;
	} else {
		accessList.innerHTML = '<div style="text-align: center"><img src="img/loading.png" class="rotate" width="128px"/></div>';
	}
	apiUser.call('list_tokens', function(result)
	{
		var sessionList = document.getElementById('user_session');
		if (checkResult(result, sessionList))
		{
			var content = "";
			for (var i = 0; i < result.length; i++)
			{
				var session = result[i];
				if (mSelectedUser.id === session.user_id)
				{
					content += '<div class="line">' + session.token;
					content += '<img src="img/delete.png" class="icon" onclick="deleteUserSession(\'' + session.token + '\')"></div>';
				}
			}
			if (!content)
			{
				content = '<i>No User Session</i>';
			}
			sessionList.innerHTML = content;
		}
	});
}

function deleteUserSession(session)
{
	if (mSelectedUser == null)
	{
		return;
	}
	apiUser.call('delete_token', function(result)
	{
		if (checkResult(result))
		{
			refreshUserSessions();
		}
	}, {'delete_token': session});
}

function addUserSession()
{
	if (mSelectedUser == null)
	{
		return;
	}
	apiUser.call('create_persistent_token', function(result)
	{
		if (checkResult(result))
		{
			refreshUserSessions();
		}
	}, {'user_id': mSelectedUser.id});
}

/*****************
***** Script *****
*****************/

function addInfo(){
	var info = document.getElementById('trigger_infos');
	apiTrigger.call('set_information_for_script', function(result)
	{
		if (checkResult(result)) {
			hideDialog('trigger');
			showScripts();
		}
	}, {'trigger': mScriptId, 'information': info.value});
}

function deleteEvent(index){
	apiTrigger.call('delete_event', function(result){
		if (checkResult(result)) {
			hideDialog('trigger');
			showScripts();
		}
	}, {'trigger': mScriptId, 'index': index});	
}

function createNewEvent(){
	var unit = document.getElementById("new_event");
	var condition = document.getElementById("new_event_condition");
	apiTrigger.call('create_event', function(result)
	{
		if (checkResult(result)) {
			hideDialog('trigger');
			showScripts();
		}
	}, {'trigger': mScriptId, 'unit': unit.value, 'condition': condition.value});
}

function createNewParameter(index){
	var key = document.getElementById("new_param_key");
	var value = document.getElementById("new_param_value");
	apiTrigger.call('add_parameter', function(result)
	{
		if (checkResult(result)) {
			hideDialog('trigger');
			showScripts();
		}
	}, {'trigger': mScriptId, 'index': index, 'key': key.value, 'value': value.value});	
}

function updateCondition(index){
	var condition = document.getElementById("condition_" + index);
	apiTrigger.call('set_condition_for_event', function(result)
	{
		if (checkResult(result)) {
			hideDialog('trigger');
			showScripts();
		}
	}, {'trigger': mScriptId, 'event_index': index, 'condition': condition.value});
}

function deleteParameter(index){
	apiTrigger.call('delete_parameter_for_event', function(result)
	{
		if (checkResult(result)) {
			hideDialog('trigger');
			showScripts();
		}
	}, {'trigger': mScriptId, 'event_index': index, 'parameter_index': (mScripts[mScriptIndex].events[index].parameter.length - 1)});
}

function showTrigger(index){
	var trigger = mScripts[index];
	mScriptIndex = index;
	mScriptId = trigger.trigger;
	title = document.getElementById('trigger_title');
	content = document.getElementById('trigger_content');
	id = document.getElementById('trigger_id');
	infos = document.getElementById('trigger_infos');
	
	title.innerHTML = 'Edit script';
	id.value = trigger.trigger;
	infos.value = trigger.information;

	contentText = '<table width="100%">';
	for (var i = 0; i < trigger.events.length; i++){
		var event = trigger.events[i];
		contentText += '<tr><td colspan="3" class="highlight">' + event.unit_id;
		contentText += '<img src="img/delete.png" height="32px" class="link right" onclick="deleteEvent(' + i + ')">';
		contentText += '</td></tr>';
		condition = '';
		if (event.condition != null)
			condition = event.condition;
		contentText += '<tr><td style="width: 195px">Condition</td><td><input class="fill" value="' + condition + '" id="condition_'+i+'"/></td>';
		contentText += '<td style="width: 40px"><img src="img/add.png" height="32px" class="link right" onclick="updateCondition(' + i + ')"></td></tr>';
		parameter = '';
		for (var j = 0; j < event.parameter.length; j++){
			parameter += event.parameter[j].key + '=\'' + event.parameter[j].value + '\', ';
		}
		contentText += '<tr><td>Parameter</td><td><input class="fill" value="' + parameter + '"/></td>';
		contentText += '<td><img src="img/delete.png" height="32px" class="link right" onclick="deleteParameter(' + i + ')"></td></tr>';
		contentText += '<tr><td>New Parameter</td>';
		contentText += '<td><input class="half" id="new_param_key" value="key"/><input class="half" id="new_param_value" value="value"/></td>';
		contentText += '<td><img src="img/add.png" height="32px" class="link right" onclick="createNewParameter(' + i + ')"></td></tr>';
	}
	contentText += '<tr><td class="highlight" style="width: 195px">New event</td>';
	contentText += '<td class="highlight"><input class="half" id="new_event" value="unit_id"/><input class="half" id="new_event_condition"  value="condition"/></td>';
	contentText += '<td><img src="img/add.png" height="32px" class="link right" onclick="createNewEvent()"></td>';
	contentText += '</tr></table>';
	content.innerHTML = contentText;

	showDialog('trigger');
}

function createScript(){
	var input = document.getElementById('new_script');
	apiTrigger.call('create_script', function(result)
	{
		if (checkResult(result)) {
			showScripts();
		}
	}, {'trigger': input.value});
}

function showScripts(){
	showDialog('scripts');
	htmlScripts.innerHTML = '<div style="text-align: center"><img src="img/loading.png" class="rotate" width="128px"/></div>';

	apiTrigger.call('scripts', function(result)
	{
		if (checkResult(result, htmlScripts)) {
			mScripts = result;
			var content = "";
			for (var i = 0; i < mScripts.length; i++) {
				var script = mScripts[i];
				content += '<div class="file"><table width="100%"><tr>';
				content += '<td width="80%" onclick="showTrigger(' + i + ')" class="link">' + script.trigger + "</td>";
				content += '<td align="right">';
				content += '<img src="img/play.png" height="32px"/ class="link" onclick="doTrigger(\'' + script.trigger + '\')">';
				content += '<img src="img/delete.png" height="32px"/ class="link" onclick="deleteScript(\'' + script.trigger + '\')">';
				content += '</td></tr></table></div>';
			}
			content += '<div class="file"><table width="100%"><tr>';
			content += '<td width="90%"><input value="new.script" id="new_script" class="fill"/></td>';
			content += '<td align="right">';
			content += '<img src="img/add.png" height="32px" class="link" onclick="createScript()">';
			content += '</td></tr></table></div>';
			htmlScripts.innerHTML = content;
		}
	});
}
