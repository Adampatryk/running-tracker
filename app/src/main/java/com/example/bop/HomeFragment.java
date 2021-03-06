package com.example.bop;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

//This is the starting fragment and displays the list of tracked sessions that can be further explored
public class HomeFragment extends Fragment {

	SimpleCursorAdapter simpleCursorAdapter;
	private ListView sessionsListView;
	private final static int DELETE_REQUEST_CODE = 0;
	View v;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		v = inflater.inflate(R.layout.fragment_home, container, false);
		sessionsListView = v.findViewById(R.id.list_view_sessions);
		setUpListView();

		return v;
	}

	private void setUpListView() {
		Cursor c;

		//Data that will be displayed on listview rows about each session
		String[] columns = new String[]{
				BopProviderContract.ACTIVITY_TITLE,
				BopProviderContract.ACTIVITY_DISTANCE,
				BopProviderContract.ACTIVITY_DATETIME,
				BopProviderContract.ACTIVITY_DURATION,
				BopProviderContract.ACTIVITY_ID,
				BopProviderContract.ACTIVITY_DESCRIPTION,
				BopProviderContract.ACTIVITY_ACTIVITY_TYPE,
				BopProviderContract.ACTIVITY_IMAGE
		};

		//Query for all the sessions, date descending so the newest sessions are on the top
		c = getContext().getContentResolver().query(BopProviderContract.ACTIVITY_URI, columns,
				null, null, BopProviderContract.ACTIVITY_DATETIME + " DESC");

		//If there are no tracked sessions display the "No tracked sessions yet" message
		if (c.getCount() == 0) {
			v.findViewById(R.id.text_view_no_sessions).setVisibility(View.VISIBLE);
		}

		//Setup and attach the custom CursorAdapter to map the data onto the listview rows from the database
		SessionCursorAdapter sessionCursorAdapter = new SessionCursorAdapter(getContext(), c);
		sessionsListView.setAdapter(sessionCursorAdapter);

		//Setup the onclick listener to allow user to view more detail about their listed sessions
		final Intent goToSessionDetailsActivity = new Intent(getContext(), SessionDetailsActivity.class);
		sessionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				int id = Integer.parseInt(((TextView) view.findViewById(R.id.text_view_row_id)).getText().toString());

				goToSessionDetailsActivity.putExtra("id", "" + id);
				//Starting for result to be able to check if it was deleted so the listview can be refreshed
				startActivityForResult(goToSessionDetailsActivity, 0);
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		//If a session was deleted, remake the listview, requerying for data (refresh)
		if (requestCode == DELETE_REQUEST_CODE) {
			if (resultCode == 1) {
				setUpListView();
			}
		}
	}
}
