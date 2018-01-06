package de.neo.smarthome.mobile.util;

import java.net.ConnectException;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;
import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.mobile.activities.MediaServerActivity;
import de.neo.smarthome.mobile.api.IWebMediaServer;
import de.neo.smarthome.mobile.api.IWebSwitch;
import de.neo.smarthome.mobile.api.PlayingBean;
import de.neo.smarthome.mobile.services.RemoteService;
import de.remote.mobile.R;

public class WidgetUpdater {

	private Context mContext;

	public WidgetUpdater(Context context) {
		mContext = context;
	}

	public void updateMusicWidget(int widgetID, IWebMediaServer.BeanMediaServer mediaserver) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
		RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.mediaserver_widget);
		if (remoteViews != null) {
			remoteViews.setViewVisibility(R.id.img_widget_thumbnail, View.INVISIBLE);
			if (mediaserver == null) {
				remoteViews.setTextViewText(R.id.lbl_widget_big, mContext.getString(R.string.no_conneciton));
				remoteViews.setTextViewText(R.id.lbl_widget_small,
						mContext.getString(R.string.no_conneciton_with_server));
				remoteViews.setTextViewText(R.id.lbl_widget_small2, "");
				remoteViews.setInt(R.id.button_widget_play, "setBackgroundResource", R.drawable.player_pause);
			} else if (mediaserver.getCurrentPlaying() != null) {
				remoteViews.setTextViewText(R.id.lbl_widget_big, mediaserver.getCurrentPlaying().getTitle());
				remoteViews.setTextViewText(R.id.lbl_widget_small, mediaserver.getCurrentPlaying().getArtist());
				remoteViews.setTextViewText(R.id.lbl_widget_small2, mediaserver.getCurrentPlaying().getAlbum());

				if (mediaserver.getCurrentPlaying().getState() == PlayingBean.STATE.PLAY)
					remoteViews.setInt(R.id.button_widget_play, "setBackgroundResource", R.drawable.player_pause);
				else
					remoteViews.setInt(R.id.button_widget_play, "setBackgroundResource", R.drawable.player_play);
			} else {
				remoteViews.setTextViewText(R.id.lbl_widget_big, mContext.getString(R.string.player_no_file_playing));
				remoteViews.setTextViewText(R.id.lbl_widget_small, mediaserver.getName());
				remoteViews.setTextViewText(R.id.lbl_widget_small2, "");
				remoteViews.setInt(R.id.button_widget_play, "setBackgroundResource", R.drawable.player_pause);
			}

			setPlayerIntents(remoteViews, mediaserver.getID(), widgetID);

			appWidgetManager.updateAppWidget(widgetID, remoteViews);
			if (RemoteService.DEBUGGING)
				Toast.makeText(mContext, mediaserver.getCurrentPlaying().getArtist(), Toast.LENGTH_SHORT).show();
		}
	}

	private void setPlayerIntents(RemoteViews remoteViews, String mediaserverID, int widgetID) {
		Intent browserIntent = new Intent(mContext, MediaServerActivity.class);
		browserIntent.putExtra(MediaServerActivity.EXTRA_MEDIA_ID, mediaserverID);
		browserIntent.setData(Uri.withAppendedPath(Uri.parse("ABCD://widget/id/"), String.valueOf(widgetID)));
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, browserIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.lbl_widget_big, pendingIntent);

		// set update functionality
		Intent playIntent = new Intent(mContext, RemoteService.class);
		playIntent.setAction(RemoteService.ACTION_UPDATE);
		playIntent.setData(Uri.withAppendedPath(Uri.parse("ABCD://widget/id/"), String.valueOf(widgetID)));
		playIntent.putExtra(RemoteService.EXTRA_WIDGET, widgetID);
		PendingIntent playPending = PendingIntent.getService(mContext, 0, playIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.button_widget_refresh, playPending);

		// set play functionality
		playIntent = new Intent(mContext, RemoteService.class);
		playIntent.setAction(RemoteService.ACTION_PLAY);
		playIntent.setData(Uri.withAppendedPath(Uri.parse("ABCD://widget/id/"), String.valueOf(widgetID)));
		playIntent.putExtra(RemoteService.EXTRA_WIDGET, widgetID);
		playPending = PendingIntent.getService(mContext, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.button_widget_play, playPending);

		// set stop functionality
		Intent stopIntent = new Intent(mContext, RemoteService.class);
		stopIntent.setAction(RemoteService.ACTION_STOP);
		stopIntent.setData(Uri.withAppendedPath(Uri.parse("ABCD://widget/id/"), String.valueOf(widgetID)));
		stopIntent.putExtra(RemoteService.EXTRA_WIDGET, widgetID);
		PendingIntent stopPending = PendingIntent.getService(mContext, 0, stopIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.button_widget_quit, stopPending);

		// set vol up functionality
		Intent nextIntent = new Intent(mContext, RemoteService.class);
		nextIntent.setAction(RemoteService.ACTION_VOLUP);
		nextIntent.setData(Uri.withAppendedPath(Uri.parse("ABCD://widget/id/"), String.valueOf(widgetID)));
		nextIntent.putExtra(RemoteService.EXTRA_WIDGET, widgetID);
		PendingIntent nextPending = PendingIntent.getService(mContext, 0, nextIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.button_widget_vol_up, nextPending);

		// set vol down functionality
		Intent prevIntent = new Intent(mContext, RemoteService.class);
		prevIntent.setAction(RemoteService.ACTION_VOLDOWN);
		prevIntent.setData(Uri.withAppendedPath(Uri.parse("ABCD://widget/id/"), String.valueOf(widgetID)));
		prevIntent.putExtra(RemoteService.EXTRA_WIDGET, widgetID);
		PendingIntent prevPending = PendingIntent.getService(mContext, 0, prevIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.button_widget_vol_down, prevPending);

		// set start activity functionality
		Intent activityIntent = new Intent(mContext, RemoteService.class);
		activityIntent.setAction(RemoteService.ACTION_MEDIA_ACTIVITY);
		activityIntent.setData(Uri.withAppendedPath(Uri.parse("ABCD://widget/id/"), String.valueOf(widgetID)));
		activityIntent.putExtra(RemoteService.EXTRA_WIDGET, widgetID);
		PendingIntent activityPending = PendingIntent.getService(mContext, 0, activityIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.lbl_widget_big, activityPending);
	}

	public void updateSwitchWidget(int widgetID, IWebSwitch.BeanSwitch s) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
		RemoteViews remoteSwitchViews = new RemoteViews(mContext.getPackageName(), R.layout.switch_widget);
		remoteSwitchViews.setImageViewResource(R.id.image_power_widget,
				getImageForSwitchType(s.getType(), s.getState() == IWebSwitch.State.ON));
		remoteSwitchViews.setTextViewText(R.id.text_power_widget, s.getName());

		// set switch functionality
		Intent switchIntent = new Intent(mContext, RemoteService.class);
		switchIntent.setAction(RemoteService.ACTION_SWITCH);
		switchIntent.setData(Uri.withAppendedPath(Uri.parse("ABCD://widget/id/"), String.valueOf(widgetID)));
		switchIntent.putExtra(RemoteService.EXTRA_WIDGET, widgetID);
		PendingIntent switchPending = PendingIntent.getService(mContext, widgetID, switchIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		remoteSwitchViews.setOnClickPendingIntent(R.id.widget_power_layout, switchPending);

		appWidgetManager.updateAppWidget(widgetID, remoteSwitchViews);
	}

	public int getImageForSwitchType(String type, boolean on) {
		if (ControlSceneRenderer.AUDIO.equals(type)) {
			if (on)
				return R.drawable.music_on;
			else
				return R.drawable.music_off;
		}
		if (ControlSceneRenderer.VIDEO.equals(type)) {
			if (on)
				return R.drawable.tv_on;
			else
				return R.drawable.tv_off;
		}
		if (ControlSceneRenderer.LAMP_FLOOR.equals(type) || ControlSceneRenderer.LAMP_LAVA.equals(type)) {
			if (on)
				return R.drawable.light_on;
			else
				return R.drawable.light_off;
		}
		if (ControlSceneRenderer.LAMP_READ.equals(type)) {
			if (on)
				return R.drawable.reading_on;
			else
				return R.drawable.reading_off;
		}
		if (ControlSceneRenderer.SWITCH_COFFEE.equals(type)) {
			if (on)
				return R.drawable.coffee_on;
			else
				return R.drawable.coffee_off;
		}
		return R.drawable.switch_unknown;
	}

	public void updateMusicWidget(int widgetID, RemoteException e) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
		RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.mediaserver_widget);
		if (remoteViews != null) {
			remoteViews.setViewVisibility(R.id.img_widget_thumbnail, View.INVISIBLE);

			remoteViews.setTextViewText(R.id.lbl_widget_big, mContext.getString(R.string.no_conneciton));
			if (e.getCause() instanceof ConnectException)
				remoteViews.setTextViewText(R.id.lbl_widget_small,
						mContext.getString(R.string.no_conneciton_with_server));
			else
				remoteViews.setTextViewText(R.id.lbl_widget_small, e.getMessage());
			remoteViews.setTextViewText(R.id.lbl_widget_small2, "");
			remoteViews.setInt(R.id.button_widget_play, "setBackgroundResource", R.drawable.player_pause);

			setPlayerIntents(remoteViews, "", widgetID);

			appWidgetManager.updateAppWidget(widgetID, remoteViews);
			if (RemoteService.DEBUGGING)
				Toast.makeText(mContext, "Error on update music widget: " + e.getClass().getSimpleName(),
						Toast.LENGTH_SHORT).show();
		}
	}

	public void updateSwitchWidget(int widgetID, Exception e) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
		RemoteViews remoteSwitchViews = new RemoteViews(mContext.getPackageName(), R.layout.switch_widget);
		remoteSwitchViews.setImageViewResource(R.id.image_power_widget, getImageForSwitchType("", false));
		if (e.getCause() instanceof ConnectException)
			remoteSwitchViews.setTextViewText(R.id.text_power_widget, "");
		else
			remoteSwitchViews.setTextViewText(R.id.text_power_widget, e.getMessage());

		// set switch functionality
		Intent switchIntent = new Intent(mContext, RemoteService.class);
		switchIntent.setAction(RemoteService.ACTION_SWITCH);
		switchIntent.setData(Uri.withAppendedPath(Uri.parse("ABCD://widget/id/"), String.valueOf(widgetID)));
		switchIntent.putExtra(RemoteService.EXTRA_WIDGET, widgetID);
		PendingIntent switchPending = PendingIntent.getService(mContext, widgetID, switchIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		remoteSwitchViews.setOnClickPendingIntent(R.id.widget_power_layout, switchPending);

		appWidgetManager.updateAppWidget(widgetID, remoteSwitchViews);
	}

}
