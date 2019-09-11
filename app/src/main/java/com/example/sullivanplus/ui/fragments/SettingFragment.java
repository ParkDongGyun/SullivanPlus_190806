package com.example.sullivanplus.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import androidx.annotation.Nullable;

import com.example.sullivanplus.R;
import com.example.sullivanplus.db.SullivanResourceData;
import com.example.sullivanplus.ui.activities.SettingActivity;

import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

public class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener
{

    SettingActivity settingActivity;
    SharedPreferences sharedPreferences;

    private PreferenceScreen screen;

    private static final String ResultVoice = "voice_guide";
    private static final String ModeSet = "mode_set";
    private static final String MenuSet = "menu_set";
    private static final String TextRecog = "textcheck";
    private static final String FaceRecog = "facecheck";
    private static final String DisplayLight = "displayBrighten";
    private static final String MessageTime = "message_time";

    private SwitchPreference mResultVoice;
    private ListPreference mModeset;
    private MultiSelectListPreference mMenuSet;
    private SwitchPreference mTextRecog;
    private SwitchPreference mFaceRecog;
    private SwitchPreference mDisplayLight;
    private EditTextPreference mMessageTime;

    SharedPreferences.Editor editor;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //SharedPreferences sharedPreferences = settingActivity.getSharedPreferences(SullivanResourceData.setting_data, MODE_PRIVATE);
        editor = SullivanResourceData.sharedPreferences.edit();

        getPreferenceManager().setSharedPreferencesName(SullivanResourceData.setting_data);
        getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);

        addPreferencesFromResource(R.xml.settings_perference);

        screen = getPreferenceScreen();

        mResultVoice = (SwitchPreference) screen.findPreference(ResultVoice);

        mResultVoice.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SwitchPreference switchPreference = (SwitchPreference)preference;
                return true;
            }
        });
        mResultVoice.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()  {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                editor.putBoolean(preference.getKey(),(Boolean) newValue);

                return true;
            }
        });

        mModeset = (ListPreference) screen.findPreference(ModeSet);

        mModeset.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()  {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue((String)newValue);
                mModeset.setSummary(listPreference.getEntries()[index]);

                editor.putString(preference.getKey(),listPreference.getEntries()[index].toString());

                return true;
            }
        });

        mMenuSet = (MultiSelectListPreference) screen.findPreference(MenuSet);

        mMenuSet.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()  {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                editor.putStringSet(preference.getKey(), (Set<String>)newValue);
                return true;
            }
        });

        mTextRecog = (SwitchPreference) screen.findPreference(TextRecog);

        mTextRecog.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()  {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                editor.putBoolean(preference.getKey(),!(Boolean) newValue);
                return true;
            }
        });

        mFaceRecog = (SwitchPreference) screen.findPreference(FaceRecog);

        mFaceRecog.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()  {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                editor.putBoolean(preference.getKey(),!(Boolean) newValue);

                return true;
            }
        });

        mDisplayLight = (SwitchPreference) screen.findPreference(DisplayLight);

        mDisplayLight.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()  {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                editor.putBoolean(preference.getKey(),!(Boolean) newValue);
                SullivanResourceData.SetDisplayBright((Boolean)newValue,settingActivity);
                return true;
            }
        });

        mMessageTime = (EditTextPreference) screen.findPreference(MessageTime);

        /*mMessageTime.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new TextWatcher()
                {
                    @Override
                    public int hashCode() {
                        return super.hashCode();
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        Log.d("setting", "111");
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        Log.d("setting", "2222");
                        if(Integer.parseInt(s.toString())> 30)
                        {
                            s.toString().substring(0,1);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        Log.d("setting", "333");
                    }
                };
                return true;
            }
        });*/
        //mMessageTime.
        mMessageTime.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                editor = SullivanResourceData.sharedPreferences.edit();
                editor.putString(preference.getKey(), (String) newValue);

                String messageValue = SullivanResourceData.sharedPreferences.getString(preference.getKey(), "30");
                mMessageTime.setSummary("메시지를 " + newValue + "초 동안 표시합니다.");

                SullivanResourceData.snackbarTime = Integer.parseInt((String) newValue) * 1000;
                //SullivanResourceData.ShowSnackBar(settingActivity.findViewById(R.id.setting_layout), (String) mMessageTime.getSummary());
                return true;
            }

        });

        updateSummary();
    }

    private void updateSummary()
    {
        mResultVoice.setChecked(SullivanResourceData.sharedPreferences.getBoolean(mResultVoice.getKey(),true));
        mModeset.setValue(SullivanResourceData.sharedPreferences.getString(mModeset.getKey(), "AI 모드"));
        mMenuSet.setValues(SullivanResourceData.sharedPreferences.getStringSet(mMenuSet.getKey(), SullivanResourceData.choiceMode));
        mTextRecog.setChecked(SullivanResourceData.sharedPreferences.getBoolean(mTextRecog.getKey(), true));
        mFaceRecog.setChecked(SullivanResourceData.sharedPreferences.getBoolean(mFaceRecog.getKey(), true));
        mDisplayLight.setChecked(SullivanResourceData.sharedPreferences.getBoolean(mDisplayLight.getKey(), false));
        mMessageTime.setText(SullivanResourceData.sharedPreferences.getString(mMessageTime.getKey(), "30"));

        mModeset.setSummary(mModeset.getValue());
        mMessageTime.setSummary("메시지를 " + SullivanResourceData.sharedPreferences.getString(mMessageTime.getKey(), "30") + "초 동안 표시합니다.");
    }

    @Override
    public  void onDestroy()
    {
        super.onDestroy();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //이 메소드가 호출될떄는 프래그먼트가 엑티비티위에 올라와있는거니깐 getActivity메소드로 엑티비티참조가능
        settingActivity = (SettingActivity) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSummary();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        return false;
    }
}