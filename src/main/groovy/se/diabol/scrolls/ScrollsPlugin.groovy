package se.diabol.scrolls

/**
 * This interface must be implemented by plugins. Scrolls will find plugins implementing this interface and invoke the
 * generate method, providing the oldVersion and the newVersion as input. It's up to the plugin how these parameters are
 * interpreted.
 *
 * In order to list all plugins it's also important to give it a name, hence the getName method.
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
     * @param config Configuration for the plugin. It's read from the Scrolls configuration file and the plugin should
     * probably treat them with some care as it's user input.
     * @param oldVersion The previous version
     * @param newVersion The current version
     * @return A binding that will work in the plugins template
     */
    Map generate(Map config, String oldVersion, String newVersion)

    /**
     * This method should provide a detailed description of the available configuration settings for the plugin. Keys
     * being the options and values being the descriptions of said options.
     * @return Config options that can be customized in the Scrolls config file
     */
    Map getConfigInfo()
}