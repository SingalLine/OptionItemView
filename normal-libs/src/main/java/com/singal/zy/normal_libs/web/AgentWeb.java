package com.singal.zy.normal_libs.web;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.singal.zy.normal_libs.view.ProgressWebView;

import java.util.Map;


/**
 * FrameLayout--嵌套WebView,ProgressBar
 *
 * Created by li on 2017/6/15.
 */

public class AgentWeb {

    private static final String TAG=AgentWeb.class.getSimpleName();

    private Activity mActivity;
    private ViewGroup mViewGroup;
    private WebCreator mWebCreator;
    private WebSettings mWebSettings;
    private AgentWeb mAgentWeb = null;
    private IndicatorController mIndicatorController;
    private WebChromeClient mWebChromeClient;
    private WebViewClient mWebViewClient;
    private boolean enableProgress;
    private Fragment mFragment;
    private IEventHandler mIEventHandler;

    private ArrayMap<String, Object> mJavaObjects = new ArrayMap<>();
    private int TAG_TARGET = 0;
    private WebListenerManager mWebListenerManager;
    private DownloadListener mDownloadListener = null;
    private ChromeClientCallbackManager mChromeClientCallbackManager;
    private WebSecurityController<WebSecurityCheckLogic> mWebSecurityController = null;
    private WebSecurityCheckLogic mWebSecurityCheckLogic = null;
    private WebChromeClient mTargetChromeClient;
    private SecurityType mSecurityType = SecurityType.default_check;
    private static final int ACTIVITY_TAG = 0;
    private static final int FRAGMENT_TAG = 1;
    private AgentWebJsInterfaceCompat mAgentWebJsInterfaceCompat = null;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private JsEntraceAccess mJsEntraceAccess = null;
    private ILoader mILoader = null;
    private WebLifeCycle mWebLifeCycle;
    private IVideo mIVideo=null;

    private boolean  webClientHelper=false;



    private AgentWeb(AgentBuilder agentBuilder) {
        this.mActivity = agentBuilder.mActivity;
        this.mViewGroup = agentBuilder.mViewGroup;
        this.enableProgress = agentBuilder.enableProgress;
        mWebCreator = agentBuilder.mWebCreator == null ? configWebCreator(agentBuilder.v, agentBuilder.index, agentBuilder.mLayoutParams, agentBuilder.mIndicatorColor, agentBuilder.mIndicatorColorWithHeight, agentBuilder.mWebView) : agentBuilder.mWebCreator;
        mIndicatorController = agentBuilder.mIndicatorController;
        this.mWebChromeClient = agentBuilder.mWebChromeClient;
        this.mWebViewClient = agentBuilder.mWebViewClient;
        mAgentWeb = this;
        this.mWebSettings = agentBuilder.mWebSettings;
        this.mIEventHandler = agentBuilder.mIEventHandler;
        TAG_TARGET = ACTIVITY_TAG;
        if (agentBuilder.mJavaObject != null && agentBuilder.mJavaObject.isEmpty())
            this.mJavaObjects.putAll((Map<? extends String, ?>) agentBuilder.mJavaObject);
        this.mChromeClientCallbackManager = agentBuilder.mChromeClientCallbackManager;
        this.mWebViewClientCallbackManager = agentBuilder.mWebViewClientCallbackManager;

        this.mSecurityType = agentBuilder.mSecurityType;
        this.mILoader = new LoaderImpl(mWebCreator.create().get(), agentBuilder.headers);
        this.mWebLifeCycle = new DefaultWebLifeCycleImpl(mWebCreator.get());
        mWebSecurityController = new WebSecurityControllerImpl(mWebCreator.get(), this.mAgentWeb.mJavaObjects, mSecurityType);
        this.webClientHelper=agentBuilder.webclientHelper;
        doCompat();
        doSafeCheck();
    }


