package com.dropsnorz.owlplug;

import com.dropsnorz.owlplug.core.controllers.MainController;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.sf.ehcache.CacheManager;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class App extends Application {

	private ConfigurableApplicationContext context;
	private Parent rootNode;
	

	@Override
	public void init() throws Exception {

		try {
			SpringApplicationBuilder builder = new SpringApplicationBuilder(App.class);
			builder.headless(false);
			context = builder.run(getParameters().getRaw().toArray(new String[0]));

			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
			loader.setControllerFactory(context::getBean);
			rootNode = loader.load();

			MainController mainController = context.getBean(MainController.class);
			mainController.dispatchPostInitialize();
		} catch (BeanCreationException e) {
			notifyPreloader(new PreloaderProgressMessage("error", "OwlPlug is already running"));
			throw e;
		} catch (Exception e) {
			notifyPreloader(new PreloaderProgressMessage("error", "OwlPlug could not be started"));
			throw e;
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		double width = 1000;
		double height = 620;

		Scene scene = new Scene(rootNode, width, height);
		String css = App.class.getResource("/owlplug.css").toExternalForm();
		scene.getStylesheets().add(css);
		
		primaryStage.getIcons().add(ApplicationDefaults.owlplugLogo);
		primaryStage.setTitle(ApplicationDefaults.APPLICATION_NAME);

		primaryStage.setScene(scene);
		primaryStage.setHeight(height);
		primaryStage.setWidth(width);
		primaryStage.setMinHeight(height);
		primaryStage.setMinWidth(width);
		primaryStage.centerOnScreen();

		primaryStage.show();

	}

	@Bean
	public Preferences getPreference() {
		return 	Preferences.userRoot().node("com.dropsnorz.owlplug.user");

	}

	@Bean 
	public ServletWebServerFactory servletWebServerFactory() {
		return new TomcatServletWebServerFactory();
	}
	
	@Bean 
	public CacheManager getCacheManager() {
		return CacheManager.create();
	}


	@Override
	public void stop() throws Exception {
		context.close();
	}

	public static void main(String[] args) {
		System.setProperty("javafx.preloader", "com.dropsnorz.owlplug.OwlPlugPreloader");
		launch(App.class, args);

	}
}
