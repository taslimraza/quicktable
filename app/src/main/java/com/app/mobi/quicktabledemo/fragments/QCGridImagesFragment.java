package com.app.mobi.quicktabledemo.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.app.mobi.quicktabledemo.R;
import com.app.mobi.quicktabledemo.adapters.ChatImagesAdapter;
import com.app.mobi.quicktabledemo.adapters.LocationListAdapter;
import com.app.mobi.quicktabledemo.adapters.SortingSpinnerAdapter;
import com.app.mobi.quicktabledemo.modelClasses.ImagesModel;
import com.app.mobi.quicktabledemo.network.VolleySingleton;
import com.app.mobi.quicktabledemo.utils.Config;
import com.app.mobi.quicktabledemo.utils.SpecificMenuSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QCGridImagesFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    private int sortByClickPosition = 0;
    private String[] sortBy;
    private ArrayList<ImagesModel> imagesArrayList;
    public static ArrayList<ImagesModel> searchedImagesList;
    private static boolean isSearched = false;
    private static boolean isLikesSelected = false;
    private static boolean isDateSelected = false;
    private ChatImagesAdapter imagesAdapter;
    private GridView gridView;
    private ImageView progressBar;

    public QCGridImagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_qcgrid_images, container, false);

        gridView = (GridView) view.findViewById(R.id.images);
        progressBar = (ImageView) view.findViewById(R.id.loading);
        final Spinner spinner = (Spinner) view.findViewById(R.id.chat_images_sorting_spinner);
        TextView noImagesText = (TextView) view.findViewById(R.id.no_image_text);
        final EditText imageSearch = (EditText) view.findViewById(R.id.image_search);
        ImageView searchIcon = (ImageView) view.findViewById(R.id.search_image);
        FloatingActionButton infoBtn = (FloatingActionButton) view.findViewById(R.id.info);
        ImageView homeBtn = (ImageView) getActivity().findViewById(R.id.menu_button);
        infoBtn.setOnClickListener(this);

        homeBtn.setVisibility(View.GONE);

        gridView.setOnItemClickListener(this);

        imagesArrayList = new ArrayList<>();
        sortBy = getResources().getStringArray(R.array.chat_images_sorting);

        final SortingSpinnerAdapter adapter = new SortingSpinnerAdapter(getActivity(), R.layout.restaurant_sorting_spinner, sortBy, sortByClickPosition);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        isLikesSelected = true;
                        isDateSelected = false;
                        sortByClickPosition = position;
                        final SortingSpinnerAdapter adapter = new SortingSpinnerAdapter(getActivity(), R.layout.restaurant_sorting_spinner, sortBy, sortByClickPosition);
                        spinner.setAdapter(adapter);
                        sortByLikes();
                        if (isSearched) {
                            imagesAdapter = new ChatImagesAdapter(getActivity(), searchedImagesList, gridView, progressBar);
                            gridView.setAdapter(imagesAdapter);
//                            gridView.setOnItemClickListener(QCGridImagesFragment.this);
                        } else {
                            imagesAdapter = new ChatImagesAdapter(getActivity(), imagesArrayList, gridView, progressBar);
                            gridView.setAdapter(imagesAdapter);
//                            gridView.setOnItemClickListener(QCGridImagesFragment.this);
                        }
                        break;

                    case 2:
                        isLikesSelected = false;
                        isDateSelected = true;
                        sortByClickPosition = position;
                        final SortingSpinnerAdapter adapter1 = new SortingSpinnerAdapter(getActivity(), R.layout.restaurant_sorting_spinner, sortBy, sortByClickPosition);
                        spinner.setAdapter(adapter1);
                        sortByDate();
                        if (isSearched) {
                            imagesAdapter = new ChatImagesAdapter(getActivity(), searchedImagesList, gridView, progressBar);
                            gridView.setAdapter(imagesAdapter);
//                            gridView.setOnItemClickListener(QCGridImagesFragment.this);
                        } else {
                            imagesAdapter = new ChatImagesAdapter(getActivity(), imagesArrayList, gridView, progressBar);
                            gridView.setAdapter(imagesAdapter);
//                            gridView.setOnItemClickListener(QCGridImagesFragment.this);
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                isLikesSelected = false;
                isDateSelected = false;
            }
        });

        getChatImages(gridView, progressBar, noImagesText);

