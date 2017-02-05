// Generated code from Butter Knife. Do not modify!
package com.bwisni.taptracker;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class AddDrinkerActivity$$ViewBinder<T extends com.bwisni.taptracker.AddDrinkerActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131755141, "field 'editTextName'");
    target.editTextName = finder.castView(view, 2131755141, "field 'editTextName'");
  }

  @Override public void unbind(T target) {
    target.editTextName = null;
  }
}
