

package com.csipsimple.ui.dialpad;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent.CanceledException;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.provider.Contacts.People;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DialerKeyListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.csip.adapter.HomeDialAdapter;
import com.csip.adapter.T9Adapter;
import com.csip.bean.CallLogBean;
import com.csip.util.MyApplication;
import com.csip.view.LogDetailsActivity;
import com.csipsimple.R;
import com.csipsimple.api.ISipService;
import com.csipsimple.api.SipCallSession;
import com.csipsimple.api.SipConfigManager;
import com.csipsimple.api.SipManager;
import com.csipsimple.api.SipProfile;
import com.csipsimple.api.SipUri.ParsedSipContactInfos;
import com.csipsimple.models.Filter;
import com.csipsimple.tool.ShowLabel;
import com.csipsimple.ui.SipHome.ViewPagerVisibilityListener;
import com.csipsimple.ui.calllog.CallLogDetailsActivity;
import com.csipsimple.ui.calllog.CallLogDetailsFragment;
import com.csipsimple.ui.dialpad.DialerLayout.OnAutoCompleteListVisibilityChangedListener;
import com.csipsimple.utils.CallHandlerPlugin;
import com.csipsimple.utils.CallHandlerPlugin.OnLoadListener;
import com.csipsimple.utils.DialingFeedback;
import com.csipsimple.utils.Log;
import com.csipsimple.utils.PreferencesWrapper;

import com.csipsimple.utils.backup.BackupWrapper;
import com.csipsimple.utils.contacts.ContactsSearchAdapter;
import com.csipsimple.widgets.AccountChooserButton;
import com.csipsimple.widgets.AccountChooserButton.OnAccountChangeListener;
import com.csipsimple.widgets.DialerCallBar;
import com.csipsimple.widgets.DialerCallBar.OnDialActionListener;
import com.csipsimple.widgets.Dialpad;
import com.csipsimple.widgets.Dialpad.OnDialKeyListener;

