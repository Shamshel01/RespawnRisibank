package com.franckrj.respawnirc;

import androidx.lifecycle.Lifecycle;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.franckrj.respawnirc.base.AbsShowSomethingFragment;

public class PageNavigationUtil {
    public static final int ID_BUTTON_CURRENT = 0;
    public static final int ID_BUTTON_OTHER = 1;

    private Button firstPageButton = null;
    private Button previousPageButton = null;
    private Button currentPageButton = null;
    private Button nextPageButton = null;
    private Button lastPageButton = null;
    private View layoutForAllNavigationButtons = null;
    private View shadowForAllNavigationButtons = null;
    private String currentLink = "";
    private ViewPager pagerView = null;
    private ScreenSlidePagerAdapter adapterForPagerView = null;
    private boolean showNavigationButtons = true;
    private AppCompatActivity parentActivity;
    private PageNavigationFunctions funcForPageNav;
    private boolean loadNeedToBeDoneOnPageCreate = false;
    private boolean goToBottomOnNextLoad = false;
    private boolean dontLoadOnFirstTimeForNextFragCreate = false;
    private boolean refreshOnNextInstanciate = false;
    private String anchorForNextLoad = null;
    private int lastPage = 0;

    private final Button.OnClickListener changePageWithNavigationButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            if (buttonView == firstPageButton && firstPageButton.getVisibility() == View.VISIBLE) {
                pagerView.setCurrentItem(0);
                return;
            } else if (buttonView == previousPageButton && previousPageButton.getVisibility() == View.VISIBLE) {
                pagerView.setCurrentItem(getCurrentItemIndex() - 1);
                return;
            }  else if (buttonView == nextPageButton && nextPageButton.getVisibility() == View.VISIBLE) {
                pagerView.setCurrentItem(getCurrentItemIndex() + 1);
                return;
            } else if (lastPageButton != null) {
                if (buttonView == lastPageButton && lastPageButton.getVisibility() == View.VISIBLE) {
                    pagerView.setCurrentItem(lastPage - 1);
                    return;
                }
            }

