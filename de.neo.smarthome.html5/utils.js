var currentVisible = undefined;

var mDialogMargin = 5;

loadAppearence();

window.onresize = function()
{
	updateVisibleCard();
	placeDialogs();
};

function centerElementHorizontally(element)
{
	var style = getComputedStyle(element);
	var width = style.getPropertyValue('--dlg_width');
	var offset = Math.max(mDialogMargin, (window.innerWidth - Number(width)) / 2);
	element.style.left = offset + 'px';
	element.style.right = offset + 'px';
}

function centerElementVertically(element)
{
	var style = getComputedStyle(element);
	var height = parseInt(style.height, 10);
	var setBottom = false;
	if(element.getAttribute("style").indexOf('--dlg_height') != -1)
	{
		height = style.getPropertyValue('--dlg_height');
		setBottom = true;
	}
	offset = Math.max(mDialogMargin, (window.innerHeight - Number(height)) / 2);
	element.style.top = offset + 'px';
	if(setBottom)
	{
		element.style.bottom = offset + 'px';
	}
}

function placeDialogs()
{
	var dialogs = document.getElementsByClassName("dialog");
	for (var i = 0; i < dialogs.length; i++)
	{
		centerElementHorizontally(dialogs[i]);
		centerElementVertically(dialogs[i]);
	}
	centerElementHorizontally(document.getElementById("toast"));
}

