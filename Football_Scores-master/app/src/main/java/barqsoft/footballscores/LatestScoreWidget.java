package barqsoft.footballscores;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import barqsoft.footballscores.service.FetchService;

/**
 * Implementation of App Widget functionality.
 */
public class LatestScoreWidget extends AppWidgetProvider {

    public static final String TAG = "LatestScoreWidget";
    public static final String SERVICE_CONTEXT = "LatestScoreWidget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //For multiple widgets loop through the Widget id's array
        Intent dataService = new Intent(context, FetchService.class);
        dataService.putExtra(Intent.EXTRA_TEXT, SERVICE_CONTEXT);
        dataService.putExtra(Intent.EXTRA_UID, appWidgetIds);
        context.startService(dataService);
    }

    @Override
    public void onEnabled(Context context) {
        /* nothing yet */
    }

    @Override
    public void onDisabled(Context context) {
        /* nothing yet */
    }

    public static void updateAppWidget(Context context, int[] appWidgetIds, Cursor cursor) {
        for(int i = 0; i < appWidgetIds.length; i++) {
            CharSequence widgetText = context.getString(R.string.appwidget_text);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.latest_scoreswidget);

            int dCol = cursor.getColumnIndex(DatabaseContract.scores_table.DATE_COL);
            int tCol = cursor.getColumnIndex(DatabaseContract.scores_table.TIME_COL);
            int awayCol = cursor.getColumnIndex(DatabaseContract.scores_table.AWAY_COL);
            int awayGoalCol = cursor.getColumnIndex(DatabaseContract.scores_table.AWAY_GOALS_COL);
            int homeCol = cursor.getColumnIndex(DatabaseContract.scores_table.HOME_COL);
            int homeGoalCol = cursor.getColumnIndex(DatabaseContract.scores_table.HOME_GOALS_COL);
            if(cursor.getCount() > 0) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US);
                Date current = new Date();
                Date closesDate = null;
                Match match = null;
                while (cursor.moveToNext()) {
                    String date = cursor.getString(dCol);
                    String time = cursor.getString(tCol);
                    String homeTeam = cursor.getString(homeCol);
                    String awayTeam = cursor.getString(awayCol);
                    String homeTeamScore = cursor.getString(homeGoalCol);
                    String awayTeamScore = cursor.getString(awayGoalCol);
                    Date matchDate;
                    try {
                        matchDate = format.parse(date + "T" + time);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        continue;
                    }
                    if (closesDate == null) {
                        closesDate = matchDate;
                        match = new Match(awayTeam, homeTeam, awayTeamScore, homeTeamScore);
                    } else {
                        if (matchDate.before(current) && matchDate.after(closesDate) && !homeTeamScore.equals("-1")) {
                            closesDate = matchDate;
                            match = new Match(awayTeam, homeTeam, awayTeamScore, homeTeamScore);
                        }
                    }
                }
                if (match != null && !match.hTeamGoal.equals("-1")) {
                    views.setTextViewText(R.id.away_text, match.aTeam);
                    views.setTextViewText(R.id.score_text, String.format("%s1 - %s2",match.aTeamGoal,match.hTeamGoal));
                    views.setTextViewText(R.id.home_text, match.hTeam);
                    views.setViewVisibility(R.id.no_scores_text,View.GONE);
                } else {
                    views.setViewVisibility(R.id.no_scores_text, View.VISIBLE);
                }
                Intent intent = new Intent(context, MainActivity.class);
                PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
                views.setOnClickPendingIntent(R.id.widet_main_view, pi);
                AppWidgetManager.getInstance(context).updateAppWidget(appWidgetIds[i], views);
            }
        }
    }

    public static class Match {
        private final String aTeam;
        private final String hTeam;
        private final String aTeamGoal;
        private final String hTeamGoal;

        public Match(String awayTeam, String homeTeam, String awayTeamScore, String homeTeamScore) {
            this.aTeam = awayTeam;
            this.hTeam = homeTeam;
            this.aTeamGoal = awayTeamScore;
            this.hTeamGoal = homeTeamScore;
        }
    }
}

