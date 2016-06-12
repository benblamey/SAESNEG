package benblamey.saesneg.experiments;

/**
 * A context in which experiments can be run. (de-couples the dependency to
 * allow running inside the browser).
 *
 * @author Ben Blamey ben@benblamey.com
 *
 */
public interface IExperimentContainer {

    public abstract String getOutputDirectoryWithTrailingSlash();

}