    public AgentWeb(AgentBuilderFragment agentBuilderFragment) {
        TAG_TARGET = FRAGMENT_TAG;
        this.mActivity = agentBuilderFragment.mActivity;
        this.mFragment = agentBuilderFragment.mFragment;
        this.mViewGroup = agentBuilderFragment.mViewGroup;
        this.mIEventHandler = agentBuilderFragment.mIEventHandler;
        this.enableProgress = agentBuilderFragment.enableProgress;
        mWebCreator = agentBuilderFragment.mWebCreator == null ? configWebCreator(agentBuilderFragment.v, agentBuilderFragment.index, agentBuilderFragment.mLayoutParams, agentBuilderFragment.mIndicatorColor, agentBuilderFragment.height_dp, agentBuilderFragment.mWebView) : agentBuilderFragment.mWebCreator;
        mIndicatorController = agentBuilderFragment.mIndicatorController;
        this.mWebChromeClient = agentBuilderFragment.mWebChromeClient;
        this.mWebViewClient = agentBuilderFragment.mWebViewClient;
        mAgentWeb = this;
        this.mWebSettings = agentBuilderFragment.mWebSettings;
        if (agentBuilderFragment.mJavaObject != null && agentBuilderFragment.mJavaObject.isEmpty())
            this.mJavaObjects.putAll((Map<? extends String, ?>) agentBuilderFragment.mJavaObject);
        this.mChromeClientCallbackManager = agentBuilderFragment.mChromeClientCallbackManager;
        this.mWebViewClientCallbackManager = agentBuilderFragment.mWebViewClientCallbackManager;
        this.mSecurityType = agentBuilderFragment.mSecurityType;
        this.mILoader = new LoaderImpl(mWebCreator.create().get(), agentBuilderFragment.additionalHttpHeaders);
        this.mWebLifeCycle = new DefaultWebLifeCycleImpl(mWebCreator.get());
        mWebSecurityController = new WebSecurityControllerImpl(mWebCreator.get(), this.mAgentWeb.mJavaObjects, this.mSecurityType);
        this.webClientHelper=agentBuilderFragment.webClientHelper;
        doCompat();
        doSafeCheck();


    }


    private void doCompat() {


        mJavaObjects.put("agentWeb", mAgentWebJsInterfaceCompat = new AgentWebJsInterfaceCompat(this, mActivity));

        LogUtils.i("Info", "AgentWebConfig.isUseAgentWebView:" + AgentWebConfig.WEBVIEW_TYPE + "  mChromeClientCallbackManager:" + mChromeClientCallbackManager);
        if (AgentWebConfig.WEBVIEW_TYPE == AgentWebConfig.WEBVIEW_AGENTWEB_SAFE_TYPE) {
            this.mChromeClientCallbackManager.setAgentWebCompatInterface((ChromeClientCallbackManager.AgentWebCompatInterface) mWebCreator.get());
            this.mWebViewClientCallbackManager.setPageLifeCycleCallback((WebViewClientCallbackManager.PageLifeCycleCallback) mWebCreator.get());
        }

    }

    public WebLifeCycle getWebLifeCycle() {
        return this.mWebLifeCycle;
    }

    private void doSafeCheck() {

        WebSecurityCheckLogic mWebSecurityCheckLogic = this.mWebSecurityCheckLogic;
        if (mWebSecurityCheckLogic == null) {
            this.mWebSecurityCheckLogic = mWebSecurityCheckLogic = WebSecurityLogicImpl.getInstance();
        }
        mWebSecurityController.check(mWebSecurityCheckLogic);

    }

    private WebCreator configWebCreator(BaseIndicatorView progressView, int index, ViewGroup.LayoutParams lp, int mIndicatorColor, int height_dp, WebView webView) {

        if (progressView != null && enableProgress) {
            return new DefaultWebCreator(mActivity, mViewGroup, lp, index, progressView, webView);
        } else {
            return enableProgress ?
                    new DefaultWebCreator(mActivity, mViewGroup, lp, index, mIndicatorColor, height_dp, webView)
                    : new DefaultWebCreator(mActivity, mViewGroup, lp, index, webView);
        }
    }

    private void loadData(String data, String mimeType, String encoding) {
        mWebCreator.get().loadData(data, mimeType, encoding);
    }

