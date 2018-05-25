package com.johnhite.recipe;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class RecipeHelperService extends Application<RecipeHelperConfiguration> {

    public static void main(final String[] args) throws Exception {
       new RecipeHelperService().run(args);
    }

    @Override
    public String getName() {
        return "Recipe Helper";
    }

    @Override
    public void initialize(final Bootstrap<RecipeHelperConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final RecipeHelperConfiguration configuration, final Environment environment) {
        // TODO: implement application
    }

}