function loadAppearence()
{
	appearence = token = window.localStorage.getItem("appearence");
	document.documentElement.style.setProperty("--card_shadow", "");
	document.documentElement.style.setProperty("--dialog_shadow", "0px 10px 30px 10px black");
	if(appearence == 'bright')
	{
		document.documentElement.style.setProperty("--dialog_shadow", "");
		document.documentElement.style.setProperty("--body_background", "white");
		document.documentElement.style.setProperty("--background", "white");
		document.documentElement.style.setProperty("--font", "black");
		document.documentElement.style.setProperty("--font_on", "white");
		document.documentElement.style.setProperty("--font_name", "'Baumans', system-ui");
		document.documentElement.style.setProperty("--title", "black");
		document.documentElement.style.setProperty("--border", "black");
		document.documentElement.style.setProperty("--card_border", "1px solid var(--border)");
		document.documentElement.style.setProperty("--card_radius", "0px");
		document.documentElement.style.setProperty("--focus", "black");
		document.documentElement.style.setProperty("--btn_radius", "0px");
		document.documentElement.style.setProperty("--btn_border", "1px");
		document.documentElement.style.setProperty("--btn_on_border_color", "#888");
		document.documentElement.style.setProperty("--btn_on_border", "var(--btn_border) solid var(--btn_on_border_color)");
		document.documentElement.style.setProperty("--btn_on_border_hover", "var(--btn_border) solid white");
		document.documentElement.style.setProperty("--btn_on_bg", "black");
		document.documentElement.style.setProperty("--btn_off_border_color", "black");
		document.documentElement.style.setProperty("--btn_off_border", "var(--btn_border) solid var(--btn_off_border_color)");		
		document.documentElement.style.setProperty("--btn_off_border_hover", "var(--btn_border) solid black");
		document.documentElement.style.setProperty("--btn_off_bg", "white");
		document.documentElement.style.setProperty("--img_filter", "brightness(0.3)");
		document.documentElement.style.setProperty("--img_filter_hover", "brightness(0)");
		document.documentElement.style.setProperty("--btn_card_color", "black");
		document.documentElement.style.setProperty("--img_card_filter", "brightness(1)");
	}
	else if(appearence == 'turquoise')
	{
		document.documentElement.style.setProperty("--body_background", "#05445e");
		document.documentElement.style.setProperty("--background", "#d4f1f4");
		document.documentElement.style.setProperty("--card_shadow", "0px 0px 8px 0px black");
		document.documentElement.style.setProperty("--font", "black");
		document.documentElement.style.setProperty("--font_on", "white");
		document.documentElement.style.setProperty("--font_name", "Ubuntu");
		document.documentElement.style.setProperty("--title", "black");
		document.documentElement.style.setProperty("--border", "black");
		document.documentElement.style.setProperty("--card_border", "0px");
		document.documentElement.style.setProperty("--card_radius", "25px");
		document.documentElement.style.setProperty("--focus", "#05445e");
		document.documentElement.style.setProperty("--btn_radius", "20px");
		document.documentElement.style.setProperty("--btn_border", "1px");
		document.documentElement.style.setProperty("--btn_on_border_color", "#05445E");
		document.documentElement.style.setProperty("--btn_on_border", "var(--btn_border) solid var(--btn_on_border_color)");
		document.documentElement.style.setProperty("--btn_on_border_hover", "var(--btn_border) solid #05445E");
		document.documentElement.style.setProperty("--btn_on_bg", "var(--body_background)");
		document.documentElement.style.setProperty("--btn_off_border_color", "#189AB4");
		document.documentElement.style.setProperty("--btn_off_border", "var(--btn_border) solid var(--btn_off_border_color)");		
		document.documentElement.style.setProperty("--btn_off_border_hover", "var(--btn_border) solid #189AB4");
		document.documentElement.style.setProperty("--btn_off_bg", "#D4F1F4");
		document.documentElement.style.setProperty("--img_filter", "brightness(0.5) sepia(100%) saturate(10000%) hue-rotate(190deg) saturate(0.6) brightness(0.8)");
		document.documentElement.style.setProperty("--img_filter_hover", "brightness(0.5) sepia(100%) saturate(10000%) hue-rotate(190deg) saturate(0.6) brightness(0.3)");
		document.documentElement.style.setProperty("--btn_card_color", "var(--body_background)");
		document.documentElement.style.setProperty("--img_card_filter", "brightness(0.9)");
	}
	else if(appearence == 'darkred')
	{
		document.documentElement.style.setProperty("--body_background", "black");
		document.documentElement.style.setProperty("--background", "#100");
		document.documentElement.style.setProperty("--card_shadow", "0px 0px 6px #a00");
		document.documentElement.style.setProperty("--font", "white");
		document.documentElement.style.setProperty("--font_on", "var(--font)");
		document.documentElement.style.setProperty("--font_name", "'Source Code Pro', monospace");
		document.documentElement.style.setProperty("--title", "white");
		document.documentElement.style.setProperty("--border", "#800");
		document.documentElement.style.setProperty("--card_border", "1px solid var(--border)");
		document.documentElement.style.setProperty("--card_radius", "13px");
		document.documentElement.style.setProperty("--focus", "#f55");
		document.documentElement.style.setProperty("--btn_radius", "8px");
		document.documentElement.style.setProperty("--btn_border", "1px");
		document.documentElement.style.setProperty("--btn_on_border_color", "#a00");
		document.documentElement.style.setProperty("--btn_on_border", "var(--btn_border) solid var(--btn_on_border_color)");
		document.documentElement.style.setProperty("--btn_on_border_hover", "var(--btn_border) solid #a00");
		document.documentElement.style.setProperty("--btn_on_bg", "#500");
		document.documentElement.style.setProperty("--btn_off_border_color", "#a00");
		document.documentElement.style.setProperty("--btn_off_border", "var(--btn_border) solid var(--btn_off_border_color)");		
		document.documentElement.style.setProperty("--btn_off_border_hover", "var(--btn_border) solid #a00");
		document.documentElement.style.setProperty("--btn_off_bg", "#000");
		document.documentElement.style.setProperty("--img_filter", "brightness(0.8)");
		document.documentElement.style.setProperty("--img_filter_hover", "brightness(1)");
		document.documentElement.style.setProperty("--btn_card_color", "white");
		document.documentElement.style.setProperty("--img_card_filter", "brightness(0.5) sepia(100%) saturate(10000%) saturate(0.4) brightness(0.6)");
		document.documentElement.style.setProperty("--dialog_shadow", "0px 0px 15px 2px white");
	}
	else if(appearence == 'sepia')
	{
		document.documentElement.style.setProperty("--body_background", "#A0522D");
		document.documentElement.style.setProperty("--background", "#D2B48C");
		document.documentElement.style.setProperty("--card_shadow", "0px 0px 8px -2px black");
		document.documentElement.style.setProperty("--font", "black");
		document.documentElement.style.setProperty("--font_on", "#D2B48C");
		document.documentElement.style.setProperty("--font_name", "'Courgette', cursive");
		document.documentElement.style.setProperty("--title", "#5B3A29");
		document.documentElement.style.setProperty("--border", "#5B3A29");
		document.documentElement.style.setProperty("--card_border", "1px solid var(--border)");
		document.documentElement.style.setProperty("--card_radius", "8px");
		document.documentElement.style.setProperty("--focus", "#5B3A29");
		document.documentElement.style.setProperty("--btn_radius", "3px");
		document.documentElement.style.setProperty("--btn_border", "2px");
		document.documentElement.style.setProperty("--btn_on_border_color", "#8B4513");
		document.documentElement.style.setProperty("--btn_on_border", "var(--btn_border) solid var(--btn_on_border_color)");
		document.documentElement.style.setProperty("--btn_on_border_hover", "var(--btn_border) solid #8B4513");
		document.documentElement.style.setProperty("--btn_on_bg", "#5B3A29");
		document.documentElement.style.setProperty("--btn_off_border_color", "#8B4513");
		document.documentElement.style.setProperty("--btn_off_border", "var(--btn_border) solid var(--btn_off_border_color)");
		document.documentElement.style.setProperty("--btn_off_border_hover", "var(--btn_border) solid #8B4513");
		document.documentElement.style.setProperty("--btn_off_bg", "#D2B48C");
		document.documentElement.style.setProperty("--img_filter", "brightness(0.5) sepia(100%) saturate(10000%) hue-rotate(45deg) saturate(0.6) brightness(1)");
		document.documentElement.style.setProperty("--img_filter_hover", "brightness(0.5) sepia(100%) saturate(10000%) hue-rotate(45deg) saturate(0.6) brightness(0.5)");
		document.documentElement.style.setProperty("--btn_card_color", "#5B3A29");
		document.documentElement.style.setProperty("--img_card_filter", "brightness(0.5) sepia(100%) saturate(10000%) hue-rotate(34deg) saturate(0.3) brightness(4)");
	}
	else
	{
		document.documentElement.style.setProperty("--body_background", "black");
		document.documentElement.style.setProperty("--background", "black");
		document.documentElement.style.setProperty("--font", "white");
		document.documentElement.style.setProperty("--font_on", "var(--font)");
		document.documentElement.style.setProperty("--font_name", "'Source Code Pro', monospace");
		document.documentElement.style.setProperty("--title", "#aaa");
		document.documentElement.style.setProperty("--border", "#666");
		document.documentElement.style.setProperty("--card_border", "1px solid var(--border)");
		document.documentElement.style.setProperty("--card_radius", "3px");
		document.documentElement.style.setProperty("--focus", "#888");
		document.documentElement.style.setProperty("--btn_radius", "2px");
		document.documentElement.style.setProperty("--btn_border", "2px");
		document.documentElement.style.setProperty("--btn_on_border_color", "#666");
		document.documentElement.style.setProperty("--btn_on_border", "2px solid var(--btn_on_border_color)");
		document.documentElement.style.setProperty("--btn_on_border_hover", "2px solid #888");
		document.documentElement.style.setProperty("--btn_on_bg", "#222");
		document.documentElement.style.setProperty("--btn_off_border_color", "#222");
		document.documentElement.style.setProperty("--btn_off_border", "2px solid var(--btn_off_border_color)");		
		document.documentElement.style.setProperty("--btn_off_border_hover", "2px solid #888");
		document.documentElement.style.setProperty("--btn_off_bg", "black");
		document.documentElement.style.setProperty("--img_filter", "brightness(0.8)");
		document.documentElement.style.setProperty("--img_filter_hover", "brightness(1)");
		document.documentElement.style.setProperty("--btn_card_color", "#e6e6e6");
		document.documentElement.style.setProperty("--img_card_filter", "brightness(0)");
	}
}