    private void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String history) {
        mWebCreator.get().loadDataWithBaseURL(baseUrl, data, mimeType, encoding, history);
    }


    public JsEntraceAccess getJsEntraceAccess() {

        JsEntraceAccess mJsEntraceAccess = this.mJsEntraceAccess;
        if (mJsEntraceAccess == null) {
            this.mJsEntraceAccess = mJsEntraceAccess = JsEntraceAccessImpl.getInstance(mWebCreator.get());
        }
        return mJsEntraceAccess;
    }


    public AgentWeb clearWebCache() {

        AgentWebUtils.clearWebViewAllCache(mActivity);
        return this;
    }


    public static AgentBuilder with(@NonNull Activity activity) {
        return new AgentBuilder(activity);
    }

    public static AgentBuilderFragment with(@NonNull Fragment fragment) {


        Activity mActivity = null;
        if ((mActivity = fragment.getActivity()) == null)
            new NullPointerException("activity can not null");
        return new AgentBuilderFragment(mActivity, fragment);
    }

    private EventInterceptor mEventInterceptor;
    private EventInterceptor getInterceptor(){

        if(this.mEventInterceptor!=null)
            return this.mEventInterceptor;

        if (mIVideo instanceof VideoImpl){
            return this.mEventInterceptor=(EventInterceptor) this.mIVideo;
        }

        return null;

    }

    public boolean handleKeyEvent(int keyCode, KeyEvent keyEvent) {

        if (mIEventHandler == null) {
            mIEventHandler = EventHandlerImpl.getInstantce(mWebCreator.get(),getInterceptor());
        }
        return mIEventHandler.onKeyDown(keyCode, keyEvent);
    }

    public boolean back() {

        if (mIEventHandler == null) {
            mIEventHandler = EventHandlerImpl.getInstantce(mWebCreator.get(),getInterceptor());
        }
        return mIEventHandler.back();
    }

    /*public static AgentWeb withCreatorWeb(WebCreator creatorWeb) {
        return new AgentBuilder(creatorWeb).buildAgentWeb();
    }*/

    public WebCreator getWebCreator() {
        return this.mWebCreator;
    }

    public IEventHandler getIEventHandler() {
        return this.mIEventHandler == null ? (this.mIEventHandler = EventHandlerImpl.getInstantce(mWebCreator.get(),getInterceptor())) : this.mIEventHandler;
    }

    private JsInterfaceHolder mJsInterfaceHolder = null;

    public WebSettings getWebSettings() {
        return this.mWebSettings;
    }

    public IndicatorController getIndicatorController() {
        return this.mIndicatorController;
    }


    private AgentWeb ready() {

        WebSettings mWebSettings = this.mWebSettings;
        if (mWebSettings == null) {
            this.mWebSettings = mWebSettings = WebDefaultSettingsManager.getInstance();
        }
        if (mWebListenerManager == null && mWebSettings instanceof WebDefaultSettingsManager) {
            mWebListenerManager = (WebListenerManager) mWebSettings;
        }
        mWebSettings.toSetting(mWebCreator.get());
        if (mJsInterfaceHolder == null) {
            mJsInterfaceHolder = JsInterfaceHolderImpl.getJsInterfaceHolder(mWebCreator.get(), this.mSecurityType);
        }
        if (mJavaObjects != null && !mJavaObjects.isEmpty()) {
            mJsInterfaceHolder.addJavaObjects(mJavaObjects);
        }
        mWebListenerManager.setDownLoader(mWebCreator.get(), getLoadListener());
        mWebListenerManager.setWebChromeClient(mWebCreator.get(), getChromeClient());
        mWebListenerManager.setWebViewClient(mWebCreator.get(), getClient());


        return this;
    }


    private DownloadListener getLoadListener() {
        DownloadListener mDownloadListener = this.mDownloadListener;
        if (mDownloadListener == null) {
            this.mDownloadListener = mDownloadListener = new DefaultDownLoaderImpl(mActivity.getApplicationContext(), false, true);
        }
        return mDownloadListener;
    }

    private WebViewClientCallbackManager mWebViewClientCallbackManager = null;

    private WebChromeClient getChromeClient() {
        IndicatorController mIndicatorController = (this.mIndicatorController == null) ? IndicatorHandler.getInstance().inJectProgressView(mWebCreator.offer()) : this.mIndicatorController;
        /*if (mWebChromeClient != null) {
            return enableProgress ? new WebChromeClientProgressWrapper(mIndicatorController, mWebChromeClient) : mWebChromeClient;
        } else {
            return new DefaultChromeClient(this.mActivity, mIndicatorController, this.mChromeClientCallbackManager);
        }*/

        return this.mTargetChromeClient = new DefaultChromeClient(this.mActivity, mIndicatorController, mWebChromeClient, this.mChromeClientCallbackManager,this.mIVideo=getIVideo());
    }

    private IVideo getIVideo(){
        return mIVideo==null?new VideoImpl(mActivity,mWebCreator.get()):mIVideo;
    }


    public JsInterfaceHolder getJsInterfaceHolder() {
        return this.mJsInterfaceHolder;
    }

    private WebViewClient getClient() {
        if (!webClientHelper&&AgentWebConfig.WEBVIEW_TYPE != AgentWebConfig.WEBVIEW_AGENTWEB_SAFE_TYPE && mWebViewClient != null) {
            return mWebViewClient;
        } else {
            return new DefaultWebClient(mActivity,this.mWebViewClient, this.mWebViewClientCallbackManager,webClientHelper);
        }

    }


    public ILoader getLoader() {
        return this.mILoader;
    }


    private AgentWeb go(String url) {
        this.getLoader().loadUrl(url);
        return this;
    }

    private boolean isKillProcess = false;

    public void destroy() {
        this.mWebLifeCycle.onDestroy();
    }

    public void destroyAndKill() {
        destroy();
        if (AgentWebUtils.isMainProcess(mActivity)) {
            LogUtils.i("Info", "退出进程");
            System.exit(0);
        }
    }

    public void uploadFileResult(int requestCode, int resultCode, Intent data) {

        IFileUploadChooser mIFileUploadChooser = null;

        if (mTargetChromeClient instanceof DefaultChromeClient) {
            DefaultChromeClient mDefaultChromeClient = (DefaultChromeClient) mTargetChromeClient;
            mIFileUploadChooser = mDefaultChromeClient.pop();
        }

        if (mIFileUploadChooser == null)
            mIFileUploadChooser = mAgentWebJsInterfaceCompat.pop();
        Log.i("Info", "file upload:" + mIFileUploadChooser);
        if (mIFileUploadChooser != null)
            mIFileUploadChooser.fetchFilePathFromIntent(requestCode, resultCode, data);

        if (mIFileUploadChooser != null)
            mIFileUploadChooser = null;
    }


    /*********************************************************为Activity构建AgentWeb***********************************************************************/


    public static class AgentBuilder {

        private Activity mActivity;
        private ViewGroup mViewGroup;
        private boolean isNeedProgress;
        private int index = -1;
        private BaseIndicatorView v;
        private IndicatorController mIndicatorController = null;
        /*默认进度条是打开的*/
        private boolean enableProgress = true;
        private ViewGroup.LayoutParams mLayoutParams = null;
        private WebViewClient mWebViewClient;
        private WebChromeClient mWebChromeClient;
        private int mIndicatorColor = -1;
        private WebSettings mWebSettings;
        private WebCreator mWebCreator;
        private WebViewClientCallbackManager mWebViewClientCallbackManager = new WebViewClientCallbackManager();
        private SecurityType mSecurityType = SecurityType.default_check;

        private ChromeClientCallbackManager mChromeClientCallbackManager = new ChromeClientCallbackManager();

        private Map<String, String> headers = null;


        private ArrayMap<String, Object> mJavaObject = null;
        private int mIndicatorColorWithHeight = -1;
        public WebView mWebView;
        public boolean webclientHelper =true;

        private void addJavaObject(String key, Object o) {
            if (mJavaObject == null)
                mJavaObject = new ArrayMap<>();
            mJavaObject.put(key, o);
        }


        private void setIndicatorColor(int indicatorColor) {
            mIndicatorColor = indicatorColor;
        }

        private AgentBuilder(Activity activity) {
            this.mActivity = activity;
        }

        private AgentBuilder enableProgress() {
            this.enableProgress = true;
            return this;
        }

        private AgentBuilder closeProgress() {
            this.enableProgress = false;
            return this;
        }


        private AgentBuilder(WebCreator webCreator) {
            this.mWebCreator = webCreator;
        }


        public ConfigIndicatorBuilder setAgentWebParent(ViewGroup viewGroup, ViewGroup.LayoutParams lp) {
            this.mViewGroup = viewGroup;
            mLayoutParams = lp;
            return new ConfigIndicatorBuilder(this);
        }

        public ConfigIndicatorBuilder setAgentWebParent(ViewGroup viewGroup, ViewGroup.LayoutParams lp, int position) {
            this.mViewGroup = viewGroup;
            mLayoutParams = lp;
            this.index = position;
            return new ConfigIndicatorBuilder(this);
        }

        public ConfigIndicatorBuilder createContentViewTag() {

            this.mViewGroup = null;
            this.mLayoutParams = null;
            return new ConfigIndicatorBuilder(this);
        }


        private void addHeader(String k, String v) {
            if (headers == null)
                headers = new ArrayMap<>();

            headers.put(k, v);

        }
//



      /*  *//*如果index==-1 默认为最后*//*
        public AgentBuilder setViewIndex(int index) {
            this.index = index;
            return this;
        }*/


        private PreAgentWeb buildAgentWeb() {
            return new PreAgentWeb(HookManager.hookAgentWeb(new AgentWeb(this), this));
        }

        private IEventHandler mIEventHandler;


        public void setIndicatorColorWithHeight(int indicatorColorWithHeight) {
            mIndicatorColorWithHeight = indicatorColorWithHeight;
        }
    }

    public static class PreAgentWeb {
        private AgentWeb mAgentWeb;
        private boolean isReady = false;

        PreAgentWeb(AgentWeb agentWeb) {
            this.mAgentWeb = agentWeb;
        }


        public PreAgentWeb ready() {
            if (!isReady) {
                mAgentWeb.ready();
                isReady = true;
            }
            return this;
        }

        public AgentWeb go(@Nullable String url) {
            if (!isReady) {
//                throw new IllegalStateException(" please call ready before go  to finish all webview settings");  //i want to do this , but i cannot;
                ready();
            }
            return mAgentWeb.go(url);
        }


    }

    public static class ConfigIndicatorBuilder {

        private AgentBuilder mAgentBuilder;

        private ConfigIndicatorBuilder(AgentBuilder agentBuilder) {
            this.mAgentBuilder = agentBuilder;
        }

        public IndicatorBuilder useDefaultIndicator() {
            this.mAgentBuilder.isNeedProgress = true;
            mAgentBuilder.enableProgress();
            return new IndicatorBuilder(mAgentBuilder);
        }

        public CommonAgentBuilder customProgress(BaseIndicatorView view) {
            this.mAgentBuilder.v = view;

            mAgentBuilder.isNeedProgress = false;
            return new CommonAgentBuilder(mAgentBuilder);
        }

        public CommonAgentBuilder closeProgressBar() {
            mAgentBuilder.closeProgress();
            return new CommonAgentBuilder(mAgentBuilder);
        }


    }

    public static class CommonAgentBuilder {
        private AgentBuilder mAgentBuilder;


        private CommonAgentBuilder(AgentBuilder agentBuilder) {
            this.mAgentBuilder = agentBuilder;

        }

        public CommonAgentBuilder setWebViewClient(WebViewClient webViewClient) {
            this.mAgentBuilder.mWebViewClient = webViewClient;
            return this;
        }


        public CommonAgentBuilder setWebChromeClient(WebChromeClient webChromeClient) {
            this.mAgentBuilder.mWebChromeClient = webChromeClient;
            return this;
        }

        public CommonAgentBuilder setEventHandler(IEventHandler iEventHandler) {
            this.mAgentBuilder.mIEventHandler = iEventHandler;
            return this;
        }

        public CommonAgentBuilder setWebSettings(WebSettings webSettings) {
            this.mAgentBuilder.mWebSettings = webSettings;
            return this;
        }


        public CommonAgentBuilder(IndicatorController indicatorController) {
            this.mAgentBuilder.mIndicatorController = indicatorController;
        }


        public CommonAgentBuilder addJavascriptInterface(String name, Object o) {
            mAgentBuilder.addJavaObject(name, o);
            return this;
        }

        public CommonAgentBuilder setWebCreator(WebCreator webCreator) {
            this.mAgentBuilder.mWebCreator = webCreator;
            return this;
        }

        public CommonAgentBuilder setReceivedTitleCallback(ChromeClientCallbackManager.ReceivedTitleCallback receivedTitleCallback) {
            this.mAgentBuilder.mChromeClientCallbackManager.setReceivedTitleCallback(receivedTitleCallback);
            return this;
        }

        public CommonAgentBuilder setSecutityType(SecurityType secutityType) {
            this.mAgentBuilder.mSecurityType = secutityType;
            return this;
        }

        public CommonAgentBuilder setWebView(WebView webView) {
            this.mAgentBuilder.mWebView = webView;
            return this;
        }

        public CommonAgentBuilder additionalHttpHeader(String k, String v) {
            this.mAgentBuilder.addHeader(k, v);
            return this;
        }

        public CommonAgentBuilder closeWebViewClientHelper(){
            mAgentBuilder.webclientHelper =false;
            return this;
        }
        public PreAgentWeb createAgentWeb() {
            return mAgentBuilder.buildAgentWeb();
        }

    }

    public static enum SecurityType {
        default_check, strict;
    }

    public static class IndicatorBuilder {

        private AgentBuilder mAgentBuilder = null;

        private IndicatorBuilder(AgentBuilder builder) {
            this.mAgentBuilder = builder;
        }

        public CommonAgentBuilder setIndicatorColor(int color) {
            mAgentBuilder.setIndicatorColor(color);
            return new CommonAgentBuilder(mAgentBuilder);
        }

        public CommonAgentBuilder defaultProgressBarColor() {
            mAgentBuilder.setIndicatorColor(-1);
            return new CommonAgentBuilder(mAgentBuilder);
        }

        public CommonAgentBuilder setIndicatorColorWithHeight(@ColorInt int color, int height_dp) {
            mAgentBuilder.setIndicatorColor(color);
            mAgentBuilder.setIndicatorColorWithHeight(height_dp);
            return new CommonAgentBuilder(mAgentBuilder);
        }


    }


    /*********************为Fragment构建AgentWeb***********************/

    public static final class AgentBuilderFragment {
        private Activity mActivity;
        private Fragment mFragment;

        private ViewGroup mViewGroup;
        private boolean isNeedDefaultProgress;
        private int index = -1;
        private BaseIndicatorView v;
        private IndicatorController mIndicatorController = null;
        /*默认进度条是打开的*/
        private boolean enableProgress = true;
        private ViewGroup.LayoutParams mLayoutParams = null;
        private WebViewClient mWebViewClient;
        private WebChromeClient mWebChromeClient;
        private int mIndicatorColor = -1;
        private WebSettings mWebSettings;
        private WebCreator mWebCreator;
        private Map<String, String> additionalHttpHeaders = null;
        private IEventHandler mIEventHandler;
        private int height_dp = -1;
        private ArrayMap<String, Object> mJavaObject;
        private ChromeClientCallbackManager mChromeClientCallbackManager = new ChromeClientCallbackManager();
        private SecurityType mSecurityType = SecurityType.default_check;
        public WebView mWebView;
        private WebViewClientCallbackManager mWebViewClientCallbackManager = new WebViewClientCallbackManager();
        public boolean webClientHelper =true;


        public AgentBuilderFragment(@NonNull Activity activity, @NonNull Fragment fragment) {
            mActivity = activity;
            mFragment = fragment;
        }

        public IndicatorBuilderForFragment setAgentWebParent(ViewGroup v, ViewGroup.LayoutParams lp) {
            this.mViewGroup = v;
            this.mLayoutParams = lp;
            return new IndicatorBuilderForFragment(this);
        }

        private PreAgentWeb buildAgentWeb() {
            if (this.mViewGroup == null)
                throw new NullPointerException("ViewGroup is null,please check you params");
            return new PreAgentWeb(HookManager.hookAgentWeb(new AgentWeb(this), this));
        }

        private void addJavaObject(String key, Object o) {
            if (mJavaObject == null)
                mJavaObject = new ArrayMap<>();
            mJavaObject.put(key, o);
        }

        private void addHeader(String k, String v) {

            if (additionalHttpHeaders == null)
                additionalHttpHeaders = new ArrayMap<>();
            additionalHttpHeaders.put(k, v);

        }
    }

    public static class IndicatorBuilderForFragment {
        AgentBuilderFragment agentBuilderFragment = null;

        public IndicatorBuilderForFragment(AgentBuilderFragment agentBuilderFragment) {
            this.agentBuilderFragment = agentBuilderFragment;
        }

        public CommonBuilderForFragment useDefaultIndicator(int color) {
            this.agentBuilderFragment.enableProgress = true;
            this.agentBuilderFragment.mIndicatorColor = color;
            return new CommonBuilderForFragment(agentBuilderFragment);
        }

        public CommonBuilderForFragment useDefaultIndicator() {
            this.agentBuilderFragment.enableProgress = true;
            return new CommonBuilderForFragment(agentBuilderFragment);
        }

        public CommonBuilderForFragment closeDefaultIndicator() {
            this.agentBuilderFragment.enableProgress = false;
            this.agentBuilderFragment.mIndicatorColor = -1;
            this.agentBuilderFragment.height_dp = -1;
            return new CommonBuilderForFragment(agentBuilderFragment);
        }

        public CommonBuilderForFragment setCustomIndicator(BaseIndicatorView v) {
            if (v != null) {
                this.agentBuilderFragment.enableProgress = true;
                this.agentBuilderFragment.v = v;
                this.agentBuilderFragment.isNeedDefaultProgress = false;
            } else {
                this.agentBuilderFragment.enableProgress = true;
                this.agentBuilderFragment.isNeedDefaultProgress = true;
            }

            return new CommonBuilderForFragment(agentBuilderFragment);
        }

        public CommonBuilderForFragment setIndicatorColorWithHeight(@ColorInt int color, int height_dp) {
            this.agentBuilderFragment.mIndicatorColor = color;
            this.agentBuilderFragment.height_dp = height_dp;
            return new CommonBuilderForFragment(this.agentBuilderFragment);
        }

    }


    public static class CommonBuilderForFragment {
        private AgentBuilderFragment mAgentBuilderFragment;

        public CommonBuilderForFragment(AgentBuilderFragment agentBuilderFragment) {
            this.mAgentBuilderFragment = agentBuilderFragment;
        }

        public CommonBuilderForFragment setEventHanadler(IEventHandler iEventHandler) {
            mAgentBuilderFragment.mIEventHandler = iEventHandler;
            return this;
        }

        public CommonBuilderForFragment closeWebViewClientHelper(){
            mAgentBuilderFragment.webClientHelper =false;
            return this;
        }

        public CommonBuilderForFragment setWebCreator(WebCreator webCreator) {
            this.mAgentBuilderFragment.mWebCreator = webCreator;
            return this;
        }

        public CommonBuilderForFragment setWebChromeClient(WebChromeClient webChromeClient) {
            this.mAgentBuilderFragment.mWebChromeClient = webChromeClient;
            return this;

        }

        public CommonBuilderForFragment setWebViewClient(WebViewClient webChromeClient) {
            this.mAgentBuilderFragment.mWebViewClient = webChromeClient;
            return this;
        }

        public CommonBuilderForFragment setWebSettings(WebSettings webSettings) {
            this.mAgentBuilderFragment.mWebSettings = webSettings;
            return this;
        }

        public PreAgentWeb createAgentWeb() {
            return this.mAgentBuilderFragment.buildAgentWeb();
        }

        public CommonBuilderForFragment setReceivedTitleCallback(ChromeClientCallbackManager.ReceivedTitleCallback receivedTitleCallback) {
            this.mAgentBuilderFragment.mChromeClientCallbackManager.setReceivedTitleCallback(receivedTitleCallback);
            return this;
        }

        public CommonBuilderForFragment addJavascriptInterface(String name, Object o) {
            this.mAgentBuilderFragment.addJavaObject(name, o);
            return this;
        }

        public CommonBuilderForFragment setSecurityType(SecurityType type) {
            this.mAgentBuilderFragment.mSecurityType = type;
            return this;
        }

        public CommonBuilderForFragment setWebView(WebView webView) {
            this.mAgentBuilderFragment.mWebView = webView;
            return this;
        }

        public CommonBuilderForFragment additionalHttpHeaders(String k, String v) {
            this.mAgentBuilderFragment.addHeader(k, v);

            return this;
        }

    }


}
