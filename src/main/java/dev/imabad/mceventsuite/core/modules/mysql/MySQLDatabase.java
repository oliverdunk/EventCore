package dev.imabad.mceventsuite.core.modules.mysql;

import dev.imabad.mceventsuite.core.EventCore;
import dev.imabad.mceventsuite.core.api.database.DatabaseProvider;
import dev.imabad.mceventsuite.core.api.database.DatabaseType;
import dev.imabad.mceventsuite.core.api.objects.EventBooth;
import dev.imabad.mceventsuite.core.api.objects.EventPlayer;
import dev.imabad.mceventsuite.core.api.objects.EventRank;
import dev.imabad.mceventsuite.core.api.objects.EventSetting;
import dev.imabad.mceventsuite.core.config.database.MySQLConfig;
import dev.imabad.mceventsuite.core.modules.mysql.dao.*;
import dev.imabad.mceventsuite.core.modules.mysql.events.MySQLLoadedEvent;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.persistence.Entity;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class MySQLDatabase extends DatabaseProvider {

    private MySQLConfig mySQLConfig;
    private SessionFactory sessionFactory;
    private Configuration configuration;

    private Set<DAO> registeredDAOs;

    public MySQLDatabase(MySQLConfig mySQLConfig){
        this.mySQLConfig = mySQLConfig;
        this.registeredDAOs = new HashSet<>();
        registerDAOs(
                new PlayerDAO(this),
                new RankDAO(this),
                new SettingDAO(this),
                new BoothDAO(this)
        );
    }

    @Override
    public boolean isConnected() {
        return !sessionFactory.isClosed();
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.PERSISTENT;
    }

    @Override
    public String getName() {
        return "mysql";
    }

    @Override
    public void connect() {
        Properties prop= new Properties();

        String connectionString = mySQLConfig.getPort() != 0
            ? String.format("jdbc:mysql://%s/%s", mySQLConfig.getHostname(), mySQLConfig.getDatabase())
            : String.format("jdbc:mysql://%s:%d/%s", mySQLConfig.getHostname(), mySQLConfig.getPort(),
                mySQLConfig.getDatabase());

        prop.setProperty("hibernate.connection.url", connectionString);

        //You can use any database you want, I had it configured for Postgres
        prop.setProperty("hibernate.dialect", "org.hibernate.dialect.MariaDB53Dialect");

        prop.setProperty("hibernate.connection.username", mySQLConfig.getUsername());

        if(mySQLConfig.getPassword().length() > 0) {
            prop.setProperty("hibernate.connection.password", mySQLConfig.getPassword());
        }

        prop.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
        prop.setProperty("hibernate.hbm2ddl.auto", "update");
        prop.setProperty("show_sql", "true");
        configuration = new Configuration().addProperties(prop);
        configuration.addAnnotatedClass(EventSetting.class);
        configuration.addAnnotatedClass(EventPlayer.class);
        configuration.addAnnotatedClass(EventRank.class);
        configuration.addAnnotatedClass(EventBooth.class);
        configuration.addPackage("dev.imabad.mceventsuite");
        sessionFactory = configuration.buildSessionFactory();
        EventCore.getInstance().getEventRegistry().handleEvent(new MySQLLoadedEvent(this));
    }

    public void registerEntity(Class clazz){
        if(configuration == null){
            return;
        }
        if(Arrays.stream(clazz.getAnnotations()).noneMatch(annotation -> annotation instanceof Entity)){
            return;
        }
        configuration.addAnnotatedClass(clazz);
        sessionFactory.close();
        sessionFactory = configuration.buildSessionFactory();
    }

    public void registerDAOs(DAO... daos){
        registeredDAOs.addAll(Arrays.asList(daos));
    }

    public <T extends DAO> T getDAO(Class<T> dao){
        return registeredDAOs.stream().filter(dao::isInstance).map(dao::cast).findFirst().orElseThrow(() -> new InvalidDAOException(dao));
    }

    @Override
    public void disconnect() {
        if(sessionFactory != null)
            sessionFactory.close();
    }

    public Session getSession(){
        return sessionFactory.openSession();
    }
}