function showArea(area)
{
	var areas = ["playlists","switches","filesystem", "information"];
	for (var i = 0; i < areas.length; i++) {
		var a = areas[i];
		var elem = document.getElementById(a);
		var nav = document.getElementById('nav_' + a);
		if (elem != null){
			if (a === area)
			{
				elem.style.zIndex = "auto";
				elem.classList.remove("card_gone");
				currentVisible = elem;
			}
			else
			{
				elem.style.zIndex = -1;
				elem.classList.add("card_gone");
			}
		}
		if (nav != null){
			if (a === area){
				nav.classList.add('on');
				nav.classList.remove('off');
			}else{
				nav.classList.remove('on');
				nav.classList.add('off');
			}
		}
	}
}


function updateVisibleCard()
{
	if (currentVisible == undefined)
	{
		currentVisible = document.getElementById("filesystem");
	}
	var areas = ["playlists","switches","filesystem", "information"];
	for (var i = 0; i < areas.length; i++)
	{
		var a = areas[i];
		var elem = document.getElementById(a);
		if (elem != null)
		{
			if (elem == currentVisible || window.innerWidth >= 900)
			{
				elem.style.zIndex = "auto";
				elem.classList.remove("card_gone");
			}
			else
			{
				elem.style.zIndex = -1;
				elem.classList.add("card_gone");
			}
		}
	}
}

function showDialog(id)
{
	var container = document.getElementById(id);
	container.classList.add("visible");
	container.classList.remove("z_index_back");
}

function hideDialog(id)
{
	var container = document.getElementById(id);
	container.classList.remove("visible");
	setTimeout(function()
	{
		container.classList.add("z_index_back");
	}, 500);
}
