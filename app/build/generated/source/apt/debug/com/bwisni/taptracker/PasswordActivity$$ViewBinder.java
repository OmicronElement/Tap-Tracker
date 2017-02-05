// Generated code from Butter Knife. Do not modify!
package com.bwisni.taptracker;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class PasswordActivity$$ViewBinder<T extends com.bwisni.taptracker.PasswordActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131755148, "field 'passwordEditText'");
    target.passwordEditText = finder.castView(view, 2131755148, "field 'passwordEditText'");
  }

  @Override public void unbind(T target) {
    target.passwordEditText = null;
  }
}
