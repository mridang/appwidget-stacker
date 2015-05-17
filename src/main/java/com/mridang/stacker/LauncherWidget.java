package com.mridang.stacker;

import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.text.style.TextAppearanceSpan;
import android.widget.RemoteViews;

import com.mridang.widgets.BaseWidget;
import com.mridang.widgets.SavedSettings;
import com.mridang.widgets.WidgetHelpers;
import com.mridang.widgets.utils.GzippedClient;

/**
 * This class is the provider for the widget and updates the data
 */
public class LauncherWidget extends BaseWidget {

	/*
	 * 
	 */
	private static void appendBadge(SpannableStringBuilder sb, Object style, int number) {

		if (number > 0) {
			sb.append(number + "");
			LauncherWidget.appendStyled(sb, style, "\u25cf ");
		}

	}

	/*
	 * 
	 */
	private static void appendStyled(SpannableStringBuilder sb, Object style, String text) {

		final int s = sb.length();
		sb.append(text);
		sb.setSpan(style, s, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

	}

	/*
	 * @see com.mridang.widgets.BaseWidget#fetchContent(android.content.Context, java.lang.Integer,
	 * com.mridang.widgets.SavedSettings)
	 */
	@Override
	public String fetchContent(Context ctxContext, Integer intInstance, SavedSettings objSettings)
			throws Exception {

		final DefaultHttpClient dhcClient = GzippedClient.createClient();
		final HttpGet getProjects = new HttpGet("http://api.stackexchange.com/2.2/users/" + objSettings.get("profile") + "?site=stackoverflow");
		final HttpResponse resProjects = dhcClient.execute(getProjects);

		final Integer intResponse = resProjects.getStatusLine().getStatusCode();
		if (intResponse != HttpStatus.SC_OK) {
			throw new HttpResponseException(intResponse, "Server responded with code " + intResponse);
		}

		final String strResponse = EntityUtils.toString(resProjects.getEntity(), "UTF-8");
		return new JSONObject(strResponse).getJSONArray("items").getJSONObject(0).toString(2);

	}

	/*
	 * @see com.mridang.widgets.BaseWidget#getIcon()
	 */
	@Override
	public Integer getIcon() {

		return R.drawable.ic_notification;

	}

	/*
	 * @see com.mridang.widgets.BaseWidget#getKlass()
	 */
	@Override
	protected Class<?> getKlass() {

		return getClass();

	}

	/*
	 * @see com.mridang.BaseWidget#getToken()
	 */
	@Override
	public String getToken() {

		return "a1b2c3d4";

	}

	/*
	 * @see com.mridang.widgets.BaseWidget#updateWidget(android.content.Context, java.lang.Integer,
	 * com.mridang.widgets.SavedSettings, java.lang.String)
	 */
	@Override
	public void updateWidget(Context ctxContext, Integer intInstance, SavedSettings objSettings, String strContent)
			throws Exception {

		final RemoteViews remView = new RemoteViews(ctxContext.getPackageName(), R.layout.widget);
		JSONObject jsoData = new JSONObject(strContent);

		final SpannableStringBuilder ssbBadges = new SpannableStringBuilder();
		LauncherWidget.appendBadge(ssbBadges, new TextAppearanceSpan(ctxContext, R.style.goldBadge), jsoData.getJSONObject("badge_counts").getInt("gold"));
		LauncherWidget.appendBadge(ssbBadges, new TextAppearanceSpan(ctxContext, R.style.silverBadge), jsoData.getJSONObject("badge_counts").getInt("silver"));
		LauncherWidget.appendBadge(ssbBadges, new TextAppearanceSpan(ctxContext, R.style.bronzeBadge), jsoData.getJSONObject("badge_counts").getInt("bronze"));

		remView.setTextViewText(R.id.creds, ssbBadges);
		remView.setTextViewText(R.id.reputation, jsoData.getString("reputation"));

		final String strWebpage = "http://stackoverflow.com/users/" + jsoData.getString("user_id");
		final PendingIntent pitOptions = WidgetHelpers.getIntent(ctxContext, WidgetSettings.class, intInstance);
		final PendingIntent pitWebpage = WidgetHelpers.getIntent(ctxContext, strWebpage);
		remView.setTextViewText(R.id.last_update, DateFormat.format("kk:mm", new Date()));
		remView.setOnClickPendingIntent(R.id.settings_button, pitOptions);
		remView.setOnClickPendingIntent(R.id.widget_icon, pitWebpage);

		AppWidgetManager.getInstance(ctxContext).updateAppWidget(intInstance, remView);

	}

}