//        switch (sortByClickPosition) {
//
//            case 0:
//                if (isSearched) {
//                    ChatImagesAdapter imagesAdapter = new ChatImagesAdapter(getActivity(), searchedImagesList);
//                    gridView.setAdapter(imagesAdapter);
//                    gridView.setOnItemClickListener(QCGridImagesFragment.this);
//                    progressBar.setVisibility(View.GONE);
//                } else {
//                    getChatImages(gridView, progressBar, noImagesText);
//                }
//                break;
//            case 1:
//                isLikesSelected = true;
//                isDateSelected = false;
//                getChatImages(gridView, progressBar, noImagesText);
//                sortByLikes();
//                if (isSearched) {
//                    imagesAdapter = new ChatImagesAdapter(getActivity(), searchedImagesList);
//                    gridView.setAdapter(imagesAdapter);
//                    gridView.setOnItemClickListener(QCGridImagesFragment.this);
//                } else {
//                    imagesAdapter = new ChatImagesAdapter(getActivity(), imagesArrayList);
//                    gridView.setAdapter(imagesAdapter);
//                    gridView.setOnItemClickListener(QCGridImagesFragment.this);
//                }
//                break;
//
//            case 2:
//                isLikesSelected = false;
//                isDateSelected = true;
//                getChatImages(gridView, progressBar, noImagesText);
//                sortByDate();
//                if (isSearched) {
//                    imagesAdapter = new ChatImagesAdapter(getActivity(), searchedImagesList);
//                    gridView.setAdapter(imagesAdapter);
//                    gridView.setOnItemClickListener(QCGridImagesFragment.this);
//                } else {
//                    imagesAdapter = new ChatImagesAdapter(getActivity(), imagesArrayList);
//                    gridView.setAdapter(imagesAdapter);
//                    gridView.setOnItemClickListener(QCGridImagesFragment.this);
//                }
//                break;
//        }

        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

                if (imageSearch.getText().toString() != null && imageSearch.getText().toString().length() > 2) {
                    isSearched = true;
                    searchedImagesList = new ArrayList<>();
                    for (int i = 0; i < imagesArrayList.size(); i++) {
                        if (imagesArrayList.get(i).getRestaurantName().toLowerCase().contains(imageSearch.getText().toString().trim().toLowerCase()) ||
                                imagesArrayList.get(i).getPatronName().toLowerCase().contains(imageSearch.getText().toString().trim().toLowerCase()) ||
                                imagesArrayList.get(i).getRestaurantCity().toLowerCase().contains(imageSearch.getText().toString().trim().toLowerCase()) ||
                                imagesArrayList.get(i).getRestaurantState().toLowerCase().contains(imageSearch.getText().toString().trim().toLowerCase())) {
                            searchedImagesList.add(imagesArrayList.get(i));
                        }
                    }

                    if (isLikesSelected) {
                        sortByLikes();
                    } else if (isDateSelected) {
                        sortByDate();
                    } else {
                        sortByLikes();
                    }

                    ChatImagesAdapter imagesAdapter = new ChatImagesAdapter(getActivity(), searchedImagesList, gridView, progressBar);
                    gridView.setAdapter(imagesAdapter);
//                    gridView.setOnItemClickListener(QCGridImagesFragment.this);

                } else if (imageSearch.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), "Empty Search Field!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Please enter atleast 3 characters!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        imageSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

                    if (imageSearch.getText().toString() != null && imageSearch.getText().toString().length() > 2) {
                        isSearched = true;
                        searchedImagesList = new ArrayList<>();
                        for (int i = 0; i < imagesArrayList.size(); i++) {
                            if (imagesArrayList.get(i).getRestaurantName().toLowerCase().contains(imageSearch.getText().toString().trim().toLowerCase()) ||
                                    imagesArrayList.get(i).getPatronName().toLowerCase().contains(imageSearch.getText().toString().trim().toLowerCase()) ||
                                    imagesArrayList.get(i).getRestaurantCity().toLowerCase().contains(imageSearch.getText().toString().trim().toLowerCase()) ||
                                    imagesArrayList.get(i).getRestaurantState().toLowerCase().contains(imageSearch.getText().toString().trim().toLowerCase())) {
                                searchedImagesList.add(imagesArrayList.get(i));
                            }
                        }

                        if (isLikesSelected) {
                            sortByLikes();
                        } else if (isDateSelected) {
                            sortByDate();
                        } else {
                            sortByLikes();
                        }

                        ChatImagesAdapter imagesAdapter = new ChatImagesAdapter(getActivity(), searchedImagesList, gridView, progressBar);
                        gridView.setAdapter(imagesAdapter);
//                        gridView.setOnItemClickListener(QCGridImagesFragment.this);

                    } else if (imageSearch.getText().toString().equals("")) {
                        Toast.makeText(getActivity(), "Empty Search Field!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Please enter atleast 3 characters!", Toast.LENGTH_SHORT).show();
                    }

                    return true;
                }

                return false;
            }
        });

        imageSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("") && (start > 0 || before > 0)) {
                    isSearched = false;
                    if (imagesArrayList != null && imagesArrayList.size() > 0) {
                        if (isDateSelected) {
                            sortByDate();
                        } else {
                            sortByLikes();
                        }
                        ChatImagesAdapter imagesAdapter = new ChatImagesAdapter(getActivity(), imagesArrayList, gridView, progressBar);
                        gridView.setAdapter(imagesAdapter);
//                        gridView.setOnItemClickListener(QCGridImagesFragment.this);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isSearched = false;
    }

    private void getChatImages(final GridView gridView, final ImageView progressBar, final TextView noImageText) {
        progressBar.setVisibility(View.VISIBLE);
        SharedPreferences sharedPreference = getActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        Config.SESSION_TOKEN_ID = sharedPreference.getString("session_token_id", null);
        String url = Config.ROOT_URL + "/qt/api/images/";
//        String url = "http://159.203.88.161/qt/core/images";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.i("ChatImages", response.toString());
                        progressBar.setVisibility(View.GONE);

                        imagesArrayList = new ArrayList<>();

                        try {
                            JSONArray image = response.getJSONArray("data");

                            for (int i = 0; i < image.length(); i++) {
                                JSONObject object = image.getJSONObject(i);
                                if(imagesArrayList.size() > 1){
                                    int matchedIndex = 0;
                                    for (matchedIndex=0; matchedIndex<imagesArrayList.size(); matchedIndex++){
                                        if (imagesArrayList.get(matchedIndex).getImagesUrl().equals(object.getString("url"))){
                                            break;
                                        }
                                    }
                                    if (matchedIndex == imagesArrayList.size()) {
                                        ImagesModel imagesModel = new ImagesModel();
                                        imagesModel.setImagesUrl(object.getString("url"));
                                        imagesModel.setRestaurantName(object.getString("restaurant_name"));
                                        imagesModel.setRestaurantCity(object.getString("city"));
                                        imagesModel.setRestaurantState(object.getString("state"));
                                        imagesModel.setUserPhoneNumber(object.getString("phone_number"));
                                        imagesModel.setImageLikes(object.getInt("likes_count"));
                                        imagesModel.setLiked(object.getBoolean("like"));
                                        imagesModel.setImageDate(object.getString("date"));
                                        if (!object.isNull("patron_name")) {
                                            imagesModel.setPatronName(object.getString("patron_name"));
                                        } else {
                                            imagesModel.setPatronName(" ");
                                        }
                                        if (!object.isNull("patron_url")) {
                                            imagesModel.setPatronImage(object.getString("patron_url"));
                                        } else {
                                            imagesModel.setPatronImage(" ");
                                        }
                                        imagesArrayList.add(imagesModel);
                                    }
                                }else {
                                    ImagesModel imagesModel = new ImagesModel();
                                    imagesModel.setImagesUrl(object.getString("url"));
                                    imagesModel.setRestaurantName(object.getString("restaurant_name"));
                                    imagesModel.setRestaurantCity(object.getString("city"));
                                    imagesModel.setRestaurantState(object.getString("state"));
                                    imagesModel.setUserPhoneNumber(object.getString("phone_number"));
                                    imagesModel.setImageLikes(object.getInt("likes_count"));
                                    imagesModel.setLiked(object.getBoolean("like"));
                                    imagesModel.setImageDate(object.getString("date"));
                                    if (!object.isNull("patron_name")) {
                                        imagesModel.setPatronName(object.getString("patron_name"));
                                    } else {
                                        imagesModel.setPatronName(" ");
                                    }
                                    if (!object.isNull("patron_url")) {
                                        imagesModel.setPatronImage(object.getString("patron_url"));
                                    } else {
                                        imagesModel.setPatronImage(" ");
                                    }
                                    imagesArrayList.add(imagesModel);
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (imagesArrayList.size() > 0) {
                            noImageText.setVisibility(View.GONE);
                            gridView.setVisibility(View.VISIBLE);

                            switch (sortByClickPosition) {
                                case 0:
                                    sortByLikes();
                                    if (isSearched) {
                                        imagesAdapter = new ChatImagesAdapter(getActivity(), searchedImagesList, gridView, progressBar);
                                        gridView.setAdapter(imagesAdapter);
//                                        gridView.setOnItemClickListener(QCGridImagesFragment.this);
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        imagesAdapter = new ChatImagesAdapter(getActivity(), imagesArrayList, gridView, progressBar);
                                        gridView.setAdapter(imagesAdapter);
//                                        gridView.setOnItemClickListener(QCGridImagesFragment.this);
                                    }
                                    break;

                                case 1:
                                    isLikesSelected = true;
                                    isDateSelected = false;
                                    sortByLikes();
                                    if (isSearched) {
                                        imagesAdapter = new ChatImagesAdapter(getActivity(), searchedImagesList, gridView, progressBar);
                                        gridView.setAdapter(imagesAdapter);
//                                        gridView.setOnItemClickListener(QCGridImagesFragment.this);
                                    } else {
                                        imagesAdapter = new ChatImagesAdapter(getActivity(), imagesArrayList, gridView, progressBar);
                                        gridView.setAdapter(imagesAdapter);
//                                        gridView.setOnItemClickListener(QCGridImagesFragment.this);
                                    }
                                    break;

                                case 2:
                                    isLikesSelected = false;
                                    isDateSelected = true;
                                    sortByDate();
                                    if (isSearched) {
                                        imagesAdapter = new ChatImagesAdapter(getActivity(), searchedImagesList, gridView, progressBar);
                                        gridView.setAdapter(imagesAdapter);
//                                        gridView.setOnItemClickListener(QCGridImagesFragment.this);
                                    } else {
                                        imagesAdapter = new ChatImagesAdapter(getActivity(), imagesArrayList, gridView, progressBar);
                                        gridView.setAdapter(imagesAdapter);
//                                        gridView.setOnItemClickListener(QCGridImagesFragment.this);
                                    }
                                    break;
                            }

//                            sortByLikes();
//                            ChatImagesAdapter imagesAdapter = new ChatImagesAdapter(getActivity(), imagesArrayList);
//                            gridView.setAdapter(imagesAdapter);
//                            gridView.setOnItemClickListener(QCGridImagesFragment.this);
                        } else {
                            noImageText.setVisibility(View.VISIBLE);
                            gridView.setVisibility(View.GONE);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error != null) {
                    progressBar.setVisibility(View.GONE);
                    if (error instanceof TimeoutError) {
                        Config.internetSlowError(getActivity());
                    } else if (error instanceof NoConnectionError) {
                        Config.internetSlowError(getActivity());
                    } else if (error instanceof ServerError) {
                        Config.serverError(getActivity());
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("QTAuthorization", Config.SESSION_TOKEN_ID);
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                Config.TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        VolleySingleton.getInstance(getActivity()).addToRequestQueue(request);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

//        ImageView likeImage = (ImageView) view.findViewById(R.id.like_image);
//        ImageView image = (ImageView) view.findViewById(R.id.image);

//        likeImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isSearched) {
//                    postLike(searchedImagesList.get(position).isLiked(), searchedImagesList.get(position).getImagesUrl(), position);
//                }else {
//                    postLike(imagesArrayList.get(position).isLiked(), imagesArrayList.get(position).getImagesUrl(), position);
//                }
//            }
//        });

//        image.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getActivity(), "Clicked", Toast.LENGTH_SHORT).show();
//            }
//        });

////        view.findViewById(R.id.like_image).setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                postLike(imagesArrayList.get(position).isLiked(), imagesArrayList.get(position).getImagesUrl(), position);
////            }
////        });
//
//
//        image.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {

                QCImageDetailsFragment qcImageDetailsFragment;
                if (isSearched) {
                    qcImageDetailsFragment = new QCImageDetailsFragment(searchedImagesList, position);
                }else {
                    qcImageDetailsFragment = new QCImageDetailsFragment(imagesArrayList, position);
                }
//                Bundle bundle = new Bundle();

//                if (isSearched) {
//                    bundle.putString("Selected Image", searchedImagesList.get(position).getImagesUrl());
//                    bundle.putString("restaurantName", searchedImagesList.get(position).getRestaurantName());
//                    bundle.putString("restaurantCity", searchedImagesList.get(position).getRestaurantCity());
//                    bundle.putString("restaurantState", searchedImagesList.get(position).getRestaurantState());
//                    bundle.putString("phoneNumber", searchedImagesList.get(position).getUserPhoneNumber());
//                    bundle.putString("patronName", searchedImagesList.get(position).getPatronName());
//                    bundle.putString("patronImage", searchedImagesList.get(position).getPatronImage());
//                    bundle.putInt("likes", searchedImagesList.get(position).getImageLikes());
//                    bundle.putBoolean("isLiked", searchedImagesList.get(position).isLiked());
//                } else {
//                    bundle.putString("Selected Image", imagesArrayList.get(position).getImagesUrl());
//                    bundle.putString("restaurantName", imagesArrayList.get(position).getRestaurantName());
//                    bundle.putString("restaurantCity", imagesArrayList.get(position).getRestaurantCity());
//                    bundle.putString("restaurantState", imagesArrayList.get(position).getRestaurantState());
//                    bundle.putString("phoneNumber", imagesArrayList.get(position).getUserPhoneNumber());
//                    bundle.putString("patronName", imagesArrayList.get(position).getPatronName());
//                    bundle.putString("patronImage", imagesArrayList.get(position).getPatronImage());
//                    bundle.putInt("likes", imagesArrayList.get(position).getImageLikes());
//                    bundle.putBoolean("isLiked", imagesArrayList.get(position).isLiked());
//                }
//
//                qcImageDetailsFragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.QCImagesFrameLayout, qcImageDetailsFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

//            }
//        });

    }

    private void sortByLikes() {

        if (isSearched) {
            for (int i = 0; i < searchedImagesList.size() - 1; i++) {
                for (int j = 0; j < searchedImagesList.size() - 1 - i; j++) {
                    if (searchedImagesList.get(j).getImageLikes() < searchedImagesList.get(j + 1).getImageLikes()) {
                        ImagesModel imagesModel = searchedImagesList.get(j);
                        searchedImagesList.set(j, searchedImagesList.get(j + 1));
                        searchedImagesList.set(j + 1, imagesModel);
                    }
                }
            }
        } else {
            if (imagesArrayList != null && imagesArrayList.size() > 0) {
                for (int i = 0; i < imagesArrayList.size() - 1; i++) {
                    for (int j = 0; j < imagesArrayList.size() - 1 - i; j++) {
                        if (imagesArrayList.get(j).getImageLikes() < imagesArrayList.get(j + 1).getImageLikes()) {
                            ImagesModel imagesModel = imagesArrayList.get(j);
                            imagesArrayList.set(j, imagesArrayList.get(j + 1));
                            imagesArrayList.set(j + 1, imagesModel);
                        }
                    }
                }
            }
        }

    }

    private void sortByDate() {

        if (isSearched) {
            for (int i = 0; i < searchedImagesList.size() - 1; i++) {
                for (int j = 0; j < searchedImagesList.size() - 1 - i; j++) {
                    if (searchedImagesList.get(j).getImageDate().compareToIgnoreCase(searchedImagesList.get(j + 1).getImageDate()) < 0) {
                        ImagesModel imagesModel = searchedImagesList.get(j);
                        searchedImagesList.set(j, searchedImagesList.get(j + 1));
                        searchedImagesList.set(j + 1, imagesModel);
                    }
                }
            }
        } else {
            if (imagesArrayList != null && imagesArrayList.size() > 0) {
                for (int i = 0; i < imagesArrayList.size() - 1; i++) {
                    for (int j = 0; j < imagesArrayList.size() - 1 - i; j++) {
                        if (imagesArrayList.get(j).getImageDate().compareToIgnoreCase(imagesArrayList.get(j + 1).getImageDate()) < 0) {
                            ImagesModel imagesModel = imagesArrayList.get(j);
                            imagesArrayList.set(j, imagesArrayList.get(j + 1));
                            imagesArrayList.set(j + 1, imagesModel);
                        }
                    }
                }
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.info:

                QuickPicsInfoFragment fragment = new QuickPicsInfoFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.QCImagesFrameLayout, fragment)
                        .addToBackStack(null).commit();

                break;
        }
    }

    private void postLike(boolean isLiked, String imageUrl, final int position) {

        progressBar.setVisibility(View.VISIBLE);
        gridView.setVisibility(View.GONE);

        isLiked = !isLiked;
        String url = Config.ROOT_URL + "/qt/api/like/";

        JSONObject object = new JSONObject();
        try {
            object.put("url", imageUrl);
            object.put("like", isLiked);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("LikePost", object.toString());

        final boolean finalIsLiked = isLiked;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("LikedResponse", response.toString());

                        progressBar.setVisibility(View.GONE);
                        gridView.setVisibility(View.VISIBLE);

                        int imageLikes = imagesArrayList.get(position).getImageLikes();

                        if (finalIsLiked) {
                            imagesArrayList.get(position).setLiked(true);
                            imageLikes++;
                            imagesArrayList.get(position).setImageLikes(imageLikes);

                        } else {
                            imagesArrayList.get(position).setLiked(false);
                            imageLikes--;
                            imagesArrayList.get(position).setImageLikes(imageLikes);
                        }
                        imagesAdapter.notifyDataSetChanged();

//                        if (QCGridImagesFragment.searchedImagesList != null && QCGridImagesFragment.searchedImagesList.size() > 0){
//                            for (int i = 0; i < QCGridImagesFragment.searchedImagesList.size(); i++) {
//                                if (QCGridImagesFragment.searchedImagesList.get(i).getImagesUrl().equalsIgnoreCase(imageUrl)) {
//                                    QCGridImagesFragment.searchedImagesList.get(i).setImageLikes(imageLikes);
//                                    QCGridImagesFragment.searchedImagesList.get(i).setLiked(finalIsLiked);
//                                }
//                            }
//                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error != null) {
                    if (error instanceof TimeoutError) {
                        Config.internetSlowError(getActivity());
                    } else if (error instanceof NoConnectionError) {
                        Config.internetSlowError(getActivity());
                    } else if (error instanceof ServerError) {
                        Config.serverError(getActivity());
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("QTAuthorization", Config.SESSION_TOKEN_ID);
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                Config.TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        VolleySingleton.getInstance(getActivity()).addToRequestQueue(request);
    }
}
