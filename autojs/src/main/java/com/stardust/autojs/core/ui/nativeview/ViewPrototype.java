package com.stardust.autojs.core.ui.nativeview;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;
import android.widget.CompoundButton;

import com.stardust.autojs.core.eventloop.EventEmitter;
import com.stardust.autojs.core.ui.BaseEvent;
import com.stardust.autojs.core.ui.widget.JsListView;
import com.stardust.autojs.runtime.ScriptRuntime;

import org.mozilla.javascript.Scriptable;

import java.util.HashSet;

public class ViewPrototype {

    private final EventEmitter mEventEmitter;
    private final View mView;
    private final HashSet<String> mRegisteredEvents = new HashSet<>();
    private final Scriptable mScope;

    public ViewPrototype(View view, Scriptable scope, ScriptRuntime runtime) {
        mView = view;
        mEventEmitter = runtime.events.emitter();
        mScope = scope;
    }

    public void click() {
        mView.performClick();
    }

    public void longClick() {
        mView.performLongClick();
    }

    public void click(Object listener) {
        on("click", listener);
    }

    public void longClick(Object listener) {
        on("long_click", listener);
    }

    public EventEmitter once(String eventName, Object listener) {
        registerEventIfNeeded(eventName);
        return mEventEmitter.once(eventName, listener);
    }

    public EventEmitter on(String eventName, Object listener) {
        registerEventIfNeeded(eventName);
        return mEventEmitter.on(eventName, listener);
    }

    public EventEmitter addListener(String eventName, Object listener) {
        registerEventIfNeeded(eventName);
        return mEventEmitter.addListener(eventName, listener);
    }

    private void registerEventIfNeeded(String eventName) {
        if (mRegisteredEvents.contains(eventName)) {
            return;
        }
        if (registerEvent(eventName)) {
            mRegisteredEvents.add(eventName);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private boolean registerEvent(String eventName) {
        switch (eventName) {
            case "touch": {
                mView.setOnTouchListener((v, event) -> {
                    BaseEvent e = new BaseEvent(mScope, event, event.getClass());
                    //Log.d(LOG_TAG, "this = " + NativeView.this + ", emitter = " + mEventEmitter + ", view = " + mView);
                    emit("touch", e, v);
                    return e.isConsumed();
                });
            }
            return true;
            case "click": {
                mView.setOnClickListener(v -> emit("click", v));
            }
            return true;
            case "long_click": {
                mView.setOnLongClickListener(v -> {
                    BaseEvent e = new BaseEvent(mScope, new NativeView.LongClickEvent(v));
                    emit("long_click", e, v);
                    return e.isConsumed();
                });
            }
            return true;
            case "key": {
                mView.setOnKeyListener((v, keyCode, event) -> {
                    BaseEvent e = new BaseEvent(mScope, event, event.getClass());
                    emit("key", e, keyCode, v);
                    return e.isConsumed();
                });
            }
            return true;
            case "scroll_change": {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                        BaseEvent e = new BaseEvent(mScope, new NativeView.ScrollEvent(scrollX, scrollY, oldScrollX, oldScrollY));
                        emit("scroll_change", e, v);
                    });
                    return true;
                }
            }
            break;
            case "check": {
                if (mView instanceof CompoundButton) {
                    ((CompoundButton) mView).setOnCheckedChangeListener((buttonView, isChecked) ->
                            emit("check", isChecked, buttonView));
                }
            }
            case "item_click":
            case "item_long_click": {
                if (mView instanceof JsListView) {
                    ((JsListView) mView).setOnItemTouchListener(new JsListView.OnItemTouchListener() {
                        @Override
                        public void onItemClick(JsListView listView, View itemView, Object item, int pos) {
                            emit("item_click", item, pos, itemView, listView);
                        }

                        @Override
                        public boolean onItemLongClick(JsListView listView, View itemView, Object item, int pos) {
                            BaseEvent e = new BaseEvent(mScope, new NativeView.LongClickEvent(itemView));
                            emit("item_long_click", e, item, pos, itemView, listView);
                            return e.isConsumed();
                        }
                    });
                    return true;
                }
            }
            break;
        }
        return false;
    }

    public boolean emit(String eventName, Object... args) {
        return mEventEmitter.emit(eventName, args);
    }

    public String[] eventNames() {
        return mEventEmitter.eventNames();
    }

    public int listenerCount(String eventName) {
        return mEventEmitter.listenerCount(eventName);
    }

    public Object[] listeners(String eventName) {
        return mEventEmitter.listeners(eventName);
    }

    public EventEmitter prependListener(String eventName, Object listener) {
        return mEventEmitter.prependListener(eventName, listener);
    }

    public EventEmitter prependOnceListener(String eventName, Object listener) {
        return mEventEmitter.prependOnceListener(eventName, listener);
    }

    public EventEmitter removeAllListeners() {
        return mEventEmitter.removeAllListeners();
    }

    public EventEmitter removeAllListeners(String eventName) {
        return mEventEmitter.removeAllListeners(eventName);
    }

    public EventEmitter removeListener(String eventName, Object listener) {
        return mEventEmitter.removeListener(eventName, listener);
    }

    public EventEmitter setMaxListeners(int n) {
        return mEventEmitter.setMaxListeners(n);
    }

    public int getMaxListeners() {
        return mEventEmitter.getMaxListeners();
    }

    public static int defaultMaxListeners() {
        return EventEmitter.defaultMaxListeners();
    }

}
