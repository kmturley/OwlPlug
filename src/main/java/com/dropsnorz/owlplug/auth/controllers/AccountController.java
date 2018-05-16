package com.dropsnorz.owlplug.auth.controllers;

import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.dropsnorz.owlplug.auth.services.AuthentificationService;
import com.jfoenix.controls.JFXButton;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;

@Controller
public class AccountController {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AuthentificationService authentificationService;
	@FXML
	private JFXButton googleButton;
	@FXML
	private ProgressIndicator authProgressIndicator;
	
	public void initialize() {
		
		googleButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				
				authProgressIndicator.setVisible(true);
				
				Task<Void> task = new Task<Void>() {
					@Override
					protected Void call() throws Exception {
						
						log.debug("Google auth task started");
						authentificationService.startAuth();
						
						this.updateProgress(1, 1);
						return null;
					}
					
				};
				
				task.setOnSucceeded(new EventHandler<WorkerStateEvent>(){
					@Override
					public void handle(WorkerStateEvent event) {
						
						log.debug("Google auth task complete");
						authProgressIndicator.setVisible(false);
					}
				});
				
				new Thread(task).start();

			};
		});	
		
		
	}

}
