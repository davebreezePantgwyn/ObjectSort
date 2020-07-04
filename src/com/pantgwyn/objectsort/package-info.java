/**
 * Provides the classes necessary perform a sort
 * of a set of java objects.
 * <p>
 * The sort will be either:
 * <ul>
 * <li>performed in storage</li>
 * <li>Performed using external workfiles</li>
 * </ul>
 * depending on the amount of memory available.
 * 
 * The sort can be:
 <ul>
 * <li>from a specified input file containing objects to an output file</li>
 * <li>from a specified input file containing objects to output callback interface</li>
 * <li>from an input callback interface to an output file</li>
 * <li>from an input callback interface to output callback interface</li>
 * </ul>
 *
 * @author  Dave Breeze
 * @version 1.0
 * @since 1.0
 */
package com.pantgwyn.objectsort;