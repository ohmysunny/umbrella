package com.umbrella;

import java.util.concurrent.ExecutorService;

import org.apache.commons.pool2.ObjectPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.util.concurrent.ServiceManager;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.wolfram.jlink.KernelLink;

public class ServiceManagerListener extends ServiceManager.Listener{

	private final Logger LOG = LogManager.getLogger("service-manager");
	
	@Inject private ObjectPool<KernelLink> kernel;
	
	@Inject @Named("kernel") private ExecutorService service;
	
	@Override
	public void healthy() {
		LOG.info("All services start");
	}
	
	@Override
	public void stopped() {
		try {
			kernel.clear();
			kernel.close();
			service.shutdown();
		} catch (Exception dontCare) {}
		LOG.info("All services shutdown");
	}

}
