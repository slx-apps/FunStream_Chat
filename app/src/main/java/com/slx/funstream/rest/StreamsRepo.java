package com.slx.funstream.rest;


import com.slx.funstream.model.Stream;
import com.slx.funstream.rest.model.Category;
import com.slx.funstream.rest.model.CategoryOptions;
import com.slx.funstream.rest.model.CategoryRequest;
import com.slx.funstream.rest.model.ContentRequest;
import com.slx.funstream.rest.services.FunstreamApi;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class StreamsRepo {
    private static final String STREAM = "stream";
    private static final String TYPE = "all";
    private static final String CATEGORY_TOP = "Top";


    private FunstreamApi api;

    public StreamsRepo(FunstreamApi api) {
        this.api = api;
    }

    public Observable<List<Stream>> getAllStreams(Category category) {
        return api.getStreams(new ContentRequest(STREAM, TYPE, category));
    }

    public Observable<List<Category>> getAllCategories() {
        return api.getCategoriesWithSubs(new CategoryRequest(APIUtils.CONTENT_STREAM,
                new CategoryOptions(true)))
                .map(cat -> {
                    List<Category> cats = new ArrayList<>();
                    // Костыль пустого имени топ категории id == 1
                    if (cat.getId() == 1) cat.setName(CATEGORY_TOP);
                    cats.add(cat);
                    if (cat.getSubCategories() != null && cat.getSubCategories().length > 0) {
                        Category[] catsArray = cat.getSubCategories();
                        for (int i = 0; i < cat.getSubCategories().length; i++) {
                            cats.add(catsArray[i]);
                        }
                    }
                    return cats;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
                //.compose(bindToLifecycle())
    }
}