public class DialerFragment extends SherlockFragment implements OnClickListener,
         ViewPagerVisibilityListener,OnLongClickListener {

	
	private AsyncQueryHandler asyncQuery;
	
	private HomeDialAdapter adapter;
	private ListView callLogList;
	
	private List<CallLogBean> list;
	private int fresh=1;
	private LinearLayout bohaopan;

	private Button phone_view;
	private Button delete;
	private Map<Integer, Integer> map =new HashMap<Integer, Integer>();
	private SoundPool spool;
	private AudioManager am = null;
	
	private MyApplication application;
	private ListView listView;
	private T9Adapter t9Adapter;
	private Context context;
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	 private static final String THIS_FILE = "DialerFragment";

	    protected static final int PICKUP_PHONE = 0;

	    //private Drawable digitsBackground, digitsEmptyBackground;
	    private DigitsEditText digits;
	    private String initText = null;
	    //private ImageButton switchTextView;

	    //private View digitDialer;

	    private AccountChooserButton accountChooserButton;
	    private Boolean isDigit = null;
	    /* , isTablet */
	    
	    private DialingFeedback dialFeedback;

    /*
    private final int[] buttonsToAttach = new int[] {
            R.id.switchTextView
    };
    */
	    private PreferencesWrapper prefsWrapper;
	    private AlertDialog missingVoicemailDialog;

	    // Auto completion for text mode
	 //   private ListView autoCompleteList;
	    private ContactsSearchAdapter autoCompleteAdapter;

	     private boolean mDualPane;

	    private DialerAutocompleteDetailsFragment autoCompleteFragment;
	    private PhoneNumberFormattingTextWatcher digitFormater;
	//!    private OnAutoCompleteListItemClicked autoCompleteListItemListener;

	    private DialerLayout dialerLayout;

	    private MenuItem accountChooserFilterItem;


    // TimingLogger timings = new TimingLogger("SIP_HOME", "test");

    private ISipService service;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            service = ISipService.Stub.asInterface(arg1);
            /*
             * timings.addSplit("Service connected"); if(configurationService !=
             * null) { timings.dumpToLog(); }
             */
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            service = null;
        }
    };

    // private GestureDetector gestureDetector;


  


   

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDualPane = getResources().getBoolean(R.bool.use_dual_panes);
        context=this.getActivity();
        application = (MyApplication)this.getActivity().getApplication();
		   if (prefsWrapper == null) {
	            prefsWrapper = new PreferencesWrapper(context);
	        }
		   autoCompleteAdapter = new ContactsSearchAdapter(getActivity());
      //  digitFormater = new PhoneNumberFormattingTextWatcher();
        // Auto complete list in case of text
      //  autoCompleteAdapter = new ContactsSearchAdapter(getActivity());
       // autoCompleteListItemListener = new OnAutoCompleteListItemClicked(autoCompleteAdapter);

        if(isDigit == null) {
            isDigit = !prefsWrapper.getPreferenceBooleanValue(SipConfigManager.START_WITH_TEXT_DIALER);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.home_dial_page, container, false);
       
        // Store the backgrounds objects that will be in use later
        /*
        Resources r = getResources();
        
        digitsBackground = r.getDrawable(R.drawable.btn_dial_textfield_active);
        digitsEmptyBackground = r.getDrawable(R.drawable.btn_dial_textfield_normal);
        */

        // Store some object that could be useful later
       // digits = (DigitsEditText) v.findViewById(R.id.digitsText);
      //!  dialPad = (Dialpad) v.findViewById(R.id.dialPad);
       // callBar = (DialerCallBar) v.findViewById(R.id.dialerCallBar);
      //  autoCompleteList = (ListView) v.findViewById(R.id.autoCompleteList);
      //  rewriteTextInfo = (TextView) v.findViewById(R.id.rewriteTextInfo);
        
        accountChooserButton = (AccountChooserButton) v.findViewById(R.id.accountChooserButton);
        listView = (ListView) v.findViewById(R.id.contact_list);
		bohaopan = (LinearLayout) v.findViewById(R.id.bohaopan);
		
		
		callLogList = (ListView)v.findViewById(R.id.call_log_list);
    /*!    accountChooserFilterItem = accountChooserButton.addExtraMenuItem(R.string.apply_rewrite);
        accountChooserFilterItem.setCheckable(true);
        accountChooserFilterItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                setRewritingFeature(!accountChooserFilterItem.isChecked());
                return true;
            }
        });
        setRewritingFeature(prefsWrapper.getPreferenceBooleanValue(SipConfigManager.REWRITE_RULES_DIALER));
      !*/  
    

        // Account chooser button setup
        accountChooserButton.setShowExternals(true);
        accountChooserButton.setOnAccountChangeListener(accountButtonChangeListener);

        asyncQuery = new MyAsyncQueryHandler(context.getApplicationContext().getContentResolver());

		am = (AudioManager) this.getActivity().getSystemService(Context.AUDIO_SERVICE);

		spool = new SoundPool(11, AudioManager.STREAM_SYSTEM, 5);
		map.put(0, spool.load(context, R.raw.dtmf0, 0));
		map.put(1, spool.load(context, R.raw.dtmf1, 0));
		map.put(2, spool.load(context, R.raw.dtmf2, 0));
		map.put(3, spool.load(context, R.raw.dtmf3, 0));
		map.put(4, spool.load(context, R.raw.dtmf4, 0));
		map.put(5, spool.load(context, R.raw.dtmf5, 0));
		map.put(6, spool.load(context, R.raw.dtmf6, 0));
		map.put(7, spool.load(context, R.raw.dtmf7, 0));
		map.put(8, spool.load(context, R.raw.dtmf8, 0));
		map.put(9, spool.load(context, R.raw.dtmf9, 0));
		map.put(11, spool.load(context, R.raw.dtmf11, 0));
		map.put(12, spool.load(context, R.raw.dtmf12, 0));
		
		phone_view = (Button) v.findViewById(R.id.phone_view);
		phone_view.setOnClickListener(this);
		phone_view.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if("*#738#".equals(s.toString()))
				{
					phone_view.setText("");
					s="";
					startActivityForResult(new Intent(SipManager.ACTION_UI_PREFS_GLOBAL),1);
				}
				if(null == application.getContactBeanList() || application.getContactBeanList().size()<1 || "".equals(s.toString())){
					init();
					listView.setVisibility(View.INVISIBLE);
					callLogList.setVisibility(View.VISIBLE);
					
					
				}else{
					if(null == t9Adapter){
						
						t9Adapter = new T9Adapter(context);
						t9Adapter.assignment(application.getContactBeanList());
						listView.setAdapter(t9Adapter);
						listView.setTextFilterEnabled(true);
						listView.setOnScrollListener(new OnScrollListener() {
							public void onScrollStateChanged(AbsListView view, int scrollState) {
								
								if(scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
									if(bohaopan.getVisibility() == View.VISIBLE){
										bohaopan.setVisibility(View.GONE);
										listView.setVisibility(View.VISIBLE);
									}
									if(bohaopan.getVisibility() == View.INVISIBLE){
										bohaopan.setVisibility(View.GONE);
										
									}
								}
							}
							public void onScroll(AbsListView view, int firstVisibleItem,
									int visibleItemCount, int totalItemCount) {
								callLogList.setVisibility(View.INVISIBLE);
								
							}
						});
					}else{
						t9Adapter.assignment(application.getContactBeanList());
						listView.setAdapter(t9Adapter);
						listView.setTextFilterEnabled(true);
						listView.setOnScrollListener(new OnScrollListener() {
							public void onScrollStateChanged(AbsListView view, int scrollState) {
								
								if(scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
									if(bohaopan.getVisibility() == View.VISIBLE){
										bohaopan.setVisibility(View.GONE);
										listView.setVisibility(View.VISIBLE);
									}
									if(bohaopan.getVisibility() == View.INVISIBLE){
										bohaopan.setVisibility(View.GONE);
										
									}
								}
							}
							public void onScroll(AbsListView view, int firstVisibleItem,
									int visibleItemCount, int totalItemCount) {
								callLogList.setVisibility(View.INVISIBLE);
								
							}
						});
						callLogList.setVisibility(View.INVISIBLE);
						listView.setVisibility(View.VISIBLE);
						t9Adapter.getFilter().filter(s);
					}
				}
			}

				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}
				public void afterTextChanged(Editable s) {
				}
			});
			delete = (Button) v.findViewById(R.id.delete);
			delete.setOnClickListener(this);
			delete.setOnLongClickListener(new OnLongClickListener() {
				public boolean onLongClick(View v) {
					phone_view.setText("");
					return false;
				}
			});

			for (int i = 0; i < 12; i++) 
			{
				View j = v.findViewById(R.id.dialNum1 + i);
				j.setOnClickListener(this);
				j.setOnLongClickListener(this);
			}

			init();
			
			return v;

			}
        
    
      
    
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
        // Optional, but here we bundle so just ensure we are using csipsimple package
        serviceIntent.setPackage(activity.getPackageName());
        getActivity().bindService(serviceIntent, connection,
                Context.BIND_AUTO_CREATE);
        // timings.addSplit("Bind asked for two");
        if (prefsWrapper == null) {
            prefsWrapper = new PreferencesWrapper(getActivity());
        }
        if (dialFeedback == null) {
            dialFeedback = new DialingFeedback(getActivity(), false);
        }

        dialFeedback.resume();
        
    }

    @Override
    public void onDetach() {
        try {
            getActivity().unbindService(connection);
        } catch (Exception e) {
            // Just ignore that
            Log.w(THIS_FILE, "Unable to un bind", e);
        }
        dialFeedback.pause();
        super.onDetach();
    }
    
    public void onResume()
    {
    	if(fresh==1)
    		init();
    	else fresh=1;
    	super.onResume();
    	
    }
    private final static String TEXT_MODE_KEY = "text_mode";

    
    private OnEditorActionListener keyboardActionListener = new OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView tv, int action, KeyEvent arg2) {
            if (action == EditorInfo.IME_ACTION_GO) {
                placeCall();
                return true;
            }
            return false;
        }
    };
    
    OnAccountChangeListener accountButtonChangeListener = new OnAccountChangeListener() {
        @Override
        public void onChooseAccount(SipProfile account) {
            long accId = SipProfile.INVALID_ID;
            if (account != null) {
                accId = account.id;
            }
            autoCompleteAdapter.setSelectedAccount(accId);
           
        }
    };
    
    

    

    
    


    private class OnAutoCompleteListItemClicked implements OnItemClickListener {
        private ContactsSearchAdapter searchAdapter;

        /**
         * Instanciate with a ContactsSearchAdapter adapter to search in when a
         * contact entry is clicked
         * 
         * @param adapter the adapter to use
         */
        public OnAutoCompleteListItemClicked(ContactsSearchAdapter adapter) {
            searchAdapter = adapter;
        }

        @Override
        public void onItemClick(AdapterView<?> list, View v, int position, long id) {
            Object selectedItem = searchAdapter.getItem(position);
            if (selectedItem != null) {
                CharSequence newValue = searchAdapter.getFilter().convertResultToString(
                        selectedItem);
                setTextFieldValue(newValue);
            }
        }

    }

   

  

    /**
     * Set the mode of the text/digit input.
     * 
     * @param textMode True if text mode. False if digit mode
     */
    public void setTextDialing(boolean textMode) {
        Log.d(THIS_FILE, "Switch to mode " + textMode);
        setTextDialing(textMode, false);
    }
    

    /**
     * Set the mode of the text/digit input.
     * 
     * @param textMode True if text mode. False if digit mode
     */
    public void setTextDialing(boolean textMode, boolean forceRefresh) {
        if(!forceRefresh && (isDigit != null && isDigit == !textMode)) {
            // Nothing to do
            return;
        }
        isDigit = !textMode;
        if(digits == null) {
            return;
        }
        if(isDigit) {
            // We need to clear the field because the formatter will now 
            // apply and unapply to this field which could lead to wrong values when unapplied
            digits.getText().clear();
            digits.addTextChangedListener(digitFormater);
        }else {
            digits.removeTextChangedListener(digitFormater);
        }
        digits.setCursorVisible(!isDigit);
        digits.setIsDigit(isDigit, true);
        
        // Update views visibility
     
        //switchTextView.setImageResource(isDigit ? R.drawable.ic_menu_switch_txt
        //        : R.drawable.ic_menu_switch_digit);

        // Invalidate to ask to require the text button to a digit button
        getSherlockActivity().supportInvalidateOptionsMenu();
    }
    
    private boolean hasAutocompleteList() {
        if(!isDigit) {
            return true;
        }
        return dialerLayout.canShowList();
    }

    /**
     * Set the value of the text field and put caret at the end
     * 
     * @param value the new text to see in the text field
     */
    public void setTextFieldValue(CharSequence value) {
        if(digits == null) {
            initText = value.toString();
            return;
        }
        digits.setText(value);
        // make sure we keep the caret at the end of the text view
        Editable spannable = digits.getText();
        Selection.setSelection(spannable, spannable.length());
    }







    // Options
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        int action = getResources().getBoolean(R.bool.menu_in_bar) ? MenuItem.SHOW_AS_ACTION_IF_ROOM : MenuItem.SHOW_AS_ACTION_NEVER;
        MenuItem delMenu = menu.add(isDigit ? R.string.switch_to_text : R.string.switch_to_digit);
        delMenu.setIcon(
                isDigit ? R.drawable.ic_dialpad
                        : R.drawable.ic_dialpad).setShowAsAction( action );
        delMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
            	dialPadShow();
                return true;
            }
        });
    }

  
    public void placeCall() {
        placeCallWithOption(null);
    }

    
    public void placeVideoCall() {
        Bundle b = new Bundle();
        b.putBoolean(SipCallSession.OPT_CALL_VIDEO, true);
        placeCallWithOption(b );
    }
    
    private void placeCallWithOption(Bundle b) {
        if (service == null) {
            return;
        }
        String toCall = "";
        Long accountToUse = SipProfile.INVALID_ID;
        // Find account to use
        SipProfile acc = accountChooserButton.getSelectedAccount();
        if(acc == null) {
            return;
        }

        accountToUse = acc.id;
        // Find number to dial
        toCall = phone_view.getText().toString();
        if(isDigit) {
            toCall = PhoneNumberUtils.stripSeparators(toCall);
        }

        if(accountChooserFilterItem != null && accountChooserFilterItem.isChecked()) {
            toCall = rewriteNumber(toCall);
        }
        
        if (TextUtils.isEmpty(toCall)) {
            return;
        }

        // Well we have now the fields, clear theses fields
        phone_view.setText("");

        // -- MAKE THE CALL --//
        if (accountToUse >= 0) {
            // It is a SIP account, try to call service for that
            try {
                service.makeCallWithOptions(toCall, accountToUse.intValue(), b);
               
            } catch (RemoteException e) {
                Log.e(THIS_FILE, "Service can't be called to make the call");
            }
        } else if (accountToUse != SipProfile.INVALID_ID) {
            // It's an external account, find correct external account
            CallHandlerPlugin ch = new CallHandlerPlugin(getActivity());
            ch.loadFrom(accountToUse, toCall, new OnLoadListener() {
                @Override
                public void onLoad(CallHandlerPlugin ch) {
                    placePluginCall(ch);
                }
            });
        }
    }
    
    public void placeVMCall() {
        Long accountToUse = SipProfile.INVALID_ID;
        SipProfile acc = null;
        acc = accountChooserButton.getSelectedAccount();
        if (acc == null) {
            // Maybe we could inform user nothing will happen here?
            return;
        }
        
        accountToUse = acc.id;

        if (accountToUse >= 0) {
            SipProfile vmAcc = SipProfile.getProfileFromDbId(getActivity(), acc.id, new String[] {
                    SipProfile.FIELD_VOICE_MAIL_NBR
            });
            if (!TextUtils.isEmpty(vmAcc.vm_nbr)) {
                // Account already have a VM number
                try {
                    service.makeCall(vmAcc.vm_nbr, (int) acc.id);
                } catch (RemoteException e) {
                    Log.e(THIS_FILE, "Service can't be called to make the call");
                }
            } else {
                // Account has no VM number, propose to create one
                final long editedAccId = acc.id;
                LayoutInflater factory = LayoutInflater.from(getActivity());
                final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);

                missingVoicemailDialog = new AlertDialog.Builder(getActivity())
                        .setTitle(acc.display_name)
                        .setView(textEntryView)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                if (missingVoicemailDialog != null) {
                                    TextView tf = (TextView) missingVoicemailDialog
                                            .findViewById(R.id.vmfield);
                                    if (tf != null) {
                                        String vmNumber = tf.getText().toString();
                                        if (!TextUtils.isEmpty(vmNumber)) {
                                            ContentValues cv = new ContentValues();
                                            cv.put(SipProfile.FIELD_VOICE_MAIL_NBR, vmNumber);

                                            int updated = getActivity().getContentResolver()
                                                    .update(ContentUris.withAppendedId(
                                                            SipProfile.ACCOUNT_ID_URI_BASE,
                                                            editedAccId),
                                                            cv, null, null);
                                            Log.d(THIS_FILE, "Updated accounts " + updated);
                                        }
                                    }
                                    missingVoicemailDialog.hide();
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (missingVoicemailDialog != null) {
                                    missingVoicemailDialog.hide();
                                }
                            }
                        })
                        .create();

                // When the dialog is up, completely hide the in-call UI
                // underneath (which is in a partially-constructed state).
                missingVoicemailDialog.getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                missingVoicemailDialog.show();
            }
        } else if (accountToUse == CallHandlerPlugin.getAccountIdForCallHandler(getActivity(),
                (new ComponentName(getActivity(), com.csipsimple.plugins.telephony.CallHandler.class).flattenToString()))) {
            // Case gsm voice mail
            TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(
                    Context.TELEPHONY_SERVICE);
            String vmNumber = tm.getVoiceMailNumber();

            if (!TextUtils.isEmpty(vmNumber)) {
                if(service != null) {
                    try {
                        service.ignoreNextOutgoingCallFor(vmNumber);
                    } catch (RemoteException e) {
                        Log.e(THIS_FILE, "Not possible to ignore next");
                    }
                }
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", vmNumber, null));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {

                missingVoicemailDialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.gsm)
                        .setMessage(R.string.no_voice_mail_configured)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (missingVoicemailDialog != null) {
                                    missingVoicemailDialog.hide();
                                }
                            }
                        })
                        .create();

                // When the dialog is up, completely hide the in-call UI
                // underneath (which is in a partially-constructed state).
                missingVoicemailDialog.getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                missingVoicemailDialog.show();
            }
        }
        // TODO : manage others ?... for now, no way to do so cause no vm stored
    }

    private void placePluginCall(CallHandlerPlugin ch) {
        try {
            String nextExclude = ch.getNextExcludeTelNumber();
            if (service != null && nextExclude != null) {
                try {
                    service.ignoreNextOutgoingCallFor(nextExclude);
                } catch (RemoteException e) {
                    Log.e(THIS_FILE, "Impossible to ignore next outgoing call", e);
                }
            }
            ch.getIntent().send();
        } catch (CanceledException e) {
            Log.e(THIS_FILE, "Pending intent cancelled", e);
        }
    }

  
    private final static String TAG_AUTOCOMPLETE_SIDE_FRAG = "autocomplete_dial_side_frag";

    @Override
    public void onVisibilityChanged(boolean visible) {
        if (visible && getResources().getBoolean(R.bool.use_dual_panes)) {
            // That's far to be optimal we should consider uncomment tests for reusing fragment
            // if (autoCompleteFragment == null) {
            autoCompleteFragment = new DialerAutocompleteDetailsFragment();

            if (digits != null) {
                Bundle bundle = new Bundle();
                bundle.putCharSequence(DialerAutocompleteDetailsFragment.EXTRA_FILTER_CONSTRAINT,
                        digits.getText().toString());

                autoCompleteFragment.setArguments(bundle);

            }
            // }
            // if
            // (getFragmentManager().findFragmentByTag(TAG_AUTOCOMPLETE_SIDE_FRAG)
            // != autoCompleteFragment) {
            // Execute a transaction, replacing any existing fragment
            // with this one inside the frame.
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.details, autoCompleteFragment, TAG_AUTOCOMPLETE_SIDE_FRAG);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commitAllowingStateLoss();
        
            // }
        }
    }
    
    private String rewriteNumber(String number) {
        SipProfile acc = accountChooserButton.getSelectedAccount();
        if (acc == null) {
            return number;
        }
        String numberRewrite = Filter.rewritePhoneNumber(getActivity(), acc.id, number);
        if(TextUtils.isEmpty(numberRewrite)) {
            return "";
        }
        ParsedSipContactInfos finalCallee = acc.formatCalleeNumber(numberRewrite);
        if(!TextUtils.isEmpty(finalCallee.displayName)) {
            return finalCallee.toString();
        }
        return finalCallee.getReadableSipUri();
    }


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
    // Loader
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(getActivity(), SipManager.CALLLOG_URI, new String[] {
                CallLog.Calls._ID, CallLog.Calls.CACHED_NAME, CallLog.Calls.CACHED_NUMBER_LABEL,
                CallLog.Calls.CACHED_NUMBER_TYPE, CallLog.Calls.DURATION, CallLog.Calls.DATE,
                CallLog.Calls.NEW, CallLog.Calls.NUMBER, CallLog.Calls.TYPE,
                SipManager.CALLLOG_PROFILE_ID_FIELD
        },
                null, null,
                Calls.DEFAULT_SORT_ORDER);
    }


