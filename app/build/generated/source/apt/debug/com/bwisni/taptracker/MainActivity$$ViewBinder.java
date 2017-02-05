// Generated code from Butter Knife. Do not modify!
package com.bwisni.taptracker;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class MainActivity$$ViewBinder<T extends com.bwisni.taptracker.MainActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131755170, "field 'drinkersListView'");
    target.drinkersListView = finder.castView(view, 2131755170, "field 'drinkersListView'");
    view = finder.findRequiredView(source, 2131755158, "field 'bannerTextView'");
    target.bannerTextView = finder.castView(view, 2131755158, "field 'bannerTextView'");
    view = finder.findRequiredView(source, 2131755167, "field 'kegTextView'");
    target.kegTextView = finder.castView(view, 2131755167, "field 'kegTextView'");
    view = finder.findRequiredView(source, 2131755169, "field 'adminLayout'");
    target.adminLayout = finder.castView(view, 2131755169, "field 'adminLayout'");
    view = finder.findRequiredView(source, 2131755147, "field 'coordinatorLayout'");
    target.coordinatorLayout = finder.castView(view, 2131755147, "field 'coordinatorLayout'");
    view = finder.findRequiredView(source, 2131755164, "field 'graph'");
    target.graph = finder.castView(view, 2131755164, "field 'graph'");
    view = finder.findRequiredView(source, 2131755165, "field 'kegGraph'");
    target.kegGraph = finder.castView(view, 2131755165, "field 'kegGraph'");
    view = finder.findRequiredView(source, 2131755168, "field 'pieChart'");
    target.pieChart = finder.castView(view, 2131755168, "field 'pieChart'");
    view = finder.findRequiredView(source, 2131755161, "field 'fab' and method 'onFabClick'");
    target.fab = finder.castView(view, 2131755161, "field 'fab'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onFabClick(p0);
        }
      });
  }

  @Override public void unbind(T target) {
    target.drinkersListView = null;
    target.bannerTextView = null;
    target.kegTextView = null;
    target.adminLayout = null;
    target.coordinatorLayout = null;
    target.graph = null;
    target.kegGraph = null;
    target.pieChart = null;
    target.fab = null;
  }
}
