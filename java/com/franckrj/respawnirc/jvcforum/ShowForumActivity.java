package com.franckrj.respawnirc.jvcforum;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.franckrj.respawnirc.MainActivity;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.dialogs.SelectTextDialogFragment;
import com.franckrj.respawnirc.jvctopic.ShowTopicActivity;
import com.franckrj.respawnirc.jvcforum.jvcforumtools.JVCForumGetter;
import com.franckrj.respawnirc.jvcforum.jvcforumtools.ShowForumFragment;
import com.franckrj.respawnirc.base.AbsShowSomethingFragment;
import com.franckrj.respawnirc.PageNavigationUtil;
import com.franckrj.respawnirc.utils.AddOrRemoveThingToFavs;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.base.AbsNavigationViewActivity;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.Utils;

public class ShowForumActivity extends AbsNavigationViewActivity implements ShowForumFragment.NewTopicWantRead, JVCForumGetter.NewForumNameAvailable,
                                                    JVCForumGetter.ForumLinkChanged, PageNavigationUtil.PageNavigationFunctions,
                                                    AddOrRemoveThingToFavs.ActionToFavsEnded, JVCForumGetter.NewNumberOfMpAndNotifSetted {
    public static final String EXTRA_NEW_LINK = "com.franckrj.respawnirc.EXTRA_NEW_LINK";
    public static final String EXTRA_GO_TO_LAST_PAGE = "com.franckrj.respawnirc.EXTRA_GO_TO_LAST_PAGE";
    public static final String EXTRA_ITS_FIRST_START = "com.franckrj.respawnirc.EXTRA_ITS_FIRST_START";

    private static final int SEND_TOPIC_REQUEST_CODE = 156;
    private static final String SAVE_CURRENT_FORUM_TITLE = "saveCurrentForumTitle";
    private static final String SAVE_REFRESH_NEEDED_NEXT_RESUME = "saveRefreshNeededOnNextResume";
    private static final String SAVE_CURRENT_NUMBER_OF_MP = "saveCurrentNumberOfMp";
    private static final String SAVE_CURRENT_NUMBER_OF_NOTIF = "saveCurrentNumberOfNotif";

    private String currentTitle = "";
    private AddOrRemoveThingToFavs currentTaskForFavs = null;
    private PageNavigationUtil pageNavigation = null;
    private ShareActionProvider shareAction = null;
    private boolean refreshNeededOnNextResume = false;
    private boolean dontConsumeRefreshOnNextResume = false;
    private PrefsManager.LinkType linkTypeForInternalBrowser = new PrefsManager.LinkType(PrefsManager.LinkType.NO_LINKS);
    private String currentNumberOfMp = null;
    private String currentNumberOfNotif = null;
    private boolean postAsModoWhenPossible = true;

    private final View.OnLongClickListener showForumTitleListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            Bundle argForFrag = new Bundle();
            SelectTextDialogFragment selectTextDialogFragment = new SelectTextDialogFragment();
            argForFrag.putString(SelectTextDialogFragment.ARG_TEXT_CONTENT, getString(R.string.showForumNames, currentTitle));
            selectTextDialogFragment.setArguments(argForFrag);
            selectTextDialogFragment.show(getSupportFragmentManager(), "SelectTextDialogFragment");
            return true;
        }
    };

    public ShowForumActivity() {
        idOfBaseActivity = ITEM_ID_FORUM;
        pageNavigation = new PageNavigationUtil(this);
        pageNavigation.setLastPageNumber(100);
    }

    private void setNewForumLink(String newLink) {
        currentTitle = getString(R.string.app_name);
        setTitle(currentTitle);
        pageNavigation.setCurrentLink(newLink);
        pageNavigation.updateAdapterForPagerView();
        pageNavigation.updateCurrentItemAndButtonsToCurrentLink();
        if (pageNavigation.getCurrentItemIndex() > 0) {
            pageNavigation.clearPageForThisFragment(0);
        }
    }

    private boolean readThisTopic(String link, boolean updateForumFragIfNeeded, String topicName, String pseudoOfAuthor, boolean goToLastPage) {
        if (!JVCParser.getPageNumberForThisTopicLink(link).isEmpty()) {
            Intent newShowTopicIntent = new Intent(this, ShowTopicActivity.class);

            if (updateForumFragIfNeeded) {
                setNewForumLink(JVCParser.getForumForTopicLink(link));
            }

            if (topicName != null) {
                newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_TOPIC_NAME, topicName);
            }
            if (pseudoOfAuthor != null) {
                newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_PSEUDO_OF_AUTHOR, pseudoOfAuthor);
            }
            if (!currentTitle.equals(getString(R.string.app_name))) {
                newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_FORUM_NAME, currentTitle);
            }
            newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_GO_TO_LAST_PAGE, goToLastPage);

            newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_TOPIC_LINK, link);
            startActivity(newShowTopicIntent);
            return true;
        }
        return false;
    }

    private boolean readThisForum(String link) {
        if (!JVCParser.getPageNumberForThisForumLink(link).isEmpty()) {
            setNewForumLink(link);
            return true;
        }
        return false;
    }

    private boolean readThisTopicOrForum(String link, boolean goToLastPage) {
        if (link != null) {
            if (!link.isEmpty()) {
                link = JVCParser.formatThisUrl(link);
            }

            if (JVCParser.checkIfItsForumLink(link)) {
                if (readThisForum(link)) {
                    return false;
                }
            } else if (readThisTopic(link, true, null, null, goToLastPage)) {
                return true;
            }
        }

        Toast.makeText(this, R.string.errorInvalidLink, Toast.LENGTH_SHORT).show();
        return false;
    }

    private void stopAllCurrentTasks() {
        if (currentTaskForFavs != null) {
            currentTaskForFavs.clearListenersAndCancel();
            currentTaskForFavs = null;
        }
    }

    private ShowForumFragment getCurrentFragment() {
        return (ShowForumFragment) pageNavigation.getCurrentFragment();
    }

    private boolean consumeIntent(Intent newIntent) {
        String newLinkToGo = newIntent.getStringExtra(EXTRA_NEW_LINK);

        //noinspection SimplifiableIfStatement
        if (newLinkToGo != null) {
            return readThisTopicOrForum(newLinkToGo, newIntent.getBooleanExtra(EXTRA_GO_TO_LAST_PAGE, false));
        }

        return false;
    }

    private void updateShareAction() {
        if (shareAction != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, pageNavigation.getCurrentPageLink());
            shareIntent.setType("text/plain");
            shareAction.setShareIntent(shareIntent);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean newActivityIsLaunched = false;

        pageNavigation.initializePagerView((ViewPager) findViewById(R.id.pager_showforum));
        pageNavigation.initializeNavigationButtons((Button) findViewById(R.id.firstpage_button_showforum), (Button) findViewById(R.id.previouspage_button_showforum),
                        (Button) findViewById(R.id.currentpage_button_showforum), (Button) findViewById(R.id.nextpage_button_showforum), null);
        pageNavigation.updateAdapterForPagerView();

        pageNavigation.setCurrentLink(PrefsManager.getString(PrefsManager.StringPref.Names.FORUM_URL_TO_FETCH));
        if (savedInstanceState == null) {
            currentTitle = getString(R.string.app_name);
            newActivityIsLaunched = consumeIntent(getIntent());
            pageNavigation.updateCurrentItemAndButtonsToCurrentLink();
        } else {
            currentTitle = savedInstanceState.getString(SAVE_CURRENT_FORUM_TITLE, getString(R.string.app_name));
            refreshNeededOnNextResume = savedInstanceState.getBoolean(SAVE_REFRESH_NEEDED_NEXT_RESUME, false);
            getNewNumberOfMpAndNotif(savedInstanceState.getString(SAVE_CURRENT_NUMBER_OF_MP, null),
                                     savedInstanceState.getString(SAVE_CURRENT_NUMBER_OF_NOTIF, null));
            pageNavigation.updateNavigationButtons();
        }
        setTitle(currentTitle);

        if (savedInstanceState == null && getIntent() != null) {
            if (getIntent().getBooleanExtra(EXTRA_ITS_FIRST_START, false) && PrefsManager.getInt(PrefsManager.IntPref.Names.LAST_ACTIVITY_VIEWED) == MainActivity.ACTIVITY_SHOW_TOPIC) {
                startActivity(new Intent(this, ShowTopicActivity.class));
                newActivityIsLaunched = true;
            }
        }

        if (newActivityIsLaunched) {
            pageNavigation.setDontLoadOnFirstTimeForNextFragCreate(true);
        } else if (savedInstanceState == null) {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    @Override
    public void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        consumeIntent(newIntent);
    }

    @Override
    public void onResume() {
        super.onResume();
        PrefsManager.putInt(PrefsManager.IntPref.Names.LAST_ACTIVITY_VIEWED, MainActivity.ACTIVITY_SHOW_FORUM);
        PrefsManager.applyChanges();

        if (refreshNeededOnNextResume && !dontConsumeRefreshOnNextResume) {
            refreshNeededOnNextResume = false;
            if (getCurrentFragment() != null) {
                getCurrentFragment().refreshForum();
            }
        }
        dontConsumeRefreshOnNextResume = false;

        linkTypeForInternalBrowser.setTypeFromString(PrefsManager.getString(PrefsManager.StringPref.Names.LINK_TYPE_FOR_INTERNAL_BROWSER));
        postAsModoWhenPossible = PrefsManager.getBool(PrefsManager.BoolPref.Names.POST_AS_MODO_WHEN_POSSIBLE);
    }

    @Override
    public void onPause() {
        stopAllCurrentTasks();
        if (!pageNavigation.getCurrentLinkIsEmpty()) {
            PrefsManager.putString(PrefsManager.StringPref.Names.FORUM_URL_TO_FETCH, pageNavigation.getCurrentPageLink());
            PrefsManager.applyChanges();
        }
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_CURRENT_FORUM_TITLE, currentTitle);
        outState.putBoolean(SAVE_REFRESH_NEEDED_NEXT_RESUME, refreshNeededOnNextResume);
        outState.putString(SAVE_CURRENT_NUMBER_OF_MP, currentNumberOfMp);
        outState.putString(SAVE_CURRENT_NUMBER_OF_NOTIF, currentNumberOfNotif);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_showforum, menu);
        shareAction = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_share_showforum));
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem favItem = menu.findItem(R.id.action_change_forum_fav_value_showforum);

        menu.findItem(R.id.action_search_topic_showforum).setEnabled(!pageNavigation.getCurrentLinkIsEmpty());
        favItem.setEnabled(false);
        menu.findItem(R.id.action_share_showforum).setEnabled(!pageNavigation.getCurrentLinkIsEmpty());
        updateShareAction();

        if (getCurrentFragment() != null) {
            menu.findItem(R.id.action_send_topic_showforum).setEnabled(!Utils.stringIsEmptyOrNull(getCurrentFragment().getLatestListOfInputInAString(false)) && !pageNavigation.getCurrentLinkIsEmpty());

            if (!pseudoOfUser.isEmpty() && getCurrentFragment().getIsInFavs() != null) {
                favItem.setEnabled(true);
                if (getCurrentFragment().getIsInFavs()) {
                    favItem.setTitle(R.string.removeFromFavs);
                } else {
                    favItem.setTitle(R.string.addToFavs);
                }
            }
        } else {
            menu.findItem(R.id.action_send_topic_showforum).setEnabled(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_forum_fav_value_showforum:
                if (currentTaskForFavs == null) {
                    currentTaskForFavs = new AddOrRemoveThingToFavs(!getCurrentFragment().getIsInFavs(), this);
                    currentTaskForFavs.execute(JVCParser.getForumIdOfThisForum(pageNavigation.getCurrentPageLink()), getCurrentFragment().getLatestAjaxInfos().pref, PrefsManager.getString(PrefsManager.StringPref.Names.COOKIES_LIST));
                } else {
                    Toast.makeText(ShowForumActivity.this, R.string.errorActionAlreadyRunning, Toast.LENGTH_SHORT).show();
                }

                return true;
            case R.id.action_search_topic_showforum:
                Intent newSearchTopicIntent = new Intent(this, SearchTopicInForumActivity.class);
                newSearchTopicIntent.putExtra(SearchTopicInForumActivity.EXTRA_FORUM_LINK, pageNavigation.getFirstPageLink());
                newSearchTopicIntent.putExtra(SearchTopicInForumActivity.EXTRA_FORUM_NAME, currentTitle);
                startActivity(newSearchTopicIntent);
                return true;
            case R.id.action_open_in_browser_showforum:
                Utils.openCorrespondingBrowser(linkTypeForInternalBrowser, pageNavigation.getCurrentPageLink(), this);
                return true;
            case R.id.action_send_topic_showforum:
                Intent newSendTopicIntent = new Intent(this, SendTopicToForumActivity.class);
                newSendTopicIntent.putExtra(SendTopicToForumActivity.EXTRA_FORUM_NAME, currentTitle);
                newSendTopicIntent.putExtra(SendTopicToForumActivity.EXTRA_FORUM_LINK, pageNavigation.getCurrentPageLink());
                newSendTopicIntent.putExtra(SendTopicToForumActivity.EXTRA_INPUT_LIST, getCurrentFragment().getLatestListOfInputInAString(postAsModoWhenPossible));
                startActivityForResult(newSendTopicIntent, SEND_TOPIC_REQUEST_CODE);
                refreshNeededOnNextResume = true;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SEND_TOPIC_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String newTopicLink = data.getStringExtra(SendTopicToForumActivity.RESULT_EXTRA_TOPIC_LINK_TO_MOVE);

                if (newTopicLink != null) {
                    readThisTopic(newTopicLink, false, null, null, false);
                    //onActivityResult est appelé avant onResume, donc il faut contourner le fait que
                    //onResume sera forcément appelé après cette fonction mais avant que ShowTopicActivity soit lancé
                    if (refreshNeededOnNextResume) {
                        dontConsumeRefreshOnNextResume = true;
                    }
                }
            }
        }
    }

    @Override
    protected void initializeViewAndToolbar() {
        setContentView(R.layout.activity_showforum);
        initToolbar(R.id.toolbar_showforum).setOnLongClickListener(showForumTitleListener);

        layoutForDrawer = findViewById(R.id.layout_drawer_showforum);
        navigationMenuList = findViewById(R.id.navigation_menu_showforum);
    }

    @Override
    protected void newForumOrTopicToRead(String link, boolean itsAForum, boolean isWhenDrawerIsClosed, boolean fromLongClick) {
        if (itsAForum && !isWhenDrawerIsClosed) {
            readThisTopicOrForum(link, fromLongClick);
        } else if (!itsAForum && isWhenDrawerIsClosed) {
            readThisTopicOrForum(link, fromLongClick);
        }
    }

    @Override
    public void setReadNewTopic(String newTopicLink, String newTopicName, String pseudoOfAuthor, boolean fromLongClick) {
        readThisTopic(newTopicLink, false, newTopicName, pseudoOfAuthor, fromLongClick);
    }

    @Override
    public void getNewForumName(String newForumName) {
        if (!newForumName.isEmpty()) {
            currentTitle = newForumName;
        } else {
            currentTitle = getString(R.string.app_name);
        }
        setTitle(currentTitle);
    }

    @Override
    public void updateForumLink(String newForumLink) {
        pageNavigation.setCurrentLink(newForumLink);
    }

    @Override
    public void extendPageSelection(View buttonView) {
        //rien
    }

    @Override
    public AbsShowSomethingFragment createNewFragmentForRead(String possibleForumLink) {
        ShowForumFragment currentFragment = new ShowForumFragment();

        if (possibleForumLink != null) {
            Bundle argForFrag = new Bundle();
            argForFrag.putString(ShowForumFragment.ARG_FORUM_LINK, possibleForumLink);
            currentFragment.setArguments(argForFrag);
        }

        return currentFragment;
    }

    @Override
    public void onNewPageSelected(int position) {
        //rien
    }

    @Override
    public void doThingsBeforeLoadOnFragment(AbsShowSomethingFragment thisFragment) {
        //rien
    }

    @Override
    public int getShowablePageNumberForThisLink(String link) {
        try {
            return ((Integer.parseInt(JVCParser.getPageNumberForThisForumLink(link)) - 1) / 25) + 1;
        } catch (Exception e) {
            return 1;
        }
    }

    @Override
    public String setShowedPageNumberForThisLink(String link, int newPageNumber) {
        return JVCParser.setPageNumberForThisForumLink(link, ((newPageNumber - 1) * 25) + 1);
    }

    @Override
    public void getActionToFavsResult(String resultInString, boolean itsAnError) {
        if (itsAnError) {
            if (resultInString.isEmpty()) {
                resultInString = getString(R.string.errorInfosMissings);
            }
            Toast.makeText(this, resultInString, Toast.LENGTH_SHORT).show();
        } else {
            if (currentTaskForFavs.getAddToFavs()) {
                resultInString = getString(R.string.favAdded);
            } else {
                resultInString = getString(R.string.favRemoved);
            }
            Toast.makeText(this, resultInString, Toast.LENGTH_SHORT).show();
            getCurrentFragment().setIsInFavs(currentTaskForFavs.getAddToFavs());
        }
        currentTaskForFavs = null;
    }

    @Override
    public void getNewNumberOfMpAndNotif(String newNumberOfMp, String newNumberOfNotif) {
        currentNumberOfMp = newNumberOfMp;
        currentNumberOfNotif = newNumberOfNotif;
        updateMpAndNotifNumberShowed(currentNumberOfMp, currentNumberOfNotif);
    }
}