            if (funcForPageNav != null) {
                funcForPageNav.extendPageSelection(buttonView);
            }
        }
    };

    private final ViewPager.OnPageChangeListener pageChangeOnPagerListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            //rien
        }

        @Override
        public void onPageSelected(int position) {
            if (funcForPageNav != null) {
                funcForPageNav.onNewPageSelected(position);
            }
            loadPageForThisFragment(position);
            updateNavigationButtons();
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                if (getCurrentItemIndex() > 0) {
                    clearPageForThisFragment(getCurrentItemIndex() - 1, true);
                }
                if (getCurrentItemIndex() < adapterForPagerView.getCount() - 1) {
                    clearPageForThisFragment(getCurrentItemIndex() + 1, true);
                }
            }
        }
    };

    public PageNavigationUtil(AppCompatActivity newParentActivity) {
        parentActivity = newParentActivity;
        if (parentActivity instanceof PageNavigationFunctions) {
            funcForPageNav = (PageNavigationFunctions) parentActivity;
        } else {
            funcForPageNav = null;
        }
    }

    public void initializeLayoutForAllNavigationButtons(View newLayout, View newShadow) {
        layoutForAllNavigationButtons = newLayout;
        shadowForAllNavigationButtons = newShadow;
    }

    public void initializePagerView(ViewPager newPagerView) {
        pagerView = newPagerView;
        pagerView.addOnPageChangeListener(pageChangeOnPagerListener);
    }

    public void initializeNavigationButtons(Button newFirstPageButton, Button newPreviousPageButton, Button newCurrentPageButton, Button newNextPageButton, Button newLastPageButton) {
        firstPageButton = newFirstPageButton;
        previousPageButton = newPreviousPageButton;
        currentPageButton = newCurrentPageButton;
        nextPageButton = newNextPageButton;
        lastPageButton = newLastPageButton;

        firstPageButton.setVisibility(View.GONE);
        firstPageButton.setOnClickListener(changePageWithNavigationButtonListener);
        previousPageButton.setVisibility(View.GONE);
        previousPageButton.setOnClickListener(changePageWithNavigationButtonListener);
        currentPageButton.setOnClickListener(changePageWithNavigationButtonListener);
        nextPageButton.setVisibility(View.GONE);
        nextPageButton.setOnClickListener(changePageWithNavigationButtonListener);

        if (lastPageButton != null) {
            lastPageButton.setVisibility(View.GONE);
            lastPageButton.setOnClickListener(changePageWithNavigationButtonListener);
        }
    }

    public void updateNavigationButtons() {
        if (showNavigationButtons) {
            firstPageButton.setVisibility(View.GONE);
            previousPageButton.setVisibility(View.GONE);
            currentPageButton.setText(R.string.waitingText);
            nextPageButton.setVisibility(View.GONE);

            if (lastPageButton != null) {
                lastPageButton.setVisibility(View.GONE);
            }

            if (getCurrentItemIndex() >= 0 && lastPage > 0) {
                currentPageButton.setText(String.valueOf(getCurrentItemIndex() + 1));

                if (getCurrentItemIndex() > 0) {
                    firstPageButton.setVisibility(View.VISIBLE);
                    firstPageButton.setText(String.valueOf(1));
                    previousPageButton.setVisibility(View.VISIBLE);
                }
                if (getCurrentItemIndex() < lastPage - 1) {
                    nextPageButton.setVisibility(View.VISIBLE);

                    if (lastPageButton != null) {
                        lastPageButton.setVisibility(View.VISIBLE);
                        lastPageButton.setText(String.valueOf(lastPage));
                    }
                }
            }
        }
    }

    public void updateAdapterForPagerView() {
        loadNeedToBeDoneOnPageCreate = true;
        adapterForPagerView = new ScreenSlidePagerAdapter(parentActivity.getSupportFragmentManager());
        pagerView.setAdapter(adapterForPagerView);
    }

    public void updateCurrentItemAndButtonsToCurrentLink() {
        if (!currentLink.isEmpty() && funcForPageNav != null) {
            pagerView.setCurrentItem(funcForPageNav.getShowablePageNumberForThisLink(currentLink) - 1);
            updateNavigationButtons();
        }
    }

    public void loadPageForThisFragment(int position) {
        if (!currentLink.isEmpty() && funcForPageNav != null) {
            AbsShowSomethingFragment currentFragment = adapterForPagerView.getFragment(position);
            if (currentFragment != null) {
                funcForPageNav.doThingsBeforeLoadOnFragment(currentFragment);
                if (goToBottomOnNextLoad) {
                    currentFragment.enableGoToBottomAtPageLoading();
                    goToBottomOnNextLoad = false;
                }
                if (anchorForNextLoad != null) {
                    currentFragment.setAnchorForNextLoad(anchorForNextLoad);
                    anchorForNextLoad = null;
                }
                currentFragment.setPageLink(funcForPageNav.setShowedPageNumberForThisLink(currentLink, position + 1));
            } else {
                loadNeedToBeDoneOnPageCreate = true;
            }
        }
    }

    public void clearPageForThisFragment(int position, boolean deleteTemporaryInfos) {
        AbsShowSomethingFragment currentFragment = adapterForPagerView.getFragment(position);
        if (currentFragment != null) {
            currentFragment.clearContent(deleteTemporaryInfos);
        }
    }

    public void notifyDataSetChanged() {
        adapterForPagerView.notifyDataSetChanged();
    }

    public int getIdOfThisButton(View thisButton) {
        if (thisButton == currentPageButton) {
            return ID_BUTTON_CURRENT;
        } else {
            return ID_BUTTON_OTHER;
        }
    }

    public AbsShowSomethingFragment getCurrentFragment() {
        return adapterForPagerView.getFragment(getCurrentItemIndex());
    }

    public int getCurrentItemIndex() {
        return pagerView.getCurrentItem();
    }

    public int getLastPage() {
        return lastPage;
    }

    public final boolean getCurrentLinkIsEmpty() {
        return currentLink.isEmpty();
    }

    public final String getCurrentPageLink() {
        if (funcForPageNav != null) {
            return funcForPageNav.setShowedPageNumberForThisLink(currentLink, getCurrentItemIndex() + 1);
        }
        return currentLink;
    }

    public final String getFirstPageLink() {
        if (funcForPageNav != null) {
            return funcForPageNav.setShowedPageNumberForThisLink(currentLink, 1);
        }
        return currentLink;
    }

    public final int getLastSupposedPageNumber() {
        if (funcForPageNav != null) {
            return funcForPageNav.getShowablePageNumberForThisLink(currentLink);
        }
        return 1;
    }

    public void setCurrentItemIndex(int newItemIndex) {
        pagerView.setCurrentItem(newItemIndex);
    }

    public void setLastPageNumber(int newLastPageNumber) {
        lastPage = newLastPageNumber;
    }

    public void setCurrentLink(String newLink) {
        currentLink = newLink;
    }

    public void setGoToBottomOnNextLoad(boolean newVal) {
        goToBottomOnNextLoad = newVal;
    }

    public void setDontLoadOnFirstTimeForNextFragCreate(boolean newVal) {
        dontLoadOnFirstTimeForNextFragCreate = newVal;
    }

    public void setRefreshOnNextInstanciate(boolean newVal) {
        refreshOnNextInstanciate = newVal;
    }

    public void setAnchorForNextLoad(String newVal) {
        anchorForNextLoad = newVal;
    }

    public void setDrawableForCurrentPageButton(Drawable thisDrawable) {
        currentPageButton.setCompoundDrawables(null, null, thisDrawable, null);
        currentPageButton.setCompoundDrawablePadding(parentActivity.getResources().getDimensionPixelSize(R.dimen.sizeBetweenTextAndArrow));
    }

    public void setShowNavigationButtons(boolean newValue) {
        showNavigationButtons = newValue;

        if (showNavigationButtons) {
            layoutForAllNavigationButtons.setVisibility(View.VISIBLE);
            shadowForAllNavigationButtons.setVisibility(View.VISIBLE);
        } else {
            layoutForAllNavigationButtons.setVisibility(View.GONE);
            shadowForAllNavigationButtons.setVisibility(View.GONE);
            lastPage = 0;
            notifyDataSetChanged();
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        SparseArray<AbsShowSomethingFragment> referenceMap = new SparseArray<>();

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public AbsShowSomethingFragment getFragment(int key) {
            return referenceMap.get(key);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            ((AbsShowSomethingFragment) object).clearContent(false);
            referenceMap.remove(position);
            super.destroyItem(container, position, object);
        }

        @Override
        public Fragment getItem(int position) {
            if (funcForPageNav != null) {
                if (loadNeedToBeDoneOnPageCreate && position == getCurrentItemIndex() && !currentLink.isEmpty()) {
                    AbsShowSomethingFragment tmpFragment = funcForPageNav.createNewFragmentForRead(funcForPageNav.setShowedPageNumberForThisLink(currentLink, position + 1));
                    funcForPageNav.doThingsBeforeLoadOnFragment(tmpFragment);
                    if (goToBottomOnNextLoad) {
                        tmpFragment.enableGoToBottomAtPageLoading();
                        goToBottomOnNextLoad = false;
                    }
                    if (anchorForNextLoad != null) {
                        tmpFragment.setAnchorForNextLoad(anchorForNextLoad);
                        anchorForNextLoad = null;
                    }
                    if (dontLoadOnFirstTimeForNextFragCreate) {
                        tmpFragment.enableDontLoadOnFirstTime();
                        dontLoadOnFirstTimeForNextFragCreate = false;
                    }
                    loadNeedToBeDoneOnPageCreate = false;
                    return tmpFragment;
                } else {
                    return funcForPageNav.createNewFragmentForRead(null);
                }
            } else {
                return new Fragment();
            }
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            AbsShowSomethingFragment fragment = (AbsShowSomethingFragment) super.instantiateItem(container, position);

            if (refreshOnNextInstanciate && position == getCurrentItemIndex()) {
                /* On refresh uniquement si le fragment a d??j?? ??t?? correctement cr????. ??a permet entre autre de refresh uniquement
                 * quand le fragment est charg?? depuis une saved state et non quand il est cr???? pour la premi??re fois. */
                if (fragment.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                    fragment.refreshContent();
                }
                refreshOnNextInstanciate = false;
            }

            referenceMap.put(position, fragment);
            return fragment;
        }

        @Override
        public int getCount() {
            if (lastPage > 0) {
                return lastPage;
            } else {
                return 1;
            }
        }
    }

    public interface PageNavigationFunctions {
        void extendPageSelection(View buttonView);
        AbsShowSomethingFragment createNewFragmentForRead(String possibleLink);
        void onNewPageSelected(int position);
        void doThingsBeforeLoadOnFragment(AbsShowSomethingFragment thisFragment);
        int getShowablePageNumberForThisLink(String link);
        String setShowedPageNumberForThisLink(String link, int newPageNumber);
    }
}
