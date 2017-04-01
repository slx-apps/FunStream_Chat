package com.slx.funstream.di;

import com.slx.funstream.auth.UserStore;
import com.slx.funstream.rest.StreamsRepo;
import com.slx.funstream.rest.services.FunstreamApi;
import com.slx.funstream.utils.PrefUtils;
import com.squareup.picasso.Picasso;

public interface DiProvider {
    public FunstreamApi getFunstreamApi();
    public UserStore getUserStore();
    public PrefUtils getPrefUtils();
}