private void init(){
Uri uri = CallLog.Calls.CONTENT_URI;

String[] projection = { 
		CallLog.Calls.DATE,
		CallLog.Calls.NUMBER,
		CallLog.Calls.TYPE,
		CallLog.Calls.CACHED_NAME,
		CallLog.Calls._ID,
		CallLog.Calls.DURATION,

		
	
}; // 查询的列

asyncQuery.startQuery(0, null, uri, projection, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);  

}


private class MyAsyncQueryHandler extends AsyncQueryHandler {

public MyAsyncQueryHandler(ContentResolver cr) {
	super(cr);
}

protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
	
	if (cursor != null && cursor.getCount() > 0) {
	
		list = new ArrayList<CallLogBean>();
		SimpleDateFormat sfd = new SimpleDateFormat("MM-dd hh:mm");
		Date date;
		cursor.moveToFirst();
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			date = new Date(cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)));
//			String date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
			String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
			int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
			String cachedName = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));//缓存的名称与电话号码，如果它的存在
			int id = cursor.getInt(cursor.getColumnIndex(CallLog.Calls._ID));
            long duration=cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));
			CallLogBean clb = new CallLogBean();
			clb.setId(id);
			clb.setNumber(number);
			clb.setName(cachedName);
			clb.setduration(duration);
			if(null == cachedName || "".equals(cachedName)){
				clb.setName(number);
				clb.setiscontact(false);
			}
			else
				clb.setiscontact(true);
			clb.setType(type);
			clb.setDate(sfd.format(date));
			
			list.add(clb);
		}
		
		if (list.size() > 0) {
			setAdapter(list);
		}
	}
	else
	{
		
		callLogList.setVisibility(View.VISIBLE);
	}
}

}

 

