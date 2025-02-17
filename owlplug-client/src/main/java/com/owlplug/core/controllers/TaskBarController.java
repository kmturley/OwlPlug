/* OwlPlug
 * Copyright (C) 2021 Arthur <dropsnorz@gmail.com>
 *
 * This file is part of OwlPlug.
 *
 * OwlPlug is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OwlPlug is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OwlPlug.  If not, see <https://www.gnu.org/licenses/>.
 */
 
package com.owlplug.core.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXPopup.PopupHPosition;
import com.jfoenix.controls.JFXPopup.PopupVPosition;
import com.owlplug.core.components.TaskRunner;
import com.owlplug.core.tasks.AbstractTask;
import java.util.ArrayList;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class TaskBarController extends BaseController {

  @Autowired
  private TaskRunner taskRunner;

  @FXML
  public Label taskLabel;
  @FXML
  public ProgressBar taskProgressBar;
  @FXML
  private JFXButton taskHistoryButton;

  /**
   * FXML initialize.
   */
  public void initialize() {

    taskHistoryButton.setOnAction(e -> openTaskHistory());
  }

  private void openTaskHistory() {

    if (!taskRunner.getTaskHistory().isEmpty()) {
      JFXListView<AbstractTask> list = new JFXListView<>();
      list.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

      ArrayList<AbstractTask> tasks = new ArrayList<>(taskRunner.getTaskHistory());
      tasks.addAll(taskRunner.getPendingTasks());
      list.getItems().addAll(tasks);

      list.setCellFactory(new Callback<ListView<AbstractTask>, ListCell<AbstractTask>>() {
        @Override
        public ListCell<AbstractTask> call(ListView<AbstractTask> param) {
          return new JFXListCell<AbstractTask>() {
            @Override
            public void updateItem(AbstractTask item, boolean empty) {
              super.updateItem(item, empty);
              if (item != null && !empty) {
                Image icon = getApplicationDefaults().taskPendingImage;
                if (item.isRunning()) {
                  icon = getApplicationDefaults().taskRunningImage;
                } else if (item.isDone()) {
                  icon = getApplicationDefaults().taskSuccessImage;
                }
                if (item.getState().equals(State.FAILED)) {
                  icon = getApplicationDefaults().taskFailImage;
                }
                ImageView imageView = new ImageView(icon);
                setGraphic(imageView);
                setText(item.getName());
              }
            }
          };
        }
      });

      JFXPopup popup = new JFXPopup(list);
      popup.show(taskHistoryButton, PopupVPosition.BOTTOM, PopupHPosition.RIGHT);
    }

  }

}
