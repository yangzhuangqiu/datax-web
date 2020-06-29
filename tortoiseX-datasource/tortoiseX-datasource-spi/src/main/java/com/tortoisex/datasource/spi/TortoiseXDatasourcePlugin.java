package com.tortoisex.datasource.spi;

import static java.util.Collections.emptyList;

/**
 * TortoiseX datasource plugin interface
 */
public interface TortoiseXDatasourcePlugin {

    default Iterable<TortoiseXDatasourceFactory> getAlertChannelFactorys()
    {
        return emptyList();
    }
}
