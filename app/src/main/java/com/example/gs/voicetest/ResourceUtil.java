package com.example.gs.voicetest;

import android.content.Context;

public class ResourceUtil {


	
	public static int getRawId(Context paramContext, String paramString) {
		return paramContext.getResources().getIdentifier(paramString, "raw",
				paramContext.getPackageName());
	}

}