package com.dropsnorz.owlplug.store.service;

import com.dropsnorz.owlplug.core.components.ApplicationDefaults;
import com.dropsnorz.owlplug.core.components.TaskFactory;
import com.dropsnorz.owlplug.core.model.platform.RuntimePlatform;
import com.dropsnorz.owlplug.store.dao.StoreDAO;
import com.dropsnorz.owlplug.store.dao.StoreProductDAO;
import com.dropsnorz.owlplug.store.model.ProductBundle;
import com.dropsnorz.owlplug.store.model.Store;
import com.dropsnorz.owlplug.store.model.StoreProduct;
import com.dropsnorz.owlplug.store.model.json.StoreJsonMapper;
import com.dropsnorz.owlplug.store.model.json.StoreModelAdapter;
import com.dropsnorz.owlplug.store.model.search.StoreCriteriaAdapter;
import com.dropsnorz.owlplug.store.model.search.StoreFilterCriteria;
import com.dropsnorz.owlplug.store.tasks.ProductInstallTask;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.annotation.PostConstruct;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class StoreService {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ApplicationDefaults applicationDefaults;
	@Autowired
	private TaskFactory taskFactory;
	@Autowired
	private StoreDAO storeDAO;
	@Autowired
	private StoreProductDAO storeProductDAO;


	@PostConstruct
	private void init() {

		Store store = storeDAO.findByName("OwlPlug Central");

		if (store == null) {
			store = new Store();
			store.setName("OwlPlug Central");
		}	
		store.setApiUrl("http://owlplug.dropsnorz.com/store.json");
		store.setUrl("http://owlplug.dropsnorz.com/store.json");

		storeDAO.save(store);
	}

	public void syncStores() {
		taskFactory.createStoreSyncTask().schedule();
	}
	
	/**
	 * Retrieves products from store with name matching the given criterias and 
	 * compatible with the current platform.
	 * @param criteriaList criteria list
	 * @return list of store products
	 */
	public Iterable<StoreProduct> getStoreProducts(List<StoreFilterCriteria> criteriaList) {
		RuntimePlatform env = applicationDefaults.getRuntimePlatform();
		
		Specification<StoreProduct> spec = StoreProductDAO.storeEnabled()
				.and(StoreProductDAO.hasPlatformTag(env.getCompatiblePlatformsTags()));
		spec = spec.and(StoreCriteriaAdapter.toSpecification(criteriaList));
		
		return storeProductDAO.findAll(spec);
	}
	
	public Iterable<StoreProduct> getProductsByName(String name) {
		return storeProductDAO.findByNameContainingIgnoreCase(name);
	}

	/**
	 * Downloads and installs a store product in a directory.
	 * @param bundle - store bundle to retrieve
	 * @param targetDirectory - directory where the product will be installed
	 */
	public void install(ProductBundle bundle, File targetDirectory) {		
		taskFactory.create(new ProductInstallTask(bundle, targetDirectory, applicationDefaults))
			.setOnSucceeded(e -> taskFactory.createPluginSyncTask().scheduleNow())
			.schedule();
	}
	
	
	/**
	 * Find the best bundle from a prpduct based on the user current platform.
	 * @param product - The store product
	 */
	public ProductBundle findBestBundle(StoreProduct product) {	
		
		RuntimePlatform runtimePlatform = applicationDefaults.getRuntimePlatform();
		
		// Look for bundles matching runtimePlatform
		for (ProductBundle bundle : product.getBundles()) {
			for (String platform : bundle.getTargets()) {
				if (platform.equals(runtimePlatform.getTag()) ||
						platform.equals(runtimePlatform.getOperatingSystem().getCode())) {
					return bundle;
				}
			}
		}
				
		//Look for bundles compatibles with current runtimePlatform
		for (ProductBundle bundle : product.getBundles()) {
			for (String platformTag : bundle.getTargets()) {
				for (String compatibleTag : runtimePlatform.getCompatiblePlatformsTags()) {
					if (platformTag.equals(compatibleTag)) {
						return bundle;
					}
				}
			}
		}
		
		
		return null;
	}


	/**
	 * Creates a PluginStore instance requesting a store url endpoint.
	 * @param url Store endpoint url
	 * @return created pluginstore instance, null if an error occurs
	 */
	public Store getPluginStoreFromUrl(String url) {

		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

			HttpGet httpGet = new HttpGet(url);
			CloseableHttpResponse response = httpclient.execute(httpGet);

			HttpEntity entity = response.getEntity();
			ObjectMapper objectMapper = new ObjectMapper()
					.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			try {
				StoreJsonMapper pluginStoreTO = objectMapper.readValue(entity.getContent(), StoreJsonMapper.class);
				EntityUtils.consume(entity);
				Store store = StoreModelAdapter.jsonMapperToEntity(pluginStoreTO);
				store.setApiUrl(url);
				return store;
			} catch (Exception e) {
				log.error("Error parsing store response: " + url, e);
				return null;
				
			} finally {
				response.close();
			}

		} catch (IOException e) {
			log.error("Error accessing store: Check your network connectivity", e);
			return null;
		}

	}
	
	public void enableStore(Store store, boolean enabled) {
		store.setEnabled(enabled);
		storeDAO.save(store);
	}
}
