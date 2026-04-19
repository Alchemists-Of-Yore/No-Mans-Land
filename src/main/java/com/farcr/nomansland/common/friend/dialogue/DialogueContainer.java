package com.farcr.nomansland.common.friend.dialogue;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
    Parses dialogue, for both the server and the client
    Abstracted this so that the server could parse the original length without having to
    interface with DialogueState or DialogueRenderer
 */
public class DialogueContainer {
    private final ArrayList<String> textList;
    private final int textLength;

    public static final List<String> DELIMITERS = List.of("/", "&PlayerName");

    public String playerName;
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public DialogueContainer(String textString) {
        ArrayList<String> finalList = new ArrayList<>();
        finalList.add(textString);

        // Split text based on delimiters
        for (String delimiter : DELIMITERS) {
            List<String> tempList = List.copyOf(finalList);

            finalList.clear();
            for (String substring : tempList) {
                finalList.addAll(Arrays.asList(
                    substring.split("((?=" + delimiter + ")|(?<=" + delimiter + "))")
                ));
            }
        }

        // hacky fix lol avert your eyes
        for (int i = 0; i < finalList.size(); i++) {
            if (finalList.get(i).contains("/")) {
                StringBuilder setString = new StringBuilder();
                int multiplier = 4;
                setString.append("/".repeat(Math.max(0, (finalList.get(i).length() * multiplier))));
                finalList.set(i, setString.toString());
            }
        }
        this.textList = finalList;
        this.textLength = getTotalText();
    }

    public int getTextLength() {
        return textLength;
    }

    public boolean isInPause(int progress) {
        if (textList == null) return false;
        int totalText = 0;
        for (String subString : textList) {
            int end = totalText + subString.length();
            if (progress >= totalText && progress < end)
                return subString.contains("/");
            totalText = end;
        }
        return false;
    }

    private int getTotalText() {
        if (textList != null) {
            int textLength = 0;
            for (int i = 0; i < textList.size(); i++)
                textLength += textList.get(i).length();
            return textLength;
        }
        return 0;
    }

    public boolean flagDoneConstructed = false;
    public List<String> constructText(int progress) {
        List<String> localText = new ArrayList<>(List.of());
        int i = 0;
        if (textList != null) {
            int totalText = 0;
            StringBuilder totalString = new StringBuilder();
            while (i < textList.size()) {
                if (progress < totalText)
                    break;
                String subString = textList.get(i);
                if (subString.contains("&PlayerName")) {
                    subString = Minecraft.getInstance().getUser().getName();
                    if (playerName != null)
                        subString = playerName;
                }
                if (!subString.contains("/"))
                    totalString.append(subString, 0, Math.min((int) (progress - totalText), subString.length()));
                totalText += subString.length();
                i++;
            }
            if (progress >= totalText)
                flagDoneConstructed = true;
            localText.addAll(Arrays.asList(totalString.toString().split("\\n")));
        }
        return localText;
    }
}
