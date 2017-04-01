/*
 *   Copyright (C) 2015 Alex Neeky
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.slx.funstream.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;

import com.slx.funstream.R;
import com.slx.funstream.adapters.SmileAdapter;
import com.slx.funstream.rest.model.Smile;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * @author Alex Neeky
 *
 * Based on version
 * Hieu Rocker (rockerhieu@gmail.com)
 * Ankush Sachdeva (sankush@yahoo.co.in)
 *
 */

public class SmileGridView {

	public View rootView;
	SmileKeyboard smileKeyboard;
	List<Smile> smiles;

	public SmileGridView(Context context, List<Smile> smilesIcons, SmileKeyboard smilePopup, Picasso picasso) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		this.smileKeyboard = smilePopup;
		rootView = inflater.inflate(R.layout.smile_grid, null);
		GridView gridView = (GridView) rootView.findViewById(R.id.smile_grid_view);

		smiles = smilesIcons;

		SmileAdapter mAdapter = new SmileAdapter(context, smiles, picasso);
		mAdapter.setSmileClickListener(smile -> {
			if (smileKeyboard.onSmileClickListener != null) {
				smileKeyboard.onSmileClickListener.onSmileClicked(smile);
			}
			// Add to recents ?
//				if (mRecents != null) {
//					mRecents.addRecentSmile(rootView.getContext(), smile);
//				}
		});
		gridView.setAdapter(mAdapter);
	}

	public interface OnSmileClickListener {
		void onSmileClicked(Smile smile);
	}

}
