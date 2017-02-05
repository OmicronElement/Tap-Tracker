// Generated code from Butter Knife. Do not modify!
package com.bwisni.taptracker;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class EditDrinkerActivity$$ViewBinder<T extends com.bwisni.taptracker.EditDrinkerActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131755140, "field 'nameTextView'");
    target.nameTextView = finder.castView(view, 2131755140, "field 'nameTextView'");
    view = finder.findRequiredView(source, 2131755146, "method 'editDrinkerDone'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.editDrinkerDone();
        }
      });
    view = finder.findRequiredView(source, 2131755145, "method 'editColor'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.editColor();
        }
      });
  }

  @Override public void unbind(T target) {
    target.nameTextView = null;
  }
}
