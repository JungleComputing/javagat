/**
 * This package contains the classes and interfaces which are
 * used for using resources, be they hardware or software resources.
 * The most important classes and interfaces in this package are
 * {@link org.gridlab.gat.resources.ResourceBroker ResourceBroker}
 * and {@link org.gridlab.gat.resources.Reservation Reservation}.
 * An instance of the class
 * {@link org.gridlab.gat.resources.ResourceBroker ResourceBroker}
 * represents a broker for resources and is used to reserve and find resources.
 * An instance of the class
 * {@link org.gridlab.gat.resources.Reservation Reservation} represents a
 * reservation for a resource and is used to manipulate this reservation.
 * <p>
 * The most important classes and interfaces dealing with hardware resources are
 * {@link org.gridlab.gat.resources.HardwareResourceDescription HardwareResourceDescription}
 * and {@link org.gridlab.gat.resources.HardwareResource HardwareResource}.
 * An instance of the class
 * {@link org.gridlab.gat.resources.HardwareResourceDescription HardwareResourceDescription}
 * represents a description of a hardware resource and is used to describe such.
 * An instance of the class
 * {@link org.gridlab.gat.resources.HardwareResource HardwareResource}
 * represents a hardware resource and is used to manipulate such.
 * <p>
 * The most important classes and interfaces dealing with software resources are
 * {@link org.gridlab.gat.resources.SoftwareResourceDescription SoftwareResourceDescription},
 * and {@link org.gridlab.gat.resources.Job Job}.
 * An instance of the class
 * {@link org.gridlab.gat.resources.SoftwareResourceDescription SoftwareResourceDescription}
 * represents a description of a software resource and is used to describe such.
 * {@link org.gridlab.gat.resources.Job Jobs} represent running tasks on a
 * grid site.
 * <p>
 * The classes in this package are related to each other as follows.
 * A {@link org.gridlab.gat.resources.JobDescription JobDescription} contains a
 * {@link org.gridlab.gat.resources.SoftwareDescription SoftwareDescription}
 * and either a
 * {@link org.gridlab.gat.resources.ResourceDescription ResourceDescription} or
 * a {@link org.gridlab.gat.resources.Resource Resource} object.
 * The method
 * {@link org.gridlab.gat.resources.ResourceBroker#submitJob submitJob} of the 
 * {@link org.gridlab.gat.resources.ResourceBroker ResourceBroker} creates a
 * {@link org.gridlab.gat.resources.Job Job} out of the
 * {@link org.gridlab.gat.resources.JobDescription JobDescription}.
 * The {@link org.gridlab.gat.resources.ResourceDescription ResourceDescription}
 * object as well as the {@link org.gridlab.gat.resources.Resource Resource}
 * object are abstract and have subclasses for hardware and software (called
 * {@link org.gridlab.gat.resources.HardwareResourceDescription HardwareResourceDescription},
 * {@link org.gridlab.gat.resources.HardwareResource HardwareResource},
 * {@link org.gridlab.gat.resources.SoftwareResourceDescription SoftwareResourceDescription}
 * and {@link org.gridlab.gat.resources.SoftwareResource SoftwareResource}).
 * There's also a special convenience sub-type of the
 * {@link org.gridlab.gat.resources.SoftwareDescription SoftwareDescription},
 * the
 * {@link org.gridlab.gat.resources.JavaSoftwareDescription JavaSoftwareDescription},
 * which makes it easier to create a description of a Java application.
 * The {@link org.gridlab.gat.resources.ResourceBroker ResourceBroker}
 * cannot only turn
 * {@link org.gridlab.gat.resources.JobDescription JobDescriptions} into
 * {@link org.gridlab.gat.resources.Job Jobs}, but also pairs of
 * {@link org.gridlab.gat.resources.Resource Resources} (or
 * {@link org.gridlab.gat.resources.ResourceDescription ResourceDescriptions})
 * and {@link org.gridlab.gat.TimePeriod TimePeriods} into
 * {@link org.gridlab.gat.resources.Reservation Reservations}.
 */

package org.gridlab.gat.resources;
