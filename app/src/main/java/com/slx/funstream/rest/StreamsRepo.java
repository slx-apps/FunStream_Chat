package com.slx.funstream.rest;


import com.slx.funstream.rest.model.Stream;
import com.slx.funstream.rest.model.Category;
import com.slx.funstream.rest.model.CategoryOptions;
import com.slx.funstream.rest.model.CategoryRequest;
import com.slx.funstream.rest.model.ContentRequest;
import com.slx.funstream.rest.services.FunstreamApi;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class StreamsRepo {
    private static final String STREAM = "stream";
    private static final String TYPE = "all";
    private static final String CATEGORY_TOP = "Top";

    private FunstreamApi api;

    public StreamsRepo(FunstreamApi api) {
        this.api = api;
    }

    public Single<List<Stream>> getAllStreams(Category category) {
        return api.getStreams(new ContentRequest(STREAM, TYPE, category));
    }

    public Single<List<Category>> getAllCategories() {
        return api.getCategoriesWithSubs(
                new CategoryRequest(APIUtils.CONTENT_STREAM,
                new CategoryOptions(true)))
                .map(category -> {
                    List<Category> cats = new ArrayList<>();
                    // Костыль пустого имени топ категории id == 1
                    if (category.getId() == 1) category.setName(CATEGORY_TOP);
                    cats.add(category);
                    if (category.getSubCategories() != null && category.getSubCategories().length > 0) {
                        Category[] catsArray = category.getSubCategories();
                        for (int i = 0; i < category.getSubCategories().length; i++) {
                            cats.add(catsArray[i]);
                        }
                    }
                    return cats;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
