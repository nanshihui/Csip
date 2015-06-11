/**
 * Copyright (C) 2010-2012 Regis Montoya (aka r3gis - www.r3gis.fr)
 * This file is part of CSipSimple.
 *
 *  CSipSimple is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  If you own a pjsip commercial license you can also redistribute it
 *  and/or modify it under the terms of the GNU Lesser General Public License
 *  as an android library.
 *
 *  CSipSimple is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.csip.view;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.csip.bean.CallLogBean;
import com.csipsimple.R;
import com.csipsimple.api.SipManager;
import com.csipsimple.api.SipProfile;
import com.csipsimple.api.SipUri;
import com.csipsimple.models.CallerInfo;
import com.csipsimple.ui.calllog.CallDetailHistoryAdapter;
import com.csipsimple.ui.calllog.PhoneCallDetails;
import com.csipsimple.ui.calllog.PhoneCallDetailsHelper;
import com.csipsimple.utils.Base64.InputStream;
import com.csipsimple.utils.ContactsAsyncHelper;
import com.csipsimple.utils.Log;
import com.csipsimple.utils.contacts.ContactsWrapper;
import com.csipsimple.widgets.AccountChooserButton;

/**
 * Displays the details of a specific call log entry.
 * <p>
 * This activity can be either started with the URI of a single call log entry,
 * or with the {@link #EXTRA_CALL_LOG_IDS} extra to specify a group of call log
 * entries.
 */
public class LogDetailsFragment extends SherlockFragment {

    private static final String THIS_FILE = "LogDetailsFragment";
    /** A long array extra containing ids of call log entries to display. */
    public static final String EXTRA_CALL_LOG_IDS = "EXTRA_CALL_LOG_IDS";

    private PhoneCallDetailsHelper mPhoneCallDetailsHelper;
    private TextView mHeaderTextView;
    private View mHeaderOverlayView;
    private ImageView mMainActionView, mContactBackgroundView;
    private ImageButton mMainActionPushLayerView;
    private AccountChooserButton mAccountChooserButton;
    private  CallLogBean logbean;
    /* package */Resources mResources;
    private LayoutInflater mInflater;

    public interface OnquitListener {
        public void onquit();

        public void onShowCallLog(long[] callIds);
    }

    private OnquitListener quitListener;

    public void setOnQuitListener(OnquitListener l) {
        quitListener = l;
    }

    private static final String[] CALL_LOG_PROJECTION = new String[] {
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            SipManager.CALLLOG_PROFILE_ID_FIELD,
            SipManager.CALLLOG_STATUS_CODE_FIELD,
            SipManager.CALLLOG_STATUS_TEXT_FIELD
    };

    private static final int DATE_COLUMN_INDEX = 0;
    private static final int DURATION_COLUMN_INDEX = 1;
    private static final int NUMBER_COLUMN_INDEX = 2;
    private static final int CALL_TYPE_COLUMN_INDEX = 3;
    private static final int PROFILE_ID_COLUMN_INDEX = 4;
    private static final int STATUS_CODE_COLUMN_INDEX = 5;
    private static final int STATUS_TEXT_COLUMN_INDEX = 6;


    /**
     * Action when the call icon is pressed
     */
    private final View.OnClickListener mPrimaryActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String nbr = (String) view.getTag();
            if (!TextUtils.isEmpty(nbr)) {
                SipProfile acc = mAccountChooserButton.getSelectedAccount();
                Intent it = new Intent(Intent.ACTION_CALL);
                it.setData(SipUri.forgeSipUri(SipManager.PROTOCOL_CSIP, nbr));
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                it.putExtra(SipProfile.FIELD_ACC_ID, acc.id);
                startActivity(it);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.call_detail, container, false);
        mResources = getResources();
        mInflater = inflater;

