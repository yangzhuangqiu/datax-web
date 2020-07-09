package com.wugui.datax.rpc.remoting.net;

import com.wugui.datax.rpc.remoting.net.params.BaseCallback;
import com.wugui.datax.rpc.remoting.provider.XxlRpcProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * server
 *
 * @author xuxueli 2015-11-24 20:59:49
 */
public abstract class AbstractServer {

    public abstract void setStartedCallback(BaseCallback startedCallback);

    public abstract void setStopedCallback(BaseCallback stopedCallback);


    /**
     * start server
     *
     * @param xxlRpcProviderFactory
     * @throws Exception
     */
    public abstract void start(final XxlRpcProviderFactory xxlRpcProviderFactory) throws Exception;


    /**
     * stop server
     *
     * @throws Exception
     */
    public abstract void stop() throws Exception;


}
