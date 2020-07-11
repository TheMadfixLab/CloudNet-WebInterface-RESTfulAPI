package me.madfix.cloudnet.webinterface;

import com.google.gson.Gson;
import de.dytanic.cloudnet.lib.serverselectors.sign.Position;
import de.dytanic.cloudnet.lib.serverselectors.sign.Sign;
import de.dytanic.cloudnetcore.CloudNet;
import de.dytanic.cloudnetcore.api.CoreModule;
import io.sentry.DefaultSentryClientFactory;
import io.sentry.Sentry;
import me.madfix.cloudnet.webinterface.database.MobDatabase;
import me.madfix.cloudnet.webinterface.logging.WebInterfaceLogger;
import me.madfix.cloudnet.webinterface.services.ConfigurationService;
import me.madfix.cloudnet.webinterface.services.DatabaseService;

import java.util.HashSet;
import java.util.Optional;
import java.util.logging.Level;


public final class WebInterface extends CoreModule {

    private ConfigurationService configurationService;
    private DatabaseService databaseService;
    private MobDatabase mobDatabase;
    private final Gson gson = new Gson();
    private WebInterfaceLogger logger;

    @Override
    public void onLoad() {
        Sentry.init("https://08a4da2c621c4b8f9f16f345d829825b@o419044.ingest.sentry.io/5327070");
        this.logger = new WebInterfaceLogger();
        this.configurationService = new ConfigurationService();
        if (!this.configurationService.loadConfigurationFile()) {
            this.logger.severe("[100] No configuration file was found with the name: interface.json.");
            this.logger.severe("[100] Web interface will not start!");
            this.logger.severe("[100] Please create your configuration file under X and follow the instructions on the website. ");
        }
        if (this.configurationService.getOptionalInterfaceConfiguration().isPresent()) {
            this.databaseService = new DatabaseService(this);
        }
    }

    @Override
    public void onBootstrap() {
        if (this.configurationService.getOptionalInterfaceConfiguration().isPresent()) {
            try {
                this.mobDatabase = new MobDatabase(
                        this.getCloud().getDatabaseManager().getDatabase("cloud_internal_cfg"));
            } catch (Exception e) {
                getLogger().log(Level.SEVERE,"[300] An unexpected error occurred while loading the mob database",e);
            }
        }


    }

    @Override
    public void onShutdown() {
    }

    public WebInterfaceLogger getLogger() {
        return logger;
    }

    public MobDatabase getMobDatabase() {
        return mobDatabase;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public DatabaseService getDatabaseService() {
        return databaseService;
    }

    public Gson getGson() {
        return gson;
    }
}