        final Intent mainActionIntent;
        int mainActionIcon;
        final String mainActionDescription;
        if(logbean==null)
        	getCallLogEntryUris();
        mPhoneCallDetailsHelper = new PhoneCallDetailsHelper(mResources);
        mHeaderTextView = (TextView) v.findViewById(R.id.header_text);
        mHeaderOverlayView = v.findViewById(R.id.photo_text_bar);
        mMainActionView = (ImageView) v.findViewById(R.id.main_action);
        mMainActionPushLayerView = (ImageButton) v.findViewById(R.id.main_action_push_layer);
        mContactBackgroundView = (ImageView) v.findViewById(R.id.contact_background);
        mAccountChooserButton = (AccountChooserButton) v.findViewById(R.id.call_choose_account);
        if(logbean.iscontact())
        {
        	
        	mHeaderTextView.setText(logbean.getName());
        //	  mainActionIntent = new Intent(Intent.ACTION_VIEW, contactUri);
              mainActionIcon = R.drawable.ic_contacts_holo_dark;
           //   mainActionDescription = nameOrNumber.toString();//联系人名字
        }else
        {
        	mHeaderTextView.setText(logbean.getNumber());
        }
        final Uri lookupUri;
        final Uri photoUri = null;
        Long a=  getPhoneContactsID(this.getActivity(),logbean.getName());
        final CharSequence nameOrNumber;
        if (!TextUtils.isEmpty(logbean.getName())) {
            nameOrNumber = logbean.getName();
        } else {
            nameOrNumber = logbean.getNumber();
        }
        Uri personUri = null;
        if (a!= null) {
        	
			   personUri= ContentUris.withAppendedId(Contacts.CONTENT_URI,Long.parseLong(a.toString()));

            mainActionIntent = new Intent(Intent.ACTION_VIEW,personUri);
            
            mainActionIcon = R.drawable.ic_contacts_holo_dark;
            mainActionDescription = nameOrNumber.toString();
        } else if(!TextUtils.isEmpty(logbean.getNumber())){
            mainActionIntent = ContactsWrapper.getInstance().getAddContactIntent((String) logbean.getName(), (String) logbean.getNumber());
            mainActionIcon = R.drawable.ic_add_contact_holo_dark;
            mainActionDescription = getString(R.string.menu_add_to_contacts);
            if(TextUtils.isEmpty(logbean.getName())) {
                mHeaderTextView.setText(R.string.menu_add_to_contacts);
            }else {
                mHeaderTextView.setText(getString(R.string.menu_add_address_to_contacts,logbean.getName()));
            }
        }else {
            // If we cannot call the number, when we probably cannot add it as a
            // contact either.
            // This is usually the case of private, unknown, or payphone
            // numbers.
            mainActionIntent = null;
            mainActionIcon = 0;
            mainActionDescription = null;
        }

