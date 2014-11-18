package com.umbrella.service.telnet;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Map;

import com.google.common.util.concurrent.Service;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.umbrella.service.ServiceConfig;
import com.umbrella.service.ServiceModule;

public final class TelnetServiceModule extends ServiceModule{
	
	private final ServiceConfig config;
	
	public TelnetServiceModule(MapBinder<String, Service> serviceBinder, ServiceConfig config) {
		super(serviceBinder);
		this.config = config;
	}

	@Override
	protected void configure() {
		MapBinder<String, ChannelHandler> handlerBinder = MapBinder.newMapBinder(binder(), String.class, ChannelHandler.class, Names.named("telnet"));
		handlerBinder.addBinding("telnet.decoder").to(TelnetDecoder.class).in(Scopes.NO_SCOPE);
		handlerBinder.addBinding("decoder").to(StringDecoder.class).in(Scopes.SINGLETON);
		handlerBinder.addBinding("encoder").to(StringEncoder.class).in(Scopes.SINGLETON);
		handlerBinder.addBinding("telnet.handler").to(TelnetHandler.class).in(Scopes.NO_SCOPE);
		serviceBinder.addBinding("telnet").toInstance(new TelnetService(config));
	}

	@Provides
	@Singleton
	@Named("telnet")
	ServerBootstrap provideServerBootstrap(@Named("telnet") Provider<Map<String, ChannelHandler>> handlers) {
		ServerBootstrap boot = new ServerBootstrap();
		boot.group(config.getBoss(), config.getWorker()).channel(config.getChannelClass()).childHandler(new ChannelInitializer<SocketChannel>(){
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline p = ch.pipeline();
						handlers.get().forEach((key,handler) -> p.addLast(key, handler));
					}});
		return boot;
	}
}
