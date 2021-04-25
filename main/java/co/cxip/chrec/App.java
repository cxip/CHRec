package co.cxip.chrec;

import android.app.Application;
import android.content.Context;

import me.grishka.appkit.utils.V;
import co.cxip.chrec.api.ClubhouseSession;

public class App extends Application{
	public static Context applicationContext;

	@Override
	public void onCreate(){
		super.onCreate();
		applicationContext=getApplicationContext();
		V.setApplicationContext(applicationContext);
		ClubhouseSession.load();
	}
}