private void setAdapter(final List<CallLogBean> list1) {
adapter = new HomeDialAdapter(this.getActivity(), list1);
//TextView tv = new TextView(this);
//tv.setBackgroundResource(R.drawable.dial_input_bg2);
//callLogList.addFooterView(tv);
callLogList.setAdapter(adapter);
callLogList.setVisibility(View.VISIBLE);
callLogList.setOnScrollListener(new OnScrollListener() {

	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if(scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
			if(bohaopan.getVisibility() == View.VISIBLE){
				bohaopan.setVisibility(View.GONE);
				callLogList.setVisibility(View.VISIBLE);
			}
		}
	}
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
	}
});
callLogList.setOnItemClickListener(new OnItemClickListener() {
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		  if(list.size()>0)
		  {
			  CallLogBean clb=list.get(position);
			 
			
			  Intent it = new Intent(getActivity(),LogDetailsActivity.class);
				it.putExtra("name",clb.getName());
				it.putExtra("number",clb.getNumber());
				it.putExtra("contact",clb.iscontact());
				it.putExtra("date",clb.getDate());
				it.putExtra("duration",clb.getduration());
				it.putExtra("id", clb.getId());
				it.putExtra("type", clb.getType());
			     startActivityForResult(it, 1);
				//getActivity().startActivity(it);
		      
		  }
		
	
		/*   直接显示联系人 代码。待插入
		 *   Long a=  getPhoneContactsID(context,clb.getName());
			 // ShowLabel.show(context, clb.getName()+clb.getNumber()+String.valueOf(clb.iscontact())+clb.getDate()+"   "+String.valueOf(clb.getduration()));
			 if(a==null)
			 {
				 
			 }
			 else
			 {
			  Intent intent = new Intent();   
			  intent.setAction(Intent.ACTION_VIEW); 
			  
			 
			 
			  Uri personUri= ContentUris.withAppendedId(Contacts.CONTENT_URI,Long.parseLong(a.toString()));
			  intent.setData(personUri);   
			  startActivity(intent);  
			 }
		 * */
		
		
	}
});
}
@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) 
{
	
	fresh=0;
	if(requestCode == 1) {
        this.context.sendBroadcast(new Intent(SipManager.ACTION_SIP_REQUEST_RESTART));
        BackupWrapper.getInstance(this.context).dataChanged();
    }
    super.onActivityResult(requestCode, resultCode, data);
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
public void onClick(View v) {
	dialFeedback.giveFeedback(300);
switch (v.getId()) {
case R.id.dialNum0:
	if (phone_view.getText().length() < 12) {
		play(1);
		input(v.getTag().toString());
	}
	break;
case R.id.dialNum1:
	if (phone_view.getText().length() < 12) {
		play(1);
		input(v.getTag().toString());
	}
	break;
case R.id.dialNum2:
	if (phone_view.getText().length() < 12) {
		play(2);
		input(v.getTag().toString());
	}
	break;
case R.id.dialNum3:
	if (phone_view.getText().length() < 12) {
		play(3);
		input(v.getTag().toString());
	}
	break;
case R.id.dialNum4:
	if (phone_view.getText().length() < 12) {
		play(4);
		input(v.getTag().toString());
	}
	break;
case R.id.dialNum5:
	if (phone_view.getText().length() < 12) {
		play(5);
		input(v.getTag().toString());
	}
	break;
case R.id.dialNum6:
	if (phone_view.getText().length() < 12) {
		play(6);
		input(v.getTag().toString());
	}
	break;
case R.id.dialNum7:
	if (phone_view.getText().length() < 12) {
		play(7);
		input(v.getTag().toString());
	}
	break;
case R.id.dialNum8:
	if (phone_view.getText().length() < 12) {
		play(8);
		input(v.getTag().toString());
	}
	break;
case R.id.dialNum9:
	if (phone_view.getText().length() < 12) {
		play(9);
		input(v.getTag().toString());
	}
	break;
case R.id.dialx:
	if (phone_view.getText().length() < 12) {
		play(11);
		input(v.getTag().toString());
	}
	break;
case R.id.dialj:
	if (phone_view.getText().length() < 12) {
		play(12);
		input(v.getTag().toString());
	}
	break;
case R.id.delete:
	delete();
	break;
case R.id.phone_view:
	if (phone_view.getText().toString().length() >= 3) {
		call(phone_view.getText().toString());
	}
	else
	{
		ShowLabel.show(this.getActivity(), "您的号码有误");
	}
	break;
default:
	break;
}
}
private void play(int id) {
int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
int current = am.getStreamVolume(AudioManager.STREAM_MUSIC);

float value = (float)0.7 / max * current;
spool.setVolume(spool.play(id, value, value, 0, 0, 1f), value, value);
}
private void input(String str) {
String p = phone_view.getText().toString();
phone_view.setText(p + str);
}
private void delete() {
String p = phone_view.getText().toString();
if(p.length()>0){
	phone_view.setText(p.substring(0, p.length()-1));
}
}
private void call(String phone) {
//Uri uri = Uri.parse("tel:" + phone);
//Intent it = new Intent(Intent.ACTION_CALL, uri);
	placeCall();
phone_view.setText("");
//startActivity(it);
}







public void dialPadShow(){
if(bohaopan.getVisibility() == View.VISIBLE){
	bohaopan.setVisibility(View.GONE);
	//keyboard_show_ll.setVisibility(View.VISIBLE);
}else{
	bohaopan.setVisibility(View.VISIBLE);
	//keyboard_show_ll.setVisibility(View.INVISIBLE);
}
}

@Override
public boolean onLongClick(View v) {
	// TODO Auto-generated method stub
	
	switch (v.getId()) {
	case R.id.dialNum0:
		if (phone_view.getText().length() < 12) {
			play(1);
			input("+");
		}
		break;
	
	default:
		break;
	
	
	}
	return false;
   }



	
	
	
	
	
	
	
	
	
	
    
}