        if (mainActionIntent == null) {
            mMainActionView.setVisibility(View.INVISIBLE);
            mMainActionPushLayerView.setVisibility(View.GONE);
            mHeaderTextView.setVisibility(View.INVISIBLE);
            mHeaderOverlayView.setVisibility(View.INVISIBLE);
        } else {
            mMainActionView.setVisibility(View.VISIBLE);
            mMainActionView.setImageResource(mainActionIcon);
            mMainActionPushLayerView.setVisibility(View.VISIBLE);
            mMainActionPushLayerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(mainActionIntent);
                }
            });
            mMainActionPushLayerView.setContentDescription(mainActionDescription);
            mHeaderTextView.setVisibility(View.VISIBLE);
            mHeaderOverlayView.setVisibility(View.VISIBLE);
        }
        CharSequence displayNumber;
        if (!TextUtils.isEmpty( logbean.getNumber())) {
            displayNumber = SipUri.getCanonicalSipContact( logbean.getNumber(), false);
        } else {
            displayNumber = mResources.getString(R.string.unknown);
        }

        String callText = getString(R.string.description_call, displayNumber);
        configureCallButton(v,callText,null, logbean.getNumber());

        ListView historyList = (ListView) v.findViewById(R.id.history);
        historyList.setAdapter(new CallDetailHistoryAdapter(getActivity(), mInflater, logbean));

        
        loadContactPhotos(photoUri, personUri);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
      
    }
    
    
    private static final String[] PHONES_PROJECTION = new String[] {  
        Phone.DISPLAY_NAME, Phone.NUMBER, Photo.PHOTO_ID,Phone.CONTACT_ID }; 
    private Long getPhoneContactsID(Context mContext,String number) {  
    	ContentResolver resolver = mContext.getContentResolver();  
    	 
    	// 获取手机联系人  
    	Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,PHONES_PROJECTION,Contacts.DISPLAY_NAME+"= ?", new String[] { 
    			number }, null);  
    	   Long contactid = null;
    	 
    	if (phoneCursor != null) {  
    	    while (phoneCursor.moveToNext()) {  
    	 
    	    //得到手机号码  
    	    String phoneNumber = phoneCursor.getString(1);  
    	    //当手机号码为空的或者为空字段 跳过当前循环  
    	    if (TextUtils.isEmpty(phoneNumber))  
    	        continue;  
    	      
    	    //得到联系人名称  
    	    String contactName = phoneCursor.getString(0);  
    	      
    	    //得到联系人ID  
    	     contactid = phoneCursor.getLong(3);  
    	 
    	    //得到联系人头像ID  
    	    Long photoid = phoneCursor.getLong(2);  
    	     
    	    //得到联系人头像Bitamp  
    	    Bitmap contactPhoto = null;  
    	 
    	    //photoid 大于0 表示联系人有头像 如果没有给此人设置头像则给他一个默认的  
    	   /* if(photoid > 0 ) {  
    	        Uri uri =ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,contactid);  
    	        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(resolver, uri);  
    	        contactPhoto = BitmapFactory.decodeStream(input);  
    	    }else {  
    	        contactPhoto = BitmapFactory.decodeResource(getResources(), R.drawable.contact_photo);  
    	    }  
    	      
    	    mContactsName.add(contactName);  
    	    mContactsNumber.add(phoneNumber);  
    	    mContactsPhonto.add(contactPhoto);  */
    	    }  
    	 
    	    phoneCursor.close();  
    	}  
    	return contactid;
    	   } 
    private void getCallLogEntryUris() {
        String name = getArguments().getString("name");
        String number = getArguments().getString("number");
       int type= getArguments().getInt("type");
        boolean contact = getArguments().getBoolean("contact");
        if(contact)
        {
        	System.out.println(contact);
        }
        String date = getArguments().getString("date");
        long duration = getArguments().getLong("duration");
        CallLogBean clb=new CallLogBean();
        clb.setName(name);
        clb.setNumber(number);
        clb.setiscontact(contact);
        clb.setDate(date);
        clb.setduration(duration);
        clb.setType(type);
        logbean= clb;
    }

    /**
     * Update user interface with details of given call.
     * 
     * @param callUris URIs into {@link CallLog.Calls} of the calls to be
     *            displayed
     */
    private void updateData(Uri ... callUris) {

        final int numCalls = callUris.length;
        if(numCalls == 0) {
            Log.w(THIS_FILE, "No calls logs as parameters");
            return;
        }
        
        PhoneCallDetails[] details = new PhoneCallDetails[numCalls];
        for (int index = 0; index < numCalls; ++index) {
            details[index] = getPhoneCallDetailsForUri(callUris[index]);
        }

     
        PhoneCallDetails firstDetails = details[0];
        final Uri contactUri = firstDetails.contactUri;
        final Uri photoUri = firstDetails.photoUri;

        // Set the details header, based on the first phone call.
        mPhoneCallDetailsHelper.setCallDetailsHeader(mHeaderTextView, firstDetails);

        final Intent mainActionIntent;
        final int mainActionIcon;
        final String mainActionDescription;

        final CharSequence nameOrNumber;
        if (!TextUtils.isEmpty(firstDetails.name)) {
            nameOrNumber = firstDetails.name;
        } else {
            nameOrNumber = firstDetails.number;
        }

        if (contactUri != null) {
            mainActionIntent = new Intent(Intent.ACTION_VIEW, contactUri);
            mainActionIcon = R.drawable.ic_contacts_holo_dark;
            mainActionDescription = nameOrNumber.toString();
        } else if(!TextUtils.isEmpty(firstDetails.number)){
            mainActionIntent = ContactsWrapper.getInstance().getAddContactIntent((String) firstDetails.name, (String) firstDetails.number);
            mainActionIcon = R.drawable.ic_add_contact_holo_dark;
            mainActionDescription = getString(R.string.menu_add_to_contacts);
            if(TextUtils.isEmpty(firstDetails.name)) {
                mHeaderTextView.setText(R.string.menu_add_to_contacts);
            }else {
                mHeaderTextView.setText(getString(R.string.menu_add_address_to_contacts, firstDetails.name));
            }
        }else {
            // If we cannot call the number, when we probably cannot add it as a
            // contact either.
            // This is usually the case of private, unknown, or payphone
            // numbers.
            mainActionIntent = null;
            mainActionIcon = 0;
            mainActionDescription = null;
        }

        if (mainActionIntent == null) {
            mMainActionView.setVisibility(View.INVISIBLE);
            mMainActionPushLayerView.setVisibility(View.GONE);
            mHeaderTextView.setVisibility(View.INVISIBLE);
            mHeaderOverlayView.setVisibility(View.INVISIBLE);
        } else {
            mMainActionView.setVisibility(View.VISIBLE);
            mMainActionView.setImageResource(mainActionIcon);
            mMainActionPushLayerView.setVisibility(View.VISIBLE);
            mMainActionPushLayerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(mainActionIntent);
                }
            });
            mMainActionPushLayerView.setContentDescription(mainActionDescription);
            mHeaderTextView.setVisibility(View.VISIBLE);
            mHeaderOverlayView.setVisibility(View.VISIBLE);
        }

        // This action allows to call the number that places the call.
        // final CharSequence displayNumber = firstDetails.formattedNumber;
        CharSequence displayNumber;
        if (!TextUtils.isEmpty(firstDetails.number)) {
            displayNumber = SipUri.getCanonicalSipContact(firstDetails.number.toString(), false);
        } else {
            displayNumber = mResources.getString(R.string.unknown);
        }

        String callText = getString(R.string.description_call, displayNumber);
        configureCallButton(new View(this.getActivity()),callText, firstDetails.numberLabel, firstDetails.number);

        ListView historyList = (ListView) getView().findViewById(R.id.history);
        historyList.setAdapter(new CallDetailHistoryAdapter(getActivity(), mInflater, details));

        mAccountChooserButton.setTargetAccount(firstDetails.accountId);
        loadContactPhotos(photoUri, contactUri);
    }

    /** Return the phone call details for a given call log URI. */
    private PhoneCallDetails getPhoneCallDetailsForUri(Uri callUri) {
        ContentResolver resolver = getActivity().getContentResolver();
        Cursor callCursor = resolver.query(callUri, CALL_LOG_PROJECTION, null, null, null);
        try {
            if (callCursor == null || !callCursor.moveToFirst()) {
                throw new IllegalArgumentException("Cannot find content: " + callUri);
            }

            // Read call log specifics.
            String number = callCursor.getString(NUMBER_COLUMN_INDEX);
            long date = callCursor.getLong(DATE_COLUMN_INDEX);
            long duration = callCursor.getLong(DURATION_COLUMN_INDEX);
            int callType = callCursor.getInt(CALL_TYPE_COLUMN_INDEX);
            Long accountId = callCursor.getLong(PROFILE_ID_COLUMN_INDEX);
            int statusCode = callCursor.getInt(STATUS_CODE_COLUMN_INDEX);
            String statusText = callCursor.getString(STATUS_TEXT_COLUMN_INDEX);
                
            // Formatted phone number.
            final CharSequence formattedNumber;
            // Read contact specifics.
            final CharSequence nameText;
            final int numberType;
            final CharSequence numberLabel;
            final Uri photoUri;
            final Uri lookupUri;
            // If this is not a regular number, there is no point in looking it
            // up in the contacts.
            CallerInfo info = CallerInfo.getCallerInfoFromSipUri(getActivity(), number);
            if (info == null) {
                formattedNumber = number;
                nameText = "";
                numberType = 0;
                numberLabel = "";
                photoUri = null;
                lookupUri = null;
            } else {
                formattedNumber = info.phoneNumber;
                nameText = info.name;
                numberType = info.numberType;
                numberLabel = info.phoneLabel;
                photoUri = info.photoUri;
                lookupUri = info.contactContentUri;
            }
            return new PhoneCallDetails(number, formattedNumber,
                    new int[] {
                            callType
                    }, date, duration,
                    accountId, statusCode, statusText,
                    nameText, numberType, numberLabel, lookupUri, photoUri);
        } finally {
            if (callCursor != null) {
                callCursor.close();
            }
        }
    }

    /** Load the contact photos and places them in the corresponding views. */
    private void loadContactPhotos(Uri photoUri, Uri contactUri) {
        int defaultLargePhoto = R.drawable.ic_contact_picture_180_holo_dark;

        if (photoUri != null) {
            // Android 4.0 - high res photo
            ContactsAsyncHelper.updateImageViewWithContactPhotoAsync(getActivity(),
                    mContactBackgroundView,
                    photoUri, defaultLargePhoto);
        } else if (contactUri != null) {
            CallerInfo person = new CallerInfo();
            person.contactContentUri = contactUri;
            // Android < 4.0 - low res picture
            ContactsAsyncHelper.updateImageViewWithContactPhotoAsync(getActivity(),
                    mContactBackgroundView, person, defaultLargePhoto);
        } else {
            // Default picture
            mContactBackgroundView.setImageResource(defaultLargePhoto);
        }
    }

    /** Configures the call button area using the given entry. */
    private void configureCallButton(View v,String callText, CharSequence nbrLabel, CharSequence number) {
        View convertView = v.findViewById(R.id.call_and_sms);
        convertView.setVisibility(TextUtils.isEmpty(number) ? View.GONE : View.VISIBLE);

        TextView text = (TextView) convertView.findViewById(R.id.call_and_sms_text);

        View mainAction = convertView.findViewById(R.id.call_and_sms_main_action);
        mainAction.setOnClickListener(mPrimaryActionListener);
        mainAction.setContentDescription(callText);
        if(TextUtils.isEmpty(number)) {
            number = "";
        }
        mainAction.setTag(SipUri.getCanonicalSipContact(number.toString(), false));
        text.setText(callText);

        TextView label = (TextView) convertView.findViewById(R.id.call_and_sms_label);
        if (TextUtils.isEmpty(nbrLabel)) {
            label.setVisibility(View.GONE);
        } else {
            label.setText(nbrLabel);
            label.setVisibility(View.VISIBLE);
        }
    }
/*
    public void onMenuRemoveFromCallLog(MenuItem menuItem) {
        final StringBuilder callIds = new StringBuilder();
        for (Uri callUri : getCallLogEntryUris()) {
            if (callIds.length() != 0) {
                callIds.append(",");
            }
            callIds.append(ContentUris.parseId(callUri));
        }

        getActivity().getContentResolver().delete(SipManager.CALLLOG_URI,
                Calls._ID + " IN (" + callIds + ")", null);
        if (quitListener != null) {
            quitListener.onquit();
        }
    }
*/
}
