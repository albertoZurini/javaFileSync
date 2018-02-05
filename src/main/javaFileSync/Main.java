package javaFileSync;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Main
{
	public static void main(String[] args) throws InterruptedException, IOException
	{
		while(true) {
			try {
				if(javaFileSync.Core.Sync()) {
					try {
						TimeUnit.SECONDS.sleep(5);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch(NullPointerException error) {
				System.out.println("Device not connected. Sleeping for 5 seconds...");
				TimeUnit.SECONDS.sleep(5);
			}
		}
	}
}