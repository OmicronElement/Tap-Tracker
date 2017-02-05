// Generated code from Butter Knife. Do not modify!
package com.bwisni.taptracker;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class ConfirmActivity$$ViewBinder<T extends com.bwisni.taptracker.ConfirmActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131755154, "field 'nameTextView'");
    target.nameTextView = finder.castView(view, 2131755154, "field 'nameTextView'");
    view = finder.findRequiredView(source, 2131755153, "field 'creditsTextView'");
    target.creditsTextView = finder.castView(view, 2131755153, "field 'creditsTextView'");
    view = finder.findRequiredView(source, 2131755155, "field 'pieChartView'");
    target.pieChartView = finder.castView(view, 2131755155, "field 'pieChartView'");
    view = finder.findRequiredView(source, 2131755156, "method 'onUserIconClick'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onUserIconClick(p0);
        }
      });
  }

  @Override public void unbind(T target) {
    target.nameTextView = null;
    target.creditsTextView = null;
    target.pieChartView = null;
  }
}
