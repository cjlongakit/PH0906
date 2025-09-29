package com.example.ph906_spalshscreen.ui.home;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.ph906_spalshscreen.DashboardFragment;
import com.example.ph906_spalshscreen.PrefsHelper;
import com.example.ph906_spalshscreen.R;
import com.example.ph906_spalshscreen.api.ApiCallback;
import com.example.ph906_spalshscreen.api.ApiClient;
import com.example.ph906_spalshscreen.ui.letters.LettersFragment;

import org.json.JSONObject;

public class HomeFragment extends Fragment {

    private ImageView imgProfile;
    private TextView tvUsername;
    private Button btnDashboard, btnLetters;

    private PrefsHelper prefs;
    private ApiClient api;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        imgProfile = v.findViewById(R.id.img_profile);
        tvUsername = v.findViewById(R.id.tv_username);
        btnDashboard = v.findViewById(R.id.btn_dashboard);
        btnLetters = v.findViewById(R.id.btn_letters);

        prefs = new PrefsHelper(requireContext());
        api = new ApiClient(requireContext());

        bindNameFromPrefs();
        bindPhotoFromPrefs(false);
        fetchHeaderFromApiIfNeeded();

        btnDashboard.setOnClickListener(v1 -> {
            if (!isAdded()) return;
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .addToBackStack(null)
                    .commit();
        });
        btnLetters.setOnClickListener(v12 -> {
            if (!isAdded()) return;
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new LettersFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        bindNameFromPrefs();
        bindPhotoFromPrefs(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try { if (imgProfile != null) Glide.with(this).clear(imgProfile); } catch (Exception ignored) {}
        imgProfile = null;
        tvUsername = null;
        btnDashboard = null;
        btnLetters = null;
    }

    private void bindNameFromPrefs() {
        if (!isAdded() || tvUsername == null) return;
        String fullName = prefs.getFullName();
        String first = "";
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] parts = fullName.trim().split("\\s+");
            if (parts.length > 0) first = parts[0];
        }
        if (first.isEmpty()) {
            String ph = prefs.getPh906();
            first = (ph == null || ph.isEmpty()) ? "Student" : "PH906-" + ph.replaceAll("[^0-9]", "");
        }
        tvUsername.setText(first == null ? "" : first);
    }

    private void bindPhotoFromPrefs(boolean bustCacheOnce) {
        if (!isAdded() || imgProfile == null) return;
        String url = prefs.getProfilePhotoUri();
        if (url == null || url.trim().isEmpty()) {
            attemptGuessPhotoFromId(bustCacheOnce);
            return;
        }
        loadWithGlide(url, bustCacheOnce, null);
    }

    private void attemptGuessPhotoFromId(boolean bustCacheOnce) {
        if (!isAdded() || imgProfile == null) return;
        String ph = prefs.getPh906();
        if (ph == null || ph.trim().isEmpty()) {
            safeLoadPlaceholder();
            return;
        }
        String digits = ph.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            safeLoadPlaceholder();
            return;
        }
        String base = "https://hjcdc.swuitapp.com/uploads/profiles/" + digits;
        String[] candidates = new String[]{base + ".jpg", base + ".jpeg", base + ".png"};
        loadWithFallback(candidates, 0, bustCacheOnce);
    }

    private void loadWithFallback(String[] urls, int idx, boolean bustCacheOnce) {
        if (!isAdded() || imgProfile == null) return;
        if (idx >= urls.length) {
            safeLoadPlaceholder();
            return;
        }
        String url = urls[idx];
        loadWithGlide(url, bustCacheOnce, new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                loadWithFallback(urls, idx + 1, bustCacheOnce);
                return true;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                if (prefs.getProfilePhotoUri() == null || prefs.getProfilePhotoUri().isEmpty()) {
                    prefs.saveProfilePhotoUri(urls[idx]);
                }
                return false;
            }
        });
    }

    private void loadWithGlide(String url, boolean bustCacheOnce, @Nullable RequestListener<Drawable> listener) {
        if (!isAdded() || imgProfile == null) return;
        String toLoad = url;
        if (bustCacheOnce) {
            String sep = url.contains("?") ? "&" : "?";
            toLoad = url + sep + "t=" + System.currentTimeMillis();
        }
        try {
            Glide.with(this)
                    .load(toLoad)
                    .placeholder(R.drawable.account_circle)
                    .error(R.drawable.account_circle)
                    .centerCrop()
                    .diskCacheStrategy(bustCacheOnce ? DiskCacheStrategy.NONE : DiskCacheStrategy.AUTOMATIC)
                    .skipMemoryCache(bustCacheOnce)
                    .listener(listener)
                    .into(imgProfile);
        } catch (IllegalStateException ignored) { }
    }

    private void safeLoadPlaceholder() {
        if (!isAdded() || imgProfile == null) return;
        try { Glide.with(this).load(R.drawable.account_circle).into(imgProfile); } catch (IllegalStateException ignored) { }
    }

    private void fetchHeaderFromApiIfNeeded() {
        String savedPhoto = prefs.getProfilePhotoUri();
        String fullName = prefs.getFullName();
        boolean needName = (fullName == null || fullName.trim().isEmpty());
        boolean needPhoto = (savedPhoto == null || savedPhoto.trim().isEmpty());

        if (!needName && !needPhoto) return;

        api.getMyProfile(new ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                if (!isAdded()) return;
                try {
                    JSONObject obj = response;
                    if (response.has("data") && response.opt("data") instanceof JSONObject) {
                        obj = response.getJSONObject("data");
                    }
                    String first = obj.optString("first_name", "").trim();
                    String last = obj.optString("last_name", "").trim();
                    if (needName && (!first.isEmpty() || !last.isEmpty())) {
                        String composed = (first + " " + last).trim();
                        if (tvUsername != null) {
                            tvUsername.post(() -> tvUsername.setText(first.isEmpty() ? composed : first));
                        }
                    }
                    String photoUrl = obj.optString("photo_url", "").trim();
                    if (!photoUrl.isEmpty()) {
                        prefs.saveProfilePhotoUri(photoUrl);
                        if (imgProfile != null) imgProfile.post(() -> bindPhotoFromPrefs(true));
                    } else if (needPhoto) {
                        if (imgProfile != null) imgProfile.post(() -> attemptGuessPhotoFromId(true));
                    }
                } catch (Exception ignore) {}
            }
            @Override
            public void onError(String message) { /* keep existing UI */ }
        });
    }
}