package name.boyle.chris.permissionsexample;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MainActivity extends Activity {

	final static File TEST_FILE = new File(Environment.getExternalStorageDirectory(), "test.txt");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				readAskingPermissionIfNeeded();
			}
		});
	}

	private void readAskingPermissionIfNeeded() {
		if (!TEST_FILE.isFile()) {
			new AlertDialog.Builder(MainActivity.this).setMessage("This test needs a file at " + TEST_FILE).create().show();
			return;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
				&& checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
				showRationaleThenRequest();
			} else {
				// First time: really I'd like to show the non-obvious rationale here too but
				// I can't distinguish the first time from the ticked-never-ask scenario. :-(
				requestStoragePermission();
			}
		} else {
			reallyReadFromSD();
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void showRationaleThenRequest() {
		new AlertDialog.Builder(MainActivity.this)
				.setMessage("Rationale goes here. In my real use case it is that " +
						"Dropbox or Bluetooth Received Files has launched " +
						"me with VIEW file:///sdcard/foo.sav ; in an ideal " +
						"world they would instead have made a " +
						"content:// URI so I would not need permission.")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						requestStoragePermission();
					}
				})
				.create().show();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void requestStoragePermission() {
		requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
		if (grantResults.length < 1) {
			new AlertDialog.Builder(this).setMessage("Permission dialog cancelled?").create().show();
			return;
		}
		if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			reallyReadFromSD();
		} else {
			new AlertDialog.Builder(this).setMessage("Permission was refused").create().show();
		}
	}

	private void reallyReadFromSD() {
//		if (! TEST_FILE.canRead()) {
//			new AlertDialog.Builder(this).setMessage("File(" + TEST_FILE + ").canRead() is false").create().show();
//			return;
//		}
		Toast.makeText(this, "Trying to read " + TEST_FILE, Toast.LENGTH_LONG).show();
		try (FileInputStream input = new FileInputStream(TEST_FILE)) {
			final int result = input.read();
			new AlertDialog.Builder(this).setMessage("read() on " + TEST_FILE + " worked: " + result).create().show();
		} catch (IOException e) {
			new AlertDialog.Builder(this).setMessage("Exception reading file, see logcat: " + e).create().show();
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		return super.onOptionsItemSelected(item);
	}
}
