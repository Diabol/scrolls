package se.diabol.scrolls.plugins

/**
 * This interface must be implemented by plugins. Scrolls will find plugins implementing this interface and invoke the
 * generate method, providing the oldVersion and the newVersion as input. It's up to the plugin how these parameters are
 * interpreted.
 *
 * In order to list all plugins it's also important to give it a name, hence the getName method.
 *
 * All plugins must also declare a config field, as that will be passed when the plugin is instantiated
 */
interface ScrollsPlugin {
    /**
     * The descriptive name for the plugin, e.g. 'git', 'subversion', 'jira' etc.
     *
     * @return The name of the plugin
     */
    String getName()

    /**
     * The actual work method being invoked by Scrolls when it's time to generate a report. It's up to the plugin to
     * determine how to use the oldVersion, newVersion parameters. (E.g. for git it's likely a tag)
     *
     * @param oldVersion The previous version
     * @param newVersion The current version
     * @return A binding that will work in the plugins template
     */
    Map generate(Map input)

    /**
     * This method should provide a detailed description of the available configuration settings for the plugin. Keys
     * being the options and values being the descriptions of said options.
     * @return Config options that can be customized in the Scrolls config file.
     */
    Map getConfigInfo()

    /**
     * This method should return the name of the template that is used to render the report. If the plugin does not
     * provide any report output but acts as input provider to other plugins this method should return null
     * images/${plugin.name}/ folder.
     * @return the name of the ftl tempalte or null
     */
    String getTemplateName()

    /**
     * This method will be invoked just after the template has been processed. The plugin is responsible for setting the
     * correct paths in it's template. Resources from here will be searched for using getResource() and copied to the
     * images/${plugin.name}/ folder.
     * @return List of image resource names that will be copied (if they exist).
     */
    List getImageResources()
}