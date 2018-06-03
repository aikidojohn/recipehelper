package com.johnhite.recipe;

import com.johnhite.recipe.cli.ImportCommand;
import com.johnhite.recipe.cli.MenuCommand;
import com.johnhite.recipe.db.entity.Entities;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.SessionFactoryFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class RecipeHelperService extends Application<RecipeHelperConfiguration> {

	private final HibernateBundle<RecipeHelperConfiguration> hibernate = new HibernateBundle<RecipeHelperConfiguration>(Entities.CLASSES, new SessionFactoryFactory()) {
	    @Override
	    public DataSourceFactory getDataSourceFactory(RecipeHelperConfiguration configuration) {
	        return configuration.getDataSourceFactory();
	    }
	};

    @Override
    public String getName() {
        return "Recipe Helper";
    }

    @Override
    public void initialize(final Bootstrap<RecipeHelperConfiguration> bootstrap) {
        bootstrap.addBundle(hibernate);
        bootstrap.addCommand(new ImportCommand(hibernate));
        bootstrap.addCommand(new MenuCommand(hibernate));
    }

    @Override
    public void run(final RecipeHelperConfiguration configuration, final Environment environment) {
        // TODO: implement application
    }

    public static void main(final String[] args) throws Exception {
        new RecipeHelperService().run(args);
     }
}
