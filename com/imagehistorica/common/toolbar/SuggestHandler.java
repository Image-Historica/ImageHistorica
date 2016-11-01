/*
 * Copyright (C) 2016 Image-Historica.com
 *
 * This file is part of the ImageHistorica: https://image-historica.com
 * ImageHistorica is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.imagehistorica.common.toolbar;

import static com.imagehistorica.util.Constants.DELIMITER;
import static com.imagehistorica.util.Constants.SUGGEST_ACCESS_POINT;

import com.imagehistorica.cache.SuggestCache;
import com.imagehistorica.common.state.State;
import com.imagehistorica.util.view.CommonAlert;
import com.imagehistorica.databases.Backend;
import com.imagehistorica.httpclient.CentralHttpClient;
import java.nio.charset.StandardCharsets;

import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.http.entity.StringEntity;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class SuggestHandler {

    private final SuggestTextField stf;
    private final ContextMenu popup;

    private final SuggestCache cache = SuggestCache.getInstance();
    private final State state = new State();

    private List<String> suggestions = new ArrayList<>();
    private List<String> imageNums = new ArrayList<>();
    private List<String> ids = new ArrayList<>();

    private String index;

    private boolean isAndMore = false;
    private Label l_isAndMore = new Label("and more...");

    protected SuggestHandler(SuggestTextField stf, ContextMenu popup) {
        this.stf = stf;
        this.popup = popup;
    }

    // For Central
    protected void doGetCentral(String input) {
        String rbody = (String) cache.get(input);
        if (rbody != null) {
            populatePopup(rbody);
            return;
        }

        rbody = CentralHttpClient.doPost(SUGGEST_ACCESS_POINT, new StringEntity(input, StandardCharsets.UTF_8), state, 3000);
        if (rbody != null && !rbody.isEmpty()) {
            populatePopup(rbody);
            cache.put(input, rbody);
        } else {
            popup.hide();
        }
    }

    // For Central
    private void populatePopup(String rbody) {
        index = null;
        ids.clear();
        suggestions.clear();
        imageNums.clear();

        if (rbody.contains(",")) {
            if (rbody.substring(rbody.lastIndexOf(",") + 1).equals("and more...")) {
                isAndMore = true;
                rbody = rbody.substring(0, rbody.lastIndexOf(","));
            }
        }

        List<String> results = Arrays.asList(rbody.split(","));
        Collections.sort(results);
        for (String result : results) {
            String[] sarray = result.split(DELIMITER);
            suggestions.add(sarray[0]);
            imageNums.add(sarray[1]);
            ids.add(sarray[2]);
        }

        popup(true);
    }

    // For Backend
    protected void doGetBackend(String input) {
        Set<String> suffixes = Backend.suggestHistoricaDirs(input);
        if (!suffixes.isEmpty()) {
            populatePopup(suffixes);
        } else {
            popup.hide();
        }
    }

    // For Backend
    protected void populatePopup(Set<String> suffixes) {
        index = null;
        ids.clear();
        suggestions.clear();
        imageNums.clear();

        suffixes.stream().forEach((s) -> suggestions.add(s));
        Collections.sort(suggestions);
        popup(false);
    }

    private void popup(boolean isCentral) {
        if (!suggestions.isEmpty()) {
            List<CustomMenuItem> menuItems = new ArrayList<>();
            for (int i = 0; i < suggestions.size(); i++) {
                String suggestion = suggestions.get(i);
                Label label;
                if (isCentral) {
                    label = new Label(suggestion + " " + imageNums.get(i));
                } else {
                    label = new Label(suggestion);
                }
                CustomMenuItem item = new CustomMenuItem(label, true);
                item.setId(String.valueOf(i));
                item.setOnAction((e) -> {
                    stf.setText(suggestion);
                    index = item.getId();
                    popup.hide();
                    e.consume();
                });
                menuItems.add(item);
            }
            if (isAndMore) {
                CustomMenuItem item = new CustomMenuItem(l_isAndMore, false);
                menuItems.add(item);
                isAndMore = false;
            }
            popup.getItems().clear();
            popup.getItems().addAll(menuItems);

            if (!popup.isShowing()) {
                popup.show(stf, Side.BOTTOM, 0, 0);
            }
        } else {
            popup.hide();
        }
    }

    protected void open() {
        CentralHttpClient.doGet(SUGGEST_ACCESS_POINT + "/", 1);
    }

    protected String getSuggestId() {
        String id = null;
        if (index != null) {
            try {
                id = ids.get(Integer.parseInt(index));
            } catch (IndexOutOfBoundsException e) {
                CommonAlert.getDebugLog("[SuggestHandler]" + e.toString());
            }
        }
        return id;
    }

    protected boolean checkSuggest(String searchTxt) {
        return suggestions.contains(searchTxt);
    }
}
