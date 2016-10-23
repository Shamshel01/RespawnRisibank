package com.pijon.respawnirc;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class JVCParser {
    private static final Pattern entireMessagePattern = Pattern.compile("(<div class=\"bloc-message-forum \".*?)(<span id=\"post_[^\"]*\" class=\"bloc-message-forum-anchor\">|<div class=\"bloc-outils-plus-modo bloc-outils-bottom\">|<div class=\"bloc-pagi-default\">)", Pattern.DOTALL);
    private static final Pattern pseudoInfosPattern = Pattern.compile("<span class=\"JvCare [^ ]* bloc-pseudo-msg text-([^\"]*)\" target=\"_blank\">[^a-zA-Z0-9_\\[\\]-]*([a-zA-Z0-9_\\[\\]-]*)[^<]*</span>");
    private static final Pattern messagePattern = Pattern.compile("<div class=\"bloc-contenu\"><div class=\"txt-msg  text-[^-]*-forum \">((.*?)(?=<div class=\"info-edition-msg\">)|(.*?)(?=<div class=\"signature-msg)|(.*))", Pattern.DOTALL);
    private static final Pattern currentPagePattern = Pattern.compile("<span class=\"page-active\">([^<]*)</span>");
    private static final Pattern pageLinkPattern = Pattern.compile("<span><a href=\"([^\"]*)\" class=\"lien-jv\">([0-9]*)</a></span>");
    private static final Pattern topicFormPattern = Pattern.compile("(<form role=\"form\" class=\"form-post-topic[^\"]*\" method=\"post\" action=\"\".*?>.*?</form>)", Pattern.DOTALL);
    private static final Pattern inputFormPattern = Pattern.compile("<input ([^=]*)=\"([^\"]*)\" ([^=]*)=\"([^\"]*)\" ([^=]*)=\"([^\"]*)\"/>");
    private static final Pattern messageIDPattern = Pattern.compile("<div class=\"bloc-message-forum \" data-id=\"([^\"]*)\">");

    static class MessageInfos {
        String pseudo;
        String message;
        int id;
    }

    private JVCParser() {
        //rien
    }

    static String getListOfInputInAString(String pageSource) {
        StringBuilder allInputInAString = new StringBuilder();
        Matcher topicFormMatcher = topicFormPattern.matcher(pageSource);

        if (topicFormMatcher.find()) {
            Matcher inputFormMatcher;
            pageSource = topicFormMatcher.group(1);

            inputFormMatcher = inputFormPattern.matcher(pageSource);

            while (inputFormMatcher.find()) {
                allInputInAString.append("&");

                if (inputFormMatcher.group(1).equals("type")) {
                    if (inputFormMatcher.group(3).equals("name")) {
                        allInputInAString.append(inputFormMatcher.group(4)).append("=").append(inputFormMatcher.group(6));
                    } else {
                        allInputInAString.append(inputFormMatcher.group(6)).append("=").append(inputFormMatcher.group(4));
                    }
                } else if (inputFormMatcher.group(3).equals("type")) {
                    if (inputFormMatcher.group(1).equals("name")) {
                        allInputInAString.append(inputFormMatcher.group(2)).append("=").append(inputFormMatcher.group(6));
                    } else {
                        allInputInAString.append(inputFormMatcher.group(6)).append("=").append(inputFormMatcher.group(2));
                    }
                } else {
                    if (inputFormMatcher.group(1).equals("name")) {
                        allInputInAString.append(inputFormMatcher.group(2)).append("=").append(inputFormMatcher.group(4));
                    } else {
                        allInputInAString.append(inputFormMatcher.group(4)).append("=").append(inputFormMatcher.group(2));
                    }
                }
            }
        }

        return allInputInAString.toString();
    }

    static String getLastPageOfTopic(String pageSource) {
        int currentPageNumber = 0;
        String lastPage = "";
        Matcher currentPageMatcher = currentPagePattern.matcher(pageSource);
        Matcher pageLinkMatcher = pageLinkPattern.matcher(pageSource);

        if (currentPageMatcher.find()) {
            currentPageNumber = Integer.parseInt(currentPageMatcher.group(1));
        }

        while (pageLinkMatcher.find()) {
            if (Integer.parseInt(pageLinkMatcher.group(2)) > currentPageNumber) {
                currentPageNumber = Integer.parseInt(pageLinkMatcher.group(2));
                lastPage = "http://www.jeuxvideo.com" + pageLinkMatcher.group(1);
            }
        }

        return lastPage;
    }

    static String createStringMessageFromInfos(MessageInfos thisMessageInfo) {
        return "<p>&lt;" + thisMessageInfo.pseudo + "&gt; " + thisMessageInfo.message + "</p>";
    }

    /*TODO: A refaire en plus propre et plus complet.*/
    private static String parseMessageToPrettyMessage(String thisMessage) {
        return thisMessage.replaceAll("\n", "")
                .replaceAll("</p> *<p>", "<br /><br />")
                .replaceAll("</p>", "")
                .replaceAll("<p>", "")
                .replaceAll("</div>", "")
                .replaceAll("<div[^>]*>", "")
                .replaceAll("<img src=\"//image.jeuxvideo.com/smileys_img/([^\"]*)\" alt=\"[^\"]*\" data-def=\"SMILEYS\" data-code=\"([^\"]*)\" title=\"[^\"]*\" />", "$2");
    }

    private static MessageInfos createMessageInfoFromEntireMessage(String thisEntireMessage) {
        MessageInfos newMessageInfo = new MessageInfos();
        Matcher pseudoInfosMatcher = pseudoInfosPattern.matcher(thisEntireMessage);
        Matcher messageMatcher = messagePattern.matcher(thisEntireMessage);
        Matcher messageIDMatcher = messageIDPattern.matcher(thisEntireMessage);

        if (pseudoInfosMatcher.find() && messageMatcher.find() && messageIDMatcher.find()) {
            newMessageInfo.pseudo = pseudoInfosMatcher.group(2);
            newMessageInfo.message = parseMessageToPrettyMessage(messageMatcher.group(1));
            newMessageInfo.id = Integer.parseInt(messageIDMatcher.group(1));
        }

        return newMessageInfo;
    }

    /*TODO: Les messages des pseudos supprimés ne sont pas récupérés.*/
    static ArrayList<MessageInfos> getMessagesOfThisPage(String sourcePage) {
        ArrayList<MessageInfos> listOfParsedMessage = new ArrayList<>();
        Matcher entireMessageMatcher = entireMessagePattern.matcher(sourcePage);

        while (entireMessageMatcher.find()) {
            String thisMessage = entireMessageMatcher.group(1);

            listOfParsedMessage.add(createMessageInfoFromEntireMessage(thisMessage));
        }

        return listOfParsedMessage;
    }
}