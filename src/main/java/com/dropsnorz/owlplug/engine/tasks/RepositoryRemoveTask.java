package com.dropsnorz.owlplug.engine.tasks;

import com.dropsnorz.owlplug.dao.PluginRepositoryDAO;
import com.dropsnorz.owlplug.model.PluginDirectory;
import com.dropsnorz.owlplug.model.PluginRepository;

import javafx.concurrent.Task;

public class RepositoryRemoveTask  extends Task<Void> {

	protected PluginRepositoryDAO repositoryDAO;
	protected PluginRepository repository;
	protected String localPath;
	
	public RepositoryRemoveTask(PluginRepositoryDAO repositoryDAO, PluginRepository repository, String localPath) {
		
		this.repositoryDAO = repositoryDAO;
		this.repository = repository;
		this.localPath = localPath;
	}
	
	@Override
	protected Void call() throws Exception{
		
		this.updateProgress(0, 2);
		this.updateMessage("Deleting repository " + repository.getName() + " ...");
		
		if(repositoryDAO.exists(repository.getId())) {
			repositoryDAO.delete(repository);
			
			this.updateProgress(1, 2);
			this.updateMessage("Deleting files ...");
			
			PluginDirectory directory = new PluginDirectory(localPath);
			
			DirectoryRemoveTask directoryRemoveTask = new DirectoryRemoveTask(directory);
			directoryRemoveTask.call();
			
			this.updateProgress(2, 2);
		}
		
		
		return null;
	};

}
