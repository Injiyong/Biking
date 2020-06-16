package cau.injiyong.biking;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

/**
 * Created by Administrator on 2017-08-07.
 */

public class CustomDialog extends DialogFragment {

    private Fragment fragment;
    public float [] starNum;
    int l;
    int a;
    float rating;


    public CustomDialog(int l) {
        this.l=l;
        this.starNum=new float[l];
    }

    public void setIndex(int a){
        this.a = a;
    }

    public void setNum(int a,float sn){
        starNum[a]=sn;
    }

    public float getNum(int a){
        float sc= starNum[a];
        return sc;
    }



    SharedPreferences sc;
    SharedPreferences.Editor editor;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.custom_dialog, container, false);
        final RatingBar rb = (RatingBar)view.findViewById(R.id.ratingBarInficator);
        LayerDrawable stars = (LayerDrawable) rb.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);

//        sc= PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
//        final float ratings = sc.getFloat("numStars"+a, 0f);
//        rb.setRating(3);


        rb.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
//                editor = sc.edit();
//                editor.putFloat("numStars"+a, ratings);
//                editor.commit();
                if(rb.getRating()<0.5){
                    rb.setRating((float)0.5);
                    Toast.makeText(getActivity().getApplicationContext(),"최소 별점은 0.5개 입니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        final Button okButton = (Button)view.findViewById(R.id.okButton);
        final Button cancelButton = (Button)view.findViewById(R.id.cancelButton);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float sn = rb.getRating(); // 별점
                setNum(a,sn);
                // Toast.makeText(getActivity().getApplicationContext(),"별점 :"+sn, Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity().getApplicationContext(),"별점 :"+sn+"//index :"+a, Toast.LENGTH_SHORT).show();
                dismiss();
                rb.setRating(3);
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                for(int i=0;i<l;i++){
//                    Toast.makeText(getActivity().getApplicationContext(),"별점 :"+starNum[i]+"//index :"+i, Toast.LENGTH_SHORT).show();
//                }
                rb.setRating(3);
                dismiss();

            }
        });
        Bundle args = getArguments();
        String value = args.getString("key");

        /*
         * DialogFragment를 종료시키려면? 물론 다이얼로그 바깥쪽을 터치하면 되지만
         * 종료하기 버튼으로도 종료시킬 수 있어야겠죠?
         */
        // 먼저 부모 프래그먼트를 받아옵니다.
        //findFragmentByTag안의 문자열 값은 Fragment1.java에서 있던 문자열과 같아야합니다.
        //dialog.show(getActivity().getSupportFragmentManager(),"tag");
        fragment = getActivity().getSupportFragmentManager().findFragmentByTag("tag");


        // 아래 코드는 버튼 이벤트 안에 넣어야겠죠?
//        if (fragment != null) {
//            DialogFragment dialogFragment = (DialogFragment) fragment;
//            dialogFragment.dismiss();
//        }
        return view;

    }

    public float getRate(int a){
        return starNum[a];
    }


//    public void onClick(View v){
//        dismiss();
//    }


}
