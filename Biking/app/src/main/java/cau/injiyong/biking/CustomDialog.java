package cau.injiyong.biking;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
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

    public CustomDialog() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.custom_dialog, container, false);
        final RatingBar rb = (RatingBar)view.findViewById(R.id.ratingBarInficator);
        rb.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

            }
        });
        final Button okButton = (Button)view.findViewById(R.id.okButton);
        final Button cancelButton = (Button)view.findViewById(R.id.cancelButton);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    public void onClick(View v){
        dismiss();
    }

//    // 호출할 다이얼로그 함수를 정의한다.
//    public void callFunction() {
//
//        // 커스텀 다이얼로그를 정의하기위해 Dialog클래스를 생성한다.
//        final Dialog dlg = new Dialog(context);
//
//        // 액티비티의 타이틀바를 숨긴다.
//        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
//
//        // 커스텀 다이얼로그의 레이아웃을 설정한다.
//        dlg.setContentView(R.layout.custom_dialog);
//
//        // 커스텀 다이얼로그를 노출한다.
//        dlg.show();
//
//        // 커스텀 다이얼로그의 각 위젯들을 정의한다.
//
//        final RatingBar ratingBarInficator = (RatingBar) dlg.findViewById(R.id.ratingBarInficator);
//        final Button okButton = (Button) dlg.findViewById(R.id.okButton);
//        final Button cancelButton = (Button) dlg.findViewById(R.id.cancelButton);
//
//        okButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//                // 커스텀 다이얼로그를 종료한다.
//                dlg.dismiss();
//            }
//        });
//        cancelButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //Toast.makeText(context, "취소 했습니다.", Toast.LENGTH_SHORT).show();
//
//                // 커스텀 다이얼로그를 종료한다.
//                dlg.dismiss();
//            }
//        });
//    }
}


