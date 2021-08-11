package com.unipi.marioschr.navscan;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.limerse.slider.ImageCarousel;
import com.limerse.slider.listener.CarouselListener;
import com.limerse.slider.model.CarouselItem;

import java.util.ArrayList;

public class LocationInfoFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_location_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageCarousel carousel = view.findViewById(R.id.carousel);

        carousel.registerLifecycle(getLifecycle());

        ArrayList<CarouselItem> list = new ArrayList<>();
        list.add(new CarouselItem("https://media-cdn.tripadvisor.com/media/photo-c/2560x500/0e/75/73/ae/caption.jpg"));
        list.add(new CarouselItem("https://mycyprusinsider.com/wp-content/uploads/2017/03/Akamas-730x445.jpg"));
        list.add(new CarouselItem("https://cyprus.wiz-guide.com/assets/modules/kat/articles/202103/2916/editor/b_b_campsites_in_cyprus211.jpg"));
        list.add(new CarouselItem("https://www.fmarc.eu/wp-content/uploads/2019/09/Akamas-Peninsula.jpg"));
        list.add(new CarouselItem("https://loveincorporated.blob.core.windows.net/contentimages/main/9ba703df-d90d-4eab-808b-79cdf9d2c4d0-akamas-peninsula-lead.jpg"));
        carousel.setData(list);

        CarouselListener carouselListener = new CarouselListener() {
            @Override
            public void onClick(int i, CarouselItem carouselItem) {
                Dialog customDialog = new Dialog(getContext());
                customDialog.setContentView(R.layout.preview_layout);
                customDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                customDialog.getWindow().setBackgroundDrawableResource(R.drawable.transparent_background);
                PhotoView photoView = customDialog.findViewById(R.id.photo_view);
                Glide.with(requireContext()).load(list.get(i).getImageUrl()).fitCenter().into(photoView);
                ImageButton closeButton = customDialog.findViewById(R.id.buttonCloseImage);
                closeButton.setOnClickListener(l -> customDialog.dismiss());
                customDialog.show();
            }

            @Override
            public void onLongClick(int i, CarouselItem carouselItem) {

            }
            @Override
            public ViewBinding onCreateViewHolder(LayoutInflater layoutInflater, ViewGroup viewGroup) {
                return null;
            }
            @Override
            public void onBindViewHolder(ViewBinding viewBinding, CarouselItem carouselItem, int i) {

            }
        };

        carousel.setCarouselListener(carouselListener);
    }
}